package com.digicoffer.lauditor.Dashboard;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.KPICARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.MYDAYCARDS;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Chat.ViewModels.Chat;
import com.digicoffer.lauditor.Relationships.ClientRelationship;
import com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels.ClientChatModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels.EmailModel;
import com.digicoffer.lauditor.Dashboard.DahboardModels.Item;
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
import com.digicoffer.lauditor.Groups.Groups;
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model;
import com.digicoffer.lauditor.Matter.ViewModels.Matter;
import com.digicoffer.lauditor.Members.Members;
import com.digicoffer.lauditor.Notifications.Notifications;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MyDayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ─────────────────────────────────────────────────────────────────────────
    // VIEW TYPE DESIGN
    //
    // Each adapter instance is permanently either MyDay or KPI (set at
    // construction, never changes).  This is stored as a boolean flag so
    // there is zero ambiguity about which mode we are in.
    //
    // MyDay view-types  :  item.getType()          → server sequence  0..49
    // KPI   view-types  :  position + KPI_OFFSET   → 100..149
    //
    // The two ranges never overlap, so RecyclerView's RecycledViewPool can
    // NEVER hand a KPI holder to a MyDay adapter or vice-versa.
    // ─────────────────────────────────────────────────────────────────────────
    private static final int KPI_OFFSET = 100;

    // ── Instance state ────────────────────────────────────────────────────────
    private ArrayList<Item> items;
    private String          mTag;           // kept for legacy callers / updateData
    private final boolean   instanceIsKpi;  // TRUE = this adapter is always KPI

    static Context        context;
    static Dashboard      dashboard;
    static LinearLayout   Card_Layout;

    // Filtered KPI cards (timesheets excluded)
    private ArrayList<Dashboard_Model> filteredCards = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────
    public MyDayAdapter(ArrayList<Item> items, String tag,
                        Context context1, Dashboard dashboard) {
        this.items       = items;
        this.mTag        = tag;
        context          = context1;
        MyDayAdapter.dashboard = dashboard;

        // Decide permanently at construction time whether this is a KPI adapter.
        // "MyDay_AAM" is the only MyDay tag; everything else is KPI.
        this.instanceIsKpi = !isMyDayTag(tag);

        filterCards();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public helpers
    // ─────────────────────────────────────────────────────────────────────────
    private static boolean isMyDayTag(String tag) {
        return "MyDay_AAM".equals(tag);
    }

    private boolean isMyDayMode() {
        // Use the immutable flag, NOT mTag, to avoid mid-flight tag changes
        return !instanceIsKpi;
    }

    private void filterCards() {
        filteredCards.clear();
        if (KPICARDS != null) {
            for (Dashboard_Model card : KPICARDS) {
                if (!"timesheets".equalsIgnoreCase(card.getName())) {
                    filteredCards.add(card);
                }
            }
        }
    }

    /** Called by Dashboard to push new data without recreating the adapter. */
    public void updateData(ArrayList<Item> newItems, String kpiData) {
        this.items = newItems;
        this.mTag  = kpiData;
        // NOTE: instanceIsKpi is intentionally NOT updated here –
        // the adapter's "personality" is fixed at construction.
        filterCards();
        notifyDataSetChanged();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getItemViewType
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public int getItemViewType(int position) {
        if (isMyDayMode()) {
            if (items == null || position >= items.size()) return 0;
            return items.get(position).getType();   // server sequence 0..49
        } else {
            return position + KPI_OFFSET;           // 100..149
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onCreateViewHolder
    // ─────────────────────────────────────────────────────────────────────────
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isMyDayMode()) {
            // viewType IS the sequence number from MYDAYCARDS
            String cardName = getMyDayCardName(viewType).toLowerCase(Locale.ROOT);
            int layoutRes = myDayLayout(cardName);
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layoutRes, parent, false);
            return createMyDayHolder(cardName, view);
        } else {
            // viewType = position + KPI_OFFSET  →  recover position
            int position = viewType - KPI_OFFSET;
            if (KPICARDS == null || position < 0 || position >= KPICARDS.size()) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.practice_head_tbh, parent, false);
                return new TBHholder(view);
            }
            String cardName = KPICARDS.get(position).getName().toLowerCase(Locale.ROOT);
            int layoutRes = kpiLayout(cardName);
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layoutRes, parent, false);
            return createKPIHolder(cardName, view);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onBindViewHolder
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isMyDayMode()) {
            if (items == null || items.isEmpty() || position >= items.size()) return;
            Object data     = items.get(position).getObject();
            int    seqType  = items.get(position).getType();
            String cardName = getMyDayCardName(seqType).toLowerCase(Locale.ROOT);
            bindMyDay(holder, cardName, data);
        } else {
            if (KPICARDS == null || KPICARDS.isEmpty() || position >= KPICARDS.size()) return;
            if (items == null || position >= items.size()) return;
            String cardName = KPICARDS.get(position).getName().toLowerCase(Locale.ROOT);
            Object data     = items.get(position).getObject();
            bindKpi(holder, cardName, data);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getItemCount
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public int getItemCount() {
        if (isMyDayMode()) {
            return items != null ? items.size() : 0;
        } else {
            return filteredCards != null ? filteredCards.size() : 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layout helpers
    // ─────────────────────────────────────────────────────────────────────────
    private int myDayLayout(String cardName) {
        switch (cardName) {
            case "meeting":       return R.layout.meeting_card;
            case "clientchat":    return R.layout.client_chat_card;
            case "teamchat":      return R.layout.client_chat_card;
            case "notifications": return R.layout.notification_card;
            case "storage":       return R.layout.super_user_kpi_data_storage;
            case "subscription":  return R.layout.subscription_card;
            default:              return R.layout.meeting_card;
        }
    }

    private int kpiLayout(String cardName) {
        switch (cardName) {
            case "billable":
            case "nonbillable":
            case "approxrevenue":
            case "avgbillingrate": return R.layout.practice_head_tbh;
            case "timesheets":
            case "submittedts":
            case "pendingts":      return R.layout.tm_submitted_timesheet;
            case "matters":        return R.layout.super_user_matter_kpi;
            case "newclients":
            case "newhires":       return R.layout.super_user_kpi_new_client;
            case "storage":        return R.layout.super_user_kpi_data_storage;
            case "relationships":  return R.layout.relationship_request_card;
            case "subscription":   return R.layout.subscription_card;
            case "groups":
            case "teammembers":    return R.layout.admin_no_of_groups;
            default:               return R.layout.practice_head_tbh;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ViewHolder factory helpers
    // ─────────────────────────────────────────────────────────────────────────
    private RecyclerView.ViewHolder createMyDayHolder(String cardName, View view) {
        switch (cardName) {
            case "meeting":       return new MeetingHolder(view);
            case "clientchat":    return new ClientChatHolder(view);
            case "teamchat":      return new TeamChatHolder(view);
            case "notifications": return new NotificationHolder(view);
            case "storage":       return new Data_storageHolder(view);
            case "subscription":  return new ProductSubcriptionHolder(view);
            default:              return new MeetingHolder(view);
        }
    }

    private RecyclerView.ViewHolder createKPIHolder(String cardName, View view) {
        switch (cardName) {
            case "billable":      return new TBHholder(view);
            case "nonbillable":   return new NBHholder(view);
            case "approxrevenue": return new ARholder(view);
            case "avgbillingrate":return new ABRholder(view);
            case "timesheets":
            case "submittedts":   return new SubmittedTimeSheetholder(view);
            case "matters":       return new Matterholder(view);
            case "newclients":    return new NewClientsHolder(view);
            case "relationships": return new RelationshipRequestHolder(view);
            case "newhires":      return new NewHiresHolder(view);
            case "storage":       return new Data_storageHolder(view);
            case "pendingts":     return new PendingTimeSheetholder(view);
            case "subscription":  return new ProductSubcriptionHolder(view);
            case "groups":        return new NOGHolder(view);
            case "teammembers":   return new TMholder(view);
            default:              return new TBHholder(view);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind helpers
    // ─────────────────────────────────────────────────────────────────────────
    private void bindMyDay(RecyclerView.ViewHolder holder, String cardName, Object data) {
        switch (cardName) {
            case "meeting":
                ((MeetingHolder) holder).setMeetingData((MeetingModel) data);
                break;
            case "clientchat":
                ((ClientChatHolder) holder).ClienChatData((ClientChatModel) data);
                break;
            case "teamchat":
                ((TeamChatHolder) holder).TeamchatData((TeamChatModel) data);
                break;
            case "notifications":
                ((NotificationHolder) holder).setNotificationData((NotificationModel) data);
                break;
            case "storage":
                ((Data_storageHolder) holder).setData_storageHolder((StorageModel) data);
                break;
            case "subscription":
                ((ProductSubcriptionHolder) holder)
                        .setProductSubcriptiondata((SubScriptionModel) data);
                break;
        }
    }

    private void bindKpi(RecyclerView.ViewHolder holder, String cardName, Object data) {
        switch (cardName) {
            case "billable":
                ((TBHholder) holder).TBHdata((BillableModel) data);
                break;
            case "nonbillable":
                ((NBHholder) holder).NBHdata((NonBillableModel) data);
                break;
            case "approxrevenue":
                ((ARholder) holder).ARdata((ApproxRevenueModel) data);
                break;
            case "avgbillingrate":
                ((ABRholder) holder).ABRdata((AverageBillingRateModel) data);
                break;
            case "matters":
                ((Matterholder) holder).Mattersdata((ActiveModel) data);
                break;
            case "newclients":
                ((NewClientsHolder) holder).setNewClientsdata((NewClientsModel) data);
                break;
            case "relationships":
                ((RelationshipRequestHolder) holder).RequestsData((RelationshipModel) data);
                break;
            case "newhires":
                ((NewHiresHolder) holder).setNewHiresdata((HiringModel) data);
                break;
            case "storage":
                ((Data_storageHolder) holder).setData_storageHolder((StorageModel) data);
                break;
            case "submittedts":
                ((SubmittedTimeSheetholder) holder)
                        .SubmittedTimeSheetdata((SubmittedTimesheetModel) data);
                break;
            case "pendingts":
                ((PendingTimeSheetholder) holder).PTSdata((PendingTimeSheetsModel) data);
                break;
            case "subscription":
                ((ProductSubcriptionHolder) holder)
                        .setProductSubcriptiondata((SubScriptionModel) data);
                break;
            case "groups":
                ((NOGHolder) holder).setNOGData((GroupsModel) data);
                break;
            case "teammembers":
                ((TMholder) holder).setTMdata((TeamModel) data);
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sequence lookup helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Find the card name for a given sequence number in MYDAYCARDS. */
    private String getMyDayCardName(int sequence) {
        if (MYDAYCARDS != null) {
            for (Dashboard_Model model : MYDAYCARDS) {
                if (model.getSequence() == sequence) return model.getName();
            }
        }
        return "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ViewHolders — all unchanged from original
    // ─────────────────────────────────────────────────────────────────────────

    public class MeetingHolder extends RecyclerView.ViewHolder {
        private LinearLayoutCompat time_duration;
        private final TextView tv_meeting_subject, tv_meeting_message, tv_from_ts,
                tv_to_ts, tv_time_meeting, tv_meeting_count, tv_date;

        public MeetingHolder(@NonNull View itemView) {
            super(itemView);
            Card_Navigation(itemView, new Meetings());
            tv_meeting_subject = itemView.findViewById(R.id.tv_meeting_subject);
            tv_meeting_message = itemView.findViewById(R.id.tv_subject_meeting);
            tv_from_ts         = itemView.findViewById(R.id.tv_from_ts);
            tv_to_ts           = itemView.findViewById(R.id.tv_to_ts);
            time_duration      = itemView.findViewById(R.id.time_duration);
            tv_meeting_count   = itemView.findViewById(R.id.tv_meeting_count);
            tv_time_meeting    = itemView.findViewById(R.id.tv_time_meeting);
            tv_time_meeting.setText("to");
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_from_ts.setPadding(10, 0, 0, 0);
        }

        void setMeetingData(MeetingModel meetingData) {
            tv_meeting_subject.setText("Meetings");
            tv_from_ts.setText(meetingData.getFrom_ts());
            tv_to_ts.setText(meetingData.getTo_ts());
            tv_meeting_message.setText(meetingData.getSubject());
            tv_meeting_message.setTextSize(DynamicUtils.fifteen);
            tv_meeting_count.setText(meetingData.getCount());
            tv_date.setText(meetingData.getDate());
            if (tv_from_ts.getText().toString().equals("00:00")
                    && tv_to_ts.getText().toString().equals("00:00")) {
                tv_from_ts.setVisibility(View.GONE);
                tv_time_meeting.setVisibility(View.GONE);
                tv_to_ts.setVisibility(View.GONE);
                tv_date.setVisibility(View.GONE);
                time_duration.setVisibility(View.GONE);
            } else {
                time_duration.setVisibility(View.VISIBLE);
                tv_time_meeting.setVisibility(View.VISIBLE);
                tv_date.setVisibility(View.VISIBLE);
            }
        }
    }

    static class RelationshipRequestHolder extends RecyclerView.ViewHolder {
        private TextView tv_accepted_relationships, tv_pending_relationships,
                Card_Name, tv_total_team_members, tv_not_submitted;

        public RelationshipRequestHolder(@NonNull View itemView) {
            super(itemView);
            Constants.Rel_Type = "Individual";
            Card_Navigation(itemView, new ClientRelationship());
            Card_Name = itemView.findViewById(R.id.Card_Name);
            Card_Name.setText(R.string.relationships);
            tv_total_team_members      = itemView.findViewById(R.id.tv_total_team_members);
            tv_not_submitted           = itemView.findViewById(R.id.tv_not_submitted);
            tv_accepted_relationships  = itemView.findViewById(R.id.tv_accepted_relationships);
            tv_pending_relationships   = itemView.findViewById(R.id.tv_pending_relationships);
        }

        void RequestsData(RelationshipModel requestModel) {
            tv_total_team_members.setText(R.string.accepted);
            tv_not_submitted.setText(R.string.pending);
            tv_accepted_relationships.setText(requestModel.getAccepted());
            tv_pending_relationships.setText(requestModel.getPending());
        }
    }

    static class ClientChatHolder extends RecyclerView.ViewHolder {
        private TextView tv_client_chat, clientChat_count, tv_chat_msg, tv_chat_new;

        public ClientChatHolder(@NonNull View itemView) {
            super(itemView);
            Card_Navigation(itemView, new Chat(), true);
            tv_client_chat   = itemView.findViewById(R.id.tv_client_chat);
            clientChat_count = itemView.findViewById(R.id.clientChat_count);
            tv_chat_new      = itemView.findViewById(R.id.tv_chat_new);
            tv_chat_new.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.eighteen);
            tv_chat_msg = itemView.findViewById(R.id.tv_chat_msg);
            tv_chat_msg.setText("Messages");
        }

        void ClienChatData(ClientChatModel clientChatModel) {
            tv_client_chat.setText("Client");
            clientChat_count.setText(Constants.clientChat_count);
            int count = Integer.parseInt(Constants.clientChat_count);
            tv_chat_new.setText(count == 0 ? "No New Messages" : "New Messages");
        }
    }

    static class TeamChatHolder extends RecyclerView.ViewHolder {
        private TextView  tv_team_chat, teamChat_count, tv_chat_msg, tv_chat_new;
        private ImageView Card_Image;

        public TeamChatHolder(@NonNull View itemView) {
            super(itemView);
            Card_Navigation(itemView, new Chat(), false);
            tv_team_chat = itemView.findViewById(R.id.tv_client_chat);
            Card_Image   = itemView.findViewById(R.id.Card_Image);
            Card_Image.setBackground(context.getDrawable(R.drawable.team_chat_icon));
            tv_chat_new = itemView.findViewById(R.id.tv_chat_new);
            tv_chat_new.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.eighteen);
            tv_chat_msg = itemView.findViewById(R.id.tv_chat_msg);
            tv_chat_msg.setText("Messages");
            teamChat_count = itemView.findViewById(R.id.clientChat_count);
        }

        void TeamchatData(TeamChatModel chatModel) {
            tv_team_chat.setText("Team");
            teamChat_count.setText(Constants.teamChat_count);
            int count = Integer.parseInt(Constants.teamChat_count);
            tv_chat_new.setText(count == 0 ? "No New Messages" : "New Messages");
        }
    }

    static class EmailHolder extends RecyclerView.ViewHolder {
        TextView tv_email_count, tv_email_time, tv_email_subject, tv_email_message;

        public EmailHolder(@NonNull View itemView) {
            super(itemView);
            tv_email_count   = itemView.findViewById(R.id.tv_email_count);
            tv_email_message = itemView.findViewById(R.id.tv_email_message);
            tv_email_subject = itemView.findViewById(R.id.tv_email_subject);
            tv_email_time    = itemView.findViewById(R.id.tv_email_time);
        }

        void EmailData(EmailModel emailModel) {
            tv_email_time.setText(emailModel.getEmail_time());
            tv_email_count.setText(emailModel.getCount());
            tv_email_subject.setText(emailModel.getEmail_subject());
            tv_email_message.setText(emailModel.getEmail_message());
        }
    }

    static class TBHholder extends RecyclerView.ViewHolder {
        private final TextView tv_billing_hours, tv_bh_percentage, tv_tbh, tv_email_message;

        public TBHholder(@NonNull View itemView) {
            super(itemView);
            if (!Constants.ROLE.equals("TM")) {
                Card_Navigation(itemView, new TimeSheets(), "Agts", false);
            }
            tv_tbh           = itemView.findViewById(R.id.tv_tbh);
            tv_billing_hours = itemView.findViewById(R.id.tv_bhours);
            tv_bh_percentage = itemView.findViewById(R.id.tv_bh_percentage);
            tv_email_message = itemView.findViewById(R.id.tv_email_message);
        }

        void TBHdata(BillableModel billableModel) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getShortMonths();
            String yearRange = months[0] + " " + year + " - " + months[11] + " " + year;
            tv_email_message.setText(yearRange);
            tv_tbh.setText(R.string.total_billable_hours);
            tv_tbh.setPadding(4, 4, 4, 4);
            tv_bh_percentage.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
            tv_bh_percentage.setTextSize(DynamicUtils.twentyFive);
            tv_billing_hours.setText(billableModel.getBillableHours());
            tv_billing_hours.setMaxLines(1);
            tv_bh_percentage.setText(billableModel.getBillablePercentage() + "%");
        }
    }

    static class NBHholder extends RecyclerView.ViewHolder {
        private final TextView  tv_non_billing_hours, tv_nbh_percentage, tv_nbh, tv_email_message;
        private final ImageView Card_Image;

        public NBHholder(@NonNull View itemView) {
            super(itemView);
            if (!Constants.ROLE.equals("TM")) {
                Card_Navigation(itemView, new TimeSheets(), "Agts", false);
            }
            tv_nbh               = itemView.findViewById(R.id.tv_tbh);
            tv_non_billing_hours = itemView.findViewById(R.id.tv_bhours);
            tv_nbh_percentage    = itemView.findViewById(R.id.tv_bh_percentage);
            tv_email_message     = itemView.findViewById(R.id.tv_email_message);
            Card_Image           = itemView.findViewById(R.id.Card_Image);
        }

        void NBHdata(NonBillableModel nonBillableModel) {
            Card_Image.setBackground(context.getDrawable(R.drawable.nbh));
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getShortMonths();
            String yearRange = months[0] + " " + year + " - " + months[11] + " " + year;
            tv_email_message.setText(yearRange);
            tv_nbh.setText(R.string.non_billable_hours);
            tv_nbh.setPadding(4, 4, 4, 4);
            tv_nbh_percentage.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
            tv_nbh_percentage.setTextSize(DynamicUtils.twentyFive);
            tv_non_billing_hours.setText(nonBillableModel.getNonBillableHours());
            tv_non_billing_hours.setMaxLines(1);
            tv_nbh_percentage.setText(nonBillableModel.getNonBillablePercentage() + "%");
        }
    }

    static class ARholder extends RecyclerView.ViewHolder {
        private final TextView  tv_revenue_value, tv_nbh_percentage, tv_nbh, tv_email_message;
        private final ImageView Card_Image;

        public ARholder(@NonNull View itemView) {
            super(itemView);
            Card_Image        = itemView.findViewById(R.id.Card_Image);
            Card_Layout       = itemView.findViewById(R.id.Card_Layout);
            tv_nbh            = itemView.findViewById(R.id.tv_tbh);
            tv_revenue_value  = itemView.findViewById(R.id.tv_bhours);
            tv_nbh_percentage = itemView.findViewById(R.id.tv_bh_percentage);
            tv_nbh_percentage.setVisibility(View.GONE);
            tv_email_message = itemView.findViewById(R.id.tv_email_message);
        }

        void ARdata(final ApproxRevenueModel approxRevenueModel) {
            Card_Image.setBackground(context.getDrawable(R.drawable.approximate_revenue));
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getShortMonths();
            tv_email_message.setText(months[0] + " " + year + " - " + months[11] + " " + year);
            tv_nbh.setText(R.string.approximate_revenue);
            tv_revenue_value.setText(
                    approxRevenueModel.getCurrencySymbol() + approxRevenueModel.getApproxRevenue());
            tv_revenue_value.setMaxLines(1);
            Card_Layout.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.setTooltipText("Approximate revenue is calculated by multiplying the"
                            + " total billing hours by the firm's average billing rate");
                } else {
                    Toast.makeText(context,
                            "Approximate revenue is calculated by multiplying the total"
                                    + " billing hours by the firm's average billing rate",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    static class ABRholder extends RecyclerView.ViewHolder {
        private final TextView  tv_billing_rate, tv_nbh_percentage, tv_nbh, tv_email_message;
        private final ImageView Card_Image;
        private       LinearLayout percentage_layout;

        public ABRholder(@NonNull View itemView) {
            super(itemView);
            Card_Image        = itemView.findViewById(R.id.Card_Image);
            tv_nbh            = itemView.findViewById(R.id.tv_tbh);
            tv_billing_rate   = itemView.findViewById(R.id.tv_bhours);
            Card_Layout       = itemView.findViewById(R.id.Card_Layout);
            tv_nbh_percentage = itemView.findViewById(R.id.tv_bh_percentage);
            tv_nbh_percentage.setVisibility(View.GONE);
            percentage_layout = itemView.findViewById(R.id.percentage_layout);
            percentage_layout.setGravity(Gravity.START);
            tv_email_message = itemView.findViewById(R.id.tv_email_message);
        }

        void ABRdata(AverageBillingRateModel averageBillingRateModel) {
            Card_Image.setBackground(context.getDrawable(R.drawable.average_billing));
            tv_email_message.setText(R.string.per_hour);
            tv_nbh.setText(R.string.average_billing_rate);
            tv_billing_rate.setText(averageBillingRateModel.getCurrencySymbol()
                    + averageBillingRateModel.getAverageBillingRate());
            tv_billing_rate.setMaxLines(1);
            Card_Layout.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.setTooltipText("It is calculated by adding the hourly rates of all"
                            + " lawyers and dividing the total by the number of lawyers");
                } else {
                    Toast.makeText(context,
                            "It is calculated by adding the hourly rates of all lawyers"
                                    + " and dividing the total by the number of lawyers",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    static class Matterholder extends RecyclerView.ViewHolder {
        TextView tv_closed_members1, tv_total_matters_count, tv_active_matters_count,
                tv_total_legal_matter_count, tv_active_legal_matter_count,
                tv_total_general_matter_count, tv_active_general_matter_count,
                tv_active, tv_closed_members;

        public Matterholder(@NonNull View itemView) {
            super(itemView);
            Constants.Matter_CreateOrViewDetails = "Create";
            Constants.create_matter = true;
            View Active_View = itemView.findViewById(R.id.ActiveMatterLayout);
            tv_active        = Active_View.findViewById(R.id.tv_closed);
            tv_active.setText("Matters");
            tv_closed_members  = itemView.findViewById(R.id.tv_closed_members);
            tv_closed_members.setText("Matters");
            tv_closed_members1 = Active_View.findViewById(R.id.tv_closed_members);
            tv_closed_members1.setText("Matters");
            tv_active_matters_count = Active_View.findViewById(R.id.tv_total_matters_count);
            tv_active_matters_count.setVisibility(View.GONE);
            tv_active_legal_matter_count = Active_View.findViewById(R.id.tv_total_legal_matter_count);
            tv_active_legal_matter_count.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.twenty);
            tv_active_general_matter_count = Active_View.findViewById(R.id.tv_total_general_matter_count);
            tv_active_general_matter_count.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.twenty);
            tv_total_matters_count = itemView.findViewById(R.id.tv_total_matters_count);
            tv_total_matters_count.setVisibility(View.GONE);
            tv_total_legal_matter_count = itemView.findViewById(R.id.tv_total_legal_matter_count);
            tv_total_legal_matter_count.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.twenty);
            tv_total_general_matter_count = itemView.findViewById(R.id.tv_total_general_matter_count);
            tv_total_general_matter_count.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.twenty);
            tv_total_general_matter_count.setOnClickListener(v -> openGeneral());
            tv_active_general_matter_count.setOnClickListener(v -> openGeneral());
            tv_total_legal_matter_count.setOnClickListener(v -> openLegal());
            tv_active_legal_matter_count.setOnClickListener(v -> openLegal());
        }

        void Mattersdata(ActiveModel activeModel) {
            tv_active.setText("Active");
            tv_closed_members.setText("Matters");
            tv_total_matters_count.setText(String.valueOf(activeModel.getTotal_closed()));
            tv_active_matters_count.setText(String.valueOf(activeModel.getTotal_active()));
            setColoredText(tv_total_legal_matter_count,
                    activeModel.getClosed_legal_count(), activeModel.getClosed_legal_type());
            setColoredText(tv_total_general_matter_count,
                    activeModel.getClosed_general_count(), activeModel.getClosed_general_type());
            setColoredText(tv_active_legal_matter_count,
                    activeModel.getLegal_count(), activeModel.getLegal_type());
            setColoredText(tv_active_general_matter_count,
                    activeModel.getGeneral_count(), activeModel.getGeneral_type());
        }

        private void setColoredText(TextView tv, int count, String type) {
            String text = count + " " + type;
            SpannableString span = new SpannableString(text);
            span.setSpan(new ForegroundColorSpan(
                            ContextCompat.getColor(context, R.color.green_count_color)),
                    0, String.valueOf(count).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(span);
        }

        private void openGeneral() {
            Constants.MATTER_TYPE    = "General";
            Constants.is_CreateMatter = false;
            Constants.isCreate        = false;
            dashboard.Page_Navigation(new Matter());
        }

        private void openLegal() {
            Constants.MATTER_TYPE    = "Legal";
            Constants.is_CreateMatter = false;
            Constants.isCreate        = false;
            dashboard.Page_Navigation(new Matter());
        }
    }

    static class NewClientsHolder extends RecyclerView.ViewHolder {
        private TextView tv_newclients_legal_count, tv_newclients_civil_count,
                tv_first_client_type, tv_second_client_type, tv_NewClients;

        public NewClientsHolder(@NonNull View itemView) {
            super(itemView);
            Constants.Rel_Type = "Individual";
            Card_Navigation(itemView, new ClientRelationship());
            tv_NewClients             = itemView.findViewById(R.id.tv_NewClients);
            tv_newclients_legal_count = itemView.findViewById(R.id.tv_newclients_legal_count);
            tv_newclients_civil_count = itemView.findViewById(R.id.tv_newclients_civil_count);
            tv_first_client_type      = itemView.findViewById(R.id.tv_first_client_type);
            tv_second_client_type     = itemView.findViewById(R.id.tv_second_client_type);
        }

        void setNewClientsdata(NewClientsModel newClientsModel) {
            tv_NewClients.setText(R.string.new_clients);
            tv_newclients_legal_count.setText(String.valueOf(newClientsModel.getCorporateCount()));
            tv_first_client_type.setText(newClientsModel.getCorporateType());
            Constants.hires_group = newClientsModel.getCorporateType();
            tv_newclients_civil_count.setText(String.valueOf(newClientsModel.getCriminalCount()));
            tv_second_client_type.setText(newClientsModel.getCriminalType());
        }
    }

    static class NewHiresHolder extends RecyclerView.ViewHolder {
        private final TextView  tv_corporate_count, tv_criminal_count,
                tv_first_type, tv_second_type, tv_NewClients;
        private final ImageView Card_Image;

        public NewHiresHolder(@NonNull View itemView) {
            super(itemView);
            if (!"solo".equals(Constants.CATEGORY)) {
                Card_Navigation(itemView, new Groups());
            }
            Card_Image        = itemView.findViewById(R.id.Card_Image);
            tv_NewClients     = itemView.findViewById(R.id.tv_NewClients);
            tv_corporate_count = itemView.findViewById(R.id.tv_newclients_legal_count);
            tv_criminal_count  = itemView.findViewById(R.id.tv_newclients_civil_count);
            tv_first_type      = itemView.findViewById(R.id.tv_first_client_type);
            tv_second_type     = itemView.findViewById(R.id.tv_second_client_type);
        }

        void setNewHiresdata(HiringModel hiringModel) {
            Card_Image.setBackground(context.getDrawable(R.drawable.new_hires));
            tv_NewClients.setText(R.string.new_hires);
            tv_corporate_count.setText(String.valueOf(hiringModel.getCorporateCount()));
            tv_first_type.setText(hiringModel.getCorporateType());
            Constants.hires_group = hiringModel.getCorporateType();
            tv_criminal_count.setText(String.valueOf(hiringModel.getCriminalCount()));
            tv_second_type.setText(hiringModel.getCriminalType());
        }
    }

    static class Data_storageHolder extends RecyclerView.ViewHolder {
        private final TextView tv_total_data_storage, tv_data_storage, tv_balance_storage,
                tv_tot_gb, tv_total, tv_ds, tv_bs, tv_ds_gb, tv_bs_gb;

        public Data_storageHolder(@NonNull View itemView) {
            super(itemView);
            tv_total = itemView.findViewById(R.id.tv_total);
            View tv_ds_layout = itemView.findViewById(R.id.tv_ds_layout);
            tv_data_storage = tv_ds_layout.findViewById(R.id.tv_total_data_storage);
            tv_ds           = tv_ds_layout.findViewById(R.id.tv_total);
            tv_ds_gb        = tv_ds_layout.findViewById(R.id.tv_tot_gb);
            tv_ds_gb.setTextSize(10);
            View tv_bs_layout = itemView.findViewById(R.id.tv_bs_layout);
            tv_balance_storage = tv_bs_layout.findViewById(R.id.tv_total_data_storage);
            tv_bs              = tv_bs_layout.findViewById(R.id.tv_total);
            tv_bs_gb           = tv_bs_layout.findViewById(R.id.tv_tot_gb);
            tv_bs_gb.setTextSize(10);
            tv_tot_gb              = itemView.findViewById(R.id.tv_tot_gb);
            tv_tot_gb.setTextSize(10);
            tv_total_data_storage = itemView.findViewById(R.id.tv_total_data_storage);
        }

        void setData_storageHolder(StorageModel storageModel) {
            tv_tot_gb.setText(R.string.gb);
            tv_ds_gb.setText(R.string.gb);
            tv_bs_gb.setText(R.string.gb);
            tv_total.setText(R.string.data_storage);
            tv_ds.setText(R.string.used_storage);
            tv_bs.setText(R.string.balance_storage);
            tv_total_data_storage.setText(String.valueOf(storageModel.getTotalStorage()));
            tv_data_storage.setText(String.valueOf(storageModel.getCurrentStorage()));
            tv_balance_storage.setText(String.valueOf(storageModel.getBalanceStorage()));
        }
    }

    static class SubmittedTimeSheetholder extends RecyclerView.ViewHolder {
        private TextView tv_from_date, tv_to_date, Card_Name;

        public SubmittedTimeSheetholder(@NonNull View itemView) {
            super(itemView);
            Card_Navigation(itemView, new TimeSheets(), "Myts", true);
            Card_Name = itemView.findViewById(R.id.Card_Name);
            Card_Name.setText(R.string.submitted_time_sheet);
            tv_from_date = itemView.findViewById(R.id.tv_from_date);
            tv_to_date   = itemView.findViewById(R.id.tv_to_date);
            tv_from_date.setTextSize(DynamicUtils.eighteen);
            tv_to_date.setTextSize(DynamicUtils.eighteen);
        }

        void SubmittedTimeSheetdata(SubmittedTimesheetModel model) {
            if (model.getStart_date().isEmpty() && model.getEnd_date().isEmpty()) {
                tv_from_date.setText(R.string.no_timesheets);
                tv_from_date.setVisibility(View.VISIBLE);
                tv_to_date.setVisibility(View.GONE);
            } else {
                tv_from_date.setText(model.getStart_date());
                tv_to_date.setText("TO " + model.getEnd_date());
            }
        }
    }

    static class PendingTimeSheetholder extends RecyclerView.ViewHolder {
        private TextView  tv_from_date, tv_to_date, Card_Name;
        ImageView Card_Image;

        public PendingTimeSheetholder(@NonNull View itemView) {
            super(itemView);
            Card_Navigation(itemView, new TimeSheets(), "Myts", false);
            Card_Image = itemView.findViewById(R.id.Card_Image);
            Card_Image.setBackground(context.getDrawable(R.drawable.pendingts));
            Card_Name = itemView.findViewById(R.id.Card_Name);
            Card_Name.setText(R.string.pending_time_sheet);
            tv_from_date = itemView.findViewById(R.id.tv_from_date);
            tv_to_date   = itemView.findViewById(R.id.tv_to_date);
            tv_from_date.setTextSize(DynamicUtils.eighteen);
            tv_to_date.setTextSize(DynamicUtils.eighteen);
        }

        void PTSdata(PendingTimeSheetsModel model) {
            if (model.getStart_date().isEmpty() && model.getEnd_date().isEmpty()) {
                tv_from_date.setText(R.string.no_timesheets);
                tv_from_date.setVisibility(View.VISIBLE);
                tv_to_date.setVisibility(View.GONE);
            } else {
                tv_from_date.setText(model.getStart_date());
                tv_to_date.setText("TO " + model.getEnd_date());
                tv_from_date.setVisibility(View.VISIBLE);
                tv_to_date.setVisibility(View.VISIBLE);
            }
        }
    }

    static class ProductSubcriptionHolder extends RecyclerView.ViewHolder {
        private TextView tv_ps_year, tv_product_subscription, tv_message, tv_pending_txt;
        Button        btn_pay_now;
        LinearLayout  subscription_card, rightLayout;

        public ProductSubcriptionHolder(@NonNull View itemView) {
            super(itemView);
            subscription_card      = itemView.findViewById(R.id.subscription_card);
            tv_ps_year             = itemView.findViewById(R.id.tv_ps_year);
            tv_product_subscription = itemView.findViewById(R.id.tv_product_subscription);
            tv_message             = itemView.findViewById(R.id.tv_message);
            tv_pending_txt         = itemView.findViewById(R.id.tv_pending_txt);
            rightLayout            = itemView.findViewById(R.id.rightLayout);
            btn_pay_now            = itemView.findViewById(R.id.btn_pay_now);
        }

        void setProductSubcriptiondata(SubScriptionModel subScriptionModel) {
            tv_ps_year.setText(subScriptionModel.getMonth());
            tv_product_subscription.setText(R.string.product_subscription);
            btn_pay_now.setText(R.string.pay_now);
            tv_message.setText(subScriptionModel.getMessage());
            tv_ps_year.setTextSize(DynamicUtils.twentyFive);
            tv_message.setMaxLines(2);
            if (subScriptionModel.isActive_pay_button()) {
                btn_pay_now.setVisibility(View.VISIBLE);
                tv_pending_txt.setVisibility(View.GONE);
                tv_pending_txt.setText(R.string.pending_payment);
                tv_message.setVisibility(View.VISIBLE);
            } else {
                tv_pending_txt.setVisibility(View.VISIBLE);
                btn_pay_now.setVisibility(View.GONE);
                tv_message.setVisibility(View.GONE);
            }
            if (tv_ps_year.getText().toString().isEmpty()) {
                tv_pending_txt.setText(R.string.subscription_expired);
                rightLayout.setVisibility(View.GONE);
            } else {
                tv_pending_txt.setText(R.string.valid_till);
                rightLayout.setVisibility(View.VISIBLE);
            }
            btn_pay_now.setOnClickListener(v ->
                    dashboard.launchPaySubscriptionPage(
                            subScriptionModel.getEmail(),
                            subScriptionModel.getUser_allowed()));
        }
    }

    static class NOGHolder extends RecyclerView.ViewHolder {
        private TextView tv_nog_count, tv_bhours, Card_Name;

        public NOGHolder(@NonNull View itemView) {
            super(itemView);
            if (!Constants.ROLE.equals("TM")) {
                Card_Navigation(itemView, new Groups());
            }
            Card_Name    = itemView.findViewById(R.id.Card_Name);
            tv_bhours    = itemView.findViewById(R.id.tv_bhours);
            tv_nog_count = itemView.findViewById(R.id.tv_nog_count);
        }

        void setNOGData(GroupsModel groupsModel) {
            tv_nog_count.setText(String.valueOf(groupsModel.getTotalGroups()));
            Card_Name.setText(R.string.total);
            tv_bhours.setText(R.string.number_of_groups);
        }
    }

    static class TMholder extends RecyclerView.ViewHolder {
        private TextView  tv_nog_count, tv_bhours, Card_Name;
        ImageView Card_Image;

        public TMholder(@NonNull View itemView) {
            super(itemView);
            if (!"solo".equals(Constants.CATEGORY)) {
                Card_Navigation(itemView, new Members());
            }
            Card_Name    = itemView.findViewById(R.id.Card_Name);
            tv_bhours    = itemView.findViewById(R.id.tv_bhours);
            tv_nog_count = itemView.findViewById(R.id.tv_nog_count);
            Card_Image   = itemView.findViewById(R.id.Card_Image);
        }

        void setTMdata(TeamModel teamModel) {
            Card_Image.setBackground(context.getDrawable(R.drawable.team_dashboard_icon));
            Card_Name.setText(R.string.total);
            tv_bhours.setText(R.string.team_members);
            tv_nog_count.setText(String.valueOf(teamModel.getTotalTms()));
        }
    }

    private class NotificationHolder extends RecyclerView.ViewHolder {
        TextView tv_message, tv_date, tv_notification, tv_total_notification_count;

        public NotificationHolder(View itemView) {
            super(itemView);
            Card_Navigation(itemView, new Notifications());
            tv_message = itemView.findViewById(R.id.tv_dash_notification);
            tv_message.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen);
            tv_total_notification_count = itemView.findViewById(R.id.tv_total_notification_count);
            tv_notification = itemView.findViewById(R.id.tv_notification);
            tv_notification.setText(R.string.notification);
            tv_date = itemView.findViewById(R.id.tv_notif_date);
            tv_message.setGravity(Gravity.START);
            tv_date.setTextSize(DynamicUtils.twenty);
        }

        void setNotificationData(NotificationModel notificationModel) {
            tv_message.setText(notificationModel.getMessage());
            tv_total_notification_count.setText(Constants.notifyBadge.getText());
            if (notificationModel.getTimestamp().equals("00:00")) {
                tv_date.setText("");
            } else {
                tv_date.setText(notificationModel.getDate());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation helpers — static, unchanged
    // ─────────────────────────────────────────────────────────────────────────

    public static void Card_Navigation(View itemView, Fragment fragment) {
        Card_Layout = itemView.findViewById(R.id.Card_Layout);
        Card_Layout.setOnClickListener(v -> {
            Constants.isCreate = false;
            dashboard.Page_Navigation(fragment);
        });
    }

    public static void Card_Navigation(View itemView, Fragment fragment, boolean ischecked) {
        Card_Layout = itemView.findViewById(R.id.Card_Layout);
        Card_Layout.setOnClickListener(v -> {
            Constants.isClient_chat = ischecked;
            dashboard.Page_Navigation(fragment);
        });
    }

    public static void Card_Navigation(View itemView, Fragment fragment,
                                       String timesheet, boolean ischecked) {
        Card_Layout = itemView.findViewById(R.id.Card_Layout);
        Card_Layout.setOnClickListener(v -> {
            Constants.ts_card_clicked = true;
            Constants.Timesheet_Card  = timesheet;
            Constants.is_ts_submitted = ischecked;
            dashboard.Page_Navigation(fragment);
        });
    }
}