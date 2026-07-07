package com.digicoffer.lauditor.CommonFiles.GlobalFiles;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import static androidx.core.app.ActivityCompat.startActivityForResult;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


import com.digicoffer.lauditor.Appointments.ViewModels.Appointments;
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Meetings.MonthCalander.CalendarMonthView;
import com.digicoffer.lauditor.Meetings.ViewModels.WeeklyCalendar;
import com.digicoffer.lauditor.Chat.ViewModels.Chat;
import com.digicoffer.lauditor.Relationships.ClientRelationship;
import com.digicoffer.lauditor.DocEditor.DocEditor;
import com.digicoffer.lauditor.Documents.ViewModel.DocumentsEn;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.Notifications.Models.Navigation;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService;
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.digicoffer.lauditor.Email.Email;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.text.DateFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Supplier;


public class AndroidUtils {
    static Calendar f_Calendar = Calendar.getInstance();
    static int attempts = 1;
    private static boolean isPasswordVisible = false;
    public static final int PERMISSION_REQUEST_CAMERA_AUDIO = 123;
    private static final String TAG = "AndroidUtils";
    private static boolean isPopupShowing = false;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static Dialog currentDialog = null;
    private static WebView currentWebView = null;
    private static boolean isWebViewShowing = false;
    private static VideoCallDelegate currentDelegate = null;
    private static String currentUrl = "";


    public static void logMsg(String msg) {
    }


    // For Click Navigation setup,
    public interface OnModuleClickListener {
        void onClick(View view);
    }


    public interface OnModuleClickListeners {
        void onCreateClick(View view);


        void onViewClick(View view);
    }


    // Recyclerview padding for Scrolling in each module
    public static void setupEdgePaddingBehavior(RecyclerView rvOuter) {
        final int bottomPaddingPx = rvOuter.getResources().getDimensionPixelSize(R.dimen.forty_dp);

        // Start with no bottom padding
        rvOuter.setPadding(
                rvOuter.getPaddingLeft(),
                rvOuter.getPaddingTop(),
                rvOuter.getPaddingRight(),
                0
        );

        rvOuter.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING
                        || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    // User is actively scrolling/flinging -> apply bottom padding
                    recyclerView.setPadding(
                            recyclerView.getPaddingLeft(),
                            recyclerView.getPaddingTop(),
                            recyclerView.getPaddingRight(),
                            bottomPaddingPx
                    );
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Scrolling stopped -> remove bottom padding
                    recyclerView.setPadding(
                            recyclerView.getPaddingLeft(),
                            recyclerView.getPaddingTop(),
                            recyclerView.getPaddingRight(),
                            0
                    );
                }
            }
        });
    }

    /**
     * Call once per RecyclerView, after setAdapter(), on ANY screen.
     * Adds a fixed, non-scrolling bottom breathing-space row (footer) instead of
     * padding, so the spacing never shifts and the last real item never sticks
     * to the screen edge. Does not alter the wrapped adapter's data, click
     * listeners, or business logic in any way.
     * <p>
     * IMPORTANT: if this screen has pagination/infinite-scroll based on item
     * count or last-visible-position, see getRealItemCount() below and update
     * that listener to use it (see usage note under the class).
     */
    public static FooterWrapperAdapter setupBottomSpacerFooter(RecyclerView rvOuter, int spacerHeightPx) {
        RecyclerView.Adapter<?> originalAdapter = rvOuter.getAdapter();
        if (originalAdapter == null) return null;

        FooterWrapperAdapter wrapped = new FooterWrapperAdapter(originalAdapter, spacerHeightPx);
        rvOuter.setAdapter(wrapped);

        // Remove any padding-based spacing so it can't combine with the footer
        rvOuter.setPadding(
                rvOuter.getPaddingLeft(),
                rvOuter.getPaddingTop(),
                rvOuter.getPaddingRight(),
                0
        );

        return wrapped; // keep this reference only if you need getRealItemCount()
    }

    public static class FooterWrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_FOOTER = Integer.MAX_VALUE - 1;
        private final RecyclerView.Adapter<RecyclerView.ViewHolder> inner;
        private final int spacerHeightPx;

        @SuppressWarnings("unchecked")
        FooterWrapperAdapter(RecyclerView.Adapter<?> inner, int spacerHeightPx) {
            this.inner = (RecyclerView.Adapter<RecyclerView.ViewHolder>) inner;
            this.spacerHeightPx = spacerHeightPx;
            this.inner.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    notifyDataSetChanged();
                }

                @Override
                public void onItemRangeInserted(int s, int c) {
                    notifyItemRangeInserted(s, c);
                }

                @Override
                public void onItemRangeRemoved(int s, int c) {
                    notifyItemRangeRemoved(s, c);
                }

                @Override
                public void onItemRangeChanged(int s, int c) {
                    notifyItemRangeChanged(s, c);
                }
            });
        }

        /**
         * Use this from pagination/scroll listeners instead of getItemCount().
         */
        public int getRealItemCount() {
            return inner.getItemCount();
        }

        public RecyclerView.Adapter<RecyclerView.ViewHolder> getWrappedAdapter() {
            return inner;
        }

        @Override
        public int getItemViewType(int position) {
            return (position == inner.getItemCount()) ? VIEW_TYPE_FOOTER : inner.getItemViewType(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_FOOTER) {
                View spacer = new View(parent.getContext());
                spacer.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT, spacerHeightPx));
                return new RecyclerView.ViewHolder(spacer) {
                };
            }
            return inner.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < inner.getItemCount()) {
                inner.onBindViewHolder(holder, position);
            }
            // footer row: nothing to bind
        }

        @Override
        public int getItemCount() {
            return inner.getItemCount() + 1;
        }

        @Override
        public long getItemId(int position) {
            return (position == inner.getItemCount()) ? Long.MIN_VALUE : inner.getItemId(position);
        }
    }

    //To Show the only title with the title show.
    public static void setupModuleView(View moduleView,
                                       String text,
                                       boolean isCreate,
                                       boolean isTitleShow,
                                       Context context,
                                       String titleName,
                                       OnModuleClickListener listener) {
        TextView title_name = moduleView.findViewById(R.id.title_name);
        TextView tvName = moduleView.findViewById(R.id.tv_module_name);
        ImageView ivIcon = moduleView.findViewById(R.id.iv_Icon);
        LinearLayout ll_module = moduleView.findViewById(R.id.ll_module);
        tvName.setText(text);
        if (isTitleShow) {
            title_name.setVisibility(VISIBLE);
        } else {
            title_name.setVisibility(GONE);
        }
        if (isCreate) {
            ivIcon.setImageDrawable(context.getDrawable(R.drawable.simple_plus_icon));
        } else {
            ivIcon.setColorFilter(Color.WHITE);
            ivIcon.setImageDrawable(context.getDrawable(R.drawable.eye_icon));
        }
        title_name.setText(titleName);
        ll_module.setOnClickListener(v -> {
            if (listener != null) listener.onClick(v);
        });
    }


    public static void setupModuleView(View moduleView,
                                       String text,
                                       boolean isCreate,
                                       boolean isTitleShow,
                                       boolean isModuleShow,
                                       Context context,
                                       String titleName,
                                       OnModuleClickListener listener) {
        TextView title_name = moduleView.findViewById(R.id.title_name);
        TextView tvName = moduleView.findViewById(R.id.tv_module_name);
        ImageView ivIcon = moduleView.findViewById(R.id.iv_Icon);
        LinearLayout ll_module = moduleView.findViewById(R.id.ll_module);
//        LinearLayout ll_moduleView = moduleView.findViewById(R.id.ll_moduleView);
        tvName.setText(text);
        if (isTitleShow) {
            title_name.setVisibility(VISIBLE);
        } else {
            title_name.setVisibility(GONE);
        }
        if (isCreate) {
            ivIcon.setImageDrawable(context.getDrawable(R.drawable.simple_plus_icon));
        } else {
            ivIcon.setColorFilter(Color.WHITE);
            ivIcon.setImageDrawable(context.getDrawable(R.drawable.eye_icon));
        }
        title_name.setText(titleName);
        if (isModuleShow) {
            ll_module.setVisibility(VISIBLE);
        } else {
            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(DynamicUtils.twenty, DynamicUtils.twenty, DynamicUtils.twenty, 0);
            moduleView.setLayoutParams(params);
            ll_module.setVisibility(GONE);
        }
        ll_module.setOnClickListener(v -> {
            if (listener != null) listener.onClick(v);
        });
    }


    //..Update module name,without changing the image or id
    public static void updateModuleTitle(View moduleView, String title) {
        TextView title_name = moduleView.findViewById(R.id.title_name);
        if (title_name != null) {
            title_name.setVisibility(View.VISIBLE);
            title_name.setText(title);
        }
    }


    public static String getChatTimeText(Date date) {
        if (date == null) return "";


        long now = System.currentTimeMillis();


        java.util.Calendar today = java.util.Calendar.getInstance();
        today.set(java.util.Calendar.HOUR_OF_DAY, 0);
        today.set(java.util.Calendar.MINUTE, 0);
        today.set(java.util.Calendar.SECOND, 0);
        today.set(java.util.Calendar.MILLISECOND, 0);


        if (date.getTime() >= today.getTimeInMillis()) {
            return AndroidUtils.getDateToString(date, "hh:mm a");
        } else {
            return AndroidUtils.getDateToString(date, "dd MMM, yyyy");
        }
    }


    //Push Notifications, Notification Navigation From Clicked.
    public static void setupNotificationHandler(Context ctx, Navigation navigation) {
        Constants.isFromNotification = true;


        if (navigation == null || navigation.getRoute_name() == null)
            return;


        String route = navigation.getRoute_name();
        JSONObject params = navigation.getParams();


        Constants.notificationBundle.clear();


        Constants.notificationBundle.putString(Constants.NavKeys.ROUTE_NAME, route);


        try {
            if (params != null) {
                if (params.has("event_id")) {
                    Constants.notificationBundle.putString(Constants.NavKeys.EVENT_ID, params.getString("event_id"));
                }
                if (params.has("meeting_id")) {
                    Constants.notificationBundle.putString(Constants.NavKeys.EVENT_ID, params.getString("meeting_id"));
                }
                if (params.has("matter_id")) {
                    Constants.notificationBundle.putString(Constants.NavKeys.MATTER_ID, params.getString("matter_id"));
                }
                if (params.has("appointment_id")) {
                    Constants.notificationBundle.putString(Constants.NavKeys.APPOINTMENT_ID, params.getString("appointment_id"));
                }
                if (params.has("client_id")) {
                    Constants.notificationBundle.putString(Constants.NavKeys.CLIENT_ID, params.getString("client_id"));
                }
                if (params.has("guid")) {
                    Constants.notificationBundle.putString(Constants.NavKeys.GUID, params.getString("guid"));
                }
                if (params.has("group_ids")) {
                    JSONArray ids = params.getJSONArray("group_ids");
                    ArrayList<String> idList = new ArrayList<>();
                    for (int i = 0; i < ids.length(); i++) {
                        idList.add(ids.getString(i));
                    }
                    Constants.notificationBundle.putStringArrayList(Constants.NavKeys.GROUPS_ID, idList);
                }
                if (params.has("highlight_ids")) {
                    JSONArray ids = params.getJSONArray("highlight_ids");
                    ArrayList<String> idList = new ArrayList<>();
                    for (int i = 0; i < ids.length(); i++) {
                        idList.add(ids.getString(i));
                    }
                    Constants.notificationBundle.putStringArrayList(Constants.NavKeys.HIGHLIGHT_IDS, idList);
                }
                if (params.has("relationship_id")) {
                    Constants.notificationBundle.putString(Constants.NavKeys.RELATIONSHIP_ID, params.getString("relationship_id"));
                }
            }
            switch (route) {
                case "appointment_list":
                    Constants.isCreate = false;
                    Constants.mainActivity.navigation_items(new Appointments());
                    break;


                case "meeting_detail":
                case "meeting_list":
                    Constants.isCreate = false;
                    Constants.mainActivity.navigation_items(new Meetings());
                    break;


                // ── Documents: each route gets its own bundle so sub-item highlight works ──
                case "document_matter_list": {
                    Constants.isCreate = false;
                    DocumentsEn frag = new DocumentsEn();
                    Bundle b = new Bundle();
                    b.putString("document_type", "matter");
                    frag.setArguments(b);
                    Constants.mainActivity.navigation_items(frag);
                    break;
                }
                case "document_client_list": {
                    Constants.isCreate = false;
                    DocumentsEn frag = new DocumentsEn();
                    Bundle b = new Bundle();
                    b.putString("document_type", "client");
                    frag.setArguments(b);
                    Constants.mainActivity.navigation_items(frag);
                    break;
                }
                case "document_firm_list": {
                    Constants.isCreate = false;
                    DocumentsEn frag = new DocumentsEn();
                    Bundle b = new Bundle();
                    b.putString("document_type", "firm");
                    frag.setArguments(b);
                    Constants.mainActivity.navigation_items(frag);
                    break;
                }
                case "document_deleted_list": {
                    Constants.isCreate = false;
                    DocumentsEn frag = new DocumentsEn();
                    Bundle b = new Bundle();
                    b.putString("document_type", "delete");
                    frag.setArguments(b);
                    Constants.mainActivity.navigation_items(frag);
                    break;
                }


                // ── Relationships: Constants.Rel_Type set before navigation so highlightCorrectSubItem reads it ──
                case "relationship_individual_list":
                case "relationship_shared_with_me_individual_list":
                    Constants.Rel_Type = "Individual";
                    Constants.mainActivity.navigation_items(new ClientRelationship());
                    break;


                case "relationship_business_list":
                case "relationship_shared_with_me_business_list":
                    Constants.Rel_Type = "Entity";
                    Constants.mainActivity.navigation_items(new ClientRelationship());
                    break;


                case "relationship_corporate_list":
                case "relationship_shared_with_me_corporate_list":
                    Constants.Rel_Type = "Corporate";
                    Constants.mainActivity.navigation_items(new ClientRelationship());
                    break;


                case "relationship_deleted_list":
                    Constants.Rel_Type = "Deleted";
                    Constants.mainActivity.navigation_items(new ClientRelationship());
                    break;


                // ── Messages ──
                case "message_client_inbox":
                    Constants.isClient_chat = true;
                    Constants.mainActivity.navigation_items(new Chat());
                    break;


                case "message_team_inbox":
                    Constants.isClient_chat = false;
                    Constants.mainActivity.navigation_items(new Chat());
                    break;


                default:
                    break;
            }
//            switch (route) {
//                case "appointment_list":
//                    Constants.isCreate = false;
//                    Constants.mainActivity.navigation_items(new Appointments());
//                    break;
//                case "meeting_detail":
//                case "meeting_list":
//                    Constants.isCreate = false;
//                    Constants.mainActivity.navigation_items(new Meetings());
//                    break;
//
//                case "document_deleted_list":
//                case "document_firm_list":
//                case "document_matter_list":
//                case "document_client_list":
//                    Constants.isCreate = false;
//                    Constants.mainActivity.navigation_items(new DocumentsEn());
//                    break;
//
//                case "relationship_individual_list":
//                case "relationship_shared_with_me_individual_list":
//                    Constants.Rel_Type = "Individual";
//                    Constants.mainActivity.navigation_items(new ClientRelationship());
//                    break;
//                case "message_client_inbox":
//                    Constants.isClient_chat = true;
//                    Constants.mainActivity.navigation_items(new Chat());
//                    break;
//                case "relationship_business_list":
//                case "relationship_shared_with_me_business_list":
//                    Constants.Rel_Type = "Entity";
//                    Constants.mainActivity.navigation_items(new ClientRelationship());
//                    break;
//
//                case "relationship_shared_with_me_corporate_list":
//                case "relationship_corporate_list":
//                    Constants.Rel_Type = "Corporate";
//                    Constants.mainActivity.navigation_items(new ClientRelationship());
//                    break;
//                case "relationship_deleted_list":
//                    Constants.Rel_Type = "Deleted";
//                    Constants.mainActivity.navigation_items(new ClientRelationship());
//                    break;
//
//                default:
//                    break;
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Different Icon Showing in the ModuleHeadertitle
    public static void setupModuleView(View moduleView,
                                       String text,
                                       Drawable icon,
                                       boolean isTitleShow,
                                       OnModuleClickListener listener) {


        TextView tvName = moduleView.findViewById(R.id.tv_module_name);
        ImageView ivIcon = moduleView.findViewById(R.id.iv_Icon);
        TextView title_name = moduleView.findViewById(R.id.title_name);
        tvName.setText(text);
        ivIcon.setImageDrawable(icon);
        ivIcon.setColorFilter(Color.WHITE);
        if (isTitleShow) {
            title_name.setVisibility(VISIBLE);
        } else {
            title_name.setVisibility(GONE);
        }


        moduleView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(v);
        });
    }


    //Advanced Search in Audit Trails..
    public static String formatTimestamp(String isoTimestamp) {
        try {
            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = isoFormat.parse(isoTimestamp);


            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a");
            outputFormat.setTimeZone(java.util.TimeZone.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return isoTimestamp;
        }
    }


    //To Show the Fullname of the User Role in Top Header profile Popup.(Switch firm)
    public static String getFullRoleName(String roleCode) {
        switch (roleCode) {
            case "SU":
                return "Super User";
            case "GH":
                return "Group Head";
            case "TM":
                return "Team Member";
            case "AAM":
                return "Admin";
            default:
                return "Unknown";
        }
    }


    //getInitials() - to show the 1st letter
    public static String getInitials(String value) {
        if (value == null || value.trim().isEmpty())
            return "";


        value = value.trim();


        if (value.length() == 1)
            return value.toUpperCase();


        return value.substring(0, 1).toUpperCase();
    }


    //Used in notification, chat.
    public static void showValidationALert(String title, String message, Context context) {


        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);


        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("Ok", null);
        dlgAlert.setCancelable(true);
        dlgAlert.show();


        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });
    }


    //Used in clientRelationship for mask Email Showing
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }


        String[] parts = email.split("@", 2);
        String username = parts[0];
        String domain = parts[1];


        if (username.length() <= 2) {
            return username.charAt(0) + "*@" + domain;
        }


        StringBuilder masked = new StringBuilder();
        masked.append(username.substring(0, 2));
        for (int i = 2; i < username.length(); i++) {
            masked.append("*");
        }


        masked.append("@").append(domain);
        return masked.toString();
    }


    //check the email is masked or not to send the email to Payload.
    public static boolean isMaskedEmail(String email) {
        if (email == null || !email.contains("@")) return false;


        String username = email.split("@")[0];
        return username.contains("*");
    }


    //Used in clientRelationship for mask Phone number Showing
    public static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 3) {
            return "";
        }
        int visibleDigits = 3;
        int maskLength = phone.length() - visibleDigits;
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            masked.append("*");
        }
        masked.append(phone.substring(maskLength));
        return masked.toString();
    }


    // To show the list item in Grid and normal List according to the Mobile and Tab Versions.
    public static void LoadingRecyclerview(RecyclerView recyclerView, Context context) {
        if (DynamicUtils.isTablet(context)) {
            recyclerView.setLayoutManager(
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            );
        } else {
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(context)
            );
        }
        recyclerView.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
        );
        recyclerView.scheduleLayoutAnimation();
    }


    // To show the list item in Grid and normal List according to the Mobile and Tab Versions with Animation
    public static void LoadAnimation(RecyclerView recyclerView, Context context) {
        recyclerView.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
        );
        recyclerView.scheduleLayoutAnimation();
    }


    public static void RefreshRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            recyclerView.post(() -> {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                if (lm instanceof StaggeredGridLayoutManager) {
                    ((StaggeredGridLayoutManager) lm).invalidateSpanAssignments();
                }
                recyclerView.requestLayout();
            });
        }
    }


    public static void showDialog(final String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                123);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    // Common dialog for showing accross all module with yes or no Confimation like want to delete the Document?
