package com.digicoffer.lauditor.Appointments.ViewModels;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Appointments.Adapters.AppointmentHistoryAdapter;
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Appointments.Models.PaymentModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// ✅ Changed from BottomSheetDialogFragment to Fragment
public class AppointmentHistory extends Fragment
        implements AsyncTaskCompleteListener,
        AppointmentHistoryAdapter.OnNoteActionListener {

    private RecyclerView rvAppointmentsTimeline;
    private TextView tvClientName, tv_appointment_history, person_icon;
    ImageView iv_close_history, iv_profile;
    private AppCompatButton btnTimeline;
    private Dialog progressDialog;
    private ArrayList<AppointmentModel> historyList = new ArrayList<>();
    private AppointmentHistoryAdapter adapter;
    private String clientId = "";
    private String clientName = "";
    private String client_profile_pic = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get client ID and name from arguments
        if (getArguments() != null) {
            clientName = getArguments().getString("client_name", "");
            clientId = getArguments().getString("client_id", "");
            client_profile_pic = getArguments().getString("client_profile_pic", "");
        }
        callAppointmentHistoryList(clientId);
    }

    public interface OnHistoryCloseListener {
        void onHistoryClosed();
    }

    private OnHistoryCloseListener closeListener;

    // Add this method to set the listener
    public void setOnHistoryCloseListener(OnHistoryCloseListener listener) {
        this.closeListener = listener;
    }
    // ✅ REMOVED onStart() - not needed for regular Fragment

    /// join-room?roomId=086-vxp6-rhw&name=Soundarya%20Digicoffer%20Mem&fromTime=11:30&toTime=12:30&date=2026-02-09
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.appointment_history, container, false);

        initializeViews(view);
        return view;
    }

    private void callAppointmentHistoryList(String client_id) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/appointment/history?client_id=" + client_id,
                    "Appointments_History",
                    postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void initializeViews(View view) {
        rvAppointmentsTimeline = view.findViewById(R.id.rv_appointments_timeline);
        tvClientName = view.findViewById(R.id.tv_client_name);
        tv_appointment_history = view.findViewById(R.id.tv_appointment_history);
        btnTimeline = view.findViewById(R.id.btn_timeline);
        iv_close_history = view.findViewById(R.id.iv_close_history);
        iv_profile = view.findViewById(R.id.iv_profile);
        person_icon = view.findViewById(R.id.person_icon);
        // Set client name in header
        tv_appointment_history.setText(R.string.appointments_history);
        tvClientName.setText(clientName);
        AndroidUtils.loadProfileImage(getContext(), client_profile_pic, iv_profile, person_icon, clientName);
        // Timeline button is selected by default
        btnTimeline.setSelected(true);

        // ✅ Close button - navigate back to appointments list
        iv_close_history.setOnClickListener(v -> {
            // Pop back stack
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }

            // Notify parent fragment
            if (closeListener != null) {
                closeListener.onHistoryClosed();
            }
        });
    }

    private void setupRecyclerView() {
        rvAppointmentsTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentHistoryAdapter(historyList, getContext(), this);
        rvAppointmentsTimeline.setAdapter(adapter);
        AndroidUtils.LoadAnimation(rvAppointmentsTimeline, getContext());
        adapter.notifyDataSetChanged();
    }

    private void callDeleteHistoryNotes(String id, String notes_id) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            postData.put("note_id", notes_id);
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/appointments/" + id + "/notes",
                    "Delete_Notes",
                    postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callUpdateHistoryNotes(String id, String notes_id, String notes) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            postData.put("note_id", notes_id);
            postData.put("note", notes);
            postData.put("note_details", "");
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/appointments/" + id + "/notes",
                    "Update_Notes",
                    postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void loadAppointmentHistoryNotes(String id, String notes) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            postData.put("note", notes);
            postData.put("note_details", "");
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/appointments/" + id + "/notes",
                    "Appointments_History_Notes",
                    postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    @Override
    public void onSaveNote(AppointmentModel appointment, String note, int position) {
        loadAppointmentHistoryNotes(appointment.getId(), note);
    }

    @Override
    public void onCancelNote(AppointmentModel appointment, int position) {
        // Handle cancel action if needed
    }

    @Override
    public void onEditNote(AppointmentModel appointment, String noteId, String noteText, int position) {
        callUpdateHistoryNotes(appointment.getId(), noteId, noteText);
    }

    @Override
    public void onDeleteNote(AppointmentModel appointment, String noteId, int position) {
//        appointments/6989c750a1db721ce05c9ff7/notes
        callDeleteHistoryNotes(appointment.getId(), noteId);

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        AndroidUtils.dismiss_dialog(progressDialog);

        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {

                JSONObject result = new JSONObject(httpResult.getResponseContent());
                boolean error = result.getBoolean("error");

                if (httpResult.getRequestType().equals("Appointments_History")) {
                    historyList.clear();
                    if (error) {
                        AndroidUtils.showAlert(result.getString("msg"), getActivity());
                    } else {

                        JSONArray historyArray = result.optJSONArray("appointments");

                        if (historyArray != null) {

                            for (int i = 0; i < historyArray.length(); i++) {

                                JSONObject obj = historyArray.optJSONObject(i);
                                AppointmentModel historyModel = new AppointmentModel();

                                historyModel.setId(obj.optString("id"));
                                historyModel.setAppointment_from(obj.optString("appointment_from"));
                                historyModel.setAppointment_to(obj.optString("appointment_to"));
                                historyModel.setAppointment_status(obj.optString("appointment_status"));
                                historyModel.setCreated_at(obj.optString("created_at"));
                                historyModel.setMeeting_room_id(obj.optString("meeting_room_id"));
                                historyModel.setMeeting_room_expires_at(obj.optString("meeting_room_expires_at"));

                                // Notes
                                if (obj.has("notes")) {
                                    historyModel.setNotes(obj.optJSONArray("notes"));
                                }

                                // Payment
                                if (obj.has("payment")) {
                                    JSONObject paymentObj = obj.getJSONObject("payment");
                                    PaymentModel payment = new PaymentModel();

                                    payment.setStatus(paymentObj.optString("status"));
                                    payment.setAmount_paid(paymentObj.optString("amount_paid"));
                                    payment.setCurrency(paymentObj.optString("currency"));
                                    payment.setSymbol(paymentObj.optString("symbol"));
                                    payment.setLabel(paymentObj.optString("label"));

                                    historyModel.setPayment(payment);
                                }

                                historyList.add(historyModel);
                            }
                            setupRecyclerView();
                        }
                    }
                } else if (httpResult.getRequestType().equals("Appointments_History_Notes")) {
                    String msg = result.getString("msg");
                    if (error) {
                        AndroidUtils.showAlert(msg, getActivity());
                    } else {
                        // Note saved successfully
                        AndroidUtils.showAlert(msg, getActivity());

                        // Reload appointments to get updated notes
                        callAppointmentHistoryList(clientId);
                    }
                } else if (httpResult.getRequestType().equals("Update_Notes")) {
                    String msg = result.getString("msg");
                    if (error) {
                        AndroidUtils.showAlert(msg, getActivity());
                    } else {
                        // Note saved successfully
                        AndroidUtils.showAlert(msg, getActivity(), "Success");

                        // Reload appointments to get updated notes
                        callAppointmentHistoryList(clientId);
                    }
                } else if (httpResult.getRequestType().equals("Delete_Notes")) {
                    String msg = result.getString("msg");
                    if (error) {
                        AndroidUtils.showAlert(msg, getActivity());
                    } else {
                        // Note saved successfully
                        AndroidUtils.showAlert(msg, getActivity());

                        // Reload appointments to get updated notes
                        callAppointmentHistoryList(clientId);
                    }
                }
            }
        } catch (
                JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
        }
    }
}