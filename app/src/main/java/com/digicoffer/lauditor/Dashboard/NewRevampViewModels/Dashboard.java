package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import static android.content.ContentValues.TAG;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.VitacapeExtention;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadclient_list;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadcount_from_list;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadteam_list;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi;
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Chat.ViewModels.Chat;
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel;
import com.digicoffer.lauditor.Matter.ViewModels.Matter;
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.Notifications.Models.Navigation;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dashboard extends Fragment implements AsyncTaskCompleteListener {

    private static final String REQ_DASHBOARD = "DASHBOARD_ALL";
    private static final int AUTH_REQUEST_CODE = 1001;

    // State - Using ArrayList instead of JSONArray for mutability
    private final List<String> teamchatlist = new ArrayList<>();
    private final List<String> clientchatlist = new ArrayList<>();
    private final List<String> totalchatlist = new ArrayList<>();

    // Views
    private RecyclerView rvOuter;
    //    private SwipeRefreshLayout swipeRefresh;
    private Dialog progressDialog;

    // Adapter / ViewModel
    private DashboardOuterAdapter outerAdapter;
    private NewModel mViewModel;
    private MenuHighlightListener menuHighlightListener;

    // Main-thread handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Track API calls
    private boolean isDashboardApiComplete = false;
    private boolean isChatListApiComplete = false;
    private boolean isFirstLoad = true;

    //==========================================================================
    // Lifecycle
    //==========================================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_dashboard, c, false);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        try {
            mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
            mViewModel.setData(getString(R.string.lauditor));

            bindViews(v);
//            setupSwipeRefresh();
            setupRecyclerView();

            Constants.dashboard_en = this;
            Constants.recyclerView = rvOuter;

            loadDashboard();
        } catch (Exception e) {
            Log.e(TAG, "onViewCreated", e);
        }
    }

    @Override
    public void onDestroyView() {
        dismissProgress();
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    //==========================================================================
    // Progress Dialog Methods
    //==========================================================================

    private void showProgress() {
        try {
            if (progressDialog == null && isFirstLoad) {
                progressDialog = AndroidUtils.get_progress(getActivity());
            }
        } catch (Exception ignored) {
        }
    }

    private void dismissProgress() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
                progressDialog = null;
            }
        } catch (Exception ignored) {
        }
    }

    private void checkAndDismissProgress() {
        if (isDashboardApiComplete && isChatListApiComplete) {
            dismissProgress();
            isFirstLoad = false;
        }
    }

    //==========================================================================
    // View binding
    //==========================================================================

    private void bindViews(View v) {
        rvOuter = v.findViewById(R.id.rv_outer);
//        swipeRefresh = v.findViewById(R.id.swipe_refresh);

        // Start with recycler invisible for smooth animation
        rvOuter.setAlpha(0f);
        rvOuter.setVisibility(View.VISIBLE);
    }

    //==========================================================================
    // SwipeRefreshLayout with user-friendly feedback
    //==========================================================================

    @SuppressLint("ResourceAsColor")