//    public static void showConfirmationDialog(Context context,
//                                              String titleText,
//                                              String confirmText, String yesText, String noText,
//                                              OnConfirmListener listener) {
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//            View view = LayoutInflater.from(context).inflate(R.layout.confirmation_popup, null);
//
//
//            TextView header_name = view.findViewById(R.id.header_name);
//            ImageView close_documents = view.findViewById(R.id.close_documents);
//            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
//            AppCompatButton btn_yes = view.findViewById(R.id.btn_yes);
//            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
//            if (yesText.isEmpty()) {
//                btn_yes.setText(R.string.yes);
//            } else {
//                btn_yes.setText(yesText);
//            }
//            if (noText.isEmpty()) {
//                btn_no.setText(R.string.no);
//            } else {
//                btn_no.setText(noText);
//            }
//            header_name.setTextColor(context.getColor(R.color.blue));
//            header_name.setText(titleText);
//            tv_confirmation.setText(confirmText);
//
//
//            AlertDialog dialog = dialogBuilder.create();
//            dialog.setView(view);
//
//
//            close_documents.setOnClickListener(v -> {
//                dialog.dismiss();
//            });
//
//
//            btn_no.setOnClickListener(v -> {
//                dialog.dismiss();
//                listener.onCancel();
//            });
//
//
//            btn_yes.setOnClickListener(v -> {
//                dialog.dismiss();
//                listener.onSave();
//            });
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            dialog.show();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public static void showConfirmationDialog(Context context,
                                              String titleText,
                                              String confirmText, String yesText, String noText,
                                              OnConfirmListener listener) {
        showConfirmationDialog(context, titleText, confirmText, yesText, noText, listener, false);
    }

    public static void showConfirmationDialog(Context context,
                                              String titleText,
                                              String confirmText, String yesText, String noText,
                                              OnConfirmListener listener, Boolean isCancelActionNeeded) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.confirmation_popup, null);


            TextView header_name = view.findViewById(R.id.header_name);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            AppCompatButton btn_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            if (yesText.isEmpty()) {
                btn_yes.setText(R.string.yes);
            } else {
                btn_yes.setText(yesText);
            }
            if (noText.isEmpty()) {
                btn_no.setText(R.string.no);
            } else {
                btn_no.setText(noText);
            }
            header_name.setTextColor(context.getColor(R.color.blue));
            header_name.setText(titleText);
            tv_confirmation.setText(confirmText);


            AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);


            close_documents.setOnClickListener(v -> {
                if (isCancelActionNeeded) {
                    listener.onCancel();
                }
                dialog.dismiss();
            });


            btn_no.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onCancel();
            });


            btn_yes.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onSave();
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // To show the Dialog for the Add tag Cancel Flow popup
    public static void Delete_Popup(Activity activity, String delete_msg, String popup_name, String id, WeeklyCalendar weeklyCalendar, CalendarMonthView monthlyCalendar, View meeting, boolean isrecur, boolean isevent_delete_scope) {
        try {
            meeting.setAlpha(0.5f);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            final View dialogLayout = inflater.inflate(R.layout.alert_dialog_delete, null);
            TextView edit_event_dialog = dialogLayout.findViewById(R.id.edit_event_dialog);
            final TextView delete_event_msg = dialogLayout.findViewById(R.id.delete_event_msg);
            delete_event_msg.setText(delete_msg);
            delete_event_msg.setTextSize(DynamicUtils.twenty);
            delete_event_msg.setMaxLines(10);
            delete_event_msg.setTextColor(Color.BLACK);
            delete_event_msg.setTypeface(null, Typeface.NORMAL);
            final Button delete = dialogLayout.findViewById(R.id.delete_event);
            final Button btn_close_event = dialogLayout.findViewById(R.id.btn_close_event);
            btn_close_event.setTextColor(Color.RED);
            btn_close_event.setText(R.string.cancel);


            final AlertDialog dialog = dialogBuilder.create();
            btn_close_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    meeting.setAlpha(1.0f);
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (popup_name.equals("Delete_Event")) {
                        String recurring_edit_choice = "this";
                        if (id != null) {
                            Log.d("event_id", id + "..." + recurring_edit_choice);
                            if (weeklyCalendar != null) {
                                if (isrecur) {
                                    weeklyCalendar.delete_recurring_event(id, isevent_delete_scope);
                                } else {
                                    weeklyCalendar.callDeleteEventwebservice(id, recurring_edit_choice, isevent_delete_scope);
                                }
                                meeting.setAlpha(1.0f);
                            }
                            if (monthlyCalendar != null) {
                                if (isrecur) {
                                    monthlyCalendar.delete_recurring_event(id, isevent_delete_scope);
                                } else {
                                    monthlyCalendar.callDeleteEventwebservice(id, recurring_edit_choice, isevent_delete_scope);
                                }
                                meeting.setAlpha(1.0f);
                            }
                        }
                    }
                }
            });
            dialog.setView(dialogLayout);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    // Documents Listing Created Date format from Api to display Format.
    public static String formatDateToReadable(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


            Date date = inputFormat.parse(inputDate);


            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy");
            outputFormat.setTimeZone(TimeZone.getDefault());


            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }


    //Check the name, description text limitation.
    private static String safeTrim(String value, int maxLength) {
        return value == null ? "" : value.substring(0, Math.min(value.length(), maxLength));
    }


    // Method to check the Documents Content changed are not for Validation
    public static boolean hasDocumentChanged(DocumentsModel original,
                                             DocumentsModel currentModel) {


        String originalName = safeTrim(original.getName(), 50);
        String newName = safeTrim(currentModel.getName(), 50);


        String originalDesc = safeTrim(original.getDescription(), 300);
        String newDesc = safeTrim(currentModel.getDescription(), 300);
        String newExp = currentModel.getExpiration_date();


        boolean newDownload = currentModel.isIsenabled();
        boolean newEncrypt = currentModel.getIsencrypted();


        String originalTags = original.getTags() != null ? original.getTags().toString() : "";
        String newTags = currentModel.getTags() != null ? currentModel.getTags().toString() : "";


        return
                !originalName.equals(newName) ||
                        !originalDesc.equals(newDesc) ||
                        !original.getExpiration_date().equals(newExp) ||
                        (original.isIsenabled() != newDownload) ||
                        (original.getIsencrypted() != newEncrypt) ||
                        !originalTags.equals(newTags);
    }


    //Used for Delete Event,
    public static void Delete_Popup(Activity activity, Dialog PreviousDialog) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            final View dialogLayout = inflater.inflate(R.layout.alert_dialog_delete, null);
            TextView edit_event_dialog = dialogLayout.findViewById(R.id.edit_event_dialog);
            edit_event_dialog.setText(R.string.alert_);


            final TextView delete_event_msg = dialogLayout.findViewById(R.id.delete_event_msg);
            delete_event_msg.setText(R.string.are_you_sure_changes_are_not_saved);
            delete_event_msg.setTextSize(DynamicUtils.twenty);
            delete_event_msg.setTextColor(Color.BLACK);
            delete_event_msg.setTypeface(null, Typeface.NORMAL);
            final Button delete = dialogLayout.findViewById(R.id.delete_event);
            final Button btn_close_event = dialogLayout.findViewById(R.id.btn_close_event);
            btn_close_event.setTextColor(Color.RED);
            btn_close_event.setText(R.string.cancel);


            final AlertDialog dialog = dialogBuilder.create();
            btn_close_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (PreviousDialog != null) {
                        PreviousDialog.dismiss();
                    }
                }
            });
            dialog.setView(dialogLayout);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    //Method to show the confimation for the Timesheet changes are need to replicate in Event an Vice Versa.
    public static void showConfirmationDialog(Context context,
                                              String titleText,
                                              String confirmText,
                                              OnConfirmListener listener) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.delete_relationship, null);


            TextView header_name = view.findViewById(R.id.header_name);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            AppCompatButton btn_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);


            header_name.setTextColor(context.getColor(R.color.blue));
            header_name.setText(titleText);
            tv_confirmation.setText(confirmText);


            AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);


            close_documents.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onCancel();
            });


            btn_no.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onCancel();
            });


            btn_yes.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onSave();
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Method to show the confimation for all the Alert and Confirmation with the Highlighting the Respective Name
    public static void showConfirmationDialog(Context context,
                                              String titleText,
                                              String confirmText,
                                              String confirmName,
                                              OnConfirmListener listener) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.delete_relationship, null);


            TextView header_name = view.findViewById(R.id.header_name);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            AppCompatButton btn_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);


            SpannableString spannable = new SpannableString(confirmText);


            spannable.setSpan(
                    new ForegroundColorSpan(context.getColor(R.color.black)),
                    0,
                    confirmText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );


            if (confirmName != null && !confirmName.isEmpty()) {
                int start = confirmText.indexOf(confirmName);
                if (start >= 0) {
                    int end = start + confirmName.length();


                    spannable.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }


            tv_confirmation.setText(spannable);


            header_name.setTextColor(context.getColor(R.color.blue));
            header_name.setText(titleText);


            AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);


            close_documents.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onCancel();
            });


            btn_no.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onCancel();
            });


            btn_yes.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onSave();
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //common setup Show the toast
    public static void showToast(String message, Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static void storeSharedPreferenceString(String key, String value, Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences("mypref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }


    public static String getSharedPreferenceStringData(String key, Context activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPref.getString(key, null);
    }


    //Check the null Value
    public static String isNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }


    public static String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        String realPath = "";
        if (cursor == null) {
            realPath = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            realPath = cursor.getString(idx);
        }
        if (cursor != null) {
            cursor.close();
        }


        return realPath;
    }


    public static boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }


    // Doc Editor to shwo the Error.
    public static Dialog showError(String message, Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.error_layout_de, null);
        TextView tv_error_msg = dialogLayout.findViewById(R.id.tv_error_msg);
        tv_error_msg.setText(message);
        ImageView iv_error_msg = dialogLayout.findViewById(R.id.iv_error_msg);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);


            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.TOP;
            layoutParams.y = 200;
            window.setAttributes(layoutParams);
        }
        iv_error_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        new android.os.Handler().postDelayed(dialog::dismiss, 3000);
        return dialog;
    }


    //DocEditor
    public static Dialog showSuccess(String message, Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.success_layout_de, null);
        TextView tv_error_msg = dialogLayout.findViewById(R.id.tv_error_msg);
        tv_error_msg.setText(message);
        ImageView iv_error_msg = dialogLayout.findViewById(R.id.iv_error_msg);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);


            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.TOP;
            layoutParams.y = 200;
            window.setAttributes(layoutParams);
        }
        iv_error_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        new android.os.Handler().postDelayed(dialog::dismiss, 3000);
        return dialog;
    }


    // Actions for Click the Yes or No Interfaces
    public interface OnConfirmListener {
        void onSave();


        void onCancel();
    }


    //Yes or No confirm dialog used in Documentsen module
    public static void showConfirmDialog(Activity activity,
                                         String titleText,
                                         String confirmText,
                                         String btnCancelText,
                                         String btnSaveText,
                                         OnConfirmListener listener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialog = inflater.inflate(R.layout.confirm_layout_popup, null);
        TextView tvTitle = dialog.findViewById(R.id.tv_confirmation);
        TextView tvMessage = dialog.findViewById(R.id.tv_confirmContent);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        ImageView cancelIcon = dialog.findViewById(R.id.cancelIcon);
        tvTitle.setText(titleText);
        tvMessage.setText(confirmText);
        btnCancel.setText(btnCancelText);
        btnSave.setText(btnSaveText);
        dialogBuilder.setView(dialog);
        dialogBuilder.setCancelable(false);


        final AlertDialog dialoglayout = dialogBuilder.create();
        dialoglayout.show();


        Window window = dialoglayout.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);
            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }
        btnCancel.setOnClickListener(v -> {
            dialoglayout.dismiss();
            listener.onCancel();
        });


        cancelIcon.setOnClickListener(v -> {
            dialoglayout.dismiss();
            listener.onCancel();
        });


        btnSave.setOnClickListener(v -> {
            dialoglayout.dismiss();
            listener.onSave();
        });


        dialoglayout.show();
    }


    //Method to show the confimation for the Timesheet changes are need to replicate in Event an Vice Versa with custom button Text
    public static Dialog showConfirmation(
            Activity activity,
            String titleText,
            String confirmText,
            String btnSaveText,
            OnConfirmListener listener
    ) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.confirm_layout, null);


        TextView tvConfirmation = dialogLayout.findViewById(R.id.tv_confirmation);
        TextView tvConfirmContent = dialogLayout.findViewById(R.id.tv_confirmContent);
        Button btnCancel = dialogLayout.findViewById(R.id.btnCancel);
        Button btnSave = dialogLayout.findViewById(R.id.btnSave);
        btnSave.setText(btnSaveText);


        tvConfirmation.setText(titleText);
        tvConfirmContent.setText(confirmText);


        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setCancelable(false);


        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);
            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }


        btnSave.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onSave();
        });


        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onCancel();
        });


        return dialog;
    }


    //Method to show the Email Alert for the Email confirmation.
    public static Dialog showEmailAlert(Activity activity, Email email) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.email_alert_popup, null);


        TextView tv_confirmContent = dialogLayout.findViewById(R.id.tv_confirmContent);
        TextView btnCancel = dialogLayout.findViewById(R.id.btnCancel);
        btnCancel.setText(R.string.cancel);
        TextView btnSave = dialogLayout.findViewById(R.id.btnSave);
        btnSave.setText(R.string.ok);
        tv_confirmContent.setTextColor(activity.getColor(R.color.grey_medium));
        btnCancel.setTextColor(activity.getColor(R.color.light_blue));
        btnSave.setTextColor(activity.getColor(R.color.light_blue));
        tv_confirmContent.setText(R.string.email_no_subject_alert);


        final AlertDialog dialog = dialogBuilder.create();
        dialog.setView(dialogLayout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);


            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (email != null) {
                    email.send_email();
                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        return dialog;
    }


    //Method to show the confirmation in DocEditor
    public static Dialog showSaveAsConfirmation(Activity activity, DocEditor docEditor, String docName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.save_as_confirmation_popup, null);


        TextView tv_saved = dialogLayout.findViewById(R.id.tv_saved);
        TextView tv_confirmContent = dialogLayout.findViewById(R.id.tv_confirmContent);
        ImageView iv_cancel = dialogLayout.findViewById(R.id.iv_cancel);
        Button btn_goBack = dialogLayout.findViewById(R.id.btn_goBack);
        Button btn_viewDocument = dialogLayout.findViewById(R.id.btn_viewDocument);


        tv_saved.setText(R.string.saved);
        tv_confirmContent.setGravity(Gravity.CENTER);
        tv_confirmContent.setText("Congratulations! You have Successfully created a " + docName + " Document");
        btn_goBack.setText(R.string.go_back);
        btn_viewDocument.setText(R.string.view_document);
        btn_viewDocument.setPadding(20, 10, 20, 10);


        final AlertDialog dialog = dialogBuilder.create();
        dialog.setView(dialogLayout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);


            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.TOP;
            layoutParams.y = 200;
            window.setAttributes(layoutParams);
        }


        btn_viewDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                docEditor.loadView();
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        btn_goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        return dialog;
    }

    private static Dialog displayDialog; // add this field in AndroidUtils

    public static Dialog showAlert(String message, Activity activity, String title) {
        showAlert(message, activity, title, null);
        return displayDialog;
    }

    public static Dialog showAlert(String message, Activity activity, String title, Runnable onOkClick) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.alert_dialog, null);
        TextView tv_ok = dialogLayout.findViewById(R.id.tv_ok);
        tv_ok.setTextSize(17);
        tv_ok.setPadding(20, 20, 20, 20);
        tv_ok.setText(R.string.ok);

        tv_ok.setTextColor(activity.getColor(R.color.light_blue));
        TextView alert_content = dialogLayout.findViewById(R.id.alert_content);
        alert_content.setText(message);
        alert_content.setGravity(Gravity.CENTER);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView alert_title = dialogLayout.findViewById(R.id.alert_title);
        if (title.isEmpty()) {
            alert_title.setVisibility(View.GONE);
        } else {
            alert_title.setVisibility(VISIBLE);
        }
        alert_title.setText(title);
        tv_ok.setOnClickListener(v -> {
            dialog.dismiss();
            if (onOkClick != null) onOkClick.run();
        });
        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        displayDialog = dialog; // assign here so the 3-arg overload can return it
        return dialog;
    }


    // Common setup for the display Alert according to the all modules.with Default Button Text
    public static Dialog showAlert(String message, Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.alert_dialog, null);
        TextView tv_ok = dialogLayout.findViewById(R.id.tv_ok);
        tv_ok.setTextSize(17);
        tv_ok.setPadding(20, 20, 20, 20);
        tv_ok.setText(R.string.ok);


        tv_ok.setTextColor(activity.getColor(R.color.light_blue));
        TextView alert_content = dialogLayout.findViewById(R.id.alert_content);
        alert_content.setText(message);


        alert_content.setTextSize(17);
        alert_content.setGravity(Gravity.CENTER);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    // succes Message showing
    public static Dialog showAlert_docs(String title, String message, Activity activity) {


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.alert_dialog, null);


        TextView alertTitle = dialogLayout.findViewById(R.id.alert_title);
        alertTitle.setText(title);


        TextView alert_content = dialogLayout.findViewById(R.id.alert_content);
        alert_content.setText(message);
        alert_content.setTextSize(17);
        alert_content.setGravity(Gravity.CENTER);


        TextView tv_ok = dialogLayout.findViewById(R.id.tv_ok);
        tv_ok.setTextSize(17);
        tv_ok.setPadding(20, 20, 20, 20);
        tv_ok.setText(R.string.ok);
        tv_ok.setTextColor(activity.getColor(R.color.light_blue));


        final AlertDialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        tv_ok.setOnClickListener(v -> dialog.dismiss());


        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


        return dialog;
    }


    // Toggle the Switch in documents Module
    public static void checkSwitchState(SwitchMaterial switchDownload) {
        if (switchDownload.isChecked()) {
            switchDownload.setTrackTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(switchDownload.getContext(), R.color.dullBlueColor)
                    )
            );
        } else {
            switchDownload.setTrackTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(switchDownload.getContext(), R.color.grey_color_dark)
                    )
            );
        }
    }


    // Show the Selected Tags, Added tags in document module while Uploadin the Docuemnt.
    public static void renderSelectedTags(
            Context context,
            LinearLayout container,
            ArrayList<DocumentsModel> tags,
            DocumentsModel editingDoc,
            ConstraintLayout cl_document,
            Activity activity
    ) {
        container.removeAllViews();


        if (tags == null || tags.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }


        container.setVisibility(VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(context);


        for (int i = 0; i < tags.size(); i++) {


            DocumentsModel tag = tags.get(i);
            final int position = i;


            View tagView = inflater.inflate(
                    R.layout.component_tag_item,
                    container,
                    false
            );


            TextView tv = tagView.findViewById(R.id.tv_tag_name);
            ImageView ivDelete = tagView.findViewById(R.id.iv_delete);
            ImageView ivEdit = tagView.findViewById(R.id.iv_edit);


            tv.setText(tag.getTag_type() + " : " + tag.getTag_name());


            ivDelete.setOnClickListener(v -> {
                DocumentsModel removedTag = tags.remove(position);


                JSONObject jsonTags = editingDoc.getTags();
                if (jsonTags != null) {
                    jsonTags.remove(removedTag.getTag_type());
                    editingDoc.setTags(jsonTags);
                }


                renderSelectedTags(context, container, tags, editingDoc, cl_document, activity);
            });


            ivEdit.setOnClickListener(v -> {
                open_edit_tag_popup(context, position, tags, editingDoc, cl_document, container, activity, true);
            });


            container.addView(tagView);
        }
    }


    // Open the Edit docuemnt Popup in documents module (Upload docuemnt)
    @SuppressLint("WrongViewCast")
    public static void open_edit_tag_popup(
            Context context,
            int position,
            ArrayList<DocumentsModel> tags,
            DocumentsModel editingDoc,
            ConstraintLayout cl_document,
            LinearLayout llSelectedTags,
            Activity activity,
            Boolean isUpdated
    ) {
        DocumentsModel editTag = tags.get(position);


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        if (cl_document != null)
            cl_document.setAlpha(0.5f);


        View view = LayoutInflater.from(context)
                .inflate(R.layout.add_tag, null);
        TextInputEditText tv_tag_type, tv_tag_name;
        TextView tag_type_name, tv_added_tags, tag_name, header_name;
        @SuppressLint("WrongViewCast") AppCompatButton btn_add, btn_save_tag;


        tv_tag_type = view.findViewById(R.id.tv_tag_type);
        tv_tag_name = view.findViewById(R.id.tv_tag_name);
        tag_type_name = view.findViewById(R.id.tag_type_name);
        tv_added_tags = view.findViewById(R.id.tv_added_tags);
        tv_added_tags.setVisibility(GONE);
        tag_type_name.setText(R.string.tag_type);
        tag_name = view.findViewById(R.id.tag_name);
        tag_name.setText(R.string.tag);
        tv_tag_type.setHint(R.string.tag_type);
        tv_tag_name.setHint(R.string.tag);
        tv_tag_name.addTextChangedListener(new Validation(tv_tag_name));
        tv_tag_type.addTextChangedListener(new Validation(tv_tag_type));
        header_name = view.findViewById(R.id.header_name);
        InputFilter[] filters = new InputFilter[]{
                new InputFilter.LengthFilter(30)
        };
        InputFilter[] filters1 = new InputFilter[]{
                new InputFilter.LengthFilter(100)
        };


        tv_tag_type.setFilters(filters);
        tv_tag_name.setFilters(filters1);


        btn_add = view.findViewById(R.id.btn_add_tags);
        btn_add.setVisibility(GONE);
        AppCompatButton btn_cancel = view.findViewById(R.id.btn_cancel_tag);
        ImageView iv_cancel = view.findViewById(R.id.close_edit_docs);


        tv_tag_type.setText(editTag.getTag_type());
        tv_tag_name.setText(editTag.getTag_name());


        btn_save_tag = view.findViewById(R.id.btn_save_tag);
        if (isUpdated)
            header_name.setText(R.string.update_tag);
        else header_name.setText(R.string.add_tag);


        if (isUpdated)
            btn_save_tag.setText(R.string.update);
        else btn_save_tag.setText(R.string.add);


        AlertDialog dialog = dialogBuilder.create();


        btn_save_tag.setOnClickListener(v -> {


            String type = Objects.requireNonNull(tv_tag_type.getText()).toString().trim();
            String name = Objects.requireNonNull(tv_tag_name.getText()).toString().trim();


            if (type.isEmpty() || name.isEmpty()) {
                AndroidUtils.showAlert("Please check the Tag Type and Tag", activity);
                return;
            }


            // ← Save OLD key BEFORE overwriting editTag
            String oldType = editTag.getTag_type();


            editTag.setTag_type(type);
            editTag.setTag_name(name);
            tags.set(position, editTag);


            JSONObject jsonTags = editingDoc.getTags();
            if (jsonTags == null) jsonTags = new JSONObject();


            try {
                jsonTags.remove(oldType);  // ← Remove OLD key, not new one
                jsonTags.put(type, name);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            editingDoc.setTags(jsonTags);


            dialog.dismiss();


            renderSelectedTags(context, llSelectedTags, tags, editingDoc, cl_document, activity);
        });


        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        iv_cancel.setOnClickListener(v -> dialog.dismiss());


        dialog.setOnDismissListener(d ->
        {
            if (cl_document != null)
                cl_document.setAlpha(1f);
        });


        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.show();
    }


    // Used for the to Sort the List in appointments, events.
    public static String[] extractDateTimeParts(String fromDateTime, String toDateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat dateOut = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            SimpleDateFormat timeOut = new SimpleDateFormat("HH:mm", Locale.ENGLISH);


            Date from = inputFormat.parse(fromDateTime);
            Date to = inputFormat.parse(toDateTime);


            if (from == null || to == null) return null;


            String date = dateOut.format(from);
            String fromTime = timeOut.format(from);
            String toTime = timeOut.format(to);


            return new String[]{date, fromTime, toTime};


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Common Setup for the AVChat URl in Events, Appoinments module.
    public static String getAVChatUrl(String roomId, String fromTime, String toTime, String date, String clientName) {
        String name = (Constants.NAME).replace(" ", "%20");
        String finalUrl = Constants.AVClientChatUrl + "join-room?roomId=" + roomId + "&name=" + name + "&fromTime=" + fromTime + "&toTime=" + toTime + "&date=" + date;
        return finalUrl;
    }


    //Used in LoadwebView for Load the Setview.
    private static String getString(String loginType, String token, String jid) {
        String name = (Constants.NAME + " ").replace(" ", "%20");
        String finalUrl = Constants.AVChatUrl + "?logintype=" + loginType + "&token=" + token + "&jid=" + jid + "&name=" + name + "&hideclient=" + Constants.isAdmin + "&plan=lauditor" + "&category=" + Constants.CATEGORY;
        return finalUrl;
    }


    // To show the Formatted date in appointments.
    public static Date parseAnyDate(String dateString) {


        String[] possibleFormats = new String[]{
                // ✅ Datetime formats FIRST
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss",
                "EEE MMM dd HH:mm:ss z yyyy",
                // Date-only formats after
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "ddMMyyyy",
                "MM-dd-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "dd MMM yyyy",
                "MMM dd, yyyy"
        };


        for (String format : possibleFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                Date date = sdf.parse(dateString);
                if (date != null) {
                    return date;
                }
            } catch (Exception ignored) {
            }
        }


        return null;
    }


    // Appoitnments module for Enable the VideoCall, chat Icon
    public static boolean isWithinOneMinute(String dateTime) {


        Date appointmentDate = parseAnyDate(dateTime);
        if (appointmentDate == null) return false;


        long timeDiff = appointmentDate.getTime() - System.currentTimeMillis();


        return timeDiff <= 60000;
    }


    public static boolean isWithinOneHour(String dateTime) {


        Date appointmentDate = parseAnyDate(dateTime);
        if (appointmentDate == null) return false;


        long timeDiff = appointmentDate.getTime() - System.currentTimeMillis();


        return timeDiff <= 3600000;
    }


    public static boolean isWithinTwoHours(String dateTime) {


        Date appointmentDate = parseAnyDate(dateTime);
        if (appointmentDate == null) return false;


        long timeDiff = appointmentDate.getTime() - System.currentTimeMillis();


        return timeDiff <= 7200000; // 2 hours in milliseconds (2 * 60 * 60 * 1000)
    }


    private static boolean isTablet(Activity activity) {
        float density = activity.getResources().getDisplayMetrics().density;
        float dpWidth = activity.getResources().getDisplayMetrics().widthPixels / density;
        int screenLayout = activity.getResources().getConfiguration().screenLayout
                & android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean isLargeScreen =
                screenLayout >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
        return isLargeScreen || dpWidth >= 600;
    }


    //Load the AVChat without endCall Delegate.
    public static void loadAVChatView(Context context, Activity activity, String url, VideoCallDelegate delegate) {
        if (isWebViewShowing) {
            Log.d(TAG, "WebView already showing, dismissing previous instance");
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }
            if (currentWebView != null) {
                currentWebView.loadUrl("about:blank");
                currentWebView.stopLoading();
                currentWebView.clearHistory();
                currentWebView.destroy();
                currentWebView = null;
            }
            isWebViewShowing = false;
            currentDialog = null;
        }


        currentDelegate = delegate;
        currentUrl = url;


        String[] permissions_local = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };


        if (!hasPermissions(permissions_local, context)) {
            // ✅ Save to MainActivity's expected prefs key
            SharedPreferences prefs = activity.getSharedPreferences("video_call_prefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("pending_avchat_url", url)
                    .putBoolean("has_pending_delegate", delegate != null)
                    .apply();


            requestPermissions(activity);
            Toast.makeText(context, "Please grant camera and microphone permissions for video call", Toast.LENGTH_LONG).show();
            return;
        }


        showVideoCallWebView(context, activity, url, delegate);
    }


    //Load the AVChat without endCall Delegate.
    private static void showVideoCallWebView(Context context, Activity activity, String url, VideoCallDelegate delegate) {
        try {
            Dialog ad_dialog = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
            ad_dialog.setCancelable(false);
            ad_dialog.setCanceledOnTouchOutside(false);


            LayoutInflater inflater = activity.getLayoutInflater();
            View webViewLayout = inflater.inflate(R.layout.webview, null);
            RelativeLayout ll_view = webViewLayout.findViewById(R.id.ll_view);
            WebView webView = webViewLayout.findViewById(R.id.webView);
            TextView tv_contactName = webViewLayout.findViewById(R.id.tv_contactName);
            ImageButton btn_close = webViewLayout.findViewById(R.id.btn_back);
            ll_view.setVisibility(GONE);
            tv_contactName.setText(Constants.NAME != null ? Constants.NAME : "Video Call");


            WebSettings webSettings = webView.getSettings();
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(false);
            webSettings.setDisplayZoomControls(false);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setMediaPlaybackRequiresUserGesture(false);
            webSettings.setAllowFileAccess(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webSettings.setGeolocationEnabled(true);


            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }


            webView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void onCallEnded() {
                    Log.d(TAG, "onCallEnded called from JavaScript");
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (delegate != null) {
                            delegate.onCallEnded();
                        } else if (currentDelegate != null) {
                            currentDelegate.onCallEnded();
                        }
                        if (ad_dialog != null && ad_dialog.isShowing()) {
                            ad_dialog.dismiss();
                        }
                        cleanup();
                    });
                }


                @JavascriptInterface
                public void onMeetingCancelled() {
                    Log.d(TAG, "onMeetingCancelled called from JavaScript");
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (delegate != null) {
                            delegate.onCallEnded();
                        } else if (currentDelegate != null) {
                            currentDelegate.onCallEnded();
                        }
                        if (ad_dialog != null && ad_dialog.isShowing()) {
                            ad_dialog.dismiss();
                        }
                        cleanup();
                    });
                }


                @JavascriptInterface
                public void log(String message) {
                    Log.d(TAG, "WebView-JS: " + message);
                }
            }, "Android");


            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    Log.d(TAG, "onPermissionRequest: " + (request != null ? request.getResources().toString() : "null"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request != null) {
                        request.grant(request.getResources());
                    }
                }


                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    if (callback != null) {
                        callback.invoke(origin, true, false);
                    }
                }


                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d(TAG, "WebView Console: " + (consoleMessage != null ? consoleMessage.message() : "null"));
                    return true;
                }
            });


            webView.setWebViewClient(new WebViewClient() {
                private boolean isCallEnded = false;
                private String loadedUrl = "";


                // ── Trigger this once, safely, from any thread ────────────────
                private void triggerCallEnded() {
                    if (isCallEnded) return;   // guard — only fire once
                    isCallEnded = true;


                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (delegate != null) {
                            delegate.onCallEnded();
                        } else if (currentDelegate != null) {
                            currentDelegate.onCallEnded();
                        }
                        if (ad_dialog != null && ad_dialog.isShowing()) {
                            ad_dialog.dismiss();
                        }
                        cleanup();
                    });
                }


                @Override
                public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    Log.d(TAG, "onPageStarted: " + url);
                }


                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.d(TAG, "onPageFinished: " + url);


                    // ── Detect redirect away from the original meeting URL ────
                    if (!loadedUrl.isEmpty()
                            && !url.equals(loadedUrl)
                            && !url.contains("about:blank")) {
                        Log.d(TAG, "Page navigated away from meeting URL — treating as call ended");
                        triggerCallEnded();
                        return;
                    }


                    if (loadedUrl.isEmpty()) {
                        loadedUrl = url;  // capture the initial meeting URL
                    }


                    // ── Inject JS hooks ───────────────────────────────────────
                    String jsCode =
                            "(function() {" +
                                    "   function hookEndCallButton() {" +
                                    "       var selectors = [" +
                                    "           '[data-testid=\"end-call\"]'," +
                                    "           '[aria-label=\"End call\"]'," +
                                    "           '[title=\"End call\"]'," +
                                    "           '.end-call-btn'," +
                                    "           '#end-call'," +
                                    "           'button[class*=\"end\"]'," +
                                    "           'button[class*=\"leave\"]'," +
                                    "           'button[class*=\"hangup\"]'," +
                                    "           '[class*=\"endCall\"]'," +
                                    "           '[class*=\"leaveCall\"]'" +
                                    "       ];" +
                                    "       var cancelSelectors = [" +
                                    "           '[data-testid=\"cancel-meeting\"]'," +
                                    "           '[aria-label=\"Cancel meeting\"]'," +
                                    "           '[title=\"Cancel meeting\"]'," +
                                    "           '[aria-label=\"Cancel Meeting\"]'," +
                                    "           '[title=\"Cancel Meeting\"]'," +
                                    "           '.cancel-meeting-btn'," +
                                    "           '#cancel-meeting'," +
                                    "           'button[class*=\"cancel\"]'," +
                                    "           '[class*=\"cancelMeeting\"]'," +
                                    "           '[class*=\"cancelCall\"]'," +
                                    "           'button[class*=\"decline\"]'," +
                                    "           '[class*=\"declineCall\"]'," +
                                    "           '[class*=\"rejectCall\"]'" +
                                    "       ];" +
                                    "       function checkButtons() {" +
                                    "           for (var i = 0; i < selectors.length; i++) {" +
                                    "               var btns = document.querySelectorAll(selectors[i]);" +
                                    "               for (var j = 0; j < btns.length; j++) {" +
                                    "                   var btn = btns[j];" +
                                    "                   if (btn && !btn._androidHooked) {" +
                                    "                       btn._androidHooked = true;" +
                                    "                       btn.addEventListener('click', function() {" +
                                    "                           Android.log('End call button clicked');" +
                                    "                           Android.onCallEnded();" +
                                    "                       });" +
                                    "                   }" +
                                    "               }" +
                                    "           }" +
                                    "           for (var ci = 0; ci < cancelSelectors.length; ci++) {" +
                                    "               var cancelBtns = document.querySelectorAll(cancelSelectors[ci]);" +
                                    "               for (var cj = 0; cj < cancelBtns.length; cj++) {" +
                                    "                   var cancelBtn = cancelBtns[cj];" +
                                    "                   if (cancelBtn && !cancelBtn._androidCancelHooked) {" +
                                    "                       cancelBtn._androidCancelHooked = true;" +
                                    "                       cancelBtn.addEventListener('click', function() {" +
                                    "                           Android.log('Cancel meeting button clicked');" +
                                    "                           Android.onMeetingCancelled();" +
                                    "                       });" +
                                    "                   }" +
                                    "               }" +
                                    "           }" +
                                    "       }" +
                                    "       checkButtons();" +
                                    "       var observer = new MutationObserver(function() { checkButtons(); });" +
                                    "       observer.observe(document.body, { childList: true, subtree: true });" +
                                    // ── Catch meeting end via window unload / beforeunload ─
                                    "       window.addEventListener('unload', function() {" +
                                    "           Android.log('Window unload fired — meeting ended');" +
                                    "           Android.onCallEnded();" +
                                    "       });" +
                                    "       window.addEventListener('beforeunload', function() {" +
                                    "           Android.log('Window beforeunload fired — meeting ended');" +
                                    "           Android.onCallEnded();" +
                                    "       });" +
                                    // ── Catch postMessage events from the meeting iframe ───
                                    "       window.addEventListener('message', function(event) {" +
                                    "           Android.log('postMessage received: ' + JSON.stringify(event.data));" +
                                    "           var data = event.data;" +
                                    "           if (data) {" +
                                    "               var str = typeof data === 'string' ? data.toLowerCase() : JSON.stringify(data).toLowerCase();" +
                                    "               if (str.indexOf('call-ended') !== -1 ||" +
                                    "                   str.indexOf('callended') !== -1 ||" +
                                    "                   str.indexOf('meeting-ended') !== -1 ||" +
                                    "                   str.indexOf('meetingended') !== -1 ||" +
                                    "                   str.indexOf('left') !== -1 ||" +
                                    "                   str.indexOf('hangup') !== -1) {" +
                                    "                   Android.log('Meeting end detected via postMessage');" +
                                    "                   Android.onCallEnded();" +
                                    "               }" +
                                    "           }" +
                                    "       });" +
                                    "       Android.log('All hooks initialized');" +
                                    "   }" +
                                    "   if (document.readyState === 'loading') {" +
                                    "       document.addEventListener('DOMContentLoaded', hookEndCallButton);" +
                                    "   } else {" +
                                    "       hookEndCallButton();" +
                                    "   }" +
                                    "})();";


                    view.evaluateJavascript(jsCode, null);
                }


                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.d(TAG, "shouldOverrideUrlLoading: " + url);


                    if (!loadedUrl.isEmpty()
                            && !url.equals(loadedUrl)
                            && !url.contains(loadedUrl)
                            && !loadedUrl.contains(url)) {
                        Log.d(TAG, "URL changed significantly — treating as call ended");
                        triggerCallEnded();
                        return true;
                    }


                    view.loadUrl(url);
                    return false;
                }


                @Override
                public void onReceivedError(WebView view, WebResourceRequest request,
                                            WebResourceError error) {
                    Log.e(TAG, "WebViewError: " + (error != null ? error.toString() : "Unknown"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error != null) {
                        Toast.makeText(context,
                                "Error loading page: " + error.getDescription(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });


            Log.d(TAG, "Loading URL: " + url);
            webView.loadUrl(url);


            ad_dialog.setOnDismissListener(dialog -> {
                Log.d(TAG, "Dialog dismissed");
                if (delegate != null) {
                    delegate.onBackPressed();
                } else if (currentDelegate != null) {
                    currentDelegate.onBackPressed();
                }
                cleanup();
            });


            btn_close.setOnClickListener(v -> {
                Log.d(TAG, "Close button clicked");
                if (delegate != null) {
                    delegate.onBackPressed();
                } else if (currentDelegate != null) {
                    currentDelegate.onBackPressed();
                }
                if (ad_dialog != null && ad_dialog.isShowing()) {
                    ad_dialog.dismiss();
                }
                cleanup();
            });


            ad_dialog.setContentView(webViewLayout);
            ad_dialog.show();


            currentDialog = ad_dialog;
            currentWebView = webView;
            isWebViewShowing = true;


        } catch (Exception e) {
            Log.e(TAG, "Error showing WebView: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Error starting video call", Toast.LENGTH_SHORT).show();
            cleanup();
        }
    }


    // Without delegate Method.
    public static void loadAVChatView(Context context, Activity activity, String url) {
        loadAVChatView(context, activity, url, null);
    }


    public static void handlePermissionResult(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("video_call_prefs", Context.MODE_PRIVATE);
        String pendingUrl = prefs.getString("pending_avchat_url", "");
        boolean hasPendingDelegate = prefs.getBoolean("has_pending_delegate", false);


        if (!pendingUrl.isEmpty() && !pendingUrl.equals("")) {
            prefs.edit().clear().apply();


            showVideoCallWebView(activity, activity, pendingUrl, new VideoCallDelegate() {
                @Override
                public void onCallEnded() {
                    Log.d(TAG, "Permission result - Call ended");
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Video call ended", Toast.LENGTH_SHORT).show();
                        activity.finish();
                    });
                }


                @Override
                public void onBackPressed() {
                    Log.d(TAG, "Permission result - Back pressed");
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Video call closed", Toast.LENGTH_SHORT).show();
                        activity.finish();
                    });
                }
            });
        }
    }


    //Terms And Condition check.
    public static void updateCachedUserData(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String existingJson = prefs.getString("Json_key", "");


            if (!existingJson.isEmpty()) {
                JSONObject userJson = new JSONObject(existingJson);
                userJson.put("requiresTermsAcceptance", Constants.requiresTermsAcceptance);
                userJson.put("termsVersion", Constants.termsVersion);


                prefs.edit().putString("Json_key", userJson.toString()).apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // check for the If the Permission is Accessed or Custom Permission check.
    public static boolean hasPermissions(String[] permissions, Context context) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    // check for the If the Permission is Requesting Custom Permission check.
    public static void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    },
                    PERMISSION_REQUEST_CAMERA_AUDIO
            );
        }
    }


    // Clean the WebView after closing the WebView.
    public static void cleanup() {
        Log.d(TAG, "Cleaning up resources");


        if (currentWebView != null) {
            currentWebView.loadUrl("about:blank");
            currentWebView.stopLoading();
            currentWebView.clearHistory();
            currentWebView.clearCache(true);
            currentWebView.destroy();
            currentWebView = null;
        }


        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }


        currentDialog = null;
        isWebViewShowing = false;
        currentDelegate = null;
        currentUrl = "";
    }


    public static boolean isWebViewShowing() {
        return isWebViewShowing;
    }


    public static String getCurrentUrl() {
        return currentUrl;
    }


    // Chat Navigation - avChat.
    public static void loadWebView(Context context, Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        Dialog ad_dialog = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        LayoutInflater inflater = activity.getLayoutInflater();
        View webViewLayout = inflater.inflate(R.layout.webview, null);
        WebView webView;
        webView = webViewLayout.findViewById(R.id.webView);
        TextView tv_contactName = webViewLayout.findViewById(R.id.tv_contactName);
        tv_contactName.setText(Constants.NAME);
        ImageButton btn_close = webViewLayout.findViewById(R.id.btn_back);


        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        String[] permissions_local = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
                if (!hasPermissions(permissions_local, context)) {
                    requestPermissions(activity);
                }
            }
        });


        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e("WebViewError", "Error loading page: " + error.toString());
            }
        });


        String loginType = "pro";
        String token = Constants.TOKEN;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jid = pref.getString("xmpp_jid", null);
        String finalUrl = getString(loginType, token, jid);
        Log.e("AV", "AVLink: " + finalUrl);
        webView.loadUrl(finalUrl);


        ad_dialog.setContentView(webViewLayout);
        ad_dialog.show();


        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad_dialog.dismiss();
            }
        });
    }


    // Permission Dialog popup before checking the microphone or camera.
    public static void showAlertDialog(View view, Activity activity) {
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(activity);


        alertDialogBuilder.setTitle("Permission");
        alertDialogBuilder.setMessage("Required Camera and Microphone permission for Video chat.");


        alertDialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(activity, "You clicked on No", Toast.LENGTH_SHORT).show();
            }
        });


        alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(activity);
            }
        });


        alertDialogBuilder.setCancelable(false);


        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    // Redirection popup from one Fragment to other (If no matter found in documents, go to the Matter Page)
    public static void showReDirectionPopup(Activity activity, Fragment fragment, String confirmText) {
        // Add a static flag to prevent multiple popups
        if (isPopupShowing) {
            return;
        }
        isPopupShowing = true;


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.redirection_popup, null);


        TextView tv_confirmation = dialogLayout.findViewById(R.id.tv_confirmation);
        tv_confirmation.setText(R.string.alert_);
        tv_confirmation.setGravity(Gravity.CENTER);
        tv_confirmation.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


        TextView tv_confirmContent = dialogLayout.findViewById(R.id.tv_confirmContent);
        ImageView iv_close = dialogLayout.findViewById(R.id.iv_close);


        // Create the full text with clickable link
        String clickHereText = "Click here";
        SpannableString spannableString = new SpannableString(confirmText);


        // Find the start and end index of "Click here"
        int startIndex = confirmText.indexOf(clickHereText);
        int endIndex = startIndex + clickHereText.length();


        final AlertDialog dialog = dialogBuilder.create();
        dialog.setView(dialogLayout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


        // Create clickable span AFTER dialog is created
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle the click action
                if (fragment != null) {
                    Constants.mainActivity.navigationToModules(fragment);
                }
                dialog.dismiss();
                isPopupShowing = false; // Reset flag on dismiss
            }


            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(activity.getColor(R.color.blue));
                ds.bgColor = Color.TRANSPARENT;
            }
        };


        // Apply the span to "Click here"
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        // Set the styled text to TextView
        tv_confirmContent.setText(spannableString);
        tv_confirmContent.setMovementMethod(LinkMovementMethod.getInstance());
        tv_confirmContent.setHighlightColor(Color.TRANSPARENT);


        // Make sure clickable areas work properly
        tv_confirmContent.setFocusable(true);
        tv_confirmContent.setClickable(true);


        // Show dialog AFTER everything is set up
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);


            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }


        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isPopupShowing = false; // Reset flag on dismiss
            }
        });


        // Reset flag when dialog is dismissed
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isPopupShowing = false;
            }
        });
    }


    // Show the Renewal Popup (subscription Expired)
    public static void showRenewalPopup(Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.reneval_popup, null);


        TextView tv_confirmation = dialogLayout.findViewById(R.id.tv_confirmation);
        TextView tv_confirmContent = dialogLayout.findViewById(R.id.tv_confirmContent);
        ImageView iv_close = dialogLayout.findViewById(R.id.iv_close);
        Button btnSave = dialogLayout.findViewById(R.id.btnSave);
        if (Constants.ROLE.equals("SU")) {
            btnSave.setVisibility(VISIBLE);
        } else {
            btnSave.setVisibility(GONE);
        }
        btnSave.setText(R.string.renew_now);
        tv_confirmation.setText(R.string.subscription_expired);
        if (Constants.CATEGORY.equals("solo")) {
            btnSave.setText(R.string.upgrade);
            tv_confirmContent.setText(R.string.this_option_is_unavailable_on_the_free_plan_click_here_to_upgrade);
        } else {
            tv_confirmContent.setText(R.string.your_subscription_has_expired_renew_now_to_keep_your_account_active_and_access_all_features);
        }
        final AlertDialog dialog = dialogBuilder.create();
        dialog.setView(dialogLayout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            int marginInDp = 20;
            float scale = activity.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);


            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);


            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);


            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPaySubscriptionPage(activity);
            }
        });


        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }


    //Nagivation for the Subscription page to web
    public static void launchPaySubscriptionPage(Activity activity) {
        final int AUTH_REQUEST_CODE = 1001;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.paymentUrl + "=" + Constants.FirmEmail.toLowerCase() + "&" + "users=" + Constants.User_Allowed));
        startActivityForResult(activity, intent, AUTH_REQUEST_CODE, null);
    }


    // Error Handing in all modules popup
    public static Dialog showErrorAlert(String message, Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.alert_dialog, null);
        LinearLayout ll_view = dialogLayout.findViewById(R.id.ll_view);
        ll_view.setBackground(activity.getDrawable(R.drawable.rectangle_light_grey_bg));
        TextView tv_ok = dialogLayout.findViewById(R.id.tv_ok);
        tv_ok.setTextSize(17);
        tv_ok.setPadding(20, 20, 20, 20);
        tv_ok.setText(R.string.ok);


        tv_ok.setTextColor(activity.getColor(R.color.light_blue));
        TextView alert_content = dialogLayout.findViewById(R.id.alert_content);
        alert_content.setText(message);
        alert_content.setGravity(Gravity.CENTER);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    // check if the Process is already in loading Stage.
    public static Dialog get_progress(Activity activity) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }


        currentDialog = new Dialog(activity);
        currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currentDialog.setContentView(R.layout.loading);
        currentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        currentDialog.setCancelable(false);
        currentDialog.setCanceledOnTouchOutside(false);


        currentDialog.show();
        return currentDialog;
    }


    // Dismiss the common Dialog after Api Response Received Stage.
    public static void dismiss_dialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    // coverting the Any String Time/Date format to the custom or Expected Time format.
    public static Date stringToDateTimeDefault(String dateTime, String format) {
        Date result = null;
        try {
            if (dateTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                result = sdf.parse(dateTime);
            }
        } catch (Exception ex) {
            ex.fillInStackTrace();
        }
        return result;
    }


    // Converting the Expected Time format to Any String Time/Date format to the custom
    public static String getDateToString(Date date, String formats) {
        String result = "";
        try {
            if (date == null) {
                date = f_Calendar.getTime();
            }
            Format dateFormat = new SimpleDateFormat(formats);
            result = dateFormat.format(date);
        } catch (Exception e) {
        }
        return result;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager objConnectivityManager;
        boolean isNetworkAvailable = false;


        try {
            objConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            e.getMessage();
            AndroidUtils.logMsg("@ commonmethods isNetworkAvailable(): " + e.toString());
        } finally {
            objConnectivityManager = null;
        }
        return isNetworkAvailable;
    }


    // Return the Expected Date Format from Any format
    public static String formatToMMMddYYYY(String dateString) {


        if (dateString == null || dateString.trim().isEmpty())
            return "";


        String[] possibleFormats = {
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
        };


        for (String format : possibleFormats) {
            try {
                SimpleDateFormat inputSdf = new SimpleDateFormat(format, Locale.US);
                inputSdf.setLenient(false);


                Date date = inputSdf.parse(dateString);
                if (date != null) {
                    SimpleDateFormat outputSdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    return outputSdf.format(date);
                }


            } catch (Exception ignored) {
            }
        }


        return "";
    }


    // Return the Deleted on Date Format from Any format used in deleted documents in documents Module,


    public static String normalizeDeletedOn(String dateString) {
        if (dateString == null || dateString.trim().isEmpty() || dateString.equals("null"))
            return null;


        Date date = null;


        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(dateString);
        } catch (Exception ignored) {
        }


        if (date == null) {
            try {
                date = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
                        .parse(dateString);
            } catch (Exception ignored) {
            }
        }


        if (date == null)
            return dateString;


        return new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
                .format(date);
    }


    // Return the true or false according to the validating Start and End Date
    public static boolean validateStartAndEndDates(Context context, TextView startDateTextView, TextView endDateTextView, String dateFormat) {
        String startDateStr = startDateTextView.getText().toString().trim();
        String endDateStr = endDateTextView.getText().toString().trim();


        // If either date is empty, no validation needed
        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            return true;
        }


        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);


            if (endDate.before(startDate)) {
                showAlert("End date should not be earlier than start date", (Activity) context);
                endDateTextView.setText(""); // Clear the invalid end date
                return false;
            }


            if (startDate.after(endDate)) {
                showAlert("Start date should not be later than end date", (Activity) context);
                startDateTextView.setText(""); // Clear the invalid start date
                return false;
            }


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }


    // Common Date Picker with the Null Date Set as Default
    public static void showDatePicker(final TextView textView, boolean isStartDate) {
        showDatePicker(textView, isStartDate, null);
    }


    // Common Date Picker with the Expected Date Set as Default
    public static void showDatePicker(final TextView textView, boolean isStartDate, @Nullable Runnable onDateSet) {
        final Calendar calendar = Calendar.getInstance();
        // Create LOCAL calendars instead of using static ones
        final Calendar localStartCalendar = Calendar.getInstance();
        final Calendar localEndCalendar = Calendar.getInstance();


        if (textView.getText() != null && !textView.getText().toString().trim().isEmpty()) {
            String value = textView.getText().toString().trim();
            String[] possibleFormats = {
                    "dd-MM-yyyy", "yyyy-MM-dd", "MM-dd-yyyy",
                    "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd",
                    "MMM dd, yyyy", "dd MMM yyyy",
                    "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"
            };
            for (String format : possibleFormats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                    Date parsed = sdf.parse(value);
                    if (parsed != null) {
                        calendar.setTime(parsed);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }


        Context context = textView.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_date_picker, null);


        NumberPicker npMonth = view.findViewById(R.id.np_month);
        NumberPicker npDay = view.findViewById(R.id.np_day);
        NumberPicker npYear = view.findViewById(R.id.np_year);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        Button btnOk = view.findViewById(R.id.btn_ok);
        Button btnCancel = view.findViewById(R.id.btn_cancel);


        final String[] months = new DateFormatSymbols(Locale.US).getShortMonths();
        final String[] monthNames = Arrays.copyOf(months, 12);
        npMonth.setMinValue(0);
        npMonth.setMaxValue(11);
        npMonth.setDisplayedValues(monthNames);
        npMonth.setValue(calendar.get(Calendar.MONTH));
        npMonth.setWrapSelectorWheel(true);


        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        npDay.setMinValue(1);
        npDay.setMaxValue(maxDay);
        npDay.setValue(calendar.get(Calendar.DAY_OF_MONTH));
        npDay.setWrapSelectorWheel(false);


        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        npYear.setMinValue(currentYear - 100);
        npYear.setMaxValue(currentYear + 10);
        npYear.setValue(calendar.get(Calendar.YEAR));
        npYear.setWrapSelectorWheel(false);


        NumberPicker.OnValueChangeListener refreshDays = (picker, oldVal, newVal) -> {
            Calendar temp = Calendar.getInstance();
            temp.set(Calendar.YEAR, npYear.getValue());
            temp.set(Calendar.MONTH, npMonth.getValue());
            int max = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
            npDay.setMaxValue(max);
            if (npDay.getValue() > max) npDay.setValue(max);
        };
        npMonth.setOnValueChangedListener(refreshDays);
        npYear.setOnValueChangedListener(refreshDays);


        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();


        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        ivClose.setOnClickListener(v -> dialog.dismiss());


        btnCancel.setOnClickListener(v -> {
            textView.setText("");
            dialog.dismiss();
        });


        btnOk.setOnClickListener(v -> {
            calendar.set(Calendar.YEAR, npYear.getValue());
            calendar.set(Calendar.MONTH, npMonth.getValue());
            calendar.set(Calendar.DAY_OF_MONTH, npDay.getValue());


            // REMOVE the static calendar comparison logic
            // Just set the date directly
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            textView.setText(displayFormat.format(calendar.getTime()));


            if (onDateSet != null) onDateSet.run();
            dialog.dismiss();
        });


        dialog.show();
    }


    // Return the Expected Date Format to "dd-MM-yyyy"
    public static String convertAnyDateToDDMMYYYY(String dateString) {


        String[] possibleFormats = new String[]{
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
        };


        for (String format : possibleFormats) {
            try {
                SimpleDateFormat input = new SimpleDateFormat(format, Locale.getDefault());
                input.setLenient(false);


                Date date = input.parse(dateString);
                if (date != null) {
                    SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    return output.format(date);
                }
            } catch (Exception ignored) {
            }
        }


        return "";
    }


    // Return the Expected Date Format to "yyyy-MM-dd"
    public static String convertAnyDateToYYYYMMDD(String dateString) {


        String[] possibleFormats = new String[]{
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
        };


        for (String format : possibleFormats) {
            try {
                SimpleDateFormat input = new SimpleDateFormat(format, Locale.getDefault());
                input.setLenient(false);


                Date date = input.parse(dateString);
                if (date != null) {
                    SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    return output.format(date);
                }
            } catch (Exception ignored) {
            }
        }


        return "";
    }


    public static String getDateSelectedFormt(String original_format, String
            selected_formate, String date) {
        Date converted_date = AndroidUtils.stringToDateTimeDefault(date, original_format);
        return AndroidUtils.getDateToString(converted_date, selected_formate);
    }


    public static String getDocTypeValue(String value) {
        String name = "";
        switch (value) {
            case "driver_license":
                name = "Driver Licence";
                break;
            case "passport":
                name = "Passport";
                break;
            case "aadhar":
                name = "AADHAR Card";
                break;
            case "pancard":
                name = "PAN Card";
                break;
            case "voterid":
                name = "Voter ID";
                break;
            case "nic":
                name = "National Identity Document (DNI)";
                break;
            case "ssc":
                name = "Social Security Card";
                break;
            case "cpf":
                name = "Cadastro de Pessoas Físicas (CPF)";
                break;
        }
        return name;
    }


    public static String get_affiliationType(String value) {
        String affiliation_type = "";
        switch (value) {
            case "citz":
                affiliation_type = "Citizen";
                break;
            case "dcitz":
                affiliation_type = "Dual Citizenship";
                break;
            case "pr":
                affiliation_type = "Permanent Resident";
                break;
            case "tvs":
                affiliation_type = "Temporary Resident - Student";
                break;
            case "tvw":
                affiliation_type = "Temporary Resident - Work";
                break;
        }
        return affiliation_type;
    }


    public static String getemailpattern() {
        return "[a-zA-Z0-9._-]+@[a-z-]+\\.+[a-z]+";
    }


    public static String getmobilepattern() {
        return "[0-9]";
    }


    public static void remove_credential_preference(Context context) {
    }


    public static void displayFile(String url, Context context) {
        String str_path = Environment.getExternalStorageDirectory().toString() + File.separator + url;
        Uri photoURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(str_path));
        try {
            File pdfFile = new File(str_path);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(photoURI, "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = Intent.createChooser(target, "Open File");
            context.startActivity(intent);
        } catch (Exception e) {
            e.getMessage();
        }
    }


    public static void send_notification(final Context context, final String UID,
                                         final String msg, final String subject) {


        if (ChatConnectionService.getState().equals(ChatConnection.ConnectionState.CONNECTED)) {
            attempts = 0;
            Intent intent = new Intent(ChatConnectionService.SEND_MESSAGE);
            intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY,
                    msg);
            intent.putExtra(ChatConnectionService.BUNDLE_TO, UID);
            intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, subject);
            context.sendBroadcast(intent);
        } else {
            attempts++;
            if (!ChatConnectionService.getState().equals(ChatConnection.ConnectionState.CONNECTED))
                reconnecXMPPServer(context);
            Handler handler = new Handler();
            final int finalAttempts = attempts;
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (finalAttempts < 5) {
                        send_notification(context, UID, msg, subject);
                    } else
                        attempts = 0;
                }
            }, 5000);
        }
    }


    private static final Calendar startCalendar = Calendar.getInstance();
    private static final Calendar endCalendar = Calendar.getInstance();


    public static String getFileType(String filename) {
        String type = "";
        String name = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        if (name.toLowerCase().equals("png") || name.toLowerCase().equals("jpg") || name.toLowerCase().equals("JPEG")) {
            type = "image/jpeg";
        } else if (name.toLowerCase().equals("pdf")) {
            type = "image/pdf";
        } else if (name.toLowerCase().equals("docx")) {
            type = "image/docx";
        }
        return type;
    }


    //Reconnecting the Xmpp Server for Chat in Chat module.
    public static void reconnecXMPPServer(final Context context) {
        Intent i1 = new Intent(context, ChatConnectionService.class);
        context.stopService(i1);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                context.startService(new Intent(context, ChatConnectionService.class));
            }
        }, 5000);
    }


    // Displaying the ListView with Common Toggle Function
    public static void display_listview(boolean ischecked, ListView listView) {
        if (ischecked) {
            listView.setVisibility(VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
        }
    }


    // Toggle the View / Button State in according to the Listview Sizes or 0 or 1.
    public static void ToggleButton(int ListSize, View button) {
        if (ListSize == 0) {
            button.setAlpha(0.5f);
            button.setEnabled(false);
        } else {
            button.setAlpha(1.0f);
            button.setEnabled(true);
        }
    }


    // Displaying the Spinnerview with Common Toggle Function
    public static void DisplaySpinnerView(ListView listView, TextView tv_listview, String
            tv_value, ImageView img_dropdown, ImageView img_clear, boolean img_dropdown_clicked) {
        if (img_dropdown_clicked) {
            img_dropdown.setVisibility(View.GONE);
            img_clear.setVisibility(VISIBLE);
            listView.setVisibility(View.GONE);
            tv_listview.setText(tv_value);
        } else {
            img_dropdown.setVisibility(VISIBLE);
            img_clear.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            tv_value = "";
            tv_listview.setText(tv_value);
        }
    }


    // Displaying the Spinnerview with Common Toggle Function with hint
    public static void DisplaySpinnerView(
            ListView listView,
            TextView tv_listview,
            String tv_value,
            ImageView img_dropdown,
            ImageView img_clear,
            boolean img_dropdown_clicked,
            CommonSpinnerAdapter adapter) {
        DisplaySpinnerView(
                listView, tv_listview, tv_value,
                img_dropdown, img_clear,
                !img_dropdown_clicked,           // open if currently closed, close if currently open
                adapter, "Search");


    }


    // Displaying the Spinnerview with Common Toggle Function with hint and Search Bar
    public static void DisplaySpinnerView(
            ListView listView,
            TextView tv_listview,
            String tv_value,
            ImageView img_dropdown,
            ImageView img_clear,
            boolean img_dropdown_clicked,
            CommonSpinnerAdapter adapter, String SearchHint) {


        if (img_dropdown_clicked) {
            // OPENING the spinner
            if (adapter != null) {
                // Reset filter first to show all items
                adapter.getFilter().filter("");


                // Get the actual item count AFTER resetting filter
                int itemCount = adapter.getCount();


                // Only attach search if list has more than 4 items
                if (itemCount > 4) {
                    // Check if search is already attached
                    View existingSearch = (View) listView.getTag(R.id.tag_search_bar);
                    if (existingSearch == null) {
                        attachSearch(listView, adapter, SearchHint);
                    } else {
                        // Make existing search visible
                        existingSearch.setElevation(10);
                        existingSearch.setVisibility(View.VISIBLE);
                        if (existingSearch.getParent() instanceof LinearLayout) {
                            ((LinearLayout) existingSearch.getParent()).setVisibility(View.VISIBLE);
                        }
                    }


                    View searchCardView = (View) listView.getTag(R.id.tag_search_bar);
                    if (searchCardView != null) {
                        searchCardView.setVisibility(View.VISIBLE);
                        if (searchCardView.getParent() instanceof LinearLayout) {
                            ((LinearLayout) searchCardView.getParent()).setVisibility(View.VISIBLE);
                        }
                        // Clear search text when opening
                        EditText etSearch = (EditText) listView.getTag(R.id.tag_search_edittext);
                        if (etSearch != null) {
                            etSearch.setText("");
                        }
                    }
                } else {
                    // Hide search bar for small lists
                    View searchCardView = (View) listView.getTag(R.id.tag_search_bar);
                    if (searchCardView != null) {
                        searchCardView.setVisibility(View.GONE);
                        if (searchCardView.getParent() instanceof LinearLayout) {
                            ((LinearLayout) searchCardView.getParent()).setVisibility(View.VISIBLE);
                        }
                    }
                }


                // IMPORTANT: Make sure listView is visible and has proper height
                listView.setVisibility(View.VISIBLE);


                // Load the list with proper height calculation
                LoadList(listView, listView.getContext(), itemCount, true);
            }


            // ✅ FIX: Update icons based on whether a value is already selected
            if (tv_value != null && !tv_value.isEmpty()) {
                // Value is selected → show clear icon, hide dropdown icon
                img_dropdown.setVisibility(View.GONE);
                img_clear.setVisibility(View.VISIBLE);
                tv_listview.setText(tv_value);
            } else {
                // No value selected → show dropdown icon, hide clear icon
                img_dropdown.setVisibility(View.VISIBLE);
                img_clear.setVisibility(View.GONE);
            }


        } else {
            // CLOSING the spinner - THIS IS WHERE THE SELECTION HAPPENS
            // Update the TextView with the selected value
            if (tv_value != null && !tv_value.isEmpty()) {
                tv_listview.setText(tv_value);
                img_dropdown.setVisibility(View.GONE);
                img_clear.setVisibility(View.VISIBLE);
            } else {
                // ✅ Explicitly clear the text so old value doesn't linger
                tv_listview.setText("");
                img_dropdown.setVisibility(View.VISIBLE);
                img_clear.setVisibility(View.GONE);
            }
            // Hide the list
            listView.setVisibility(View.GONE);


            // Hide search bar
            View searchCardView = (View) listView.getTag(R.id.tag_search_bar);
            if (searchCardView != null) {
                searchCardView.setVisibility(View.GONE);
                // Clear the EditText inside
                EditText etSearch = (EditText) listView.getTag(R.id.tag_search_edittext);
                if (etSearch != null) {
                    etSearch.setText("");
                }
                if (searchCardView.getParent() instanceof LinearLayout) {
                    ((LinearLayout) searchCardView.getParent()).setVisibility(View.GONE);
                }
            }


            // Reset filter for next open - IMPORTANT: Use original adapter, not filtered
            if (adapter != null) {
                adapter.getFilter().filter("");
            }
        }
    }


    // ✅ Interface for popup callbacks
    public interface TagPopupCallback {
        void onTagsSaved(JSONObject combinedTags, Object documentModel);


        void onDismiss();
    }


    // ✅ Reusable duplicate tag type check used in Document Add Tag flow.
    public static boolean isDuplicateTagType(
            String enteredTagType,
            ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> tags_list,
            int skipPosition // pass -1 for add, edit_position for edit
    ) {
        for (int i = 0; i < tags_list.size(); i++) {
            if (i == skipPosition) continue;
            if (tags_list.get(i).getTag_type().equalsIgnoreCase(enteredTagType.trim())) {
                return true;
            }
        }
        return false;
    }


    // ✅ Reusable save_edited_tags logic with Edit Flow of Added Tags in Documents Module
    public static void saveEditedTag(
            String tag_type,
            String tag_name,
            int edit_position,
            ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> tags_list,
            LinearLayout ll_added_tags,
            TextInputEditText tv_tag_type,
            TextInputEditText tv_tag_name,
            Activity activity
    ) {
        try {
            if (edit_position < 0 || edit_position >= tags_list.size()) {
                showAlert("Invalid position for editing tag.", activity);
                return;
            }


            // ✅ Check tag type only, skip own position
            if (isDuplicateTagType(tag_type, tags_list, edit_position)) {
                showAlert("This tag type already exists. Please use a different tag type.", activity);
                return;
            }


            com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = tags_list.get(edit_position);
            documentsModel.setTag_type(tag_type);
            documentsModel.setTag_name(tag_name);
            tags_list.set(edit_position, documentsModel);


            View view_to_update = ll_added_tags.getChildAt(edit_position);
            if (view_to_update != null) {
                TextView tv_edit_tag_document_name = view_to_update.findViewById(R.id.tv_document_name);
                tv_edit_tag_document_name.setText(tag_type + " - " + tag_name);
            }


            if (tv_tag_name != null && tv_tag_type != null) {
                tv_tag_name.setText("");
                tv_tag_type.setText("");
            }
        } catch (Exception e) {
            Log.e("saveEditedTag", "Error saving edited tag", e);
            showAlert("An error occurred while saving the tag: " + e.getMessage(), activity);
        }
    }


    // Display the Add tag Popup
    public static <T> void openAddTagsPopup(
            Context context,
            Activity activity,
            LayoutInflater inflater,
            View backgroundView,             // cl_document or null
            boolean isUpdateTag,
            ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> tags_list,
            ArrayList<T> selected_documents_list,
            LinearLayout[] ll_added_tags_ref, // single-element array to allow mutation
            TextInputEditText[] tv_tag_type_ref,
            TextInputEditText[] tv_tag_name_ref,
            boolean[] isedit_ref,
            int[] edit_position_ref,
            OnTagsSavedCallback<T> onSaved
    ) {
        if (!isUpdateTag && selected_documents_list.isEmpty()) {
            showAlert("Please select atleast one document to add tags", activity);
            return;
        }


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        if (backgroundView != null) backgroundView.setAlpha(0.5f);


        View view = inflater.inflate(R.layout.add_tag, null);
        TextInputEditText tv_tag_type = view.findViewById(R.id.tv_tag_type);
        TextInputEditText tv_tag_name = view.findViewById(R.id.tv_tag_name);
        tv_tag_type_ref[0] = tv_tag_type;
        tv_tag_name_ref[0] = tv_tag_name;


        TextView tag_type_name = view.findViewById(R.id.tag_type_name);
        TextView tag_name_tv = view.findViewById(R.id.tag_name);
        TextView tv_added_tags = view.findViewById(R.id.tv_added_tags);
        TextView header_name = view.findViewById(R.id.header_name);
        LinearLayout ll_added_tags = view.findViewById(R.id.ll_added_tags);
        ll_added_tags_ref[0] = ll_added_tags;


        tv_added_tags.setText(R.string.added_tags);
        tag_type_name.setText(R.string.tag_type);
        tag_name_tv.setText(R.string.tag);
        tv_tag_type.setHint(R.string.tag_type);
        tv_tag_name.setHint(R.string.tag);
        tv_tag_name.addTextChangedListener(new Validation(tv_tag_name));
        tv_tag_type.addTextChangedListener(new Validation(tv_tag_type));
        header_name.setText(isUpdateTag ? R.string.update_tag : R.string.add_tag);


        Button btn_add = view.findViewById(R.id.btn_add_tags);
        btn_add.setText(R.string.add);


        InputFilter[] filters_title = {new InputFilter.LengthFilter(30)};
        InputFilter[] filters_desc = {new InputFilter.LengthFilter(100)};
        tv_tag_type.setFilters(filters_title);
        tv_tag_name.setFilters(filters_desc);


        AppCompatButton btn_cancel = view.findViewById(R.id.btn_cancel_tag);
        AppCompatButton btn_save_tag = view.findViewById(R.id.btn_save_tag);
        ImageView iv_cancel = view.findViewById(R.id.close_edit_docs);


        // Show/hide added tags label
        if (ll_added_tags.getChildCount() > 0) {
            tv_added_tags.setVisibility(View.VISIBLE);
        } else {
            tv_added_tags.setVisibility(View.GONE);
        }


        final AlertDialog dialog = dialogBuilder.create();


        // Pre-fill existing tags if update mode
        if (!tags_list.isEmpty()) {
            addTagsListing(
                    context, activity, inflater, btn_save_tag, tags_list,
                    ll_added_tags, tv_added_tags, tv_tag_type, tv_tag_name,
                    isedit_ref, edit_position_ref
            );
        }


        btn_save_tag.setEnabled(false);
        btn_save_tag.setAlpha(0.5f);


        // ✅ Cancel / close listeners
        View.OnClickListener dismissListener = v -> {
            if (!tags_list.isEmpty()) AndroidUtils.Delete_Popup(activity, dialog);
            else dialog.dismiss();
        };
        iv_cancel.setOnClickListener(dismissListener);
        btn_cancel.setOnClickListener(dismissListener);
//        if (isUpdateTag && !tags_list.isEmpty()) {
//            addTagsListing(
//                    context, activity, inflater, btn_save_tag, tags_list,
//                    ll_added_tags, tv_added_tags, tv_tag_type, tv_tag_name,
//                    isedit_ref, edit_position_ref
//            );
//            // ← Enable save button since tags already exist
//          AndroidUtils.ToggleButton(1,btn_save_tag);
//        } else {
//            AndroidUtils.ToggleButton(0,btn_save_tag);
//        }
        // ✅ Add button — with tag type duplicate check
        btn_add.setOnClickListener(v -> {
            String typeText = Objects.requireNonNull(tv_tag_type.getText()).toString();
            String nameText = Objects.requireNonNull(tv_tag_name.getText()).toString();


            if (typeText.isEmpty() && nameText.isEmpty()) {
                showAlert("Please check the Tag Type, Tag", activity);
            } else if (typeText.isEmpty()) {
                showAlert("Please check the Tag Type", activity);
            } else if (nameText.isEmpty()) {
                showAlert("Please check the Tag", activity);
            } else {
                if (isedit_ref[0]) {
                    saveEditedTag(
                            typeText, nameText,
                            edit_position_ref[0], tags_list,
                            ll_added_tags, tv_tag_type, tv_tag_name, activity
                    );
                } else {
                    // ✅ Duplicate tag type check on Add
                    if (isDuplicateTagType(typeText, tags_list, -1)) {
                        showAlert("This tag type already exists. Please use a different tag type.", activity);
                        return;
                    }
                    addTagsListing(
                            context, activity, inflater, btn_save_tag, tags_list,
                            ll_added_tags, tv_added_tags, tv_tag_type, tv_tag_name,
                            isedit_ref, edit_position_ref
                    );
                }
                isedit_ref[0] = false;
                tv_tag_name.setText("");
                tv_tag_type.setText("");
                btn_save_tag.setEnabled(true);
                btn_save_tag.setAlpha(1.0f);
            }
        });


        // ✅ Save button
        btn_save_tag.setOnClickListener(v -> {
            onSaved.onSave(tags_list, dialog);
        });


        dialog.setOnDismissListener(d -> {
            if (backgroundView != null) backgroundView.setAlpha(1.0f);
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.show();
    }


    // ✅ Callback interface for save
    public interface OnTagsSavedCallback<T> {
        void onSave(
                ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> tags_list,
                AlertDialog dialog
        );
    }


    // ✅ Shared addTagsListing used in openAddTagsPopup
    public static void addTagsListing(
            Context context,
            Activity activity,
            LayoutInflater inflater,
            Button btn_save_tag,
            ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> tags_list,
            LinearLayout ll_added_tags,
            TextView tv_added_tags,
            TextInputEditText tv_tag_type,
            TextInputEditText tv_tag_name,
            boolean[] isedit_ref,
            int[] edit_position_ref
    ) {
        ll_added_tags.removeAllViews();


        String typeText = Objects.requireNonNull(tv_tag_type.getText()).toString();
        String nameText = Objects.requireNonNull(tv_tag_name.getText()).toString();


        if (!typeText.isEmpty() && !nameText.isEmpty()) {
            com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
            documentsModel.setTag_type(typeText);
            documentsModel.setTag_name(nameText);
            tags_list.add(documentsModel);
        }


        for (int i = 0; i < tags_list.size(); i++) {
            View view_added_tags = inflater.inflate(R.layout.displays_documents_list, null);
            TextView tv_tag_document_name = view_added_tags.findViewById(R.id.tv_document_name);
            LinearLayout ll_tags = view_added_tags.findViewById(R.id.ll_tags);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            ll_tags.setLayoutParams(params);


            LinearLayoutCompat chk_box_layout = view_added_tags.findViewById(R.id.chk_box_layout);
            chk_box_layout.setVisibility(View.GONE);
            CheckBox chk_selected_documents = view_added_tags.findViewById(R.id.chk_selected_documents);
            chk_selected_documents.setVisibility(View.GONE);


            ImageView iv_edit_tag = view_added_tags.findViewById(R.id.iv_edit_meta);
            ImageView iv_remove_tag = view_added_tags.findViewById(R.id.iv_cancel);
            iv_remove_tag.setTag(i);


            iv_remove_tag.setOnClickListener(v -> {
                int pos = (int) v.getTag();
                ll_added_tags.removeViewAt(pos);
                btn_save_tag.setEnabled(true);
                btn_save_tag.setAlpha(1.0f);
                com.digicoffer.lauditor.Documents.Models.DocumentsModel model = tags_list.get(pos);
                model.setTag_name("");
                model.setTag_type("");
                model.setChecked(false);
                tags_list.set(pos, model);
                tags_list.remove(pos);
                // Re-tag remaining remove buttons
                for (int j = 0; j < ll_added_tags.getChildCount(); j++) {
                    ImageView iv_remove = ll_added_tags.getChildAt(j).findViewById(R.id.iv_cancel);
                    if (iv_remove != null) iv_remove.setTag(j);
                }
                tv_added_tags.setVisibility(ll_added_tags.getChildCount() > 0 ? View.VISIBLE : View.GONE);
            });


            iv_edit_tag.setTag(i);
            iv_edit_tag.setOnClickListener(v -> {
                int pos = (int) v.getTag();
                com.digicoffer.lauditor.Documents.Models.DocumentsModel model = tags_list.get(pos);
                if (model != null) {
                    edit_position_ref[0] = pos;
                    isedit_ref[0] = true;
                    tv_tag_type.setText(model.getTag_type());
                    tv_tag_name.setText(model.getTag_name());
                }
            });


            iv_edit_tag.setVisibility(View.VISIBLE);
            tv_tag_document_name.setText(
                    tags_list.get(i).getTag_type() + " - " + tags_list.get(i).getTag_name()
            );
            ll_added_tags.addView(view_added_tags);
            tv_added_tags.setVisibility(ll_added_tags.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        }
    }


    // show the Date Picker with future, Past , Current Showing. used in Matterinformation_En
    public static void showDatePicker(
            final TextView textView,
            boolean allowPastDates,
            boolean allowCurrentDate,
            boolean allowFutureDates,
            @Nullable Runnable onDateSet
    ) {
        final Calendar calendar = Calendar.getInstance();
        final Calendar minCalendar = Calendar.getInstance();
        final Calendar maxCalendar = Calendar.getInstance();


        // Set min and max dates based on parameters
        if (allowPastDates && allowFutureDates) {
            // No restrictions - allow all dates
            minCalendar.set(Calendar.YEAR, 1900);
            minCalendar.set(Calendar.MONTH, 0);
            minCalendar.set(Calendar.DAY_OF_MONTH, 1);


            maxCalendar.set(Calendar.YEAR, 2100);
            maxCalendar.set(Calendar.MONTH, 11);
            maxCalendar.set(Calendar.DAY_OF_MONTH, 31);
        } else if (!allowPastDates && allowFutureDates) {
            // Only current and future dates
            minCalendar.setTime(new Date());
            if (!allowCurrentDate) {
                minCalendar.add(Calendar.DAY_OF_MONTH, 1); // Start from tomorrow
            }
            maxCalendar.set(Calendar.YEAR, 2100);
            maxCalendar.set(Calendar.MONTH, 11);
            maxCalendar.set(Calendar.DAY_OF_MONTH, 31);
        } else if (allowPastDates && !allowFutureDates) {
            // Only past dates
            maxCalendar.setTime(new Date());
            if (!allowCurrentDate) {
                maxCalendar.add(Calendar.DAY_OF_MONTH, -1); // End at yesterday
            }
            minCalendar.set(Calendar.YEAR, 1900);
            minCalendar.set(Calendar.MONTH, 0);
            minCalendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (!allowPastDates && !allowFutureDates && allowCurrentDate) {
            // Only current date
            minCalendar.setTime(new Date());
            maxCalendar.setTime(new Date());
        } else {
            // No dates allowed - disable picker
            return;
        }


        // Parse existing text if present
        if (textView.getText() != null && !textView.getText().toString().trim().isEmpty()) {
            String value = textView.getText().toString().trim();
            String[] possibleFormats = {
                    "dd-MM-yyyy", "yyyy-MM-dd", "MM-dd-yyyy",
                    "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd",
                    "MMM dd, yyyy", "dd MMM yyyy",
                    "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"
            };
            for (String format : possibleFormats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                    Date parsed = sdf.parse(value);
                    if (parsed != null) {
                        calendar.setTime(parsed);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }


        Context context = textView.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_date_picker, null);


        NumberPicker npMonth = view.findViewById(R.id.np_month);
        NumberPicker npDay = view.findViewById(R.id.np_day);
        NumberPicker npYear = view.findViewById(R.id.np_year);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        Button btnOk = view.findViewById(R.id.btn_ok);
        Button btnCancel = view.findViewById(R.id.btn_cancel);


        final String[] months = new DateFormatSymbols(Locale.US).getShortMonths();
        final String[] monthNames = Arrays.copyOf(months, 12);


        // Configure year picker with min/max
        int minYear = minCalendar.get(Calendar.YEAR);
        int maxYear = maxCalendar.get(Calendar.YEAR);
        npYear.setMinValue(minYear);
        npYear.setMaxValue(maxYear);


        // Ensure current selection is within bounds
        int currentYear = calendar.get(Calendar.YEAR);
        if (currentYear < minYear) currentYear = minYear;
        if (currentYear > maxYear) currentYear = maxYear;
        npYear.setValue(currentYear);
        npYear.setWrapSelectorWheel(false);


        // Configure month picker
        npMonth.setMinValue(0);
        npMonth.setMaxValue(11);
        npMonth.setDisplayedValues(monthNames);
        npMonth.setWrapSelectorWheel(true);


        // Update day range based on selected year/month and min/max dates
        Runnable updateDayRange = () -> {
            int year = npYear.getValue();
            int month = npMonth.getValue();


            Calendar temp = Calendar.getInstance();
            temp.set(year, month, 1);
            int maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH);


            // Calculate min day based on minCalendar
            int minDay = 1;
            if (year == minCalendar.get(Calendar.YEAR) && month == minCalendar.get(Calendar.MONTH)) {
                minDay = minCalendar.get(Calendar.DAY_OF_MONTH);
            }


            // Calculate max day based on maxCalendar
            int maxAllowedDay = maxDay;
            if (year == maxCalendar.get(Calendar.YEAR) && month == maxCalendar.get(Calendar.MONTH)) {
                maxAllowedDay = maxCalendar.get(Calendar.DAY_OF_MONTH);
            }


            npDay.setMinValue(minDay);
            npDay.setMaxValue(maxAllowedDay);


            // Adjust current day selection if needed
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            if (currentDay < minDay) currentDay = minDay;
            if (currentDay > maxAllowedDay) currentDay = maxAllowedDay;
            npDay.setValue(currentDay);
        };


        // Set initial month value
        int currentMonth = calendar.get(Calendar.MONTH);
        if (currentYear == minYear && currentMonth < minCalendar.get(Calendar.MONTH)) {
            currentMonth = minCalendar.get(Calendar.MONTH);
        }
        if (currentYear == maxYear && currentMonth > maxCalendar.get(Calendar.MONTH)) {
            currentMonth = maxCalendar.get(Calendar.MONTH);
        }
        npMonth.setValue(currentMonth);


        // Set up listeners
        NumberPicker.OnValueChangeListener onValueChange = (picker, oldVal, newVal) -> {
            updateDayRange.run();
        };


        npYear.setOnValueChangedListener(onValueChange);
        npMonth.setOnValueChangedListener(onValueChange);


        // Initial day range setup
        updateDayRange.run();


        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();


        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        ivClose.setOnClickListener(v -> dialog.dismiss());


        btnCancel.setOnClickListener(v -> {
            textView.setText("");
            dialog.dismiss();
        });


        btnOk.setOnClickListener(v -> {
            int year = npYear.getValue();
            int month = npMonth.getValue();
            int day = npDay.getValue();


            calendar.set(year, month, day);


            // Validate against min/max constraints
            if (calendar.getTimeInMillis() < minCalendar.getTimeInMillis()) {
                calendar.setTime(minCalendar.getTime());
            }
            if (calendar.getTimeInMillis() > maxCalendar.getTimeInMillis()) {
                calendar.setTime(maxCalendar.getTime());
            }


            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            textView.setText(displayFormat.format(calendar.getTime()));


            if (onDateSet != null) onDateSet.run();
            dialog.dismiss();
        });


        dialog.show();
    }


    // Attach the Search Bar in Spinner View
    public static void attachSearch(ListView listView, CommonSpinnerAdapter adapter, String SearchHint) {
        try {
            Context context = listView.getContext();


            Object existing = listView.getTag(R.id.tag_search_bar);
            if (existing instanceof View) {
                ((View) existing).setVisibility(View.VISIBLE);
                View existingParent = (View) ((View) existing).getParent();
                if (existingParent instanceof LinearLayout) {
                    existingParent.setVisibility(View.VISIBLE);
                }
                return;
            }


            View searchCardView = LayoutInflater.from(context).inflate(R.layout.search_layout_new, null);
            TextInputEditText etSearch = searchCardView.findViewById(R.id.et_Search);
            if (etSearch == null) {
                Log.e("attachSearch", "Could not find et_Search in layout");
                return;
            }
            if (!SearchHint.isEmpty()) {
                etSearch.setHint(SearchHint);
            } else {
                etSearch.setHint(R.string.search);
            }
            searchCardView.setElevation(10);
            final CommonSpinnerAdapter finalAdapter = adapter;
            final int maxHeightPx = context.getResources().getDimensionPixelSize(R.dimen.one_fifty_dp);


            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                }


                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    if (finalAdapter != null && s != null) {
                        try {
                            // ✅ Use FilterListener to resize AFTER filtering is done
                            finalAdapter.getFilter().filter(s, count -> {
                                listView.post(() -> updateListViewHeight(listView, count, maxHeightPx));
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }


                @Override
                public void afterTextChanged(Editable s) {
                }
            });


            // Search card params
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(dpToPx(context, 5), dpToPx(context, 5), dpToPx(context, 5), dpToPx(context, 5));
            searchCardView.setLayoutParams(cardParams);


            // ListView starts with WRAP_CONTENT; height applied after attach
            LinearLayout.LayoutParams lvParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(context, DynamicUtils.thirty)
            );
            listView.setLayoutParams(lvParams);


            // Wrapper
            LinearLayout wrapper = new LinearLayout(context);
            wrapper.setOrientation(LinearLayout.VERTICAL);
            wrapper.setBackgroundColor(Color.TRANSPARENT);


            ViewGroup parent = (ViewGroup) listView.getParent();
            if (parent == null) {
                Log.e("attachSearch", "ListView has no parent — cannot attach search bar");
                return;
            }


            int index = parent.indexOfChild(listView);


            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
//            wrapperParams.setMargins(dpToPx(context, 4), 0, dpToPx(context, 4), 0);


            parent.removeView(listView);
            wrapper.addView(searchCardView);
            wrapper.addView(listView);
            listView.setVisibility(View.VISIBLE);
            parent.addView(wrapper, index, wrapperParams);


            // ✅ Initial height after attach
            listView.post(() -> {
                int count = adapter != null ? adapter.getCount() : 0;
                updateListViewHeight(listView, count, maxHeightPx);
            });


            listView.setTag(R.id.tag_search_bar, searchCardView);
            listView.setTag(R.id.tag_search_edittext, etSearch);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ✅ Centralized height logic — called on first load and after every filter Search
    private static void updateListViewHeight(ListView listView, int filteredCount, int maxHeightPx) {
        if (filteredCount <= 0) {
            // No results — collapse to 0 so nothing shows
            ViewGroup.LayoutParams p = listView.getLayoutParams();
            p.height = 0;
            listView.setLayoutParams(p);
            return;
        }


        if (filteredCount <= 4) {
            // Few items — shrink-wrap exactly to content
            setDynamicHeight(listView);
        } else {
            // Many items — cap at 150dp and let it scroll
            ViewGroup.LayoutParams p = listView.getLayoutParams();
            p.height = maxHeightPx;
            listView.setLayoutParams(p);
        }
    }


    // Default CurrentList used in Members, Basic Profile Edit Page


    public static ArrayList<String> getCurrency_list() {
        ArrayList<String> currency = new ArrayList<>();
        currency.add("USDollar(USD)");
        currency.add("Euro(EUR)");
        currency.add("JapaneseYen(JPY)");
        currency.add("Pound(GBP)");
        currency.add("AustralianDollar(AUD)");
        currency.add("CanadianDOllar(CAD)");
        currency.add("SwissFranc(CHF)");
        currency.add("KuwaitiDinar(KWD)");
        currency.add("BahrainiDinar(BHD)");
        currency.add("IndianRupee(INR)");
        return currency;
    }


    public static void closeSpinnerDropdown(
            ListView listView,
            TextView tv_listview,
            String selectedValue,
            ImageView img_dropdown,
            ImageView img_clear,
            CommonSpinnerAdapter adapter) {


        // 1. Update display text
        tv_listview.setText(selectedValue != null ? selectedValue : "");


        // 2. Icon state — item selected
        img_dropdown.setVisibility(View.GONE);
        img_clear.setVisibility(View.VISIBLE);


        // 3. Hide ListView
        listView.setVisibility(View.GONE);


        // 4. Hide search bar FIRST (before setText so TextWatcher guard fires correctly)
        Object tag = listView.getTag(R.id.tag_search_bar);
        if (tag instanceof EditText) {
            EditText etSearch = (EditText) tag;
            etSearch.setVisibility(View.GONE);  // ← GONE before setText triggers TextWatcher
            etSearch.setText("");               // ← TextWatcher fires, but guard returns early
        }


        // 5. Hide wrapper (after search bar is already GONE)
        ViewParent parent = listView.getParent();
        if (parent instanceof LinearLayout) {
            ((LinearLayout) parent).setVisibility(View.GONE);
        }


        // 6. Reset filter for next open
        if (adapter != null) {
            adapter.getFilter().filter("");
        }
    }


    // Dynamic Height Setup according to the Listview Sizes
    public static void LoadList(View rv_documents, Context context, int count_file, boolean islistview) {
        ViewGroup.LayoutParams params = rv_documents.getLayoutParams();
        if (islistview) {
            if (rv_documents instanceof ListView) {
                if (count_file <= 4) {
                    setDynamicHeight(rv_documents);
                } else {
                    params.height = context.getResources().getDimensionPixelSize(R.dimen.one_fifty_dp);
                }
            } else if (rv_documents instanceof RecyclerView) {
                if (count_file <= 2) {
                    setDynamicHeight(rv_documents);
                } else {
                    params.height = context.getResources()
                            .getDimensionPixelSize(R.dimen.one_fifty_dp);
                    rv_documents.setLayoutParams(params); // ← add this line
                }
            }
        } else {
            if (count_file == 1) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                params.height = context.getResources().getDimensionPixelSize(R.dimen.one_fifty_dp);
            }
        }
        rv_documents.setLayoutParams(params);
    }


    // Dynamic Height Setup according to the RecyclerView Sizes
    public static void setDynamicHeight(View view) {
        if (view instanceof ListView) {
            ListView listView = (ListView) view;
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null || listAdapter.getCount() == 0) {
                ViewGroup.LayoutParams params = listView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                listView.setLayoutParams(params);
                return;
            }


            int totalHeight = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY);


            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);


                // Set layout params to match parent width if not already set
                if (listItem.getLayoutParams() == null) {
                    listItem.setLayoutParams(new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }


                // Measure with EXACTLY spec for width to handle multi-line text properly
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();
            }


            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();


        } else if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter == null || adapter.getItemCount() == 0) {
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                recyclerView.setLayoutParams(params);
                return;
            }


            // Wait until the RecyclerView has been laid out so getWidth() returns correctly
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            // Remove listener to avoid repeated calls
                            recyclerView.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);


                            RecyclerView.Adapter adapter = recyclerView.getAdapter();
                            if (adapter == null || adapter.getItemCount() == 0) return;


                            int totalHeight = 0;
                            int desiredWidth = View.MeasureSpec.makeMeasureSpec(
                                    recyclerView.getWidth(), View.MeasureSpec.EXACTLY);


                            for (int i = 0; i < adapter.getItemCount(); i++) {
                                RecyclerView.ViewHolder vh = adapter.createViewHolder(
                                        recyclerView, adapter.getItemViewType(i));
                                adapter.onBindViewHolder(vh, i);


                                View itemView = vh.itemView;
                                if (itemView.getLayoutParams() == null) {
                                    itemView.setLayoutParams(new RecyclerView.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT));
                                }
                                itemView.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                                totalHeight += itemView.getMeasuredHeight();
                            }


                            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                            params.height = totalHeight;
                            recyclerView.setLayoutParams(params);
                            recyclerView.requestLayout();
                        }
                    }
            );
        }
    }


    // To display the Text in View With firstletter as Capital
    public static String CapitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    // Dynamic handling of Mobile Number with + and *
    public static void NumberFilterwithStar(TextInputEditText editText, boolean isPhoneNumber) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {


                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);


                    if (isPhoneNumber) {
                        if (!Character.isDigit(c) && c != '+' && c != '*') {
                            return "";
                        }
                    } else {
                        if (!Character.isDigit(c) && c != '*') {
                            return "";
                        }
                    }
                }
                return null;
            }
        };


        if (isPhoneNumber) {
            editText.setFilters(new InputFilter[]{
                    filter,
                    new InputFilter.LengthFilter(15)
            });
        } else {
            editText.setFilters(new InputFilter[]{filter});
        }
    }


    // Dynamic Filter for the Mobile number
    public static void NumberFilter(TextInputEditText editText, boolean isPhoneNumber) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (isPhoneNumber) {
                        if (!Character.isDigit(source.charAt(i)) && source.charAt(i) != '+') {
                            return "";
                        }
                    } else {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                }
                return null;
            }
        };
        if (isPhoneNumber) {
            editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(10)});
        } else {
            editText.setFilters(new InputFilter[]{filter});
        }
    }


    public static void ZipFilter(TextInputEditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(6)});
    }


    // Password Eye Showing/Opening
    public static void password(TextInputEditText et_login_password, ImageView iv_toggle_password) {
        final boolean[] isVisible = {false}; // ✅ local — one per field
        iv_toggle_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVisible[0] = !isVisible[0]; // ✅ was: isPasswordVisible = !isPasswordVisible
                if (isVisible[0]) {
                    et_login_password.setInputType(
                            android.text.InputType.TYPE_CLASS_TEXT |
                                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    iv_toggle_password.setImageResource(R.drawable.eye_open);
                } else {
                    et_login_password.setInputType(
                            android.text.InputType.TYPE_CLASS_TEXT |
                                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    iv_toggle_password.setImageResource(R.drawable.eye_close);
                }
                et_login_password.setSelection(et_login_password.getText() != null
                        ? et_login_password.getText().length() : 0);
            }
        });
    }


    // Used in Month View of Events by user Selecting the particular date.
    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    // Dynamic check of the Correct email or not
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}");
    }


    // Dynamic Height Setup bottom Scrolling
    public static void adjustBottomMargin(Context context, ViewGroup.MarginLayoutParams params) {
        int bottomMargin;


        if (Build.VERSION.SDK_INT >= 35) {
            if (hasNavigationBar(context)) {
                bottomMargin = getNavigationBarHeight(context);
            } else {
                bottomMargin = 20;
            }
            params.setMargins(0, 0, 0, bottomMargin);
        } else {
            params.setMargins(0, 0, 0, 0);
        }
    }


    // Dynamic Height Setup according to the Listview Sizes (Android Default Nav Icons)
    private static boolean hasNavigationBar(Context context) {
        boolean hasNav = false;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId > 0) {
            hasNav = res.getBoolean(resourceId);
        }


        if (ViewConfiguration.get(context).hasPermanentMenuKey()) {
            hasNav = false;
        }


        return hasNav;
    }


    // Dynamic Height Setup according to the Listview Sizes (Android Default Nav Icons)
    private static int getNavigationBarHeight(Context context) {
        int result = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && hasNavigationBar(context)) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }


