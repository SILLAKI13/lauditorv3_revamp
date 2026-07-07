package com.digicoffer.lauditor.Appointments.ViewModels;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Appointments.Adapters.AppointmentsAdapter;
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Appointments.Models.PaymentModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Appointments extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener, AppointmentsAdapter.InterfaceListener {

    // ─── Views ────────────────────────────────────────────────────────────────
    LinearLayout linear_notes, ll_nav_buttons;
    CardView tl_search_matter;
    RecyclerView rv_appointments_list;
    Button btn_send_request;
    Dialog progressDialog;
    View tl_search_appointments;
    AppCompatButton btn_prev, btn_next, btn_search;
    TextInputEditText et_search_appointments;

    // Empty state views
    private LinearLayout ll_empty_state;
    private LinearLayout ll_search_empty_state;

    // ─── ViewModel ────────────────────────────────────────────────────────────
    private NewModel mViewModel;

    // ─── Data ─────────────────────────────────────────────────────────────────
    private AppointmentModel selectedAppointment;
    String pendingHighlightIds = "";

    /**
     * masterAppointmentList holds ALL appointments fetched from the API in one call.
     * This list is sorted once and never modified afterwards.
     * All pagination and search filtering work on top of this list.
     */
    private final ArrayList<AppointmentModel> masterAppointmentList = new ArrayList<>();

    /**
     * filteredAppointmentList is a subset of masterAppointmentList based on the
     * current search query. When there is no search text it mirrors masterAppointmentList.
     * Pagination always operates on this list.
     */
    private final ArrayList<AppointmentModel> filteredAppointmentList = new ArrayList<>();

    /**
     * currentPageList is the slice of filteredAppointmentList for the current page.
     * This is what the RecyclerView adapter receives.
     */
    private final ArrayList<AppointmentModel> currentPageList = new ArrayList<>();

    // ─── Pagination state ─────────────────────────────────────────────────────

    /**
     * Number of items displayed per page.
     */
    private static final int PAGE_SIZE = 10;

    /**
     * Zero-based index of the page currently on screen.
     */
    private int currentPage = 0;

    /**
     * Total number of pages derived from filteredAppointmentList.size().
     */
    private int totalPages = 0;

    // =========================================================================
    //  Lifecycle
    // =========================================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Scoped to Activity so Activity can observe it too
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
    }

    private void setViewModelData(String data) {
        mViewModel.setData(data);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.appointments);
        // Prevent keyboard from pushing nav buttons up
        requireActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        // Restore original mode for other fragments
        requireActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
    }

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        setViewModelData(getString(R.string.appointments));
        requireActivity().setTitle(R.string.appointments);

        View view = inflater.inflate(R.layout.view_appointments, container, false);

        rv_appointments_list = view.findViewById(R.id.rv_appointments_list);
        ll_nav_buttons = view.findViewById(R.id.ll_nav_buttons);
        btn_prev = view.findViewById(R.id.btn_prev);
        btn_next = view.findViewById(R.id.btn_next);
        tl_search_matter = view.findViewById(R.id.ll_search_appointments);
        btn_search = tl_search_matter.findViewById(R.id.btn_search);
        tl_search_appointments = view.findViewById(R.id.tl_search_appointments);
        et_search_appointments = tl_search_appointments.findViewById(R.id.et_Search);
        et_search_appointments.setHint(R.string.search_appointments);

        // Empty state views
        ll_empty_state = view.findViewById(R.id.ll_empty_state);
        ll_search_empty_state = view.findViewById(R.id.ll_search_empty_state);

        ll_nav_buttons.setVisibility(View.VISIBLE);
        btn_prev.setText(R.string.prev_);
        btn_next.setText(R.string.next_);

        // Both nav buttons start as disabled until data arrives
        AndroidUtils.ToggleButton(0, btn_prev);
        AndroidUtils.ToggleButton(0, btn_next);

        btn_prev.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        btn_search.setOnClickListener(this);

        // Initially show empty state while loading
        showEmptyState(true, false);

        // Live search: filter on every keystroke
        et_search_appointments.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                String query = (s != null) ? s.toString().trim() : "";
                searchRunnable = () -> applySearchFilter(query);
                handler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Fetch all appointments from API
        callAppointmentList();

        handleNotificationNavigation();

        return view;
    }

    // =========================================================================
    //  Empty State Methods
    // =========================================================================

    private void showEmptyState(boolean showEmpty, boolean isSearching) {
        if (showEmpty) {
            // Hide recycler view
            rv_appointments_list.setVisibility(View.GONE);
            // Hide navigation buttons
            ll_nav_buttons.setVisibility(View.GONE);

            if (isSearching) {
                // Show search empty state
                if (ll_search_empty_state != null) {
                    ll_search_empty_state.setVisibility(View.VISIBLE);
                }
                if (ll_empty_state != null) {
                    ll_empty_state.setVisibility(View.GONE);
                }
            } else {
                // Show regular empty state
                if (ll_empty_state != null) {
                    ll_empty_state.setVisibility(View.VISIBLE);
                }
                if (ll_search_empty_state != null) {
                    ll_search_empty_state.setVisibility(View.GONE);
                }
            }
        } else {
            // Show recycler view and hide empty states
            rv_appointments_list.setVisibility(View.VISIBLE);
            if (ll_empty_state != null) {
                ll_empty_state.setVisibility(View.GONE);
            }
            if (ll_search_empty_state != null) {
                ll_search_empty_state.setVisibility(View.GONE);
            }
            // Show navigation buttons if there are items
            if (filteredAppointmentList != null && !filteredAppointmentList.isEmpty()) {
                ll_nav_buttons.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateEmptyState() {
        boolean hasItems = filteredAppointmentList != null && !filteredAppointmentList.isEmpty();
        String searchQuery = et_search_appointments.getText() != null ?
                et_search_appointments.getText().toString().trim() : "";
        boolean isSearching = !TextUtils.isEmpty(searchQuery);

        if (!hasItems) {
            showEmptyState(true, isSearching);
        } else {
            showEmptyState(false, false);
        }
    }

    // =========================================================================
    //  Notification navigation
    // =========================================================================

    private void handleNotificationNavigation() {
        Bundle bundle = Constants.notificationBundle;
        String route = bundle.getString(Constants.NavKeys.ROUTE_NAME);
        pendingHighlightIds = bundle.getString(Constants.NavKeys.APPOINTMENT_ID);
        Constants.isFromNotification = false;
        Constants.notificationBundle.clear();
    }

    // =========================================================================
    //  API calls
    // =========================================================================

    /**
     * Fetches ALL appointments from the server in a single request.
     * Client-side pagination then slices the sorted result into pages of PAGE_SIZE.
     */
    private void callAppointmentList() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/appointments",
                    "Appointments_List",
                    postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callCancelAppointments(AppointmentModel appointmentModel) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/appointments/" + appointmentModel.getId() + "/cancel",
                    "Cancel_Appointments",
                    postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    /**
     * Calls DELETE v3/appointments/<appointment_id>/delete
     * Uses the same WebServiceHelper pattern as Cancel so the response
     * is handled in onAsyncTaskComplete() under "Delete_Appointment".
     */
    private void callDeleteAppointment(AppointmentModel appointmentModel) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/appointments/" + appointmentModel.getId() + "/delete",
                    "Delete_Appointment",
                    postData.toString());
            Log.d("Delete_Payload", postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    // =========================================================================
    //  JSON Parsing
    // =========================================================================

    /**
     * Parses the appointments JSON array into masterAppointmentList,
     * applies the custom sort once, mirrors the result into filteredAppointmentList,
     * resets currentPage to 0 and renders the first page.
     */
    private void loadAppointmentsList(JSONArray appointments) {
        try {
            masterAppointmentList.clear();

            for (int i = 0; i < appointments.length(); i++) {
                JSONObject jsonObject = appointments.optJSONObject(i);

                AppointmentModel appointmentModel = new AppointmentModel();
                appointmentModel.setId(jsonObject.optString("id", ""));
                appointmentModel.setClient_id(jsonObject.optString("client_id", ""));
                appointmentModel.setGuid(jsonObject.optString("guid", ""));
                appointmentModel.setClient_name(jsonObject.optString("client_name"));
                appointmentModel.setAppointment_from(jsonObject.optString("appointment_from"));
                appointmentModel.setAppointment_to(jsonObject.optString("appointment_to"));
                appointmentModel.setConsultation_mode(jsonObject.optString("consultation_mode"));
                appointmentModel.setAppointment_status(jsonObject.optString("appointment_status"));
                appointmentModel.setMeeting_room_id(jsonObject.optString("meeting_room_id"));
                appointmentModel.setMeeting_room_expires_at(
                        jsonObject.optString("meeting_room_expires_at"));
                appointmentModel.setRsvp_status(jsonObject.optString("rsvp_status"));
                appointmentModel.setCreated_at(jsonObject.optString("created_at"));

                if (jsonObject.has("client_profile_pic")) {
                    appointmentModel.setClient_profile_pic(
                            jsonObject.optString("client_profile_pic", ""));
                }
                if (jsonObject.has("services_offered")) {
                    appointmentModel.setServices_offered(
                            jsonObject.optJSONArray("services_offered"));
                }
                if (jsonObject.has("payment")) {
                    JSONObject paymentObject = jsonObject.getJSONObject("payment");
                    PaymentModel payment = new PaymentModel();
                    payment.setStatus(paymentObject.optString("status"));
                    payment.setAmount_paid(paymentObject.optString("amount_paid"));
                    payment.setCurrency(paymentObject.optString("currency"));
                    payment.setSymbol(paymentObject.optString("symbol"));
                    payment.setLabel(paymentObject.optString("label"));
                    appointmentModel.setPayment(payment);
                }

                masterAppointmentList.add(appointmentModel);
            }

            // Post to main thread for UI operations
            new Handler(Looper.getMainLooper()).post(() -> {

                // Step 1: Sort the master list once
                applySortToList(masterAppointmentList);

                // Step 2: Mirror sorted master into filtered list (no search active yet)
                filteredAppointmentList.clear();
                filteredAppointmentList.addAll(masterAppointmentList);

                // Step 3: Reset to page 0 and calculate total pages
                currentPage = 0;
                calculateTotalPages();

                // Step 4: Render the first page
                renderCurrentPage();

                // Step 5: Update empty state
                updateEmptyState();
            });

        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
            // Show empty state on error
            showEmptyState(true, false);
        }
    }

    // =========================================================================
    //  Sorting
    // =========================================================================

    /**
     * Sorts the given list using the following rules:
     * <p>
     * Rule 1 — upcoming / ongoing appointments come first, sorted ASCENDING
     * by appointment_from so that 10:00 AM appears before 11:00 AM.
     * <p>
     * Rule 2 — All other statuses (cancelled, completed, etc.) come after,
     * sorted DESCENDING by appointment_from (most recent first).
     */
    private void applySortToList(List<AppointmentModel> list) {
        final SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

        Collections.sort(list, (o1, o2) -> {
            try {
                String status1 = (o1.getAppointment_status() != null)
                        ? o1.getAppointment_status().toLowerCase(Locale.ROOT) : "";
                String status2 = (o2.getAppointment_status() != null)
                        ? o2.getAppointment_status().toLowerCase(Locale.ROOT) : "";

                boolean isPriority1 = status1.equals("upcoming") || status1.equals("ongoing");
                boolean isPriority2 = status2.equals("upcoming") || status2.equals("ongoing");

                // Both are upcoming/ongoing → ASCENDING (earliest time first)
                if (isPriority1 && isPriority2) {
                    Date d1 = dateFormat.parse(o1.getAppointment_from());
                    Date d2 = dateFormat.parse(o2.getAppointment_from());
                    if (d1 == null || d2 == null) return 0;
                    return d1.compareTo(d2);
                }

                // Only o1 is priority → o1 goes to the top
                if (isPriority1) return -1;

                // Only o2 is priority → o2 goes to the top
                if (isPriority2) return 1;

                // Neither is priority → DESCENDING (most recent first)
                Date d1 = dateFormat.parse(o1.getAppointment_from());
                Date d2 = dateFormat.parse(o2.getAppointment_from());
                if (d1 == null || d2 == null) return 0;
                return d2.compareTo(d1);

            } catch (ParseException e) {
                e.fillInStackTrace();
                return 0;
            }
        });
    }

    // =========================================================================
    //  Search filter
    // =========================================================================

    /**
     * Filters masterAppointmentList by the given query string (case-insensitive).
     * Matching is done against client name, appointment status and consultation mode.
     * After filtering the result is re-sorted and pagination resets to page 0.
     */
    private void applySearchFilter(String query) {
        filteredAppointmentList.clear();

        if (query.isEmpty()) {
            filteredAppointmentList.addAll(masterAppointmentList);
        } else {
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            for (AppointmentModel model : masterAppointmentList) {
                String clientName = (model.getClient_name() != null)
                        ? model.getClient_name().toLowerCase(Locale.ROOT) : "";
                String status = (model.getAppointment_status() != null)
                        ? model.getAppointment_status().toLowerCase(Locale.ROOT) : "";
                String consultationMode = (model.getConsultation_mode() != null)
                        ? model.getConsultation_mode().toLowerCase(Locale.ROOT) : "";

                if (clientName.contains(lowerQuery)
                        || status.contains(lowerQuery)
                        || consultationMode.contains(lowerQuery)) {
                    filteredAppointmentList.add(model);
                }
            }
        }

        // Keep the sort order consistent after filtering
        applySortToList(filteredAppointmentList);

        // Always go back to page 0 when the filter changes
        currentPage = 0;
        calculateTotalPages();
        renderCurrentPage();

        // Update empty state after search
        updateEmptyState();
    }

    // =========================================================================
    //  Pagination – core logic
    // =========================================================================

    /**
     * Calculates totalPages from filteredAppointmentList.size() and PAGE_SIZE.
     * Must be called every time filteredAppointmentList is rebuilt.
     */
    private void calculateTotalPages() {
        int total = filteredAppointmentList.size();
        if (total == 0) {
            totalPages = 0;
        } else {
            totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        }
    }

    /**
     * Builds currentPageList as the PAGE_SIZE slice of filteredAppointmentList
     * at index currentPage, then binds it to the RecyclerView and updates the
     * Prev / Next button states.
     */
    private void renderCurrentPage() {
        currentPageList.clear();

        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, filteredAppointmentList.size());

        for (int i = startIndex; i < endIndex; i++) {
            currentPageList.add(filteredAppointmentList.get(i));
        }

        loadAppointmentsRecyclerview(currentPageList);
        updatePaginationButtons();
    }

    /**
     * Enables Prev when not on the first page.
     * Enables Next when there are more pages after the current one.
     */
    private void updatePaginationButtons() {
        if (currentPage > 0) {
            AndroidUtils.ToggleButton(1, btn_prev);
        } else {
            AndroidUtils.ToggleButton(0, btn_prev);
        }

        if (currentPage < totalPages - 1) {
            AndroidUtils.ToggleButton(1, btn_next);
        } else {
            AndroidUtils.ToggleButton(0, btn_next);
        }
    }

    // =========================================================================
    //  RecyclerView binding
    // =========================================================================

    /**
     * Binds the given list (always a single page slice) to the RecyclerView.
     */

    private void loadAppointmentsRecyclerview(ArrayList<AppointmentModel> pageList) {
        try {
            if (rv_appointments_list != null) {
                rv_appointments_list.removeAllViews();
            }

            AppointmentsAdapter appointmentsAdapter = new AppointmentsAdapter(
                    pageList,
                    getContext(),
                    this,
                    pendingHighlightIds,
                    requireActivity());

            if (rv_appointments_list != null) {
                rv_appointments_list.setAdapter(appointmentsAdapter);
                AndroidUtils.LoadingRecyclerview(rv_appointments_list, getContext());
                AndroidUtils.setupBottomSpacerFooter(rv_appointments_list, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
            }

        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }
    // =========================================================================
    //  Click events
    // =========================================================================

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_prev) {
            if (currentPage > 0) {
                currentPage--;
                renderCurrentPage();
            }
        } else if (id == R.id.btn_next) {
            if (currentPage < totalPages - 1) {
                currentPage++;
                renderCurrentPage();
            }
        } else if (id == R.id.btn_search) {
            String query = (et_search_appointments.getText() != null)
                    ? et_search_appointments.getText().toString().trim() : "";
            applySearchFilter(query);
        }
    }

    // =========================================================================
    //  AsyncTask callback
    // =========================================================================

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog != null && progressDialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progressDialog);
        }

        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                boolean error = result.getBoolean("error");

                // ─── Handle Appointments_List response ────────────────────────
                if (httpResult.getRequestType().equals("Appointments_List")) {
                    if (error) {
                        String msg = result.getString("msg");
                        AndroidUtils.showAlert(msg, getActivity());
                        showEmptyState(true, false);
                    } else {
                        JSONArray appointments = result.optJSONArray("appointments");
                        if (appointments != null && appointments.length() > 0) {
                            loadAppointmentsList(appointments);
                        } else {
                            masterAppointmentList.clear();
                            filteredAppointmentList.clear();
                            currentPageList.clear();
                            currentPage = 0;
                            totalPages = 0;
                            loadAppointmentsRecyclerview(currentPageList);
                            updatePaginationButtons();
                            showEmptyState(true, false);
                        }
                    }
                }

                // ─── Handle Cancel_Appointments response ──────────────────────
                else if (httpResult.getRequestType().equals("Cancel_Appointments")) {
                    String msg = result.getString("msg");
                    AndroidUtils.showAlert(msg, getActivity());
                    if (!error) {
                        // Reload all data and return to page 0
                        callAppointmentList();
                    }
                }

                // ─── Handle Delete_Appointment response ───────────────────────
                else if (httpResult.getRequestType().equals("Delete_Appointment")) {
                    String msg = result.getString("msg");
                    AndroidUtils.showAlert(msg, getActivity());
                    if (!error) {
                        // Reload all data and return to page 0
                        callAppointmentList();
                    }
                }

            } else {
                // Non-success HTTP status
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                String msg = result.getString("msg");
                AndroidUtils.showAlert(msg, getActivity());
                showEmptyState(true, false);
            }

        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
            showEmptyState(true, false);
        }
    }

    // =========================================================================
    //  Adapter interface callbacks
    // =========================================================================

    @Override
    public void ViewAppointmentHistory(AppointmentModel appointmentModel,
                                       ArrayList<AppointmentModel> itemsArrayList) {
        selectedAppointment = appointmentModel;

        // Hide list UI elements
        ll_nav_buttons.setVisibility(View.GONE);
        rv_appointments_list.setVisibility(View.GONE);
        if (tl_search_matter != null) {
            tl_search_matter.setVisibility(View.GONE);
        }
        if (tl_search_appointments != null) {
            tl_search_appointments.setVisibility(View.GONE);
        }
        // Hide empty states if visible
        if (ll_empty_state != null) {
            ll_empty_state.setVisibility(View.GONE);
        }
        if (ll_search_empty_state != null) {
            ll_search_empty_state.setVisibility(View.GONE);
        }

        // Show the history container frame
        View historyContainer = getView().findViewById(R.id.fl_appointment_history);
        if (historyContainer != null) {
            historyContainer.setVisibility(View.VISIBLE);
        }

        // Build AppointmentHistory fragment with client data
        AppointmentHistory historyFragment = new AppointmentHistory();
        Bundle bundle = new Bundle();
        bundle.putString("client_id", selectedAppointment.getClient_id());
        bundle.putString("client_name", selectedAppointment.getClient_name());
        bundle.putString("client_profile_pic", selectedAppointment.getClient_profile_pic());
        historyFragment.setArguments(bundle);

        // Restore list UI when the history screen is closed
        historyFragment.setOnHistoryCloseListener(() -> {
            rv_appointments_list.setVisibility(View.VISIBLE);
            ll_nav_buttons.setVisibility(View.VISIBLE);

            if (tl_search_appointments != null) {
                tl_search_appointments.setVisibility(View.VISIBLE);
            }
            if (historyContainer != null) {
                historyContainer.setVisibility(View.GONE);
            }

            // Update empty state when returning
            updateEmptyState();

            requireActivity().setTitle(R.string.appointments);
        });

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fl_appointment_history, historyFragment)
                .addToBackStack("appointment_history")
                .commit();
    }

    @Override
    public void CancelAppointment(AppointmentModel appointmentModel) {
        AndroidUtils.showConfirmationDialog(
                getContext(),
                "Confirmation",
                "Are you sure you want to cancel this appointment? This action cannot be undone.",
                new AndroidUtils.OnConfirmListener() {
                    @Override
                    public void onSave() {
                        callCancelAppointments(appointmentModel);
                    }

                    @Override
                    public void onCancel() {
                        // dismissed — do nothing
                    }
                }
        );
    }

    @Override
    public void DeleteAppointment(AppointmentModel appointmentModel) {
        AndroidUtils.showConfirmationDialog(
                getContext(),
                "Confirmation",
                "Are you sure you want to delete this appointment? This action cannot be undone.",
                new AndroidUtils.OnConfirmListener() {
                    @Override
                    public void onSave() {
                        callDeleteAppointment(appointmentModel);
                    }

                    @Override
                    public void onCancel() {
                        // dismissed — do nothing
                    }
                }
        );
    }
}