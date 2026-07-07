package com.digicoffer.lauditor.Appointments.Adapters;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.CapitalizeFirstLetter;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.extractDateTimeParts;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getAVChatUrl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Chat.ViewModels.Chat;
import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.MyViewHolder> implements Filterable {

    ArrayList<AppointmentModel> itemsArrayList;
    ArrayList<AppointmentModel> list_item;
    Context context;
    InterfaceListener eventListener;
    private int expandedPosition = -1;
    private String highlightIds = "";
    Activity activity;

    // ─── RecyclerView reference ───────────────────────────────────────────────
    private RecyclerView recyclerView;

    public AppointmentsAdapter(ArrayList<AppointmentModel> itemsArrayList,
                               Context context,
                               InterfaceListener eventListener,
                               String highlightIds,
                               Activity activity) {
        this.itemsArrayList = itemsArrayList;
        this.list_item      = new ArrayList<>(itemsArrayList);
        this.context        = context;
        this.eventListener  = eventListener;
        this.highlightIds   = highlightIds;
        this.activity       = activity;
    }

    /**
     * Call this from the Fragment/Activity after setting the adapter so that
     * clipChildren=false is propagated up the view tree, allowing the
     * action_list_card to draw outside its RecyclerView item boundary and
     * overlap the cards below.
     */
    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
        // Allow children (the action_list_card) to draw outside the RecyclerView bounds
        rv.setClipChildren(false);
        rv.setClipToPadding(false);
        // Also propagate to the RecyclerView's parent if it is a ViewGroup
        if (rv.getParent() instanceof ViewGroup) {
            ((ViewGroup) rv.getParent()).setClipChildren(false);
            ((ViewGroup) rv.getParent()).setClipToPadding(false);
        }
    }

    // =========================================================================
    //  Interface
    // =========================================================================

    public interface InterfaceListener {
        /**
         * Fired when the user taps the card or selects "History" from the menu.
         */
        void ViewAppointmentHistory(AppointmentModel appointmentModel,
                                    ArrayList<AppointmentModel> itemsArrayList);

        /**
         * Fired when the user selects "Cancel" from the three-dot menu.
         * Shown for upcoming / ongoing only.
         * Enabled only when NOT within 2 hours of start time.
         * API: DELETE v3/appointments/<id>/cancel
         */
        void CancelAppointment(AppointmentModel appointmentModel);

        /**
         * Fired when the user selects "Delete" from the three-dot menu.
         * Only available for completed or cancelled/canceled appointments.
         * API: DELETE v3/appointments/<id>/delete
         */
        void DeleteAppointment(AppointmentModel appointmentModel);
    }

    // =========================================================================
    //  Safe notify — posts to RecyclerView message queue to avoid
    //  "Cannot call notifyItemChanged inside onBindViewHolder" crashes
    // =========================================================================

    private void safeNotify(int position) {
        Runnable r = () -> {
            if (position >= 0 && position < getItemCount()) {
                notifyItemChanged(position);
            }
        };
        if (recyclerView != null) recyclerView.post(r);
        else new Handler(Looper.getMainLooper()).post(r);
    }

    // =========================================================================
    //  RecyclerView lifecycle
    // =========================================================================

    @NonNull
    @Override
    public AppointmentsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                               int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appointment_card_listing, parent, false);
        return new AppointmentsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentsAdapter.MyViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
        AppointmentModel appointmentModel = itemsArrayList.get(position);

        try {
            // ── Client Name ──────────────────────────────────────────────────────
            holder.tv_client_name.setText(appointmentModel.getClient_name());

            // ── Date and Time ────────────────────────────────────────────────────
            String formattedDateTime = formatAppointmentDateTime(
                    appointmentModel.getAppointment_from(),
                    appointmentModel.getAppointment_to()
            );
            holder.tv_appointment_datetime.setText(formattedDateTime);

            // ── Payment Status ───────────────────────────────────────────────────
            String paymentStatus = appointmentModel.getPayment().getStatus();
            String amountPaid    = appointmentModel.getPayment().getAmount_paid();
            String symbol        = appointmentModel.getPayment().getSymbol();

            if ("paid".equalsIgnoreCase(paymentStatus)) {
                holder.tv_payment_status.setText("Paid - " + symbol + amountPaid);
                holder.tv_payment_status.setTextColor(
                        context.getResources().getColor(R.color.payment_paid_color));
            } else {
                holder.tv_payment_status.setText("Pending - " + symbol + "0");
                holder.tv_payment_status.setTextColor(
                        context.getResources().getColor(R.color.payment_pending_color));
            }

            // ── Appointment Status ───────────────────────────────────────────────
            String appointmentStatus = appointmentModel.getAppointment_status()
                    .toLowerCase(Locale.ROOT);

            // Chat icon: enabled within 1 hour OR completed
            if (AndroidUtils.isWithinOneHour(appointmentModel.getAppointment_from())
                    || appointmentStatus.equalsIgnoreCase("completed")) {
                AndroidUtils.ToggleButton(1, holder.iv_chat);
            } else {
                AndroidUtils.ToggleButton(0, holder.iv_chat);
            }

            // Video call icon: enabled within 1 minute
            if (AndroidUtils.isWithinOneMinute(appointmentModel.getAppointment_from())) {
                AndroidUtils.ToggleButton(1, holder.iv_video_call);
            } else {
                AndroidUtils.ToggleButton(0, holder.iv_video_call);
            }

            // ── Status badge + icon visibility per status ────────────────────────
            if ("completed".equalsIgnoreCase(appointmentStatus)) {
                holder.tv_appointment_status.setText("Completed");
                holder.tv_appointment_status.setBackgroundResource(R.drawable.completed_badge);
                holder.tv_appointment_status.setTextColor(
                        context.getResources().getColor(R.color.completed_text));

                holder.iv_chat.setVisibility(View.VISIBLE);
                holder.iv_video_call.setVisibility(View.GONE);

            } else if ("cancelled".equalsIgnoreCase(appointmentStatus)
                    || "canceled".equalsIgnoreCase(appointmentStatus)) {
                holder.tv_appointment_status.setText("Cancelled");
                holder.tv_appointment_status.setBackgroundResource(R.drawable.cancelled_badge);
                holder.tv_appointment_status.setTextColor(
                        context.getResources().getColor(R.color.cancelled_text));

                holder.iv_chat.setVisibility(View.GONE);
                holder.iv_video_call.setVisibility(View.GONE);

            } else if ("upcoming".equalsIgnoreCase(appointmentStatus)
                    || "ongoing".equalsIgnoreCase(appointmentStatus)) {
                holder.tv_appointment_status.setText(CapitalizeFirstLetter(appointmentStatus));
                holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge);
                holder.tv_appointment_status.setTextColor(
                        context.getResources().getColor(R.color.blue_dark));

                holder.iv_chat.setVisibility(View.VISIBLE);
                holder.iv_video_call.setVisibility(View.VISIBLE);

            } else if ("payment_pending".equalsIgnoreCase(appointmentStatus)) {
                holder.tv_appointment_status.setText("Payment Pending");
                holder.tv_appointment_status.setBackgroundResource(R.drawable.pending_badge);
                holder.tv_appointment_status.setTextColor(
                        context.getResources().getColor(R.color.pending_text));

                holder.iv_chat.setVisibility(View.GONE);
                holder.iv_video_call.setVisibility(View.GONE);

            } else {
                // Default / scheduled
                holder.tv_appointment_status.setText("Scheduled");
                holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge);
                holder.tv_appointment_status.setTextColor(
                        context.getResources().getColor(R.color.scheduled_text));

                holder.iv_chat.setVisibility(View.GONE);
                holder.iv_video_call.setVisibility(View.GONE);
            }

            // ── Profile image ────────────────────────────────────────────────────
            AndroidUtils.loadProfileImage(context,
                    appointmentModel.getClient_profile_pic(),
                    holder.iv_profile,
                    holder.person_icon,
                    appointmentModel.getClient_name());

            // ── Video call click ─────────────────────────────────────────────────
            holder.iv_video_call.setOnClickListener(view -> {
                String[] parts = extractDateTimeParts(
                        appointmentModel.getAppointment_from(),
                        appointmentModel.getAppointment_to()
                );
                if (parts != null) {
                    String date     = parts[0];
                    String fromTime = parts[1];
                    String toTime   = parts[2];
                    String url = getAVChatUrl(
                            appointmentModel.getMeeting_room_id(),
                            fromTime,
                            toTime,
                            date,
                            appointmentModel.getClient_name()
                    );
                    AndroidUtils.loadAVChatView(context, activity, url);
                    Log.d("AVCHAT_URL", url);
                }
            });

            // ── Chat click ───────────────────────────────────────────────────────
            holder.iv_chat.setOnClickListener(v -> {
                Constants.isClient_chat    = true;
                Constants.pendingChatJid   = appointmentModel.getGuid();
                Constants.pendingChatName  = appointmentModel.getClient_name();
                Constants.pendingChatSource = "appointment";
                Constants.mainActivity.navigation_items(new Chat());
            });

            // ── Card click → history ─────────────────────────────────────────────
            holder.appointment_card.setOnClickListener(view ->
                    eventListener.ViewAppointmentHistory(appointmentModel, itemsArrayList));

            // ── Three-dot action menu ────────────────────────────────────────────
            bindActionMenu(holder, appointmentModel, position, appointmentStatus);

            // ── Highlight ────────────────────────────────────────────────────────
            applyHighlight(holder, appointmentModel);

        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), context);
            Log.d("Appointment Details", Objects.requireNonNull(e.getMessage()));
            e.fillInStackTrace();
        }
    }

    // =========================================================================
    //  Three-dot action menu
    // =========================================================================

    /**
     * Builds the three-dot (⋮) action menu dynamically per card based on status.
     *
     * The action_list_card lives inside a FrameLayout root in the item XML.
     * Because FrameLayout does NOT grow to fit a GONE child, toggling the card
     * to VISIBLE does not push the next RecyclerView item downward.
     * Combined with clipChildren=false on the RecyclerView (set in setRecyclerView),
     * the card visually floats over the cards below — identical to the Document View.
     *
     * ┌──────────┬──────────────────────────────────────────────────────────────────┐
     * │ Action   │ Behaviour                                                        │
     * ├──────────┼──────────────────────────────────────────────────────────────────┤
     * │ History  │ Always shown, always enabled.                                    │
     * ├──────────┼──────────────────────────────────────────────────────────────────┤
     * │ Cancel   │ Shown only for upcoming / ongoing.                               │
     * │          │ ENABLED  when NOT within 2 hours  → fires CancelAppointment()    │
     * │          │ DISABLED when within 2 hours      → item greyed-out, no action   │
     * ├──────────┼──────────────────────────────────────────────────────────────────┤
     * │ Delete   │ Shown only for completed or cancelled/canceled.                  │
     * │          │ Always enabled → fires DeleteAppointment()                       │
     * └──────────┴──────────────────────────────────────────────────────────────────┘
     */
    private void bindActionMenu(@NonNull MyViewHolder holder,
                                AppointmentModel appointmentModel,
                                int position,
                                String appointmentStatus) {

        // ── Determine per-status flags ────────────────────────────────────────
        boolean isUpcomingOrOngoing = "upcoming".equalsIgnoreCase(appointmentStatus)
                || "ongoing".equalsIgnoreCase(appointmentStatus);

        // true  → Cancel ENABLED  (more than 2 hours away)
        // false → Cancel DISABLED (within 2 hours)
        boolean cancelEnabled = !AndroidUtils.isWithinTwoHours(
                appointmentModel.getAppointment_from());

        boolean canDelete = "completed".equalsIgnoreCase(appointmentStatus)
                || "cancelled".equalsIgnoreCase(appointmentStatus)
                || "canceled".equalsIgnoreCase(appointmentStatus);

        // ── Build action list ─────────────────────────────────────────────────
        ArrayList<ActionModel> itemActions = new ArrayList<>();

        // History — always present
        itemActions.add(new ActionModel("History"));

        // Cancel — shown for upcoming/ongoing
        if (isUpcomingOrOngoing) {
            ActionModel cancelAction = new ActionModel("Cancel");
            cancelAction.setEnabled(cancelEnabled);
            itemActions.add(cancelAction);
        }

        // Delete — shown for completed or cancelled
        if (canDelete) {
            itemActions.add(new ActionModel("Delete"));
        }

        // ── Reset listeners before re-binding ─────────────────────────────────
        holder.custom_spinner_cardview.setOnClickListener(null);
        holder.sp_action.setOnItemClickListener(null);

        boolean isExpanded = (position == expandedPosition);

        if (isExpanded) {
            CommonSpinnerAdapter itemAdapter =
                    new CommonSpinnerAdapter((Activity) context, itemActions);
            holder.sp_action.setAdapter(itemAdapter);
            // Size the ListView to show all items without internal scrolling
            holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));
            // Make the card and list visible — FrameLayout keeps item height unchanged
            holder.action_list_card.setVisibility(View.VISIBLE);
            holder.sp_action.setVisibility(View.VISIBLE);
            // Bring to front so it draws on top of sibling cards in the RecyclerView
            holder.action_list_card.bringToFront();
            holder.action_list_card.invalidate();
        } else {
            holder.sp_action.setAdapter(null);
            holder.action_list_card.setVisibility(View.GONE);
            holder.sp_action.setVisibility(View.GONE);
        }

        // ── Three-dot tap: toggle expand / collapse ───────────────────────────
        holder.custom_spinner_cardview.setOnClickListener(v -> {
            int cur = holder.getAdapterPosition();
            if (cur == RecyclerView.NO_POSITION) return;
            int prev      = expandedPosition;
            expandedPosition = (expandedPosition == cur) ? -1 : cur;
            if (prev != -1 && prev != cur) safeNotify(prev);
            safeNotify(cur);
        });

        // ── Action item selected ───────────────────────────────────────────────
        holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
            int cur = holder.getAdapterPosition();
            if (cur == RecyclerView.NO_POSITION) return;

            ActionModel selectedAction = itemActions.get(pos);

            // Disabled items (Cancel within 2 hours) — do nothing
            if (!selectedAction.isEnabled()) return;

            String actionName = selectedAction.getName();

            // Collapse the menu immediately
            expandedPosition = -1;
            safeNotify(cur);

            // Dispatch on main thread so any dialog / navigation is safe
            new Handler(Looper.getMainLooper()).post(() ->
                    dispatchAction(actionName, appointmentModel));
        });
    }

    /**
     * Routes the selected menu action to the correct interface callback.
     *
     * "History" → ViewAppointmentHistory()  (same as tapping the card)
     * "Cancel"  → CancelAppointment()       (only reachable when cancelEnabled = true)
     * "Delete"  → DeleteAppointment()
     */
    private void dispatchAction(String action, AppointmentModel appointmentModel) {
        switch (action) {
            case "History":
                eventListener.ViewAppointmentHistory(appointmentModel, itemsArrayList);
                break;
            case "Cancel":
                eventListener.CancelAppointment(appointmentModel);
                break;
            case "Delete":
                eventListener.DeleteAppointment(appointmentModel);
                break;
        }
    }

    // =========================================================================
    //  Highlight
    // =========================================================================

    private void applyHighlight(AppointmentsAdapter.MyViewHolder holder,
                                AppointmentModel model) {
        if (highlightIds != null && highlightIds.contains(model.getId())) {
            holder.appointment_card.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(),
                            R.drawable.blue_stroke_card)
            );
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    holder.appointment_card.setBackground(
                            ContextCompat.getDrawable(holder.itemView.getContext(),
                                    R.drawable.rectangular_white_background)
                    ), 5000);
        } else {
            holder.appointment_card.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(),
                            R.drawable.rectangular_white_background));
        }
    }

    // =========================================================================
    //  Sort (kept for external callers — internal sort handled by Fragment)
    // =========================================================================

    public void sortAppointmentsByDate() {
        Collections.sort(itemsArrayList, new Comparator<AppointmentModel>() {
            @Override
            public int compare(AppointmentModel a1, AppointmentModel a2) {
                try {
                    SimpleDateFormat format =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                    Date date1 = format.parse(a1.getAppointment_from());
                    Date date2 = format.parse(a2.getAppointment_from());
                    return date2.compareTo(date1); // descending
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        notifyDataSetChanged();
    }

    // =========================================================================
    //  Date / time formatter
    // =========================================================================

    private String formatAppointmentDateTime(String fromDateTime, String toDateTime) {
        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat =
                    new SimpleDateFormat("h:mm a", Locale.ENGLISH);

            Date fromDate = inputFormat.parse(fromDateTime);
            Date toDate   = inputFormat.parse(toDateTime);

            if (fromDate != null && toDate != null) {
                String date     = dateFormat.format(fromDate);
                String fromTime = timeFormat.format(fromDate);
                String toTime   = timeFormat.format(toDate);
                return date + " • " + fromTime + " - " + toTime;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    // =========================================================================
    //  RecyclerView overrides
    // =========================================================================

    @Override
    public int getItemCount() {
        return itemsArrayList.size();
    }

    // =========================================================================
    //  Filter
    // =========================================================================

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemsArrayList = new ArrayList<>(list_item);
                } else {
                    ArrayList<AppointmentModel> filteredList = new ArrayList<>();
                    for (AppointmentModel row : list_item) {
                        if (AndroidUtils.isNull(row.getClient_name())
                                .toLowerCase()
                                .contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    itemsArrayList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count  = itemsArrayList.size();
                filterResults.values = itemsArrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence,
                                          FilterResults filterResults) {
                itemsArrayList = (ArrayList<AppointmentModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    // =========================================================================
    //  ViewHolder
    // =========================================================================

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView  tv_client_name, tv_appointment_datetime,
                tv_payment_status, tv_appointment_status, person_icon;
        ImageView iv_calendar_icon, iv_video_call, iv_chat, iv_profile;
        CardView  action_list_card;

        // Three-dot menu
        ImageView custom_spinner_cardview;  // ⋮ button
        ListView  sp_action;               // dropdown list inside action_list_card

        LinearLayout action_layout, appointment_card;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            appointment_card        = itemView.findViewById(R.id.appointment_card);
            tv_client_name          = itemView.findViewById(R.id.tv_client_name);
            iv_calendar_icon        = itemView.findViewById(R.id.iv_calendar_icon);
            iv_video_call           = itemView.findViewById(R.id.iv_video_call);
            iv_chat                 = itemView.findViewById(R.id.iv_chat);
            person_icon             = itemView.findViewById(R.id.person_icon);
            iv_profile              = itemView.findViewById(R.id.iv_profile);
            tv_appointment_datetime = itemView.findViewById(R.id.tv_appointment_datetime);
            tv_payment_status       = itemView.findViewById(R.id.tv_payment_status);
            tv_payment_status.setVisibility(View.VISIBLE);
            tv_appointment_status   = itemView.findViewById(R.id.tv_appointment_status);
            action_layout           = itemView.findViewById(R.id.action_layout);
            custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
            action_list_card        = itemView.findViewById(R.id.action_list_card);
            sp_action               = itemView.findViewById(R.id.list_actions);
        }
    }
}