//    public static void setStatusBackground(View view, int bgColor, int textColor) {
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setShape(GradientDrawable.OVAL);
//        drawable.setColor(bgColor);
//        drawable.setCornerRadius(dpToPx(view.getContext(), 5));
//        view.setBackground(drawable);
//
//        if (textColor != 0 && view instanceof TextView) {
//            ((TextView) view).setTextColor(textColor);
//        }
//    }


//    public static void setStatusBackground(Context context, View view, @ColorRes int bgColorRes, @ColorRes int textColorRes) {
//        int bgColor = ContextCompat.getColor(context, bgColorRes);
//        int textColor = ContextCompat.getColor(context, textColorRes);
//        setStatusBackground(view, bgColor, textColor);
//    }


    // Default Pixel sizes
    public static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }


    public static int dpToPx1(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }


    // Load the Picture in Default Storage in Cache no need to Redownload it used in appointments.
    public static void loadProfileImage(
            Context context,
            String imageUrl,
            ImageView imageView,
            TextView fallbackTextView, String name
    ) {
        AppImageCache.load(context, imageUrl, imageView, name, fallbackTextView);
    }


    // Load the Picture in Default Storage in Cache no need to Redownload it with Role Based Condition
    public static void loadProfileImage(
            Context context,
            String imageUrl,
            ImageView imageView,
            TextView fallbackTextView
    ) {
        if (Constants.ROLE.equals("AAM")) {
            AppImageCache.load(context, imageUrl, imageView, Constants.FIRM_NAME, fallbackTextView);
        } else {
            AppImageCache.load(context, imageUrl, imageView, Constants.NAME, fallbackTextView);
        }
    }


    //Consultation Fees Stepper Flow and Logic
    public static void setupStepper(
            TextView editText,
            TextView tvMinus,
            TextView tvPlus,
            int defaultValue,
            int minValue,
            int maxValue,
            int step
    ) {
        editText.setHint("₹ " + defaultValue);


        // Helper to get raw int from field (strips ₹)
        Supplier<String> getRaw = () -> {
            String raw = editText.getText() != null
                    ? editText.getText().toString().replace("₹", "").trim()
                    : "";
            return raw;
        };


        Runnable syncButtons = () -> {
            String raw = getRaw.get();


            int val;
            if (TextUtils.isEmpty(raw)) {
                val = defaultValue;
            } else {
                try {
                    val = Integer.parseInt(raw);
                } catch (NumberFormatException e) {
                    val = defaultValue;
                }


                if (val < minValue) {
                    val = minValue;
                    editText.setText("₹ " + minValue);
                }
                if (val > maxValue) {
                    val = maxValue;
                    editText.setText("₹ " + maxValue);
                }
            }


            final int finalVal = val;
            tvMinus.setEnabled(finalVal > minValue);
            tvMinus.setAlpha(finalVal > minValue ? 1f : 0.5f);
            tvPlus.setEnabled(finalVal < maxValue);
            tvPlus.setAlpha(finalVal < maxValue ? 1f : 0.5f);
        };


        syncButtons.run();


        tvMinus.setOnClickListener(v -> {
            String raw = getRaw.get();
            int val;
            try {
                val = TextUtils.isEmpty(raw) ? defaultValue : Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                val = defaultValue;
            }
            editText.setText("₹ " + Math.max(minValue, val - step));
            syncButtons.run();
        });


        tvPlus.setOnClickListener(v -> {
            String raw = getRaw.get();
            int val;
            try {
                if (TextUtils.isEmpty(raw)) {
                    // Empty field → just show minValue (500), don't add step yet
                    editText.setText("₹ " + minValue);
                    syncButtons.run();
                    return;
                }
                val = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                val = minValue;
            }
            // Field already has a value → increment normally
            editText.setText("₹ " + Math.min(maxValue, val + step));
            syncButtons.run();
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
            }


            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            }


            @Override
            public void afterTextChanged(Editable s) {
                String current = s.toString();


                // Skip if empty or already prefixed — avoid infinite loop
                if (current.isEmpty() || current.startsWith("₹")) {
                    syncButtons.run();
                    return;
                }


                // User typed a plain number — prefix it
                String digits = current.replace("₹", "").trim();
                if (!digits.isEmpty()) {
                    editText.removeTextChangedListener(this); // prevent re-entry
                    editText.setText("₹ " + digits);
//                    editText.setSelection(editText.getText().length()); // cursor at end
                    editText.addTextChangedListener(this);
                }


                syncButtons.run();
            }
        });
    }
}

