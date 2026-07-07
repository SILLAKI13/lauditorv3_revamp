package com.digicoffer.lauditor.Notifications;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils.isTablet;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NotificationCountApi;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.Notifications.Models.Navigation;
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Notifications extends Fragment
        implements AsyncTaskCompleteListener, View.OnClickListener,
        NotificationsAdapter.EventListener {

    // ─── Views ───────────────────────────────────────────────────────────────
    private RecyclerView rv_notifications;
    private TextView tv_notification_count;
    private ImageView btn_delete_all;
    private ImageView ib_read;
    private CheckBox chk_select_all;
    private EditText et_Search;
    private LinearLayout layout_empty_state;   // ← empty state container

    // ─── State ───────────────────────────────────────────────────────────────
    private NewModel mViewModel;
    private NotificationsAdapter adapter;
    private Dialog progress_dialog;
    private NotificationsDo selectedNotification;
    private boolean isAllSelected = false;
    private final ArrayList<NotificationsDo> notificationList = new ArrayList<>();
    String pendingHighlightIds = "";

    public Notifications() { /* required empty constructor */ }

    // ─── Fragment lifecycle ──────────────────────────────────────────────────

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.notifications, container, false);

        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        mViewModel.setData("Notifications");

        bindViews(v);
        setupSearch();
        setupSelectAll();
        setupActionButtons();
        loadRecyclerView();
        callWebservice();
        handleNotificationNavigation();
        return v;
    }

    // ─── View binding ────────────────────────────────────────────────────────

    private void bindViews(View v) {
        tv_notification_count = v.findViewById(R.id.tv_notification_count);
        btn_delete_all        = v.findViewById(R.id.btn_delete_all);
        ib_read               = v.findViewById(R.id.ib_read);
        chk_select_all        = v.findViewById(R.id.chk_select_all);
        et_Search             = v.findViewById(R.id.et_Search);
        rv_notifications      = v.findViewById(R.id.rv_list1);
        layout_empty_state    = v.findViewById(R.id.layout_empty_state);  // ← bind empty state

        setActionButtonsEnabled(false);
    }

    // ─── RecyclerView setup ──────────────────────────────────────────────────

    private void loadRecyclerView() {
        int spanCount = isTablet(getContext()) ? 2 : 1;
        rv_notifications.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        adapter = new NotificationsAdapter(notificationList, this, this, pendingHighlightIds);
        rv_notifications.setAdapter(adapter);
        AndroidUtils.setupBottomSpacerFooter(rv_notifications, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
        rv_notifications.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down));
        rv_notifications.scheduleLayoutAnimation();
    }
    // ─── Search ──────────────────────────────────────────────────────────────

    private void setupSearch() {
        et_Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (adapter != null) adapter.getFilter().filter(s.toString());
            }
        });
    }

    // ─── Select All (CheckBox toggle) ────────────────────────────────────────

    private void setupSelectAll() {
        chk_select_all.setOnClickListener(view -> {
            if (adapter == null) return;
            isAllSelected = !isAllSelected;
            updateSelectAllIcon(isAllSelected);
            adapter.selectOrDeselectAll(isAllSelected);
            load_list(adapter.getList_item());
        });
    }

    private void updateSelectAllIcon(boolean selected) {
        chk_select_all.setChecked(selected);
    }

    // ─── Delete All / Read All buttons ───────────────────────────────────────

    private void setupActionButtons() {
        btn_delete_all.setOnClickListener(v -> {
            if (adapter == null) return;
            AndroidUtils.showConfirmationDialog(
                    getContext(),
                    "Confirmation",
                    "Are you sure you want to delete the selected notifications?",
                    new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                            deleteSelectedNotifications(adapter.getList_item());
                        }

                        @Override
                        public void onCancel() {
                        }
                    }
            );
        });

        ib_read.setOnClickListener(v -> {
            if (adapter != null) readSelectedNotifications(adapter.getList_item());
        });
    }

    // ─── Enable / disable action buttons ─────────────────────────────────────

    public void load_list(ArrayList<NotificationsDo> list) {
        boolean hasSelection = false;
        for (NotificationsDo n : list) {
            if (n.isChecked()) {
                hasSelection = true;
                break;
            }
        }
        setActionButtonsEnabled(hasSelection);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        btn_delete_all.setAlpha(enabled ? 1.0f : 0.4f);
        btn_delete_all.setEnabled(enabled);
        ib_read.setAlpha(enabled ? 1.0f : 0.4f);
        ib_read.setEnabled(enabled);
    }

    // ─── Empty State toggle ───────────────────────────────────────────────────

    /**
     * Shows the empty-state illustration when the list has no items,
     * and hides it (showing the RecyclerView) when items are present.
     */
    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            layout_empty_state.setVisibility(View.VISIBLE);
            rv_notifications.setVisibility(View.GONE);
        } else {
            layout_empty_state.setVisibility(View.GONE);
            rv_notifications.setVisibility(View.VISIBLE);
        }
    }

    // ─── Web service: fetch ───────────────────────────────────────────────────

    private void callWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(
                    this, getContext(), WebServiceHelper.RestMethodType.GET,
                    "notification", "NOTIFICATIONS", new JSONObject().toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    // ─── Web service: delete single ───────────────────────────────────────────

    public void callDeleteNotificationsWebservice(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONArray arr = new JSONArray();
            arr.put(id);
            WebServiceHelper.callHttpWebService(
                    this, getContext(), WebServiceHelper.RestMethodType.PUT,
                    "notification/delete", "DELETE_NOTIFICATIONS", arr.toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    // ─── Web service: read single ─────────────────────────────────────────────

    public void readSingleNotification(String id, NotificationsDo item) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONArray arr = new JSONArray();
            arr.put(id);
            selectedNotification = item;
            WebServiceHelper.callHttpWebService(
                    this, getContext(), WebServiceHelper.RestMethodType.PUT,
                    "notification/read", "READ_SINGLE", arr.toString());
        } catch (Exception e) {
            e.printStackTrace();
            dismissProgress();
        }
    }

    // ─── Web service: read selected ───────────────────────────────────────────

    private void readSelectedNotifications(ArrayList<NotificationsDo> list) {
        try {
            JSONArray arr = new JSONArray();
            for (NotificationsDo n : list) {
                if (n.isChecked()) arr.put(n.getId());
            }
            if (arr.length() == 0) {
                AndroidUtils.showToast("Select at least 1 notification", getActivity());
                return;
            }
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(
                    this, getContext(), WebServiceHelper.RestMethodType.PUT,
                    "notification/read", "READ_NOTIFICATIONS", arr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Web service: delete selected ─────────────────────────────────────────

    private void deleteSelectedNotifications(ArrayList<NotificationsDo> list) {
        try {
            JSONArray arr = new JSONArray();
            for (NotificationsDo n : list) {
                if (n.isChecked()) arr.put(n.getId());
            }
            if (arr.length() == 0) return;
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(
                    this, getContext(), WebServiceHelper.RestMethodType.PUT,
                    "notification/delete", "DELETE_NOTIFICATIONS", arr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    public void openNotificationNavigation(Navigation nav) {
        if (nav == null) {
            callWebservice();
            return;
        }
        AndroidUtils.setupNotificationHandler(getContext(), nav);
    }

    // ─── Parse + display notification list ───────────────────────────────────

    public void loadNotificationsData(JSONObject data) {
        try {
            JSONArray jsonArray = data.getJSONArray("notifications");
            notificationList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                NotificationsDo n = new NotificationsDo();

                n.setId(obj.getString("id"));
                n.setMessage(obj.getString("message"));
                n.setTimestamp(obj.getString("timestamp"));
                n.setStatus(obj.getString("status"));

                if (obj.has("priority"))
                    n.setPriority(obj.getInt("priority"));

                if (obj.has("navigation") && !obj.isNull("navigation")) {
                    JSONObject navObj = obj.getJSONObject("navigation");
                    Navigation nav = new Navigation();
                    nav.setRoute_name(navObj.getString("route_name"));
                    if (navObj.has("params"))
                        nav.setParams(navObj.getJSONObject("params"));
                    n.setNavigation(nav);
                }

                notificationList.add(n);
            }

            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        tv_notification_count.setText(String.valueOf(notificationList.size()));

        // ── Show empty state when list has no items ──
        toggleEmptyState(notificationList.isEmpty());

        // Reset select-all to unchecked state
        isAllSelected = false;
        updateSelectAllIcon(false);
        setActionButtonsEnabled(false);

        if (adapter == null) {
            loadRecyclerView();
        } else {
            adapter = new NotificationsAdapter(notificationList, this, this, pendingHighlightIds);
            rv_notifications.setAdapter(adapter);
            AndroidUtils.setupBottomSpacerFooter(rv_notifications, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
            rv_notifications.scheduleLayoutAnimation();
        }
    }

    private void handleNotificationNavigation() {
        Bundle bundle = Constants.notificationBundle;
        String route = bundle.getString(Constants.NavKeys.ROUTE_NAME);
        pendingHighlightIds = bundle.getString(Constants.NavKeys.APPOINTMENT_ID);
        Constants.isFromNotification = false;
        Constants.notificationBundle.clear();
    }

    // ─── AsyncTask callback ───────────────────────────────────────────────────

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        dismissProgress();

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                String type = httpResult.getRequestType();

                switch (type) {

                    case "NOTIFICATIONS":
                        if (!result.getBoolean("error")) {
                            JSONObject response = new JSONObject(result.getString("data"));
                            setActionButtonsEnabled(false);
                            loadNotificationsData(response);
                        } else {
                            AndroidUtils.showValidationALert("Alert",
                                    result.getString("msg"), getContext());
                        }
                        break;

                    case "READ_SINGLE":
                        if (!result.getBoolean("error") && selectedNotification != null) {
                            selectedNotification.setStatus("read");
                            openNotificationNavigation(selectedNotification.getNavigation());
                            selectedNotification = null;
                        }
                        break;

                    case "DELETE_NOTIFICATIONS":
                        AndroidUtils.showAlert(result.getString("msg"), getActivity(),
                                result.getBoolean("error") ? null : "");
                        callWebservice();
                        refreshNotificationCount();
                        break;

                    case "READ_NOTIFICATIONS":
                        AndroidUtils.showAlert(result.getString("msg"), getActivity(),
                                result.getBoolean("error") ? null : "");
                        callWebservice();
                        refreshNotificationCount();
                        break;
                }

            } catch (Exception e) {
                AndroidUtils.logMsg(e.getMessage());
            }
        } else {
            AndroidUtils.showAlert(httpResult.getResponseContent(), getActivity());
        }
    }

    // ─── EventListener (from adapter) ────────────────────────────────────────

    /**
     * Called when the delete icon on a single notification card is tapped.
     */
    @Override
    public void onEvent(String notification_id) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Alert")
                .setMessage("Are you sure you want to delete this notification?")
                .setPositiveButton("Yes", (dialog, which) ->
                        callDeleteNotificationsWebservice(notification_id))
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }


    @Override
    public void onSelectAllChanged(boolean allSelected) {
        isAllSelected = allSelected;
        updateSelectAllIcon(allSelected);
    }



    @Override
    public void onClick(View view) {  }

    private void dismissProgress() {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
    }

    private void refreshNotificationCount() {
        new NotificationCountApi(getContext()).fetchNotificationCount();
    }
}