//    private void setupSwipeRefresh() {
//        if (swipeRefresh == null) return;
//        swipeRefresh.setColorSchemeResources(R.color.blue_accent);
//        swipeRefresh.setProgressBackgroundColorSchemeColor(android.R.color.white);
//        swipeRefresh.setDistanceToTriggerSync(300);
//
//        swipeRefresh.setOnRefreshListener(() -> {
//            resetApiFlags();
//            loadDashboardWithSwipe();
//        });
//    }

    private void resetApiFlags() {
        isDashboardApiComplete = false;
        isChatListApiComplete = false;
    }

    //==========================================================================
    // RecyclerView with ULTRA SMOOTH optimizations
    //==========================================================================

    private void setupRecyclerView() {
        // Optimized LayoutManager
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setInitialPrefetchItemCount(10);
        llm.setItemPrefetchEnabled(true);

        rvOuter.setLayoutManager(llm);
        rvOuter.setHasFixedSize(true);
        rvOuter.setNestedScrollingEnabled(true);
        rvOuter.setItemViewCacheSize(50);
        rvOuter.setDrawingCacheEnabled(true);
        rvOuter.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Custom smooth animator
        CustomItemAnimator animator = new CustomItemAnimator();
        animator.setAddDuration(400);
        animator.setMoveDuration(350);
        animator.setChangeDuration(300);
        rvOuter.setItemAnimator(animator);

        // Optimize view pooling
        rvOuter.getRecycledViewPool().setMaxRecycledViews(0, 25);
        AndroidUtils.setupEdgePaddingBehavior(rvOuter);
        // Add smooth scroll listener
//        rvOuter.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    recyclerView.setItemAnimator(animator);
//                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    recyclerView.setItemAnimator(null);
//                }
//            }
//        });

        outerAdapter = new DashboardOuterAdapter(requireContext(), cardActionListener);
        rvOuter.setAdapter(outerAdapter);
        AndroidUtils.setupBottomSpacerFooter(rvOuter, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
    }

    // Custom ItemAnimator for ultra smooth animations
    private class CustomItemAnimator extends DefaultItemAnimator {
        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            View view = holder.itemView;
            view.setAlpha(0f);
            view.setTranslationY(50f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();
            return super.animateAdd(holder);
        }

        @Override
        public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
            if (newHolder != null) {
                View view = newHolder.itemView;
                view.setAlpha(0f);
                view.setScaleX(0.95f);
                view.setScaleY(0.95f);
                view.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator(0.8f))
                        .start();
            }
            return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
        }
    }

    //==========================================================================
    // API calls
    //==========================================================================

    private void loadDashboard() {
        if (isFirstLoad) {
            showProgress();
        }

        try {
            WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.dashboardAllEndpoint,
                    REQ_DASHBOARD,
                    new JSONObject().toString());
        } catch (Exception e) {
            Log.e(TAG, "loadDashboard", e);
            isDashboardApiComplete = true;
            checkAndDismissProgress();
        }
    }

    private void loadDashboardWithSwipe() {
        try {
            WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.dashboardAllEndpoint,
                    REQ_DASHBOARD,
                    new JSONObject().toString());
        } catch (Exception e) {
            Log.e(TAG, "loadDashboardWithSwipe", e);
//            if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
//                swipeRefresh.setRefreshing(false);
//            }
            isDashboardApiComplete = true;
            checkAndDismissProgress();
        }
    }

    private void callchatlist() {
        try {
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.chatlistEndpoint,
                    "CHAT_LIST",
                    new JSONObject().toString());
        } catch (Exception e) {
            Log.e(TAG, "callchatlist", e);
            isChatListApiComplete = true;
            checkAndDismissProgress();
        }
    }

    //==========================================================================
    // API response
    //==========================================================================

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        runOnMain(() -> {
            // Hide swipe refresh
//            if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
//                swipeRefresh.setRefreshing(false);
//            }

            if (httpResult.getResult() != WebServiceHelper.ServiceCallStatus.Success) {
                Log.e(TAG, "API error: " + httpResult.getResult());
                AndroidUtils.showAlert("Something went wrong, please try again.", getActivity());

                String reqType = httpResult.getRequestType();
                if (REQ_DASHBOARD.equalsIgnoreCase(reqType)) {
                    isDashboardApiComplete = true;
                } else if ("CHAT_LIST".equalsIgnoreCase(reqType)) {
                    isChatListApiComplete = true;
                }
                checkAndDismissProgress();
                return;
            }

            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                String reqType = httpResult.getRequestType();

                if ("CHAT_LIST".equalsIgnoreCase(reqType)) {
                    handleChatList(result);
                    isChatListApiComplete = true;
                    checkAndDismissProgress();
                } else if (REQ_DASHBOARD.equalsIgnoreCase(reqType)) {
                    handleDashboard(result);
                    isDashboardApiComplete = true;
                    checkAndDismissProgress();
                }

            } catch (JSONException e) {
                Log.e(TAG, "JSON parse error", e);
                AndroidUtils.showAlert("Something went wrong, please try again.", getActivity());

                String reqType = httpResult.getRequestType();
                if (REQ_DASHBOARD.equalsIgnoreCase(reqType)) {
                    isDashboardApiComplete = true;
                } else if ("CHAT_LIST".equalsIgnoreCase(reqType)) {
                    isChatListApiComplete = true;
                }
                checkAndDismissProgress();
            }
        });
    }

    //==========================================================================
    // CHAT_LIST Handler
    //==========================================================================

    private void handleChatList(JSONObject result) throws JSONException {
        JSONArray team_array = result.getJSONArray("team");
        JSONArray client_array = result.getJSONArray("clients");

        // Clear lists before adding new data
        unreadteam_list.clear();
        teamchatlist.clear();

        for (int i = 0; i < team_array.length(); i++) {
            UnreadCountModel m = new UnreadCountModel();
            Constants.listid1 = team_array.optString(i);
            m.setFromjid(Constants.listid1);
            unreadteam_list.add(m);
            teamchatlist.add(Constants.listid1);
        }

        unreadclient_list.clear();
        clientchatlist.clear();

        for (int i = 0; i < client_array.length(); i++) {
            UnreadCountModel m = new UnreadCountModel();
            Constants.listid = client_array.optString(i);
            m.setFromjid(Constants.listid);
            unreadclient_list.add(m);
            clientchatlist.add(Constants.listid);
        }

        // Clear and rebuild total chat list
        totalchatlist.clear();
        for (String id : teamchatlist) {
            totalchatlist.add(id + VitacapeExtention);
        }
        for (String id : clientchatlist) {
            totalchatlist.add(id + VitacapeExtention);
        }

        // Convert to JSONArray for Constants
        JSONArray totalArray = new JSONArray();
        for (String item : totalchatlist) {
            totalArray.put(item);
        }
        Constants.totalchatclientlist = totalArray;

        // ✅ REPLACED: new ChatCountApi(getContext()).callgetunreadcountlist();
        //    Single GET /conversation/meta call now fetches counts + last
        //    message directly via ConversationMetaApi instead of ChatCountApi.
        ConversationMetaApi.fetch(getContext(), totalchatlist, metaMap -> {
            Constants.unreadList.clear();
            unreadcount_from_list.clear();

            for (String jid : totalchatlist) {
                String guid = jid.replace(VitacapeExtention, "");

                ConversationMetaApi.ConversationMeta meta = metaMap.get(guid);
                if (meta == null) continue;

                UnreadCountModel unreadCountModel = new UnreadCountModel();
                unreadCountModel.setFromjid(guid);
                unreadCountModel.setCount(meta.unreadCount);
                Constants.unreadList.add(unreadCountModel);
                unreadcount_from_list.add(unreadCountModel);
            }
        });
    }

    //==========================================================================
    // DASHBOARD Handler with PREMIUM SMOOTH ANIMATIONS
    //==========================================================================

    private void handleDashboard(JSONObject result) throws JSONException {
        if (result.optBoolean("error", false)) {
            String msg = result.optString("msg", "Something went wrong");
            Log.e(TAG, "Server error: " + msg);
            AndroidUtils.showAlert(msg, getActivity());
            return;
        }

        Log.d("Dashboard_Result", result.toString());

        // Process in background for smooth UI
        new Thread(() -> {
            try {
                List<DashboardItem> all = DashboardParser.parse(result);
                List<DashboardSection> sections = buildSections(all);

                runOnMain(() -> {
                    outerAdapter.submitSections(sections);
                    animateDashboardPremium();
                    callchatlist();
                });
            } catch (Exception e) {
                Log.e(TAG, "DashboardParser.parse error", e);
                runOnMain(() -> AndroidUtils.showAlert("Something went wrong, please try again.", getActivity()));
            }
        }).start();
    }

    //==========================================================================
    // PREMIUM SMOOTH ANIMATIONS FOR PROFESSIONAL DASHBOARD
    //==========================================================================

    private void animateDashboardPremium() {
        if (rvOuter == null) return;

        // Fade in the entire recycler view
        rvOuter.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate each card with premium staggered effect
        rvOuter.postDelayed(() -> {
            LinearLayoutManager llm = (LinearLayoutManager) rvOuter.getLayoutManager();
            if (llm == null) return;

            int first = llm.findFirstVisibleItemPosition();
            int last = llm.findLastVisibleItemPosition();

            if (first == -1) return;

            for (int i = first; i <= last; i++) {
                View child = llm.findViewByPosition(i);
                if (child == null) continue;

                animatePremiumCard(child, i - first);
            }
        }, 50);
    }

    private void animatePremiumCard(View card, int position) {
        // Staggered delays for natural feel
        long delay;
        if (position == 0) {
            delay = 100;
        } else if (position == 1) {
            delay = 180;
        } else if (position == 2) {
            delay = 260;
        } else {
            delay = Math.min(260 + (position - 2) * 50L, 500);
        }

        // Premium animation: scale + fade + translate
        card.setAlpha(0f);
        card.setTranslationY(60f);
        card.setScaleX(0.92f);
        card.setScaleY(0.92f);

        card.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delay)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(0.6f))
                .start();

        // Add subtle elevation change during animation
        card.animate()
                .translationZ(8f)
                .setStartDelay(delay)
                .setDuration(300)
                .withEndAction(() -> {
                    card.animate()
                            .translationZ(0f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    //==========================================================================
    // Build sections
    //==========================================================================

    private List<DashboardSection> buildSections(List<DashboardItem> all) {
        String role = Constants.ROLE;

        DashboardSection today = new DashboardSection(DashboardSection.SECTION_TODAY, "Today's Activities");
        DashboardSection analytics = new DashboardSection(DashboardSection.SECTION_ANALYTICS, "Analytics Overview");
        DashboardSection metrics = new DashboardSection(DashboardSection.SECTION_METRICS, "Business Metrics");

        for (DashboardItem item : all) {
            if (!shouldShowCard(item.getType(), role)) continue;
            switch (item.getType()) {
                case DashboardItem.TYPE_MEETING:
                case DashboardItem.TYPE_APPOINTMENT:
                case DashboardItem.TYPE_MESSAGES:
                case DashboardItem.TYPE_NOTIFICATION:
                    today.items.add(item);
                    break;
                case DashboardItem.TYPE_APPOINTMENT_TREND:
                case DashboardItem.TYPE_REVENUE_TREND:
                case DashboardItem.TYPE_MATTER:
                case DashboardItem.TYPE_STORAGE:
                    analytics.items.add(item);
                    break;
                case DashboardItem.TYPE_BILLABLE:
                case DashboardItem.TYPE_APPROX_REVENUE:
                case DashboardItem.TYPE_SUBSCRIPTION:
                case DashboardItem.TYPE_HIRING:
                    metrics.items.add(item);
                    break;
                default:
                    break;
            }
        }

        List<DashboardSection> result = new ArrayList<>();
        if (!today.items.isEmpty()) result.add(today);
        if (!analytics.items.isEmpty()) result.add(analytics);
        if (!metrics.items.isEmpty()) result.add(metrics);
        return result;
    }

    private boolean shouldShowCard(int type, String role) {
        List<Integer> allowed = new ArrayList<>();
        switch (role) {
            case "SU":
                allowed.addAll(Arrays.asList(
                        DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                        DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                        DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                        DashboardItem.TYPE_MATTER, DashboardItem.TYPE_STORAGE,
                        DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE,
                        DashboardItem.TYPE_SUBSCRIPTION));
                if (!"solo".equals(Constants.CATEGORY))
                    allowed.add(DashboardItem.TYPE_HIRING);
                break;
            case "AAM":
                allowed.addAll(Arrays.asList(
                        DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                        DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                        DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                        DashboardItem.TYPE_STORAGE, DashboardItem.TYPE_APPROX_REVENUE,
                        DashboardItem.TYPE_SUBSCRIPTION));
                break;
            case "GH":
                allowed.addAll(Arrays.asList(
                        DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                        DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                        DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                        DashboardItem.TYPE_MATTER, DashboardItem.TYPE_STORAGE,
                        DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE));
                break;
            case "TM":
                allowed.addAll(Arrays.asList(
                        DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                        DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                        DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                        DashboardItem.TYPE_MATTER, DashboardItem.TYPE_BILLABLE));
                break;
        }
        return allowed.contains(type);
    }

    //==========================================================================
    // Public methods
    //==========================================================================

    public void refreshChatCounts() {
        runOnMain(() -> {
            if (outerAdapter != null) outerAdapter.notifyDataSetChanged();
        });
    }

    //==========================================================================
    // Navigation
    //==========================================================================

    public interface MenuHighlightListener {
        void highlightMenuForCardType(int cardType);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuHighlightListener)
            menuHighlightListener = (MenuHighlightListener) context;
    }

    private final DashboardCardAdapter.CardActionListener cardActionListener =
            new DashboardCardAdapter.CardActionListener() {
                @Override
                public void onNavigate(int cardType, Navigation navigation) {
                    handleNavigation(cardType, navigation, null);
                }

                @Override
                public void onNavigate(int cardType, Navigation navigation, String subType) {
                    handleNavigation(cardType, navigation, subType);
                }

                @Override
                public void onPaySubscription(String planLabel, String validityText) {
                    launchPaySubscriptionPage(planLabel, validityText);
                }
            };

    private void handleNavigation(int cardType, Navigation navigation,
                                  @Nullable String subType) {
        try {
            if (menuHighlightListener != null)
                menuHighlightListener.highlightMenuForCardType(cardType);

            if (navigation != null
                    && navigation.getRoute_name() != null
                    && !navigation.getRoute_name().isEmpty()) {
                Constants.isFromNotification = true;
                AndroidUtils.setupNotificationHandler(requireContext(), navigation);
                return;
            }

            switch (cardType) {
                case DashboardItem.TYPE_MATTER:
                    Constants.is_CreateMatter = false;
                    Constants.isCreate = false;
                    Constants.matterFilterType = (subType != null) ? subType : "";
                    Page_Navigation(new Matter());
                    break;
                case DashboardItem.TYPE_BILLABLE:
                case DashboardItem.TYPE_APPROX_REVENUE:
                    Page_Navigation(new TimeSheets());
                    break;
                case DashboardItem.TYPE_HIRING:
                    Page_Navigation(new com.digicoffer.lauditor.Groups.Groups());
                    break;
                case DashboardItem.TYPE_NOTIFICATION:
                    Page_Navigation(new com.digicoffer.lauditor.Notifications.Notifications());
                    break;
                case DashboardItem.TYPE_MESSAGES:
                    Page_Navigation(new Chat());
                    break;
                case DashboardItem.TYPE_MEETING:
                    Page_Navigation(new Meetings());
                    break;
                case DashboardItem.TYPE_APPOINTMENT:
                    Page_Navigation(new com.digicoffer.lauditor.Appointments.ViewModels.Appointments());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "handleNavigation error", e);
        }
    }

    //==========================================================================
    // Helpers
    //==========================================================================

    private void runOnMain(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            mainHandler.post(r);
        }
    }

    public void Page_Navigation(Fragment fragment) {
        Constants.mainActivity.navigation_items(fragment);
    }

    public void launchPaySubscriptionPage(String email, String userCount) {
        startActivityForResult(
                new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://staging.payment.digicoffer.com/renew?useremail="
                                + email + "&users=" + userCount)),
                AUTH_REQUEST_CODE);
    }

    @Override
    public void onClick(View view) { /* no-op */ }
}