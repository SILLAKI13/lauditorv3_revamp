package com.digicoffer.lauditor.Meetings.ViewModels;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.CapitalizeFirstLetter;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.extractDateTimeParts;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getAVChatUrl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Appointments.Models.PaymentModel;
import com.digicoffer.lauditor.Meetings.Models.CalendarItem;
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO;
import com.digicoffer.lauditor.Meetings.Models.Events_Do;
import com.digicoffer.lauditor.Meetings.Models.InviteesInternal_Model;
import com.digicoffer.lauditor.Chat.ViewModels.Chat;
import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.Matter.Models.DocumentsModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.github.barteksc.pdfviewer.PDFView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Events_Adapter extends RecyclerView.Adapter<Events_Adapter.MyViewHolder>
        implements Filterable, View.OnClickListener, AsyncTaskCompleteListener {

    private static String FLAG = "";
    private EventListener context;
    AlertDialog dialog = null;
    private boolean isDetailsVisible = true;
    private Meetings.FilterType currentFilter = Meetings.FilterType.ALL;
    private SparseBooleanArray visibleItemPositions = new SparseBooleanArray();
    Dialog progress_dialog;
    ArrayList<Event_Details_DO> event_details_list = new ArrayList<>();
    ArrayList<InviteesInternal_Model> inviteesInternalArrayList = new ArrayList<>();
    Context mcontext;
    DocumentsModel documentsModel = new DocumentsModel();
    List<DocumentsModel> documentsList = new ArrayList<>();
    ArrayList<CalendarItem> list_item = new ArrayList<>();
    ArrayList<CalendarItem> filtered_list = new ArrayList<>();
    JSONObject event_details = new JSONObject();
    String event_id;
    boolean isRecurring = false;
    boolean isRsvpChanged = false;
    String rsvp_value;
    String event_delete_scope = "";
    Activity activity;
    MyViewHolder my_view_holder;
    Dialog dialog1;
    ArrayList<Events_Do> list_item1 = new ArrayList<>();
    ArrayList<Events_Do> filtered_list1 = new ArrayList<>();

    private int expandedPosition = -1;
    private RecyclerView recyclerView;

    // ─────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────

    public Events_Adapter(ArrayList<Events_Do> events_list, EventListener mcontext,
                          Context context, Activity activity,
                          ArrayList<Event_Details_DO> event_details_list, Dialog dialog) {
        this.list_item1 = events_list;
        this.filtered_list1 = events_list;
        this.context = mcontext;
        this.mcontext = context;
        this.activity = activity;
        this.event_details_list = event_details_list;
        dialog1 = dialog;
        this.filtered_list = new ArrayList<>();
        for (Events_Do event : events_list) {
            this.filtered_list.add(new CalendarItem(event));
        }
        this.list_item = this.filtered_list;
    }

    public Events_Adapter(ArrayList<Events_Do> events_list, EventListener mcontext,
                          Context context, Activity activity) {
        this.filtered_list = new ArrayList<>();
        for (Events_Do event : events_list) {
            this.filtered_list.add(new CalendarItem(event));
        }
        this.list_item = this.filtered_list;
        this.context = mcontext;
        this.mcontext = context;
        this.activity = activity;
    }

    public Events_Adapter(ArrayList<Events_Do> events_list,
                          ArrayList<AppointmentModel> appointments_list,
                          EventListener mcontext, Context context, Activity activity) {
        this.filtered_list = new ArrayList<>();
        for (Events_Do event : events_list) {
            this.filtered_list.add(new CalendarItem(event));
        }
        for (AppointmentModel appointment : appointments_list) {
            this.filtered_list.add(new CalendarItem(appointment));
        }
        this.list_item = this.filtered_list;
        this.context = mcontext;
        this.mcontext = context;
        this.activity = activity;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Interface
    // ─────────────────────────────────────────────────────────────────────

    public interface EventListener {
        void onEvent(ArrayList<Event_Details_DO> event_details_list);
        void delete_events(Events_Do events_do);
        void load_events();
        void delete(String event_id, boolean recur);
    }

    @Override
    public void onClick(View view) { }

    // ─────────────────────────────────────────────────────────────────────
    // Async callback
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (httpResult.getRequestType().equals("EVENT DETAILS")) {
                        if (!result.getBoolean("error")) {
                            event_details_list.clear();
                            load_event_details(result.getJSONObject("event"), event_id, isRecurring);
                        }
                    } else if (httpResult.getRequestType().equals("Cancel_Appointments")) {
                        AndroidUtils.showAlert(result.getString("msg"), activity);
                        context.load_events();
                    } else if (httpResult.getRequestType().equals("Event_rsvp")) {
                        AndroidUtils.showAlert(result.getString("msg"), activity);
                        context.load_events();
                    } else if (httpResult.getRequestType().equals("Appointment_rsvp")) {
                        AndroidUtils.showAlert(result.getString("msg"), activity);
                        context.load_events();
                    } else if (httpResult.getRequestType().equals("View Doc")) {
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            String url = result.getJSONObject("data").getString("url");
                            checkViewType(url, documentsModel);
                            Log.d("TAG_Image", url);
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity);
                        }
                    } else if (httpResult.getRequestType().equals("Decrypt Doc")
                            || httpResult.getRequestType().equals("Other Doc View")) {
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            String url = result.getJSONObject("data").getString("url");
                            display_doc(url, documentsModel);
                            Log.d("TAG_Image", url);
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity);
                        }
                    }
                } catch (JSONException e) {
                    e.fillInStackTrace();
                }
            } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    AndroidUtils.showErrorAlert(result.optString("msg"), activity);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            } else {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), activity);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // RecyclerView helpers
    // ─────────────────────────────────────────────────────────────────────

    public void setRecyclerView(RecyclerView rv) { this.recyclerView = rv; }

    private void safeNotify(int position) {
        Runnable r = () -> {
            if (position >= 0 && position < getItemCount()) notifyItemChanged(position);
        };
        if (recyclerView != null) recyclerView.post(r);
        else new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }

    private void safeNotifyAll() {
        Runnable r = this::notifyDataSetChanged;
        if (recyclerView != null) recyclerView.post(r);
        else new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }

    public void collapseExpanded() {
        if (expandedPosition == -1) return;
        int pos = expandedPosition;
        expandedPosition = -1;
        safeNotify(pos);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Document viewing helpers
    // ─────────────────────────────────────────────────────────────────────

    private void checkViewType(String url, DocumentsModel sharedDocumentsDo) {
        boolean isImage = File_Content_Type.isImage(sharedDocumentsDo.getContentType());
        boolean isPDF   = File_Content_Type.isPDF(sharedDocumentsDo.getContentType());
        boolean isEncrypted = sharedDocumentsDo.isAdded_encryption() || sharedDocumentsDo.isIs_encrypted();
        if (isEncrypted) {
            callDecryptApi(sharedDocumentsDo.getDocid());
        } else if (!isPDF && !isImage) {
            callOtherDocViewApi(sharedDocumentsDo.getDocid());
        } else {
            display_doc(url, sharedDocumentsDo);
        }
    }

    private void display_doc(String url, DocumentsModel sharedDocumentsDo) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.view_documents, null);
        ProgressBar progressBar        = view.findViewById(R.id.progress_pdf);
        ImageView   iv_image           = view.findViewById(R.id.doc_image);
        PDFView     idPDFView          = view.findViewById(R.id.idPDFView);
        WebView     webView            = view.findViewById(R.id.doc_webview);
        TextView    header             = view.findViewById(R.id.header_name);
        ImageView   iv_close_edit_docs = view.findViewById(R.id.close_edit_docs);
        boolean isImage = File_Content_Type.isImage(sharedDocumentsDo.getContentType());
        header.setText(sharedDocumentsDo.getName());
        final AlertDialog dialog = dialogBuilder.create();
        final RetrievePDFfromUrl[] pdfTask = new RetrievePDFfromUrl[1];
        iv_close_edit_docs.setOnClickListener(v -> {
            try {
                if (idPDFView != null) idPDFView.recycle();
                if (pdfTask[0] != null) { pdfTask[0].cancelLoading(); pdfTask[0].cancel(true); }
            } catch (Exception ignored) { }
            dialog.dismiss();
        });
        String lowerUrl = url.toLowerCase();
        boolean urlIsPDF = lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf");
        if (urlIsPDF) {
            idPDFView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
            pdfTask[0].execute(url);
        } else {
            if (isImage) {
                iv_image.setVisibility(View.VISIBLE);
                Glide.with(mcontext).load(url)
                        .placeholder(R.drawable.progress_animation).centerCrop().into(iv_image);
            } else {
                idPDFView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
                pdfTask[0].execute(url);
            }
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    public void callOtherDocViewApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity);
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET,
                    Constants.base_URL + "v3/document/" + id + "/view", "Other Doc View",
                    new JSONObject().toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    @Override
    public Filter getFilter() { return null; }

    // ─────────────────────────────────────────────────────────────────────
    // Event detail model loading
    // ─────────────────────────────────────────────────────────────────────

    private void load_event_details(JSONObject event_details, String event_id, boolean isRecurring) {
        event_details_list.clear();
        this.event_details = event_details;
        try {
            Event_Details_DO event_details_do = new Event_Details_DO();
            event_details_do.setEvent_type(event_details.getString("event_type"));
            event_details_do.setId(event_details.getString("id"));
            event_details_do.setTitle(event_details.getString("title"));
            event_details_do.setDescription(event_details.getString("description"));
            event_details_do.setFrom_ts(event_details.getString("from_ts"));
            event_details_do.setAll_day(event_details.getBoolean("allday"));
            String from_ts = event_details_do.getFrom_ts();
            Date event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
            event_details_do.setDate(AndroidUtils.getDateToString(event_date, "dd-MM-yyyy"));
            event_details_do.setIs_linked_with_timesheet(event_details.optBoolean("is_linked_with_timesheet"));
            event_details_do.setRecurring(event_details.getBoolean("isrecurring"));
            event_details_do.setRepeat_interval(event_details.getString("repeat_interval"));
            event_details_do.setLocation(event_details.getString("location"));
            event_details_do.setDialin(event_details.getString("dialin"));
            event_details_do.setTo_ts(event_details.getString("to_ts"));
            event_details_do.setOffset(event_details.getString("timezone_offset"));
            event_details_do.setOffset_location(event_details.getString("timezone_location"));
            event_details_do.setConverted_Start_time(AndroidUtils.getDateToString(event_date, "HH:mm"));
            event_details_do.setOwner(event_details.getBoolean("owner"));
            String to_ts = event_details_do.getTo_ts();
            event_details_do.setMeeting_link(event_details.getString("meeting_link"));
            Date event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
            event_details_do.setConverted_End_time(AndroidUtils.getDateToString(event_date2, "HH:mm"));
            event_details_do.setRepeat_interval(event_details.getString("repeat_interval"));
            event_details_do.setNotifications(event_details.getJSONArray("notifications"));
            event_details_do.setOwner_name(event_details.getString("owner_name"));
            event_details_do.setAttachments(event_details.getJSONArray("attachments"));
            event_details_do.setTeam_name(event_details.getJSONArray("invitees_internal"));
            event_details_do.setTm_name(event_details.getJSONArray("invitees_external"));
            event_details_do.setCorporate(event_details.optJSONArray("invitees_corporate"));
            if (event_details.has("invitees_consumer_external")) {
                Log.d("ArrayListLog", event_details.getJSONArray("invitees_consumer_external").toString());
                event_details_do.setConsumer_external(event_details.getJSONArray("invitees_consumer_external"));
            }
            if (event_details.has("matter_name"))
                event_details_do.setMatter_name(event_details.getString("matter_name"));
            if (event_details.has("matter_id"))
                event_details_do.setMatter_id(event_details.getString("matter_id"));
            if (event_details.has("matter_type"))
                event_details_do.setMatter_type(event_details.getString("matter_type"));
            if (event_details.has("timesheet_added"))
                event_details_do.setTimesheet_added(event_details.optBoolean("timesheet_added"));
            event_details_list.add(event_details_do);
            if (Objects.equals(FLAG, "MORE")) load_more_details();
            else context.onEvent(event_details_list);
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public void View_doc(String doc_id) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity);
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET,
                    "v3/document/" + doc_id + "/view", "View Doc", new JSONObject().toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public void callDecryptApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", id);
            jsonObject.put("download", false);
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.POST,
                    Constants.decryptUrl, "Decrypt Doc", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // load_more_details  — expanded detail rendering
    // Changes:
    //   • Document rows: rounded grey background applied programmatically
    //   • Attendee rows: avatar initials circle + RSVP icon (no background)
    //   • RSVP summary ("X Yes  X No  X Waiting") shown below attendee header
    //     — counts all groups (team + consumer-external + external + corporate)
    //   • "Team Member(s)" sub-header injected as a plain bold label
    //   • Meeting link row + video icon hidden when meeting link is empty
    //   • Location row hidden when location is empty
    //   • Dial-in row hidden when dial-in is empty
    //   • Total attendee count = team + consumer_external + external + corporate
    // ─────────────────────────────────────────────────────────────────────

    private void load_more_details() {
        my_view_holder.ll_documents.removeAllViews();
        my_view_holder.ll_team_members.removeAllViews();
        my_view_holder.ll_clients.removeAllViews();
        my_view_holder.ll_corp_clients.removeAllViews();

        for (int i = 0; i < event_details_list.size(); i++) {
            Event_Details_DO events_do = event_details_list.get(i);

            // ── Description / meeting agenda ──────────────────────────────
            my_view_holder.event_description.setText(events_do.getDescription());
            if (!events_do.getDescription().isEmpty()) {
                if (events_do.getEvent_type().equals("reminders")) {
                    my_view_holder.tv_event_description.setVisibility(View.GONE);
                } else {
                    my_view_holder.tv_event_description.setVisibility(View.VISIBLE);
                }
                my_view_holder.event_description.setVisibility(View.VISIBLE);
            } else {
                my_view_holder.tv_event_description.setVisibility(View.GONE);
                my_view_holder.event_description.setVisibility(View.GONE);
            }

            // ── Reminder row ──────────────────────────────────────────────
            JSONArray notification = events_do.getNotifications();
            if (notification.length() > 0) {
                try {
                    StringBuilder notificationText = new StringBuilder();
                    for (int j = 0; j < notification.length(); j++) {
                        String value_notify = notification.getString(j);
                        String[] time = value_notify.split("-");
                        String time_format = time[0].trim();
                        String time_value = time.length > 1 ? time[1].trim() : "";
                        if (!time_value.isEmpty())
                            time_value = time_value.substring(0, 1).toUpperCase() + time_value.substring(1);
                        if (j > 0) notificationText.append(", ");
                        notificationText.append(time_format).append(" ").append(time_value).append(" before");
                    }
                    my_view_holder.event_notification.setText(notificationText.toString());
                    my_view_holder.ll_notifications_view.setVisibility(View.VISIBLE);
                    // Show "Email Notification" secondary label
                    TextView tvNotifType = my_view_holder.ll_notifications_view
                            .findViewById(R.id.tv_notification_type);
                    if (tvNotifType != null) tvNotifType.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                my_view_holder.ll_notifications_view.setVisibility(View.GONE);
            }

            // ── Documents — rounded grey background per row ───────────────
            for (int a = 0; a < events_do.getAttachments().length(); a++) {
                try {
                    JSONObject att_obj = events_do.getAttachments().getJSONObject(a);
                    View view = LayoutInflater.from(mcontext)
                            .inflate(R.layout.event_details_notifications, null);

                    TextView  attchment_list  = view.findViewById(R.id.tv_event_notifications);
                    ImageView iv_event_docView = view.findViewById(R.id.iv_event_docView);
                    FrameLayout fl_avatar      = view.findViewById(R.id.fl_avatar);

                    // Hide avatar for document rows
                    if (fl_avatar != null) fl_avatar.setVisibility(View.GONE);

                    // Apply rounded grey background ONLY to document rows
                    view.setBackgroundResource(R.drawable.rectangle_light_grey_bg);

                    iv_event_docView.setVisibility(View.VISIBLE);

                    DocumentsModel document = new DocumentsModel();
                    document.setName(att_obj.optString("name"));
                    document.setDocid(att_obj.optString("docid"));
                    document.setDoctype(att_obj.optString("doctype"));
                    document.setIs_encrypted(att_obj.optBoolean("is_encrypted"));
                    document.setIs_password(att_obj.getBoolean("is_password"));
                    document.setAdded_encryption(att_obj.getBoolean("added_encryption"));
                    document.setContentType(att_obj.optString("content_type"));

                    attchment_list.setText(document.getName());
                    documentsList.add(document);
                    final int position = documentsList.size() - 1;

                    iv_event_docView.setOnClickListener(v -> {
                        Log.d("Clicked Position", "Position: " + position);
                        documentsModel = documentsList.get(position);
                        View_doc(documentsModel.getDocid());
                    });
                    my_view_holder.ll_documents.addView(view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // ── Organiser row ─────────────────────────────────────────────
            if (!events_do.getOwner_name().isEmpty()) {
                View view1 = LayoutInflater.from(mcontext)
                        .inflate(R.layout.event_details_notifications, null);
                TextView    owner_name  = view1.findViewById(R.id.tv_event_notifications);
                ImageView   iv_rsvp_org = view1.findViewById(R.id.iv_rsvp);
                FrameLayout fl_av       = view1.findViewById(R.id.fl_avatar);

                owner_name.setText(events_do.getOwner_name() + " (Organizer)");
                owner_name.setTypeface(null, Typeface.BOLD);

                if (fl_av != null) {
                    fl_av.setVisibility(View.VISIBLE);
                    TextView tvInitials = fl_av.findViewById(R.id.tv_avatar_initials);
                    if (tvInitials != null)
                        tvInitials.setText(getInitials(events_do.getOwner_name()));
                }
                iv_rsvp_org.setVisibility(View.VISIBLE);
                iv_rsvp_org.setImageDrawable(mcontext.getDrawable(R.drawable.ic_check_green));
                my_view_holder.ll_team_members.addView(view1);
            }

            // ── "Team Member(s)" sub-header label ────────────────────────
            if (events_do.getTeam_name().length() > 0) {
                View subHeader = LayoutInflater.from(mcontext)
                        .inflate(R.layout.event_details_notifications, null);
                TextView tvSubHeader = subHeader.findViewById(R.id.tv_event_notifications);
                FrameLayout fl_sh    = subHeader.findViewById(R.id.fl_avatar);
                if (fl_sh != null) fl_sh.setVisibility(View.GONE);
                tvSubHeader.setText(mcontext.getString(R.string.team_members));
                tvSubHeader.setTypeface(null, Typeface.BOLD);
                tvSubHeader.setTextColor(Color.BLACK);
                my_view_holder.ll_team_members.addView(subHeader);
            }

            // ── Team member rows ──────────────────────────────────────────
            // RSVP counters — will accumulate across ALL invitee groups
            int yesCount = 0, noCount = 0, waitingCount = 0;

            try {
                for (int a = 0; a < events_do.getTeam_name().length(); a++) {
                    JSONObject att_team_obj = events_do.getTeam_name().getJSONObject(a);
                    View view = LayoutInflater.from(mcontext)
                            .inflate(R.layout.event_details_notifications, null);
                    TextView    team_list = view.findViewById(R.id.tv_event_notifications);
                    ImageView   iv_rsvp   = view.findViewById(R.id.iv_rsvp);
                    FrameLayout fl_av     = view.findViewById(R.id.fl_avatar);

                    String memberName = att_team_obj.getString("name");
                    team_list.setText(memberName);

                    if (fl_av != null) {
                        fl_av.setVisibility(View.VISIBLE);
                        TextView tvInitials = fl_av.findViewById(R.id.tv_avatar_initials);
                        if (tvInitials != null) tvInitials.setText(getInitials(memberName));
                    }

                    InviteesInternal_Model model = new InviteesInternal_Model();
                    model.setId(att_team_obj.getString("id"));
                    model.setName(memberName);
                    model.setRsvp(att_team_obj.getString("rsvp"));

                    applyRsvpIcon(iv_rsvp, model.getRsvp());

                    switch (model.getRsvp().toLowerCase(Locale.ROOT)) {
                        case "yes": yesCount++;     break;
                        case "no":  noCount++;      break;
                        default:    waitingCount++; break;
                    }

                    my_view_holder.ll_team_members.addView(view);
                    Log.d("Team Members", att_team_obj.getString("name"));
                }
            } catch (JSONException e) {
                e.fillInStackTrace();
            }

            // ── Consumer-external (individual) client rows ────────────────
            my_view_holder.ll_clients.removeAllViews();
            my_view_holder.ll_corp_clients.removeAllViews();

            for (int a = 0; a < events_do.getConsumer_external().length(); a++) {
                try {
                    Log.d("Clients", events_do.getConsumer_external().toString());
                    JSONObject att_team_obj = events_do.getConsumer_external().getJSONObject(a);
                    View view = LayoutInflater.from(mcontext)
                            .inflate(R.layout.event_details_notifications, null);
                    TextView    team_list = view.findViewById(R.id.tv_event_notifications);
                    ImageView   iv_rsvp   = view.findViewById(R.id.iv_rsvp);
                    FrameLayout fl_av     = view.findViewById(R.id.fl_avatar);

                    String name = att_team_obj.optString("tmName");
                    team_list.setText(name);

                    if (fl_av != null) {
                        fl_av.setVisibility(View.VISIBLE);
                        TextView tvInitials = fl_av.findViewById(R.id.tv_avatar_initials);
                        if (tvInitials != null) tvInitials.setText(getInitials(name));
                    }

                    InviteesInternal_Model inviteesInternalModel = new InviteesInternal_Model();
                    inviteesInternalModel.setId(att_team_obj.optString("tmId"));
                    inviteesInternalModel.setName(name);
                    inviteesInternalModel.setRsvp(att_team_obj.optString("rsvp"));

                    // Accumulate RSVP counts from consumer-external group
                    switch (inviteesInternalModel.getRsvp().toLowerCase(Locale.ROOT)) {
                        case "yes": yesCount++;     break;
                        case "no":  noCount++;      break;
                        default:    waitingCount++; break;
                    }

                    applyRsvpIcon(iv_rsvp, inviteesInternalModel.getRsvp());
                    my_view_holder.ll_clients.addView(view);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }

            // ── Entity-grouped external / corporate clients ───────────────
            Map<String, List<InviteesInternal_Model>> entityMap     = new HashMap<>();
            Map<String, List<InviteesInternal_Model>> entitycorpMap = new HashMap<>();
            inviteesInternalArrayList.clear();
            populateEntityMap(events_do.getTm_name(),   entityMap);
            populateEntityMap(events_do.getCorporate(), entitycorpMap);

            // Accumulate RSVP counts from external (entity) clients
            for (List<InviteesInternal_Model> groupList : entityMap.values()) {
                for (InviteesInternal_Model m : groupList) {
                    switch (m.getRsvp().toLowerCase(Locale.ROOT)) {
                        case "yes": yesCount++;     break;
                        case "no":  noCount++;      break;
                        default:    waitingCount++; break;
                    }
                }
            }

            // Accumulate RSVP counts from corporate clients
            for (List<InviteesInternal_Model> groupList : entitycorpMap.values()) {
                for (InviteesInternal_Model m : groupList) {
                    switch (m.getRsvp().toLowerCase(Locale.ROOT)) {
                        case "yes": yesCount++;     break;
                        case "no":  noCount++;      break;
                        default:    waitingCount++; break;
                    }
                }
            }

            if (!entityMap.isEmpty())
                load_Clients_view(entityMap, my_view_holder.ll_clients);
            if (!entitycorpMap.isEmpty())
                load_Clients_view(entitycorpMap, my_view_holder.ll_corp_clients);

            // Update RSVP summary counts (all groups combined)
            updateRsvpSummary(yesCount, noCount, waitingCount);

            // ── Section visibility ────────────────────────────────────────
            int clientSize     = events_do.getTm_name()           != null ? events_do.getTm_name().length()           : 0;
            int individualSize = events_do.getConsumer_external() != null ? events_do.getConsumer_external().length() : 0;
            int corpSize       = events_do.getCorporate()         != null ? events_do.getCorporate().length()         : 0;
            int totClientSize  = clientSize + individualSize;

            // Total attendees = team members + consumer-external + external + corporate
            int teamSize       = events_do.getTeam_name() != null ? events_do.getTeam_name().length() : 0;
            int totalAttendees = teamSize + individualSize + clientSize + corpSize;

            if (totClientSize > 0) {
                my_view_holder.clients.setText(totClientSize + " " + mcontext.getString(R.string.clients));
                my_view_holder.ll_clients_view.setVisibility(View.VISIBLE);
            } else {
                my_view_holder.clients.setText(R.string.clients);
                my_view_holder.ll_clients_view.setVisibility(View.GONE);
            }

            if (events_do.getCorporate() != null && events_do.getCorporate().length() > 0) {
                my_view_holder.corp_clients.setText(
                        events_do.getCorporate().length() + " " + mcontext.getString(R.string.corporate_clients));
                my_view_holder.ll_corp_clients_view.setVisibility(View.VISIBLE);
            } else {
                my_view_holder.corp_clients.setText(R.string.corporate_clients);
                my_view_holder.ll_corp_clients_view.setVisibility(View.GONE);
            }

            // Show total attendee count across all groups
            if (totalAttendees > 0) {
                my_view_holder.team_members.setText(
                        totalAttendees + " " + mcontext.getString(R.string.team_members));
                my_view_holder.ll_team_members_view.setVisibility(View.VISIBLE);
            } else if (my_view_holder.ll_team_members.getChildCount() > 0) {
                my_view_holder.team_members.setText(
                        my_view_holder.ll_team_members.getChildCount() + " " + mcontext.getString(R.string.team_members));
                my_view_holder.ll_team_members_view.setVisibility(View.VISIBLE);
            } else {
                my_view_holder.team_members.setText(R.string.team_members);
                my_view_holder.ll_team_members_view.setVisibility(View.GONE);
            }

            if (my_view_holder.ll_documents.getChildCount() > 0) {
                my_view_holder.documents.setText(
                        events_do.getAttachments().length() + " " + mcontext.getString(R.string.documents));
                my_view_holder.ll_documents_view.setVisibility(View.VISIBLE);
            } else {
                my_view_holder.documents.setText(R.string.documents);
                my_view_holder.ll_documents_view.setVisibility(View.GONE);
            }

            if (events_do.getNotifications().length() > 0) {
                my_view_holder.ll_notifications_view.setVisibility(View.VISIBLE);
            } else {
                my_view_holder.ll_notifications_view.setVisibility(View.GONE);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // RecyclerView lifecycle
    // ─────────────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.events_recyler_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        my_view_holder = holder;
        position = holder.getAdapterPosition();
        final CalendarItem calendarItem = filtered_list.get(position);
        if (calendarItem.isEvent()) {
            bindEventView(holder, calendarItem.getEvent(), position);
        } else if (calendarItem.isAppointment()) {
            bindAppointmentView(holder, calendarItem.getAppointment(), position);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // bindEventView
    // Key changes:
    //   • tv_meeting_link + v_btn (video icon) hidden when meeting link empty
    //   • location label + tv_location hidden when location empty
    //   • dialin label + tv_phone_dialin hidden when dial-in empty
    // ─────────────────────────────────────────────────────────────────────

    private void bindEventView(MyViewHolder holder, Events_Do events_do, int position) {
        final String from_ts = events_do.getEvent_start_time();
        Date event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat amPmFormat = new SimpleDateFormat("a", Locale.US);
        String amPm_from_ts = amPmFormat.format(event_date);
        final String converted_date = AndroidUtils.getDateToString(event_date, "dd MMMM yyyy");
        String converted_time = AndroidUtils.getDateToString(event_date, "hh:mm");

        final String to_ts = events_do.getEvent_end_time();
        Date end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
        String converted_end_time = AndroidUtils.getDateToString(end_date, "hh:mm");
        SimpleDateFormat amPmFormat_to_ts = new SimpleDateFormat("a", Locale.US);
        String amPm_to_ts = amPmFormat_to_ts.format(end_date);

        if (converted_time.equals("00:00"))          converted_time     = "12:00";
        else if (converted_end_time.equals("00:00")) converted_end_time = "12:00";

        holder.events_names.setText(converted_date);
        holder.statusView.setBackgroundDrawable(mcontext.getDrawable(R.color.light_blue));
        holder.rl_actions.setVisibility(View.GONE);

        // 3-dot action menu
        ArrayList<ActionModel> itemActions = new ArrayList<>();
        itemActions.add(new ActionModel("Edit"));
        itemActions.add(new ActionModel("Delete"));
        boolean isExpanded = (position == expandedPosition);

        if (events_do.isOwner()) {
            holder.custom_spinner_cardview.setVisibility(View.VISIBLE);
            holder.custom_spinner_cardview.setOnClickListener(null);
            holder.sp_action.setOnItemClickListener(null);

            if (isExpanded) {
                CommonSpinnerAdapter itemAdapter = new CommonSpinnerAdapter(activity, itemActions);
                holder.sp_action.setAdapter(itemAdapter);
                holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));
                holder.action_list_card.setVisibility(View.VISIBLE);
                holder.sp_action.setVisibility(View.VISIBLE);
            } else {
                holder.sp_action.setAdapter(null);
                holder.action_list_card.setVisibility(View.GONE);
                holder.sp_action.setVisibility(View.GONE);
            }

            holder.custom_spinner_cardview.setOnClickListener(v -> {
                int cur = holder.getAdapterPosition();
                if (cur == RecyclerView.NO_POSITION) return;
                int prev = expandedPosition;
                expandedPosition = (expandedPosition == cur) ? -1 : cur;
                if (prev != -1 && prev != cur) safeNotify(prev);
                safeNotify(cur);
            });

            holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
                int cur = holder.getAdapterPosition();
                if (cur == RecyclerView.NO_POSITION) return;
                String actionName = itemActions.get(pos).getName();
                expandedPosition = -1;
                safeNotify(cur);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (dialog1 != null) dialog1.dismiss();
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(activity);
                    } else {
                        if (actionName.equals("Edit")) {
                            FLAG = "VIEW";
                            callEventDetailsWebservice(events_do.getEvent_id());
                            isRecurring = events_do.isRecurring();
                        } else if (actionName.equals("Delete")) {
                            context.delete_events(events_do);
                        }
                    }
                });
            });

            holder.ib_view_events.setVisibility(View.GONE);
            holder.ib_delete_events.setVisibility(View.GONE);
            holder.ll_rsvp.setVisibility(View.GONE);
        } else {
            holder.custom_spinner_cardview.setVisibility(View.GONE);
            holder.action_list_card.setVisibility(View.GONE);
            holder.ib_view_events.setVisibility(View.GONE);
            holder.ib_delete_events.setVisibility(View.GONE);
            holder.ll_rsvp.setVisibility(View.VISIBLE);
            holder.tv_maybe.setVisibility(View.VISIBLE);
        }

        holder.event_title.setText(events_do.getEvent_Name());

        String title = events_do.getEvent_Name();
        String existing_task = title.contains("-") ? title.split(" - ")[0] : title;
        String second_str    = title.contains("-") ? title.split(" - ")[1] : "";

        if (existing_task.equals("reminders")) {
            holder.tv_matter_title.setText(R.string.reminders);
            holder.ll_event_details.setVisibility(View.GONE);
            holder.event_title.setText(R.string.reminders);
        } else {
            holder.tv_matter_title.setText(existing_task);
            holder.ll_event_details.setVisibility(View.VISIBLE);
        }

        if (events_do.isAll_day()) {
            holder.event_time.setText(R.string.all_day);
        } else {
            holder.event_time.setText(converted_time + amPm_from_ts + " - " + converted_end_time + amPm_to_ts);
        }

        holder.ll_repetation.setVisibility(View.VISIBLE);
        if (events_do.getRepeat_interval().isEmpty()) {
            holder.event_repetation.setText("None");
        } else {
            holder.event_repetation.setText(AndroidUtils.CapitalizeFirstLetter(events_do.getRepeat_interval()));
        }

        holder.event_timezone.setText(events_do.getTimezone_location());

        // ── "Join Meeting" link — show only when meeting link is non-empty ──
        String meetingLink = events_do.getMeeting_link();
        if (meetingLink != null && !meetingLink.trim().isEmpty()) {
            final String finalMeetingLink = meetingLink.trim();
            // Show the video icon
            if (holder.v_btn != null) holder.v_btn.setVisibility(View.VISIBLE);
            holder.tv_meeting_link.setVisibility(View.VISIBLE);
            holder.tv_meeting_link.setText("Join Meeting");
            holder.tv_meeting_link.setTextColor(mcontext.getColor(R.color.light_blue));
            holder.tv_meeting_link.setPaintFlags(
                    holder.tv_meeting_link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.tv_meeting_link.setOnClickListener(v -> {
                try {
                    String url = finalMeetingLink.startsWith("http://") || finalMeetingLink.startsWith("https://")
                            ? finalMeetingLink : "https://" + finalMeetingLink;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setPackage("com.android.chrome");
                    try {
                        v.getContext().startActivity(intent);
                    } catch (Exception ex) {
                        // Fall back to default browser
                        intent.setPackage(null);
                        v.getContext().startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e("MeetingLink", "Failed to open: " + finalMeetingLink, e);
                }
            });
        } else {
            // Hide both the video icon and the text when there is no meeting link
            if (holder.v_btn != null) holder.v_btn.setVisibility(View.GONE);
            holder.tv_meeting_link.setVisibility(View.GONE);
            holder.tv_meeting_link.setText("");
            holder.tv_meeting_link.setOnClickListener(null);
        }

        // ── Dial-in — show only when non-empty ──────────────────────────
        String dialinValue = events_do.getDialin();
        if (dialinValue != null && !dialinValue.trim().isEmpty()) {
            holder.tv_phone_dialin.setText(dialinValue);
            holder.tv_phone_dialin.setVisibility(View.VISIBLE);
            holder.dialin.setVisibility(View.VISIBLE);
        } else {
            holder.tv_phone_dialin.setVisibility(View.GONE);
            holder.dialin.setVisibility(View.GONE);
        }

        // ── Location — show only when non-empty ─────────────────────────
        String locationValue = events_do.getLocation();
        if (locationValue != null && !locationValue.trim().isEmpty()) {
            holder.tv_location.setText(locationValue);
            holder.tv_location.setVisibility(View.VISIBLE);
            holder.location.setVisibility(View.VISIBLE);
        } else {
            holder.tv_location.setVisibility(View.GONE);
            holder.location.setVisibility(View.GONE);
        }

        // View More / Less
        boolean isVisible = visibleItemPositions.get(position, false);
        holder.ll_view_more.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        holder.bt_hide_details.setText(isVisible ? R.string.view_less : R.string.view_more);
        holder.bt_hide_details.setVisibility(View.VISIBLE);
        holder.ib_close_events.setVisibility(View.GONE);

        if (dialog1 != null) {
            load_more_details();
            holder.ib_close_events.setVisibility(View.VISIBLE);
            holder.bt_hide_details.setVisibility(View.GONE);
            holder.ll_view_more.setVisibility(View.VISIBLE);
        } else if (isVisible) {
            load_more_details();
            holder.ll_view_more.setVisibility(View.VISIBLE);
        }

        holder.ib_close_events.setOnClickListener(view -> {
            if (dialog1 != null) dialog1.dismiss();
        });

        holder.bt_hide_details.setOnClickListener(v -> {
            if (dialog1 != null) { dialog1.dismiss(); return; }
            long currentTime = System.currentTimeMillis();
            if (currentTime - holder.lastClickTime < 300) return;
            holder.lastClickTime = currentTime;
            int currentPosition = holder.getAdapterPosition();
            boolean isCurrentlyVisible = visibleItemPositions.get(currentPosition, false);
            notifyItemChanged(currentPosition);
            visibleItemPositions.put(currentPosition, !isCurrentlyVisible);
            if (!isCurrentlyVisible) {
                FLAG = "MORE";
                callEventDetailsWebservice(events_do.getEvent_id());
            } else {
                FLAG = "";
            }
            Log.d("Events_Adapter",
                    "Click registered for position: " + currentPosition + ", Current State: " + isCurrentlyVisible);
        });

        // RSVP buttons (non-owner only)
        if (!events_do.isOwner()) {
            holder.tv_yes.setOnClickListener(v -> {
                events_do.setUserRsvp("Yes");
                call_choosen_rsvp(events_do.getEvent_id(), "Yes");
                load_Yes(holder);
            });
            holder.tv_no.setOnClickListener(v -> {
                events_do.setUserRsvp("No");
                call_choosen_rsvp(events_do.getEvent_id(), "No");
                load_No(holder);
            });
            holder.tv_maybe.setOnClickListener(v -> {
                events_do.setUserRsvp("Maybe");
                call_choosen_rsvp(events_do.getEvent_id(), "Maybe");
                load_Maybe(holder);
            });
        }

        setLoggedInUserRsvp(events_do, holder);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Filter
    // ─────────────────────────────────────────────────────────────────────

    public void setFilter(Meetings.FilterType filterType) {
        this.currentFilter = filterType;
        applyFilter();
    }

    private void applyFilter() {
        filtered_list.clear();
        for (CalendarItem item : list_item) {
            if (currentFilter == Meetings.FilterType.ALL) {
                filtered_list.add(item);
            } else if (currentFilter == Meetings.FilterType.MY_MEETINGS && item.isEvent()) {
                filtered_list.add(item);
            } else if (currentFilter == Meetings.FilterType.APPOINTMENTS && item.isAppointment()) {
                filtered_list.add(item);
            }
        }
        notifyDataSetChanged();
    }

    // ─────────────────────────────────────────────────────────────────────
    // bindAppointmentView
    // ─────────────────────────────────────────────────────────────────────

    private void bindAppointmentView(MyViewHolder holder, AppointmentModel appointment_do, int position) {
        final String from_ts = appointment_do.getAppointment_from();
        Date appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat amPmFormat = new SimpleDateFormat("a", Locale.US);
        String amPm_from_ts = amPmFormat.format(appointment_date);
        final String converted_date = AndroidUtils.getDateToString(appointment_date, "dd MMMM yyyy");
        String converted_time = AndroidUtils.getDateToString(appointment_date, "hh:mm");

        final String to_ts = appointment_do.getAppointment_to();
        Date end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
        String converted_end_time = AndroidUtils.getDateToString(end_date, "hh:mm");
        SimpleDateFormat amPmFormat_to_ts = new SimpleDateFormat("a", Locale.US);
        String amPm_to_ts = amPmFormat_to_ts.format(end_date);

        if (converted_time.equals("00:00"))          converted_time     = "12:00";
        else if (converted_end_time.equals("00:00")) converted_end_time = "12:00";

        holder.events_names.setText(converted_date);
        holder.custom_spinner_cardview.setVisibility(View.GONE);
        holder.action_list_card.setVisibility(View.GONE);
        holder.ib_view_events.setVisibility(View.GONE);
        holder.ib_delete_events.setVisibility(View.GONE);
        holder.ll_rsvp.setVisibility(View.VISIBLE);
        holder.tv_maybe.setVisibility(View.GONE);
        holder.statusView.setBackgroundDrawable(mcontext.getDrawable(R.color.yellow_clear));
        holder.event_title.setText(appointment_do.getClient_name());
        holder.event_time.setText(converted_time + amPm_from_ts + " - " + converted_end_time + amPm_to_ts);
        holder.ll_repetation.setVisibility(View.GONE);
        holder.event_timezone.setText("Asia / Kolkata");
        holder.rl_actions.setVisibility(View.VISIBLE);

        String appointmentStatus = appointment_do.getAppointment_status().toLowerCase(Locale.ROOT);

        if ("completed".equalsIgnoreCase(appointmentStatus)) {
            holder.tv_appointment_status.setText("Completed");
            holder.tv_appointment_status.setBackgroundResource(R.drawable.completed_badge);
            holder.tv_appointment_status.setTextColor(mcontext.getResources().getColor(R.color.completed_text));
            holder.iv_chat.setVisibility(View.VISIBLE);
            holder.iv_video_call.setVisibility(View.GONE);
            holder.ll_rsvp.setVisibility(View.VISIBLE);
        } else if ("cancelled".equalsIgnoreCase(appointmentStatus) || "canceled".equalsIgnoreCase(appointmentStatus)) {
            holder.tv_appointment_status.setText("Cancelled");
            holder.tv_appointment_status.setBackgroundResource(R.drawable.cancelled_badge);
            holder.tv_appointment_status.setTextColor(mcontext.getResources().getColor(R.color.cancelled_text));
            holder.iv_chat.setVisibility(View.GONE);
            holder.iv_video_call.setVisibility(View.GONE);
            holder.ll_rsvp.setVisibility(View.GONE);
        } else if ("upcoming".equalsIgnoreCase(appointmentStatus) || "ongoing".equalsIgnoreCase(appointmentStatus)) {
            holder.tv_appointment_status.setText(CapitalizeFirstLetter(appointmentStatus));
            holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge);
            holder.tv_appointment_status.setTextColor(mcontext.getResources().getColor(R.color.scheduled_text));
            holder.iv_chat.setVisibility(View.VISIBLE);
            holder.iv_video_call.setVisibility(View.VISIBLE);
            holder.ll_rsvp.setVisibility(View.VISIBLE);
        } else if ("payment_pending".equalsIgnoreCase(appointmentStatus)) {
            holder.tv_appointment_status.setText("Payment Pending");
            holder.tv_appointment_status.setBackgroundResource(R.drawable.pending_badge);
            holder.tv_appointment_status.setTextColor(mcontext.getResources().getColor(R.color.pending_text));
            holder.iv_chat.setVisibility(View.GONE);
            holder.iv_video_call.setVisibility(View.GONE);
            holder.ll_rsvp.setVisibility(View.VISIBLE);
        } else {
            holder.tv_appointment_status.setText("Scheduled");
            holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge);
            holder.tv_appointment_status.setTextColor(mcontext.getResources().getColor(R.color.scheduled_text));
            holder.iv_chat.setVisibility(View.GONE);
            holder.iv_video_call.setVisibility(View.GONE);
            holder.ll_rsvp.setVisibility(View.VISIBLE);
        }

        if (AndroidUtils.isWithinOneHour(appointment_do.getAppointment_from())
                || appointmentStatus.equalsIgnoreCase("completed")) {
            AndroidUtils.ToggleButton(1, holder.iv_chat);
        } else {
            AndroidUtils.ToggleButton(0, holder.iv_chat);
        }

        if (AndroidUtils.isWithinOneMinute(appointment_do.getAppointment_from())) {
            AndroidUtils.ToggleButton(1, holder.iv_video_call);
        } else {
            AndroidUtils.ToggleButton(0, holder.iv_video_call);
        }

        holder.iv_video_call.setOnClickListener(v -> {
            String[] parts = extractDateTimeParts(
                    appointment_do.getAppointment_from(),
                    appointment_do.getAppointment_to());
            if (parts != null) {
                String url = getAVChatUrl(
                        appointment_do.getMeeting_room_id(),
                        parts[1], parts[2], parts[0],
                        appointment_do.getClient_name());
                AndroidUtils.loadAVChatView(mcontext, activity, url);
                Log.d("AVCHAT_URL", url);
            }
        });

        holder.iv_chat.setOnClickListener(v -> {
            Constants.isClient_chat       = true;
            Constants.pendingChatJid      = appointment_do.getGuid();
            Constants.pendingChatName     = appointment_do.getClient_name();
            Constants.pendingChatSource   = "appointment";
            Constants.mainActivity.navigation_items(new Chat());
        });

        // ── "Join Meeting" for appointments — show only when non-empty ───
        String meetingLink = appointment_do.getMeeting_room_id();
        if (meetingLink != null && !meetingLink.trim().isEmpty()) {
            final String finalMeetingLink = meetingLink.trim();
            if (holder.v_btn != null) holder.v_btn.setVisibility(View.VISIBLE);
            holder.tv_meeting_link.setVisibility(View.VISIBLE);
            holder.tv_meeting_link.setText("Join Meeting");
            holder.tv_meeting_link.setTextColor(mcontext.getColor(R.color.light_blue));
            holder.tv_meeting_link.setPaintFlags(
                    holder.tv_meeting_link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.tv_meeting_link.setOnClickListener(v -> {
                try {
                    String url = finalMeetingLink.startsWith("http://") || finalMeetingLink.startsWith("https://")
                            ? finalMeetingLink : "https://" + finalMeetingLink;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setPackage("com.android.chrome");
                    try {
                        v.getContext().startActivity(intent);
                    } catch (Exception ex) {
                        intent.setPackage(null);
                        v.getContext().startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e("MeetingLink", "Failed to open: " + finalMeetingLink, e);
                }
            });
        } else {
            // Hide both icon and text when no meeting link
            if (holder.v_btn != null) holder.v_btn.setVisibility(View.GONE);
            holder.tv_meeting_link.setVisibility(View.GONE);
            holder.tv_meeting_link.setText("");
            holder.tv_meeting_link.setOnClickListener(null);
        }

        holder.tv_phone_dialin.setVisibility(View.GONE);
        holder.tv_location.setVisibility(View.GONE);
        holder.dialin.setVisibility(View.GONE);
        holder.location.setVisibility(View.GONE);

        if (appointment_do.getPayment() != null) {
            PaymentModel payment = appointment_do.getPayment();
            String paymentInfo = payment.getStatus() + " - "
                    + payment.getSymbol() + payment.getAmount_paid() + " "
                    + payment.getCurrency();
            holder.event_description.setText(paymentInfo);
            holder.event_description.setVisibility(View.VISIBLE);
            holder.tv_event_description.setText("Payment Status");
            holder.tv_event_description.setVisibility(View.VISIBLE);
        } else {
            holder.event_description.setVisibility(View.GONE);
            holder.tv_event_description.setVisibility(View.GONE);
        }

        holder.bt_hide_details.setVisibility(View.GONE);
        holder.ll_view_more.setVisibility(View.GONE);

        holder.tv_yes.setOnClickListener(v -> {
            call_appointment_rsvp(appointment_do.getId(), "Yes");
            load_AppointmentYes(holder);
        });
        holder.tv_no.setOnClickListener(v -> {
            AndroidUtils.showConfirmationDialog(mcontext, "Confirmation",
                    "Are you sure you want to cancel this appointment? This action cannot be undone.",
                    new AndroidUtils.OnConfirmListener() {
                        @Override public void onSave() {
                            callCancelAppointments(appointment_do);
                            load_AppointmentNo(holder);
                        }
                        @Override public void onCancel() { }
                    });
        });

        loadAppointmentRsvpView(appointment_do, holder);
    }

    // ─────────────────────────────────────────────────────────────────────
    // API calls
    // ─────────────────────────────────────────────────────────────────────

    private void call_appointment_rsvp(String appointmentId, String rsvpValue) {
        progress_dialog = AndroidUtils.get_progress(activity);
        try {
            JSONObject postdata = new JSONObject();
            postdata.put("rsvp_status", rsvpValue);
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PATCH,
                    "v3/appointments/" + appointmentId + "/rsvp", "Appointment_rsvp", postdata.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callCancelAppointments(AppointmentModel appointmentModel) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity);
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(this, mcontext,
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/appointments/" + appointmentModel.getId() + "/cancel",
                    "Cancel_Appointments", postData.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.fillInStackTrace();
        }
    }

    private void loadAppointmentRsvpView(AppointmentModel appointment_do, MyViewHolder holder) {
        String rsvp = appointment_do.getRsvp_status().toLowerCase(Locale.ROOT);
        if (rsvp.equalsIgnoreCase("accepted"))      load_AppointmentYes(holder);
        else if (rsvp.equalsIgnoreCase("declined")) load_AppointmentNo(holder);
        else                                         load_AppointmentNothing(holder);
    }

    // ─────────────────────────────────────────────────────────────────────
    // RSVP button state helpers
    // ─────────────────────────────────────────────────────────────────────

    private void load_Nothing(MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_maybe.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void load_Yes(MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.WHITE);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_maybe.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_green_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void load_Maybe(MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_maybe.setTextColor(Color.WHITE);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_green_round_background));
    }

    private void load_No(MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.WHITE);
        holder.tv_maybe.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_green_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void load_AppointmentNothing(MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void load_AppointmentYes(MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.WHITE);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_green_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void load_AppointmentNo(MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.WHITE);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_green_round_background));
    }

    private void loadRsvpView(Events_Do events_do, MyViewHolder holder) {
        switch (events_do.getUserRsvp().toLowerCase(Locale.ROOT)) {
            case "yes":   load_Yes(holder);     break;
            case "maybe": load_Maybe(holder);   break;
            case "no":    load_No(holder);      break;
            default:      load_Nothing(holder); break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // load_Clients_view — entity-grouped, avatar + RSVP icon, no background
    // ─────────────────────────────────────────────────────────────────────

    private void load_Clients_view(Map<String, List<InviteesInternal_Model>> entityMap,
                                   LinearLayout clients) {
        for (Map.Entry<String, List<InviteesInternal_Model>> entry : entityMap.entrySet()) {
            String entityName = entry.getKey();
            List<InviteesInternal_Model> tmList = entry.getValue();

            // Entity name header (bold, no avatar)
            View headerView = LayoutInflater.from(mcontext)
                    .inflate(R.layout.event_details_notifications, null);
            TextView  tvHeader = headerView.findViewById(R.id.tv_event_notifications);
            FrameLayout fl_h   = headerView.findViewById(R.id.fl_avatar);
            if (fl_h != null) fl_h.setVisibility(View.GONE);
            tvHeader.setText(entityName);
            tvHeader.setTypeface(null, Typeface.BOLD);
            clients.addView(headerView);

            for (InviteesInternal_Model model : tmList) {
                View view = LayoutInflater.from(mcontext)
                        .inflate(R.layout.event_details_notifications, null);
                TextView    tvName  = view.findViewById(R.id.tv_event_notifications);
                ImageView   iv_rsvp = view.findViewById(R.id.iv_rsvp);
                FrameLayout fl_av   = view.findViewById(R.id.fl_avatar);

                tvName.setText(model.getName());

                if (fl_av != null) {
                    fl_av.setVisibility(View.VISIBLE);
                    TextView tvInitials = fl_av.findViewById(R.id.tv_avatar_initials);
                    if (tvInitials != null) tvInitials.setText(getInitials(model.getName()));
                }

                applyRsvpIcon(iv_rsvp, model.getRsvp());
                clients.addView(view);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // API helpers
    // ─────────────────────────────────────────────────────────────────────

    private void call_choosen_rsvp(String id, String rsvpValue) {
        progress_dialog = AndroidUtils.get_progress(activity);
        try {
            JSONObject postdata = new JSONObject();
            postdata.put("rsvp_response", rsvpValue);
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PUT,
                    "v3/event/response/" + id, "Event_rsvp", postdata.toString());
            Log.d("Event_rsvp", postdata.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void callEventDetailsWebservice(String id) {
        progress_dialog = AndroidUtils.get_progress(activity);
        JSONObject postData = new JSONObject();
        Calendar calendar = new GregorianCalendar();
        TimeZone timeZone = calendar.getTimeZone();
        int offset = timeZone.getRawOffset();
        long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
        long timezoneoffset = (-1) * hours;
        event_id = id;
        WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v3/event/" + event_id + "/" + timezoneoffset, "EVENT DETAILS", postData.toString());
    }

    private void populateEntityMap(JSONArray jsonArray,
                                   Map<String, List<InviteesInternal_Model>> entityMap) {
        try {
            for (int c = 0; c < jsonArray.length(); c++) {
                JSONObject attClientObj = jsonArray.getJSONObject(c);
                String entityName = attClientObj.getString("entityName");
                String tmName     = attClientObj.getString("tmName");
                String rsvp       = attClientObj.optString("rsvp", "");

                InviteesInternal_Model model = new InviteesInternal_Model();
                model.setId(attClientObj.optString("tmId"));
                model.setName(tmName);
                model.setRsvp(rsvp);

                inviteesInternalArrayList.add(model);

                if (!entityMap.containsKey(entityName))
                    entityMap.put(entityName, new ArrayList<>());
                Objects.requireNonNull(entityMap.get(entityName)).add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("EntityMapError", "Error while populating entity map: " + e.getMessage());
        }
    }

    private void setLoggedInUserRsvp(Events_Do eventDetails, MyViewHolder holder) {
        try {
            String rsvp_value = "";

            for (int i = 0; i < eventDetails.getInvitees_internal().length(); i++) {
                JSONObject obj = eventDetails.getInvitees_internal().getJSONObject(i);
                if (Constants.NAME.equals(obj.optString("tmName", obj.optString("name")))) {
                    rsvp_value = obj.optString("rsvp", "");
                    break;
                }
            }
            if (rsvp_value.isEmpty()) {
                for (int i = 0; i < eventDetails.getInvitees_external().length(); i++) {
                    JSONObject obj = eventDetails.getInvitees_external().getJSONObject(i);
                    if (Constants.NAME.equals(obj.optString("tmName"))) {
                        rsvp_value = obj.optString("rsvp", "");
                        break;
                    }
                }
            }
            if (rsvp_value.isEmpty() && eventDetails.getInvitees_consumer_external() != null) {
                for (int i = 0; i < eventDetails.getInvitees_consumer_external().length(); i++) {
                    JSONObject obj = eventDetails.getInvitees_consumer_external().getJSONObject(i);
                    if (Constants.NAME.equals(obj.optString("tmName"))) {
                        rsvp_value = obj.optString("rsvp", "");
                        break;
                    }
                }
            }
            eventDetails.setUserRsvp(rsvp_value);
            loadRsvpView(eventDetails, holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() { return filtered_list.size(); }

    // ─────────────────────────────────────────────────────────────────────
    // Helper — RSVP icon:  yes=green tick  no=red cancel  maybe=timer  else=grey info
    // ─────────────────────────────────────────────────────────────────────

    private void applyRsvpIcon(ImageView iv_rsvp, String rsvp) {
        if (rsvp == null || rsvp.isEmpty()) {
            iv_rsvp.setVisibility(View.VISIBLE);
            iv_rsvp.setImageDrawable(mcontext.getDrawable(R.drawable.ic_info_grey));
            return;
        }
        iv_rsvp.setVisibility(View.VISIBLE);
        switch (rsvp.toLowerCase(Locale.ROOT)) {
            case "yes":
                iv_rsvp.setImageDrawable(mcontext.getDrawable(R.drawable.ic_check_green));
                break;
            case "no":
                iv_rsvp.setImageDrawable(mcontext.getDrawable(R.drawable.icon_cancel));
                break;
            case "maybe":
                iv_rsvp.setImageDrawable(mcontext.getDrawable(R.drawable.timermaybe));
                break;
            default:
                iv_rsvp.setImageDrawable(mcontext.getDrawable(R.drawable.ic_info_grey));
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helper — 2-letter initials from display name
    // ─────────────────────────────────────────────────────────────────────

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(1, parts[0].length())).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)).toUpperCase();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helper — update RSVP summary row
    // Shows Yes and Waiting always (when > 0); hides No when zero.
    // ─────────────────────────────────────────────────────────────────────

    private void updateRsvpSummary(int yes, int no, int waiting) {
        if (my_view_holder == null) return;
        LinearLayout summaryRow = my_view_holder.itemView.findViewById(R.id.ll_rsvp_summary);
        if (summaryRow == null) return;

        TextView tvYes     = summaryRow.findViewById(R.id.tv_rsvp_yes_count);
        TextView tvNo      = summaryRow.findViewById(R.id.tv_rsvp_no_count);
        TextView tvWaiting = summaryRow.findViewById(R.id.tv_rsvp_waiting_count);

        // Yes count — always show when > 0
        if (tvYes != null) {
            if (yes > 0) {
                tvYes.setText(yes + " Yes");
                tvYes.setVisibility(View.VISIBLE);
            } else {
                tvYes.setVisibility(View.GONE);
            }
        }

        // No count — show when > 0
        if (tvNo != null) {
            if (no > 0) {
                tvNo.setText(no + " No");
                tvNo.setVisibility(View.VISIBLE);
            } else {
                tvNo.setVisibility(View.GONE);
            }
        }

        // Waiting count — always show when > 0
        if (tvWaiting != null) {
            if (waiting > 0) {
                tvWaiting.setText(waiting + " Waiting");
                tvWaiting.setVisibility(View.VISIBLE);
            } else {
                tvWaiting.setVisibility(View.GONE);
            }
        }

        // Show the summary row only when at least one count is visible
        summaryRow.setVisibility((yes > 0 || no > 0 || waiting > 0) ? View.VISIBLE : View.GONE);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ViewHolder — all original IDs preserved
    // ─────────────────────────────────────────────────────────────────────

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView events_names, tv_meeting_link, tv_phone_dialin, tv_location,
                tv_yes, tv_no, tv_maybe, event_title, event_time,
                event_description, tv_event_description, event_timezone, tv_matter_title;
        TextView event_notification, tv_appointment_status, time, meeting_link,
                dialin, location, notified_before, documents, team_members, clients, corp_clients;
        ImageView ib_delete_events;
        ImageView ib_view_events, ib_close_events, iv_video_call, iv_chat, v_btn;
        ImageButton ib_view;
        ImageButton ib_delete;
        CardView cv_client_details;
        View statusView;
        RelativeLayout rl_actions;
        TextView bt_hide_details, event_repetation;
        LinearLayout ll_notification, ll_view_more, ll_rsvp, ll_documents,
                ll_team_members, ll_clients, ll_corp_clients,
                ll_team_members_view, ll_documents_view, ll_clients_view,
                ll_corp_clients_view, ll_notifications_view, ll_repetation, ll_event_details;
        ImageView custom_spinner_cardview;
        CardView action_list_card;
        ListView sp_action;
        long lastClickTime = 0;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cv_client_details       = itemView.findViewById(R.id.cv_client_details);
            tv_appointment_status   = itemView.findViewById(R.id.tv_appointment_status);
            iv_video_call           = itemView.findViewById(R.id.iv_video_call);
            iv_chat                 = itemView.findViewById(R.id.iv_chat);
            rl_actions              = itemView.findViewById(R.id.rl_actions);
            events_names            = itemView.findViewById(R.id.events_names);
            statusView              = itemView.findViewById(R.id.statusView);
            time                    = itemView.findViewById(R.id.time);
            tv_matter_title         = itemView.findViewById(R.id.tv_matter_title);
            event_notification      = itemView.findViewById(R.id.event_notification);
            ll_event_details        = itemView.findViewById(R.id.ll_event_details);
            meeting_link            = itemView.findViewById(R.id.tv_meeting_link);
            // v_btn is the video icon next to "Join Meeting"
            v_btn                   = itemView.findViewById(R.id.v_btn);
            if (meeting_link != null) meeting_link.setText(R.string.meeting_link);
            dialin                  = itemView.findViewById(R.id.dialin);
            if (dialin != null) dialin.setText(R.string.join_by_phone);
            location                = itemView.findViewById(R.id.location);
            if (location != null) location.setText(R.string.location);
            ll_team_members_view    = itemView.findViewById(R.id.ll_team_members_view);
            ib_close_events         = itemView.findViewById(R.id.ib_close_events);
            ll_corp_clients_view    = itemView.findViewById(R.id.ll_corp_clients_view);
            ll_corp_clients         = itemView.findViewById(R.id.ll_corp_clients);
            corp_clients            = itemView.findViewById(R.id.corp_clients);
            ll_clients_view         = itemView.findViewById(R.id.ll_clients_view);
            ll_documents_view       = itemView.findViewById(R.id.ll_documents_view);
            ll_notifications_view   = itemView.findViewById(R.id.ll_notifications_view);
            documents               = itemView.findViewById(R.id.documents);
            if (documents != null) documents.setText(R.string.documents);
            team_members            = itemView.findViewById(R.id.team_members);
            if (team_members != null) team_members.setText(R.string.team_members);
            clients                 = itemView.findViewById(R.id.clients);
            if (clients != null) clients.setText(R.string.client);
            event_title             = itemView.findViewById(R.id.event_title);
            if (event_title != null) event_title.setTextColor(Color.BLACK);
            ib_view_events          = itemView.findViewById(R.id.ib_view_events);
            tv_yes                  = itemView.findViewById(R.id.tv_yes);
            if (tv_yes != null) tv_yes.setTextColor(Color.WHITE);
            tv_no                   = itemView.findViewById(R.id.tv_no);
            tv_maybe                = itemView.findViewById(R.id.tv_maybe);
            event_time              = itemView.findViewById(R.id.event_time);
            event_repetation        = itemView.findViewById(R.id.event_repetation);
            ll_repetation           = itemView.findViewById(R.id.ll_repetation);
            event_timezone          = itemView.findViewById(R.id.event_timezone);
            ib_delete_events        = itemView.findViewById(R.id.ib_delete_events);
            event_description       = itemView.findViewById(R.id.event_description);
            tv_event_description    = itemView.findViewById(R.id.tv_event_description);
            if (tv_event_description != null) {
                tv_event_description.setText(R.string.meeting_agenda);
                tv_event_description.setTextColor(mcontext.getColor(R.color.blue));
            }
            tv_meeting_link         = itemView.findViewById(R.id.tv_meeting_link);
            tv_phone_dialin         = itemView.findViewById(R.id.tv_phone_dialin);
            tv_location             = itemView.findViewById(R.id.tv_location);
            bt_hide_details         = itemView.findViewById(R.id.bt_hide_details);
            if (bt_hide_details != null) {
                bt_hide_details.setTextColor(mcontext.getColor(R.color.light_blue));
                bt_hide_details.setText(R.string.view_more);
                bt_hide_details.setTextSize(17);
            }
            ll_view_more            = itemView.findViewById(R.id.ll_view_more);
            if (ll_view_more != null) ll_view_more.setVisibility(View.GONE);
            ll_documents            = itemView.findViewById(R.id.ll_documents);
            ll_team_members         = itemView.findViewById(R.id.ll_team_members);
            ll_clients              = itemView.findViewById(R.id.ll_clients);
            ll_rsvp                 = itemView.findViewById(R.id.ll_rsvp);
            custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
            action_list_card        = itemView.findViewById(R.id.action_list_card);
            sp_action               = itemView.findViewById(R.id.action_list);
        }
    }
}