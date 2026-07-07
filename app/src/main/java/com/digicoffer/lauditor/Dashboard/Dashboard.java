package com.digicoffer.lauditor.Dashboard;

import static android.content.ContentValues.TAG;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.KPICARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.MYDAYCARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.VitacapeExtention;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.clientTeamApiEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.groupsEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.hiringEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.hoursEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.matterEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.meetingApiEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.newclientEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.notificationEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.relationshipsEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.storageEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.subscriptionEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.timesheetEndpoint;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadclient_list;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadteam_list;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi;
import com.digicoffer.lauditor.Dashboard.DahboardModels.Item;
import com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels.ClientChatModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels.MeetingModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels.NotificationModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels.TeamChatModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.ActiveModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.ApproxRevenueModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.AverageBillingRateModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.BillableModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.GroupsModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.HiringModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.NewClientsModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.NonBillableModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.PendingTimeSheetsModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.RelationshipModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.StorageModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.SubScriptionModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.SubmittedTimesheetModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels.TeamModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel;
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dashboard extends Fragment implements AsyncTaskCompleteListener {

    private NewModel mViewModel;
    private static final int AUTH_REQUEST_CODE = 1001;

    private TextClock tv_date_time, tv_time, tv_am_pm;
    private View center_line;
    private Calendar calendar;
    private JSONArray teamchatlist = new JSONArray();
    private JSONArray clientchatlist = new JSONArray();
    private JSONArray totalchatlist = new JSONArray();
    private SimpleDateFormat dateFormat;
    private String date;
    private ExecutorService executorService;
    private TextView bt_MyDay, bt_KPI, personalize_btn;
    private MainActivity mainActivity;
    private static String KPI_DATA = "";

    // ── Both RecyclerViews ──────────────────────────────────────────────────
    private RecyclerView rv_myday;  // left column  (My Day)
    private RecyclerView rv_kpi;    //

    private LinearLayout ll_column_left;
    private LinearLayout ll_column_right;

    private boolean isTablet = false;

    private ArrayList<Item> itemArrayList = new ArrayList<>();
    private ArrayList<Item> kpiItemArrayList = new ArrayList<>();   // KPI items for right column
    private ArrayList<UnreadCountModel> unreadcountlist = new ArrayList<>();
    private Dialog progressDialog;
    private int pendingApiCalls = 0;

    MyDayAdapter myDayAdapter;
    MyDayAdapter kpiAdapter;   // adapter for right column on tablets

    private String currentMode = "MYDAY";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newFixedThreadPool(2);
    }

    @SuppressLint({"SimpleDateFormat", "MissingInflatedId"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dashboard_screen, container, false);
        KPI_DATA = "MyDay_AAM";
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        Constants.Chat_id = "";
        try {
            mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
            mViewModel.setData(getString(R.string.lauditor));
            calendar = Calendar.getInstance();
            Constants.dashboard = this;
            dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            date = dateFormat.format(calendar.getTime());

            // ── Detect tablet ──────────────────────────────────────────────
            isTablet = DynamicUtils.isTablet(requireContext());

            center_line = v.findViewById(R.id.center_line);
            bt_KPI      = v.findViewById(R.id.bt_kpi);
            bt_MyDay    = v.findViewById(R.id.bt_Myday);
            personalize_btn = v.findViewById(R.id.personalize_btn);
            personalize_btn.setText(R.string.personalize);
            bt_MyDay.setText(R.string.my_day);
            tv_am_pm    = v.findViewById(R.id.am_pm);
            tv_time   = v.findViewById(R.id.time);
            tv_date_time = v.findViewById(R.id.tv_date);

            // ── RecyclerViews & column containers ─────────────────────────
            rv_myday       = v.findViewById(R.id.rv_myday);
            rv_kpi         = v.findViewById(R.id.rv_kpi);
            ll_column_left = v.findViewById(R.id.ll_column_left);
            ll_column_right = v.findViewById(R.id.ll_column_right);

            Constants.recyclerView = rv_myday;

            // ── Tablet: show right column, fix left column width ───────────
            if (isTablet) {
                ll_column_right.setVisibility(View.VISIBLE);


                LinearLayout.LayoutParams leftParams = (LinearLayout.LayoutParams) ll_column_left.getLayoutParams();
                leftParams.width = 0;
                leftParams.weight = 1f;
                ll_column_left.setLayoutParams(leftParams);

                LinearLayout.LayoutParams rightParams =
                        (LinearLayout.LayoutParams) ll_column_right.getLayoutParams();
                rightParams.width = 0;
                rightParams.weight = 1f;
                ll_column_right.setLayoutParams(rightParams);
            }


            rv_myday.setLayoutManager(new LinearLayoutManager(getContext()));
            myDayAdapter = new MyDayAdapter(itemArrayList, KPI_DATA, getContext(), this);
            rv_myday.setAdapter(myDayAdapter);

            if (isTablet) {
                rv_kpi.setLayoutManager(new LinearLayoutManager(getContext()));
                kpiAdapter = new MyDayAdapter(kpiItemArrayList,
                        getKpiDataForRole(Constants.ROLE), getContext(), this);
                rv_kpi.setAdapter(kpiAdapter);
            }

            // ── Initial load ──────────────────────────────────────────────
            showProgress();
            itemArrayList.clear();
            rv_myday.removeAllViews();

            if (isTablet) {
                // On tablet, ALWAYS show MyDay on left + KPI on right simultaneously
                loadTabletDashboard();
            } else {
                // Phone: honour the saved preference
                if (Constants.IS_MyDay) {
                    MyDay_or_Kpi(true);
                } else {
                    KPI_DATA = getKpiDataForRole(Constants.ROLE);
                    MyDay_or_Kpi(false);
                }
            }

            // ── Tab click listeners ────────────────────────────────────────
            bt_MyDay.setOnClickListener(view -> {
                if (isTablet) {
                    // On tablet tabs just reload both columns
                    loadTabletDashboard();
                } else {
                    rv_myday.removeAllViews();
                    itemArrayList.clear();
                    Constants.IS_MyDay = true;
                    MyDay_or_Kpi(true);
                }
            });

            bt_KPI.setOnClickListener(view -> {
                if (isTablet) {
                    loadTabletDashboard();
                } else {
                    Constants.IS_MyDay = false;
                    rv_myday.removeAllViews();
                    itemArrayList.clear();
                    KPI_DATA = getKpiDataForRole(Constants.ROLE);
                    MyDay_or_Kpi(false);
                    loadRecyclerview();
                }
            });

        } catch (Resources.NotFoundException e) {
            e.fillInStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TABLET: Load both columns simultaneously
    // ─────────────────────────────────────────────────────────────────────────
    private void loadTabletDashboard() {
        // Style both tab buttons as "selected"
        bt_MyDay.setBackground(requireContext().getDrawable(R.drawable.button_left_grey_bg));
        bt_KPI.setBackground(requireContext().getDrawable(R.drawable.button_right_grey_bg));
        bt_MyDay.setTextColor(requireContext().getColor(R.color.blue));
        bt_KPI.setTextColor(requireContext().getColor(R.color.blue));

        itemArrayList.clear();
        kpiItemArrayList.clear();
        myDayAdapter.notifyDataSetChanged();
        kpiAdapter.notifyDataSetChanged();

        showProgress();
        pendingApiCalls = 0;

        // Fire MyDay APIs
        currentMode = "MYDAY";
        for (Dashboard_Model card : new ArrayList<>(MYDAYCARDS)) {
            if (triggerApiForCard(card)) pendingApiCalls++;
        }

        // Fire KPI APIs
        currentMode = "KPI";
        for (Dashboard_Model card : new ArrayList<>(KPICARDS)) {
            if (triggerApiForCard(card)) pendingApiCalls++;
        }

        // Reset mode to MYDAY for data routing (updateItem uses this)
        // We handle routing in onAsyncTaskComplete based on the request type
        currentMode = "BOTH"; // special sentinel for tablet

        if (pendingApiCalls == 0) dismissProgress();
    }

    private String getKpiDataForRole(String role) {
        switch (role) {
            case "AAM": return "Kpi_AAM";
            case "SU":  return "SU_KPI";
            case "GH":  return "GH_KPI";
            case "TM":  return "TM_KPI";
            default:    return "";
        }
    }

    public void MyDay_or_Kpi(boolean isMyDay) {
        currentMode = isMyDay ? "MYDAY" : "KPI";

        rv_myday.removeAllViews();
        itemArrayList.clear();

        if (isMyDay) {
            bt_MyDay.setBackground(requireContext().getDrawable(R.drawable.button_left_grey_bg));
            bt_KPI.setBackground(requireContext().getDrawable(R.drawable.button_right_white_bg));
            bt_MyDay.setTextColor(requireContext().getColor(R.color.blue));
            bt_KPI.setTextColor(requireContext().getColor(R.color.black));
        } else {
            bt_KPI.setBackground(requireContext().getDrawable(R.drawable.button_right_grey_bg));
            bt_MyDay.setBackground(requireContext().getDrawable(R.drawable.button_left_white_bg));
            bt_KPI.setTextColor(requireContext().getColor(R.color.blue));
            bt_MyDay.setTextColor(requireContext().getColor(R.color.black));
        }

        rv_myday.getAdapter().notifyDataSetChanged();
        ArrayList<Dashboard_Model> cards = new ArrayList<>(isMyDay ? MYDAYCARDS : KPICARDS);

        if (cards.isEmpty()) return;

        showProgress();
        pendingApiCalls = 0;

        for (Dashboard_Model card : cards) {
            if (triggerApiForCard(card)) pendingApiCalls++;
        }

        if (pendingApiCalls == 0) dismissProgress();
    }

    private boolean triggerApiForCard(Dashboard_Model card) {
        int seq = card.getSequence();
        String type = card.getName().toLowerCase(Locale.ROOT);

        try {
            // Determine which mode this card belongs to for API firing
            boolean isMydayCard = MYDAYCARDS.contains(card);
            boolean isKpiCard   = KPICARDS.contains(card);

            String modeToUse = isMydayCard ? "MYDAY" : "KPI";

            if ("MYDAY".equalsIgnoreCase(modeToUse) || "MYDAY".equalsIgnoreCase(currentMode)) {
                switch (type) {
                    case "meeting":
                        callGet(meetingApiEndpoint, "MEETING");
                        return true;
                    case "clientchat":
                        callGet(clientTeamApiEndpoint, "CLIENT_CHAT");
                        return true;
                    case "teamchat":
                        callGet(clientTeamApiEndpoint, "TEAM_CHAT");
                        return true;
                    case "notifications":
                        callGet(notificationEndpoint, "NOTIFICATION");
                        return true;
                    case "storage":
                        if ("MYDAY".equalsIgnoreCase(modeToUse)) {
                            callGet(storageEndpoint, "STORAGE");
                            return true;
                        }
                        break;
                    case "subscription":
                        if ("MYDAY".equalsIgnoreCase(modeToUse)) {
                            callGet(subscriptionEndpoint, "SUBSCRIPTION");
                            return true;
                        }
                        break;
                }
            }
            if ("KPI".equalsIgnoreCase(modeToUse) || "KPI".equalsIgnoreCase(currentMode)) {
                switch (type) {
                    case "billable":
                        callGet(hoursEndpoint, "HOURS");
                        return true;
                    case "relationships":
                        callGet(relationshipsEndpoint, "RELATIONSHIP");
                        return true;
                    case "matters":
                        callGet(matterEndpoint, "MATTER");
                        return true;
                    case "newclients":
                        callGet(newclientEndpoint, "NEW_CLIENT");
                        return true;
                    case "newhires":
                        callGet(hiringEndpoint, "HIRING");
                        return true;
                    case "storage":
                        callGet(storageEndpoint, "STORAGE_KPI");
                        return true;
                    case "submittedts":
                        callGet(timesheetEndpoint, "SUBMITTED_TS");
                        return true;
                    case "pendingts":
                        callGet(timesheetEndpoint, "PENDING_TS");
                        return true;
                    case "subscription":
                        callGet(subscriptionEndpoint, "SUBSCRIPTION_KPI");
                        return true;
                    case "groups":
                        callGet(groupsEndpoint, "GROUPS");
                        return true;
                    case "teammembers":
                        callGet(groupsEndpoint, "TEAM_MEMBERS");
                        return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to trigger API for type=" + type + ", seq=" + seq, e);
        }
        return false;
    }

    private void callGet(String endpoint, String requestType) {
        try {
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    endpoint,
                    requestType,
                    postData.toString()
            );
        } catch (Exception e) {
            Log.e(TAG, "callGet error for " + requestType, e);
            safeDecrementAndDismissIfDone();
        }
    }

    private void showProgress() {
        try {
            if (progressDialog == null) {
                progressDialog = AndroidUtils.get_progress(getActivity());
            }
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        } catch (Exception ignored) {}
    }

    private void dismissProgress() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
        } catch (Exception ignored) {}
    }

    private void profile() {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET, "v3/profile/pic", "Profile",
                    jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void safeDecrementAndDismissIfDone() {
        pendingApiCalls--;
        if (pendingApiCalls <= 0) dismissProgress();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateItem — routes to left (MyDay) or right (KPI) list on tablet
    // ─────────────────────────────────────────────────────────────────────────
    private void updateItem(int type, Object newModel) {
        updateItemInList(type, newModel, itemArrayList, myDayAdapter);
    }

    private void updateKpiItem(int type, Object newModel) {
        if (isTablet) {
            updateItemInList(type, newModel, kpiItemArrayList, kpiAdapter);
        } else {
            updateItemInList(type, newModel, itemArrayList, myDayAdapter);
        }
    }

    private void updateItemInList(int type, Object newModel,
                                  ArrayList<Item> list, MyDayAdapter adapter) {
        boolean updated = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType() == type) {
                list.set(i, new Item(type, newModel));
                updated = true;
                adapter.notifyItemChanged(i);
                break;
            }
        }
        if (!updated) {
            list.add(new Item(type, newModel));
            adapter.notifyItemInserted(list.size() - 1);
        }
        adapter.updateData(list, KPI_DATA);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void callchatlist() {
        try {
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET, Constants.chatlistEndpoint,
                    "CHAT_LIST", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadRecyclerview() {
        if ("MYDAY".equalsIgnoreCase(currentMode)) {
            Collections.sort(itemArrayList, (o1, o2) ->
                    Integer.compare(o1.getType(), o2.getType()));
        }
        rv_myday.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_myday.setAdapter(new MyDayAdapter(itemArrayList, KPI_DATA, getContext(), this));
        rv_myday.scrollToPosition(0);
        AndroidUtils.LoadAnimation(rv_myday, getContext());
    }

    private void loadKpiRecyclerview() {
        if (!isTablet) {
            loadRecyclerview();
            return;
        }
        Collections.sort(kpiItemArrayList, (o1, o2) ->
                Integer.compare(o1.getType(), o2.getType()));
        rv_kpi.setLayoutManager(new LinearLayoutManager(getContext()));
        kpiAdapter = new MyDayAdapter(kpiItemArrayList,
                getKpiDataForRole(Constants.ROLE), getContext(), this);
        rv_kpi.setAdapter(kpiAdapter);
        rv_kpi.scrollToPosition(0);
        AndroidUtils.LoadAnimation(rv_kpi, getContext());
    }

    @Override
    public void onClick(View view) {}

    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                String reqType = httpResult.getRequestType();

                // ── MyDay responses ────────────────────────────────────────
                if (reqType.equalsIgnoreCase("MEETING")) {
                    if (result.getBoolean("error")) {
                        MeetingModel meetingModel = new MeetingModel("", "00:00", "00:00",
                                result.optString("message"), result.optString("meetings_count"));
                        itemArrayList.add(new Item(0, meetingModel));
                    } else {
                        if (!result.has("data")) {
                            MeetingModel meetingModel = new MeetingModel("", "00:00", "00:00",
                                    result.optString("message"), result.optString("meetings_count"));
                            itemArrayList.add(new Item(0, meetingModel));
                        } else {
                            JSONObject data = result.optJSONObject("data");
                            loadMeetingData(data);
                        }
                    }

                } else if (reqType.equalsIgnoreCase("CLIENT_CHAT")) {
                    if (result.getBoolean("error")) {
                        ClientChatModel clientChatModel = new ClientChatModel("00:00", "",
                                result.optString("message"));
                        itemArrayList.add(new Item(1, clientChatModel));
                    } else {
                        JSONObject data = result.optJSONObject("data");
                        loadClientdata(data);
                    }

                } else if (reqType.equalsIgnoreCase("TEAM_CHAT")) {
                    if (result.getBoolean("error")) {
                        TeamChatModel teamChatModel = new TeamChatModel("00:00", "",
                                result.optString("message"));
                        itemArrayList.add(new Item(2, teamChatModel));
                    } else {
                        JSONObject data = result.optJSONObject("data");
                        loadTeamdata(data);
                    }

                } else if (reqType.equalsIgnoreCase("Profile")) {
                    boolean isError = result.optBoolean("error");
                    if (!isError) {
                        JSONObject dataObj = result.optJSONObject("data");
                        if (dataObj != null) {
                            Constants.dashboard_image = dataObj.optString("imageUrl");
                        }
                    }

                } else if (reqType.equalsIgnoreCase("CHAT_LIST")) {
                    JSONArray team_array = result.getJSONArray("team");
                    unreadteam_list.clear();
                    for (int i = 0; i < team_array.length(); i++) {
                        UnreadCountModel unreadCountModel = new UnreadCountModel();
                        Constants.listid1 = team_array.optString(i);
                        unreadCountModel.setFromjid(Constants.listid1);
                        unreadteam_list.add(unreadCountModel);
                        teamchatlist.put(Constants.listid1);
                    }
                    JSONArray client_array = result.getJSONArray("clients");
                    unreadclient_list.clear();
                    for (int i = 0; i < client_array.length(); i++) {
                        UnreadCountModel unreadCountModel = new UnreadCountModel();
                        Constants.listid = client_array.optString(i);
                        unreadCountModel.setFromjid(Constants.listid);
                        unreadclient_list.add(unreadCountModel);
                        clientchatlist.put(Constants.listid);
                    }
                    for (int i = 0; i < teamchatlist.length(); i++) {
                        totalchatlist.put(teamchatlist.optString(i) + VitacapeExtention);
                    }
                    for (int i = 0; i < clientchatlist.length(); i++) {
                        totalchatlist.put(clientchatlist.optString(i) + VitacapeExtention);
                    }
                    Constants.totalchatclientlist = totalchatlist;

//                    ConversationMetaApi.fetch(getContext(), metaMap -> {
//                        try {
//                            for (int i = 0; i < Constants.totalchatclientlist.length(); i++) {
//                                JSONObject client = Constants.totalchatclientlist.getJSONObject(i);
//                                String guid = client.optString("guid", "");
//
////                                ConversationMetaApi.ConversationMeta meta = metaMap.get(guid);
////                                if (meta == null) continue;
////
////                                client.put("lastMessage", meta.lastMessage);
////                                client.put("lastMessageTime", AndroidUtils.getChatTimeText(new java.util.Date(meta.lastMessageTimestamp)));
////                                client.put("lastMessageTimestamp", meta.lastMessageTimestamp);
////                                client.put("unread_count", meta.unreadCount);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        KPI_DATA = "MyDay_AAM";
//                        loadRecyclerview();
//                    });


                } else if (reqType.equalsIgnoreCase("NOTIFICATION")) {
                    if (result.getBoolean("error")) {
                        NotificationModel notificationModel = new NotificationModel("00:00",
                                result.optString("message"), "");
                        itemArrayList.add(new Item(3, notificationModel));
                        callchatlist();
                    } else {
                        JSONObject data = result.optJSONObject("data");
                        loadNotificationdata(data);
                        callchatlist();
                    }

                } else if (reqType.equalsIgnoreCase("STORAGE")) {
                    // MyDay storage
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadStoragedata(data, false);
                    } else {
                        StorageModel storageModel = new StorageModel("", "", "");
                        int seq = findSequenceFor("storage", false);
                        if (seq != -1) updateItem(seq, storageModel);
                        loadRecyclerview();
                    }

                    // ── KPI responses ──────────────────────────────────────────
                } else if (reqType.equalsIgnoreCase("HOURS")) {
                    JSONObject data = result.optJSONObject("data");
                    loadHoursdata(data);

                } else if (reqType.equalsIgnoreCase("HIRING")) {
                    if (!result.optBoolean("error")) {
                        JSONArray data = result.getJSONArray("data");
                        loadHiringData(data);
                    } else {
                        HiringModel hiringModel = new HiringModel("", 0, "", 0);
                        int seq = findSequenceFor("newHires", true);
                        if (seq != -1) updateKpiItem(seq, hiringModel);
                    }

                } else if (reqType.equalsIgnoreCase("STORAGE_KPI")) {
                    // KPI storage (tablet right column)
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadStoragedata(data, true);
                    } else {
                        StorageModel storageModel = new StorageModel("", "", "");
                        int seq = findSequenceFor("storage", true);
                        if (seq != -1) updateKpiItem(seq, storageModel);
                        loadKpiRecyclerview();
                    }

                } else if (reqType.equalsIgnoreCase("SUBMITTED_TS")) {
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadSubmittedTsdata(data);
                    } else {
                        SubmittedTimesheetModel submittedModel = new SubmittedTimesheetModel("", "");
                        int seq = findSequenceFor("submittedTs", true);
                        if (seq != -1) updateKpiItem(seq, submittedModel);
                    }

                } else if (reqType.equalsIgnoreCase("PENDING_TS")) {
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadPendingTsdata(data);
                    } else {
                        PendingTimeSheetsModel pendingModel = new PendingTimeSheetsModel("", "");
                        int seq = findSequenceFor("pendingTs", true);
                        if (seq != -1) updateKpiItem(seq, pendingModel);
                    }

                } else if (reqType.equalsIgnoreCase("MATTER")) {
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadMatterdata(data);
                    } else {
                        ActiveModel activeModel = new ActiveModel("", "", 0, "", 0,
                                "", "", 0, "", 0);
                        int seq = findSequenceFor("matters", true);
                        if (seq != -1) updateKpiItem(seq, activeModel);
                        loadKpiRecyclerview();
                    }

                } else if (reqType.equalsIgnoreCase("SUBSCRIPTION")) {
                    // MyDay subscription
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadSubscriptiondata(data, false);
                    }

                } else if (reqType.equalsIgnoreCase("SUBSCRIPTION_KPI")) {
                    // KPI subscription (tablet right column)
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadSubscriptiondata(data, true);
                    }

                } else if (reqType.equalsIgnoreCase("GROUPS")) {
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadGroupsdata(data);
                    } else {
                        GroupsModel groupsModel = new GroupsModel("0");
                        int seq = findSequenceFor("groups", true);
                        if (seq != -1) updateKpiItem(seq, groupsModel);
                    }

                } else if (reqType.equalsIgnoreCase("TEAM_MEMBERS")) {
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadTeamsdata(data);
                    } else {
                        TeamModel teamModel = new TeamModel("0");
                        int seq = findSequenceFor("teamMembers", true);
                        if (seq != -1) updateKpiItem(seq, teamModel);
                    }

                } else if (reqType.equalsIgnoreCase("NEW_CLIENT")) {
                    if (!result.optBoolean("error")) {
                        JSONArray data = result.getJSONArray("data");
                        loadNewClientsdata(data);
                    } else {
                        NewClientsModel newClientsModel = new NewClientsModel("", 0, "", 0);
                        int seq = findSequenceFor("newClients", true);
                        if (seq != -1) updateKpiItem(seq, newClientsModel);
                    }

                } else if (reqType.equalsIgnoreCase("RELATIONSHIP")) {
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        loadRelationshipdata(data);
                    } else {
                        RelationshipModel relationshipModel = new RelationshipModel("0", "0");
                        int seq = findSequenceFor("relationships", true);
                        if (seq != -1) updateKpiItem(seq, relationshipModel);
                        loadKpiRecyclerview();
                    }
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        safeDecrementAndDismissIfDone();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findSequenceFor — searches MyDay or KPI cards
    // ─────────────────────────────────────────────────────────────────────────
    private int findSequenceFor(String cardName, boolean searchKpi) {
        ArrayList<Dashboard_Model> cardsToSearch = searchKpi ? KPICARDS : MYDAYCARDS;
        for (Dashboard_Model model : cardsToSearch) {
            if (model.getName().equalsIgnoreCase(cardName)) {
                return model.getSequence();
            }
        }
        return -1;
    }

    private int findSequenceFor(String cardName) {
        // Legacy overload — search based on currentMode
        boolean isKpi = "KPI".equalsIgnoreCase(currentMode);
        return findSequenceFor(cardName, isKpi);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data loaders (unchanged logic, routing updated for KPI vs MyDay)
    // ─────────────────────────────────────────────────────────────────────────

    private void loadRelationshipdata(JSONObject data) throws JSONException {
        String accepted = "0", pending = "0";
        if (data != null) {
            accepted = data.optString("accepted", accepted);
            pending  = data.optString("pending", pending);
        }
        RelationshipModel model = new RelationshipModel(accepted, pending);
        int seq = findSequenceFor("relationships", true);
        if (seq != -1) updateKpiItem(seq, model);
        loadKpiRecyclerview();
    }

    private void loadNotificationdata(JSONObject data) throws JSONException {
        String timeStamp = "", message = "", date = "";
        if (data != null) {
            timeStamp = data.optString("timestamp", "");
            message   = data.optString("message", "");
            date      = data.optString("date", "");
        }
        NotificationModel notificationModel = new NotificationModel(timeStamp, message, date);
        int sequence = findSequenceFor("notifications", false);
        itemArrayList.add(new Item(sequence, notificationModel));
        KPI_DATA = "MyDay_AAM";
        loadRecyclerview();
    }

    private void loadNewClientsdata(JSONArray data) throws JSONException {
        String corporateType = ""; int corporateCount = 0;
        String criminalType  = ""; int criminalCount  = 0;
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj  = data.optJSONObject(i);
            String type = obj.optString("type");
            int count   = obj.optInt("count");
            if (i == 0) { corporateType = type; corporateCount = count; }
            else if (i == 1) { criminalType = type; criminalCount = count; }
        }
        NewClientsModel model = new NewClientsModel(corporateType, corporateCount,
                criminalType, criminalCount);
        int seq = findSequenceFor("newClients", true);
        if (seq != -1) updateKpiItem(seq, model);
        loadKpiRecyclerview();
    }

    private void loadGroupsdata(JSONObject data) throws JSONException {
        String totalGroups = data != null ? data.optString("totalGroups", "0") : "0";
        GroupsModel model = new GroupsModel(totalGroups);
        int seq = findSequenceFor("groups", true);
        if (seq != -1) updateKpiItem(seq, model);
    }

    private void loadTeamsdata(JSONObject data) {
        String totalTeams = data != null ? data.optString("totalTms", "0") : "0";
        TeamModel model = new TeamModel(totalTeams);
        int seq = findSequenceFor("teamMembers", true);
        if (seq != -1) updateKpiItem(seq, model);
    }

    private void loadSubscriptiondata(JSONObject data, boolean isKpi) throws JSONException {
        SubScriptionModel subscriptionModel = new SubScriptionModel();
        if (data != null) {
            subscriptionModel.setIs_paid_sub(data.optBoolean("is_paid_sub", false));
            subscriptionModel.setMessage(data.optString("message", ""));
            subscriptionModel.setMonth(data.optString("month", ""));
            subscriptionModel.setEmail(data.optString("email", ""));
            subscriptionModel.setUser_allowed(data.optString("user_allowed", ""));
            subscriptionModel.setIs_active_sub(data.optBoolean("is_active_sub", false));
            subscriptionModel.setActive_pay_button(data.optBoolean("active_pay_btn", false));
        }
        if (subscriptionModel.isIs_paid_sub()) {
            int seq = findSequenceFor("subscription", isKpi);
            if (seq != -1) {
                if (isKpi) {
                    updateKpiItem(seq, subscriptionModel);
                    loadKpiRecyclerview();
                } else {
                    itemArrayList.add(new Item(seq, subscriptionModel));
                    loadRecyclerview();
                }
            }
        }
    }

    private void loadMatterdata(JSONObject data) throws JSONException {
        String activeTotal = "0", closedTotal = "0";
        String active_legal_type = ""; int active_legal_count = 0;
        String active_general_type = ""; int active_general_count = 0;
        String closed_legal_type = ""; int closed_legal_count = 0;
        String closed_general_type = ""; int closed_general_count = 0;

        JSONObject active = data != null ? data.optJSONObject("active") : null;
        JSONObject closed = data != null ? data.optJSONObject("closed") : null;

        if (active != null) {
            activeTotal = active.optString("total", activeTotal);
            JSONArray activeCounts = active.optJSONArray("countsByType");
            if (activeCounts != null) {
                for (int i = 0; i < activeCounts.length(); i++) {
                    JSONObject obj = activeCounts.optJSONObject(i);
                    if (obj == null) continue;
                    if (i == 0) { active_legal_type = obj.optString("type"); active_legal_count = obj.optInt("count"); }
                    else if (i == 1) { active_general_type = obj.optString("type"); active_general_count = obj.optInt("count"); }
                }
            }
        }
        if (closed != null) {
            closedTotal = closed.optString("total", closedTotal);
            JSONArray closeCounts = closed.optJSONArray("countsByType");
            if (closeCounts != null) {
                for (int i = 0; i < closeCounts.length(); i++) {
                    JSONObject obj = closeCounts.optJSONObject(i);
                    if (obj == null) continue;
                    if (i == 0) { closed_legal_type = obj.optString("type"); closed_legal_count = obj.optInt("count"); }
                    else if (i == 1) { closed_general_type = obj.optString("type"); closed_general_count = obj.optInt("count"); }
                }
            }
        }

        ActiveModel activeModel = new ActiveModel(activeTotal, active_legal_type, active_legal_count,
                active_general_type, active_general_count, closedTotal,
                closed_legal_type, closed_legal_count, closed_general_type, closed_general_count);

        int seq = findSequenceFor("matters", true);
        if (seq != -1) updateKpiItem(seq, activeModel);
        loadKpiRecyclerview();
    }

    public void launchPaySubscriptionPage(String email, String user_count) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://staging.payment.digicoffer.com/renew?useremail="
                + email + "&users=" + user_count));
        startActivityForResult(intent, AUTH_REQUEST_CODE);
    }

    private void loadSubmittedTsdata(JSONObject data) throws JSONException {
        JSONObject submittedDates = data.optJSONObject("submittedDates");
        String submittedStart = submittedDates != null ? submittedDates.optString("startDate") : "";
        String submittedEnd   = submittedDates != null ? submittedDates.optString("endDate") : "";
        SubmittedTimesheetModel model = new SubmittedTimesheetModel(submittedStart, submittedEnd);
        int seq = findSequenceFor("submittedTs", true);
        if (seq != -1) updateKpiItem(seq, model);
    }

    private void loadPendingTsdata(JSONObject data) throws JSONException {
        JSONObject pendingDates = data.optJSONObject("pendingDates");
        String pendingStart = pendingDates != null ? pendingDates.optString("startDate") : "";
        String pendingEnd   = pendingDates != null ? pendingDates.optString("endDate") : "";
        PendingTimeSheetsModel model = new PendingTimeSheetsModel(pendingStart, pendingEnd);
        int seq = findSequenceFor("pendingTs", true);
        if (seq != -1) updateKpiItem(seq, model);
    }

    private void loadStoragedata(JSONObject data, boolean isKpi) throws JSONException {
        String balanceStorage = "", currentStorage = "", totalStorage = "";
        if (data != null) {
            balanceStorage  = data.optString("balanceStorage", "");
            currentStorage  = data.optString("currentStorage", "");
            totalStorage    = data.optString("totalStorage", "");
        }
        StorageModel storageModel = new StorageModel(balanceStorage, currentStorage, totalStorage);
        int seq = findSequenceFor("storage", isKpi);
        if (seq != -1) {
            if (isKpi) {
                updateKpiItem(seq, storageModel);
                loadKpiRecyclerview();
            } else {
                itemArrayList.add(new Item(seq, storageModel));
                loadRecyclerview();
            }
        }
    }

    private void loadHiringData(JSONArray data) throws JSONException {
        String corporateType = ""; int corporateCount = 0;
        String criminalType  = ""; int criminalCount  = 0;
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.optJSONObject(i);
            if (i == 0) { corporateType = obj.optString("type"); corporateCount = obj.optInt("count"); }
            else if (i == 1) { criminalType = obj.optString("type"); criminalCount = obj.optInt("count"); }
        }
        HiringModel model = new HiringModel(corporateType, corporateCount, criminalType, criminalCount);
        int seq = findSequenceFor("newHires", true);
        if (seq != -1) updateKpiItem(seq, model);
    }

    private void loadHoursdata(JSONObject data) throws JSONException {
        String approxRevenue = "0", averageBillingRate = "0";
        String billableHours = "0:00", billablePercentage = "0";
        String nonBillableHours = "0:00", nonBillablePercentage = "0";
        String CurrencySymbol = "", CurrencyCode = "";

        if (data != null) {
            approxRevenue          = data.optString("approxRevenue", approxRevenue);
            averageBillingRate     = data.optString("averageBillingRate", averageBillingRate);
            billableHours          = data.optString("billableHours", billableHours);
            billablePercentage     = data.optString("billablePercentage", billablePercentage);
            nonBillableHours       = data.optString("nonBillableHours", nonBillableHours);
            nonBillablePercentage  = data.optString("nonBillablePercentage", nonBillablePercentage);
            if (data.has("currency")) {
                JSONObject cur = data.optJSONObject("currency");
                if (cur != null) {
                    CurrencyCode   = cur.optString("code");
                    CurrencySymbol = cur.optString("symbol");
                }
            }
        }

        ApproxRevenueModel     approxModel   = new ApproxRevenueModel(approxRevenue, CurrencySymbol, CurrencyCode);
        AverageBillingRateModel avgModel      = new AverageBillingRateModel(averageBillingRate, CurrencySymbol, CurrencyCode);
        BillableModel           billModel     = new BillableModel(billableHours, billablePercentage);
        NonBillableModel        nonBillModel  = new NonBillableModel(nonBillableHours, nonBillablePercentage);

        int seqBill    = findSequenceFor("billable", true);
        int seqNon     = findSequenceFor("nonbillable", true);
        int seqApprox  = findSequenceFor("approxrevenue", true);
        int seqAvg     = findSequenceFor("avgbillingrate", true);

        if (seqBill != -1)   updateKpiItem(seqBill, billModel);
        if (seqNon != -1)    updateKpiItem(seqNon, nonBillModel);
        if (seqApprox != -1) updateKpiItem(seqApprox, approxModel);
        if (seqAvg != -1)    updateKpiItem(seqAvg, avgModel);

        loadKpiRecyclerview();
    }

    private void loadTeamdata(JSONObject data) throws JSONException {
        JSONObject team = data != null ? data.optJSONObject("team") : null;
        String timeStamp = "", message = "", user = "";
        if (team != null) {
            timeStamp = team.optString("timestamp", "");
            message   = team.optString("message", "");
            user      = team.optString("user", "");
        }
        if (!timeStamp.isEmpty()) {
            SimpleDateFormat in  = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("E hh:mm a", Locale.getDefault());
            try {
                Date d = in.parse(timeStamp);
                TeamChatModel teamChatModel = new TeamChatModel(out.format(d), user, message);
                int seq = findSequenceFor("teamChat", false);
                itemArrayList.add(new Item(seq, teamChatModel));
            } catch (ParseException e) { e.printStackTrace(); }
        }
    }

    private void loadClientdata(JSONObject data) throws JSONException {
        JSONObject client = data != null ? data.optJSONObject("client") : null;
        String timeStamp = "", message = "", user = "";
        if (client != null) {
            timeStamp = client.optString("timestamp", "");
            message   = client.optString("message", "");
            user      = client.optString("user", "");
        }
        if (!timeStamp.isEmpty()) {
            SimpleDateFormat in  = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("E hh:mm a", Locale.getDefault());
            try {
                Date d = in.parse(timeStamp);
                ClientChatModel clientChatModel = new ClientChatModel(out.format(d), user, message);
                int seq = findSequenceFor("clientChat", false);
                if (!Constants.ROLE.equalsIgnoreCase("AAM")) {
                    itemArrayList.add(new Item(seq, clientChatModel));
                }
            } catch (ParseException e) { e.printStackTrace(); }
        }
    }

    private void loadMeetingData(JSONObject data) throws JSONException {
        String date = "", from_ts = "", to_ts = "", subject = "", count = "";
        if (data != null) {
            date    = data.optString("date", "");
            from_ts = data.optString("fromTs", "");
            to_ts   = data.optString("toTs", "");
            subject = data.optString("subject", "");
            count   = data.optString("meetings_count", "");
        }
        MeetingModel meetingModel = new MeetingModel(date, from_ts, to_ts, subject, count);
        int seq = findSequenceFor("meeting", false);
        itemArrayList.add(new Item(seq, meetingModel));
    }

    private void loadEmaildata(JSONObject data) throws JSONException {
        if (data == null) return;
        String timeStamp = data.optString("timestamp");
        String message   = data.optString("message");
        String subject   = data.optString("user");
    }

    public void Page_Navigation(Fragment fragment) {
        Constants.mainActivity.navigation_items(fragment);
    }
}