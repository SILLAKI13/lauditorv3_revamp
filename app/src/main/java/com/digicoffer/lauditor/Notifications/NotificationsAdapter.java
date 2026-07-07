package com.digicoffer.lauditor.Notifications;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Notifications.Models.NotificationsDo;
import com.digicoffer.lauditor.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.GroupViewHolder> implements Filterable {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private ArrayList<NotificationsDo> list_item;       // original full list
    private ArrayList<NotificationsDo> filtered_list;   // shown in RecyclerView
    private ArrayList<DateGroup> groupedList;           // List of date groups (each group is one card)
    private final Notifications mcontext;
    private final EventListener eventListener;
    String highlightIds;

    public interface EventListener {
        void onEvent(String id);
        void onSelectAllChanged(boolean allSelected);
    }

    public NotificationsAdapter(ArrayList<NotificationsDo> notificationList,
                                EventListener eventListener,
                                Notifications fragment, String pendingHighlightIds) {
        this.list_item = new ArrayList<>(notificationList);
        this.filtered_list = new ArrayList<>(notificationList);
        this.eventListener = eventListener;
        this.mcontext = fragment;
        this.highlightIds = pendingHighlightIds;

        // Sort the filtered_list in descending order (latest first) before grouping
        sortNotificationsDescending();
        buildGroupedList();
    }

    // Sort notifications in descending order (latest date first)
    private void sortNotificationsDescending() {
        if (filtered_list == null || filtered_list.isEmpty()) {
            return;
        }

        Collections.sort(filtered_list, new Comparator<NotificationsDo>() {
            @Override
            public int compare(NotificationsDo n1, NotificationsDo n2) {
                Date date1 = parseDate(n1.getTimestamp());
                Date date2 = parseDate(n2.getTimestamp());

                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1;
                if (date2 == null) return -1;

                // Descending order (latest first)
                return date2.compareTo(date1);
            }
        });
    }

    // Parse date from timestamp string
    private Date parseDate(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return null;
        try {
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            return isoFmt.parse(timestamp);
        } catch (ParseException e) {
            return null;
        }
    }

    // Build grouped list - each date becomes ONE card with multiple notifications inside
    private void buildGroupedList() {
        groupedList = new ArrayList<>();

        if (filtered_list == null || filtered_list.isEmpty()) {
            return;
        }

        // Group notifications by date using TreeMap with descending order
        Map<String, ArrayList<NotificationsDo>> grouped = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String date1, String date2) {
                // Compare dates in descending order (latest first)
                Date d1 = parseDateKey(date1);
                Date d2 = parseDateKey(date2);

                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;

                return d2.compareTo(d1);
            }
        });

        for (NotificationsDo notification : filtered_list) {
            String dateKey = getDateKey(notification.getTimestamp());
            if (!grouped.containsKey(dateKey)) {
                grouped.put(dateKey, new ArrayList<>());
            }
            grouped.get(dateKey).add(notification);
        }

        // Create a DateGroup for each date
        for (Map.Entry<String, ArrayList<NotificationsDo>> entry : grouped.entrySet()) {
            String dateKey = entry.getKey();
            ArrayList<NotificationsDo> items = entry.getValue();
            groupedList.add(new DateGroup(dateKey, items, items.size()));
        }
    }

    // Get formatted date key for grouping (MMM dd, yyyy)
    private String getDateKey(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "Unknown Date";
        try {
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat keyFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            keyFmt.setTimeZone(TimeZone.getDefault());
            Date date = isoFmt.parse(timestamp);
            return date != null ? keyFmt.format(date) : "Unknown Date";
        } catch (ParseException e) {
            return "Unknown Date";
        }
    }

    // Parse date key string to Date object for comparison
    private Date parseDateKey(String dateKey) {
        if (dateKey == null || dateKey.equals("Unknown Date")) return null;
        try {
            SimpleDateFormat keyFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return keyFmt.parse(dateKey);
        } catch (ParseException e) {
            return null;
        }
    }

    // Format time only for display inside notification row
    private String formatTimeOnly(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            outFmt.setTimeZone(TimeZone.getDefault());
            Date date = isoFmt.parse(timestamp);
            return date != null ? outFmt.format(date) : "";
        } catch (ParseException e) {
            return "";
        }
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_view, parent, false);
        return new GroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        DateGroup dateGroup = groupedList.get(position);

        // Set header
        holder.tv_date_header.setText(dateGroup.dateKey);
        holder.tv_count_header.setText(String.format("(%d)", dateGroup.count));

        // Clear existing views
        holder.notifications_container.removeAllViews();

        // Add each notification as a row inside the container
        for (int i = 0; i < dateGroup.notifications.size(); i++) {
            NotificationsDo notification = dateGroup.notifications.get(i);
            View rowView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.notification_item_row, holder.notifications_container, false);

            // Bind data to row
            bindNotificationRow(rowView, notification, i == dateGroup.notifications.size() - 1);

            // Set click listener for the entire row
            final int finalI = i;
            rowView.setOnClickListener(v -> {
                NotificationsDo item = dateGroup.notifications.get(finalI);
                if ("unread".equalsIgnoreCase(item.getStatus())) {
                    mcontext.readSingleNotification(item.getId(), item);
                } else {
                    mcontext.openNotificationNavigation(item.getNavigation());
                }
            });

            holder.notifications_container.addView(rowView);
        }

        // Apply highlight to entire card if needed
        applyHighlightToCard(holder, dateGroup);
    }

    private void bindNotificationRow(View rowView, NotificationsDo notification, boolean isLastItem) {
        TextView tv_message = rowView.findViewById(R.id.tv_messageView);
        TextView tv_timestamp = rowView.findViewById(R.id.tv_timestampView);
        CheckBox checkbox = rowView.findViewById(R.id.chk_selected);
        View view_unread_indicator = rowView.findViewById(R.id.view_unread_indicator);
        View view_indicator_space = rowView.findViewById(R.id.view_indicator_space);
        View divider = rowView.findViewById(R.id.divider);

        // Set text
        tv_message.setText(notification.getMessage());
        tv_timestamp.setText(formatTimeOnly(notification.getTimestamp()));

        // Show/hide divider (don't show for last item)
        if (divider != null) {
            if (isLastItem) {
                divider.setVisibility(View.GONE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }
        }

        // Unread vs Read styling
        boolean isUnread = "unread".equalsIgnoreCase(notification.getStatus());

        if (isUnread) {
            // Show blue DOT instead of line
            if (view_unread_indicator != null) {
                view_unread_indicator.setVisibility(View.VISIBLE);
            }
            if (view_indicator_space != null) {
                view_indicator_space.setVisibility(View.GONE);
            }
            tv_message.setTextColor(
                    ContextCompat.getColor(mcontext.requireContext(), R.color.Primary_new));
//            tv_message.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            // Hide dot, show spacer
            if (view_unread_indicator != null) {
                view_unread_indicator.setVisibility(View.GONE);
            }
            if (view_indicator_space != null) {
                view_indicator_space.setVisibility(View.VISIBLE);
            }
            tv_message.setTextColor(
                    ContextCompat.getColor(mcontext.requireContext(), R.color.dark_text));
//            tv_message.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // Checkbox logic
        checkbox.setChecked(notification.isChecked());
        checkbox.setOnClickListener(v -> {
            notification.setChecked(checkbox.isChecked());
            if (eventListener != null) {
                eventListener.onSelectAllChanged(areAllItemsSelected());
            }
            mcontext.load_list(filtered_list);
            notifyDataSetChanged();
        });
    }

    private void applyHighlightToCard(GroupViewHolder holder, DateGroup dateGroup) {
        boolean shouldHighlight = false;
        for (NotificationsDo notification : dateGroup.notifications) {
            if (highlightIds != null && highlightIds.contains(notification.getId())) {
                shouldHighlight = true;
                break;
            }
        }

        if (shouldHighlight) {
            holder.card_view1.setBackground(
                    mcontext.getResources().getDrawable(R.drawable.blue_stroke_card)
            );
            holder.card_view1.setCardElevation(8f);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                holder.card_view1.setBackground(
                        ContextCompat.getDrawable(holder.itemView.getContext(),
                                R.drawable.rectangular_white_background)
                );
            }, 5000);
        } else {
            holder.card_view1.setCardBackgroundColor(
                    mcontext.getResources().getColor(android.R.color.white)
            );
            holder.card_view1.setCardElevation(4f);
        }
    }

    @Override
    public int getItemCount() {
        return groupedList != null ? groupedList.size() : 0;
    }

    public boolean areAllItemsSelected() {
        for (NotificationsDo n : filtered_list) {
            if (!n.isChecked()) return false;
        }
        return !filtered_list.isEmpty();
    }

    public void selectOrDeselectAll(boolean isChecked) {
        for (NotificationsDo n : filtered_list) {
            n.setChecked(isChecked);
        }
        sortNotificationsDescending();
        buildGroupedList();
        notifyDataSetChanged();
        if (eventListener != null) {
            eventListener.onSelectAllChanged(isChecked && !filtered_list.isEmpty());
        }
    }

    public ArrayList<NotificationsDo> getList_item() {
        return list_item;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                ArrayList<NotificationsDo> result = new ArrayList<>();
                if (query == null || query.toString().trim().isEmpty()) {
                    result.addAll(list_item);
                } else {
                    String lower = query.toString().toLowerCase(Locale.getDefault()).trim();
                    for (NotificationsDo n : list_item) {
                        String msg = n.getMessage() != null ? n.getMessage().toLowerCase() : "";
                        if (msg.contains(lower)) {
                            result.add(n);
                        }
                    }
                }
                FilterResults fr = new FilterResults();
                fr.values = result;
                fr.count = result.size();
                return fr;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence query, FilterResults results) {
                filtered_list = (ArrayList<NotificationsDo>) results.values;
                sortNotificationsDescending(); // Sort after filtering
                buildGroupedList();
                notifyDataSetChanged();
            }
        };
    }

    // ViewHolder for Group Card
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        final CardView card_view1;
        final TextView tv_date_header;
        final TextView tv_count_header;
        final LinearLayout notifications_container;
        final LinearLayout message_list;

        public GroupViewHolder(View itemView) {
            super(itemView);
            card_view1 = itemView.findViewById(R.id.card_view1);
            message_list = itemView.findViewById(R.id.message_list);
            tv_date_header = itemView.findViewById(R.id.tv_date_header);
            tv_count_header = itemView.findViewById(R.id.tv_count_header);
            notifications_container = itemView.findViewById(R.id.notifications_container);
        }
    }

    // DateGroup class
    private static class DateGroup {
        String dateKey;
        ArrayList<NotificationsDo> notifications;
        int count;

        DateGroup(String dateKey, ArrayList<NotificationsDo> notifications, int count) {
            this.dateKey = dateKey;
            this.notifications = notifications;
            this.count = count;
        }
    }
}