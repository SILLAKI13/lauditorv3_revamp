package com.digicoffer.lauditor.Chat.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Model.MessageDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    // New row type: the animated "..." bubble shown while the other
    // person is composing. Always rendered as the very last item when
    // active - never part of mMessageList/filtered_List itself.
    private static final int VIEW_TYPE_TYPING_BUBBLE = 3;

    private Context mContext;
    private List<MessageDo> filtered_List;
    private List<MessageDo> mMessageList;
    public String dateTimeHeader = "";
    String person_name = "";
    private List<MessageDo> mtimeList;
    String formattedDate = "";
    private String lastDateHeader = "";

    // Whether the typing bubble row should currently be shown as the last
    // item in the list. Toggled via setTypingBubbleVisible() from
    // MessagesList.java, driven by TypingStateManager.
    private boolean typingBubbleVisible = false;

    public MessageListAdapter(Context context, List<MessageDo> messageList, String person_name, List<MessageDo> time_list) {
        mContext = context;
        mMessageList = messageList;
        this.filtered_List = messageList;
        mtimeList = time_list;
        this.person_name = person_name;
    }

    @Override
    public int getItemCount() {
        return filtered_List.size() + (typingBubbleVisible ? 1 : 0);
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        // The typing bubble, if visible, is always the last row - one
        // past the end of filtered_List.
        if (typingBubbleVisible && position == filtered_List.size()) {
            return VIEW_TYPE_TYPING_BUBBLE;
        }

        MessageDo message = mMessageList.get(position);

        if (message.getViewType().equals(Constants.chat_SENT)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_msg_design, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_msg_design, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_TYPING_BUBBLE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.typing_bubble_row, parent, false);
            return new TypingBubbleHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypingBubbleHolder) {
            // Nothing to bind - the bubble is static markup, just shown/hidden
            // via item count + view type.
            return;
        }

        MessageDo message = filtered_List.get(position);
        MessageDo time = mtimeList.get(position);
        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message, time);
        } else if (holder instanceof ReceivedMessageHolder) {
            ((ReceivedMessageHolder) holder).bind(message, time);
        }
    }

    /**
     * Shows or hides the typing bubble as the last row in the list.
     * Call from MessagesList.java whenever TypingStateManager reports a
     * change for the contact currently open in this chat.
     */
    public void setTypingBubbleVisible(boolean visible) {
        if (typingBubbleVisible == visible) return;

        if (visible) {
            typingBubbleVisible = true;
            notifyItemInserted(filtered_List.size());
        } else {
            int removedPosition = filtered_List.size();
            typingBubbleVisible = false;
            notifyItemRemoved(removedPosition);
        }
    }

    public boolean isTypingBubbleVisible() {
        return typingBubbleVisible;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                List<MessageDo> filteredList;

                if (charString.isEmpty()) {
                    // When search bar is cleared, reset to the original list
                    filteredList = new ArrayList<>(mMessageList);
                } else {
                    filteredList = new ArrayList<>();
                    for (MessageDo row : mMessageList) {
                        String name = row.getMessage();
                        if (name != null && name.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.count = filteredList.size();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filtered_List = (ArrayList<MessageDo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, person_icon, id_dateTimeHeader, date;

        SentMessageHolder(View itemView) {
            super(itemView);

            person_icon = itemView.findViewById(R.id.person_icon);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            date = (TextView) itemView.findViewById(R.id.date);
        }

        void bind(MessageDo message, MessageDo time) {

            messageText.setGravity(Gravity.START);

            if (TextUtils.isEmpty(message.getMessage())) return;

            messageText.setText(message.getMessage());

            String createdAt;

            if (message.isIscurrentchat()) {
                createdAt = message.getCreatedAt();
            } else {
                createdAt = time.getCreatedAt();
            }

            Date dateObj;
            try {
                SimpleDateFormat parser =
                        new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH);
                dateObj = parser.parse(createdAt);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }

            SimpleDateFormat dateFormatter =
                    new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            String formattedDate = dateFormatter.format(dateObj);

            SimpleDateFormat timeFormatter =
                    new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            String formattedTime = timeFormatter.format(dateObj);

            SimpleDateFormat todayFormat =
                    new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

            String today = todayFormat.format(new Date());
            String msgDay = todayFormat.format(dateObj);

            String groupKey = today.equals(msgDay) ? "Today" : formattedDate;

            int position = getBindingAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                date.setVisibility(View.GONE);
                return;
            }

            if (position == 0) {
                date.setText(groupKey);
                date.setVisibility(View.VISIBLE);
                timeText.setText(formattedTime);
                return;
            }

            MessageDo prevMsg = filtered_List.get(position - 1);
            String prevCreatedAt = prevMsg.isIscurrentchat()
                    ? prevMsg.getCreatedAt()
                    : mtimeList.get(position - 1).getCreatedAt();

            Date prevDate;
            try {
                prevDate = new SimpleDateFormat(
                        "MMM dd, yyyy hh:mm a", Locale.ENGLISH
                ).parse(prevCreatedAt);
            } catch (ParseException e) {
                date.setVisibility(View.GONE);
                return;
            }

            String prevDay = todayFormat.format(prevDate);
            String prevGroupKey = today.equals(prevDay) ? "Today"
                    : dateFormatter.format(prevDate);

            if (!groupKey.equals(prevGroupKey)) {
                date.setText(groupKey);
                date.setVisibility(View.VISIBLE);
            } else {
                date.setVisibility(View.GONE);
            }

            timeText.setText(formattedTime);

            if (!TextUtils.isEmpty(Constants.NAME)) {
                person_icon.setText(Constants.NAME.substring(0, 1).toUpperCase());
            } else {
                person_icon.setText("?");
            }
        }
    }


    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, person_icon, nameText, id_dateTimeHeader, date;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            person_icon = (TextView) itemView.findViewById(R.id.person_icon);
            date = (TextView) itemView.findViewById(R.id.date);
        }

        void bind(MessageDo message, MessageDo time) {

            messageText.setGravity(Gravity.START);

            if (TextUtils.isEmpty(message.getMessage())) return;

            messageText.setText(message.getMessage());

            String createdAt;
            if (message.isIscurrentchat()) {
                createdAt = message.getCreatedAt();
            } else {
                createdAt = time.getCreatedAt();
            }

            Date dateObj;
            try {
                SimpleDateFormat parser = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH);
                dateObj = parser.parse(createdAt);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            String formattedDate = dateFormatter.format(dateObj);

            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            String formattedTime = timeFormatter.format(dateObj);

            SimpleDateFormat todayFormat =
                    new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

            String today = todayFormat.format(new Date());
            String msgDay = todayFormat.format(dateObj);

            String groupKey = today.equals(msgDay) ? "Today" : formattedDate;

            int position = getBindingAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                date.setVisibility(View.GONE);
                return;
            }
            timeText.setText(formattedTime);
            if (!TextUtils.isEmpty(Constants.NAME)) {
                person_icon.setText(Constants.NAME.substring(0, 1).toUpperCase());
            } else {
                person_icon.setText("");
            }
            if (position == 0) {
                date.setText(groupKey);
                date.setVisibility(View.VISIBLE);
                return;
            }

            MessageDo prevMsg = filtered_List.get(position - 1);
            String prevCreatedAt = prevMsg.isIscurrentchat()
                    ? prevMsg.getCreatedAt()
                    : mtimeList.get(position - 1).getCreatedAt();

            Date prevDate;
            try {
                prevDate = new SimpleDateFormat(
                        "MMM dd, yyyy hh:mm a", Locale.ENGLISH
                ).parse(prevCreatedAt);
            } catch (ParseException e) {
                date.setVisibility(View.GONE);
                return;
            }

            String prevDay = todayFormat.format(prevDate);
            String prevGroupKey = today.equals(prevDay) ? "Today"
                    : dateFormatter.format(prevDate);

            if (!groupKey.equals(prevGroupKey)) {
                date.setText(groupKey);
                date.setVisibility(View.VISIBLE);
            } else {
                date.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Holder for the typing-indicator bubble row. Animates its three dots
     * in a "wave" pattern (each dot bounces up then down, staggered after
     * the previous one) for as long as this row is visible on screen.
     */
    private static class TypingBubbleHolder extends RecyclerView.ViewHolder {
        private final List<android.animation.ObjectAnimator> animators = new ArrayList<>();

        TypingBubbleHolder(View itemView) {
            super(itemView);

            View dot1 = itemView.findViewById(R.id.dot1);
            View dot2 = itemView.findViewById(R.id.dot2);
            View dot3 = itemView.findViewById(R.id.dot3);

            startWaveAnimation(dot1, 0);
            startWaveAnimation(dot2, 150);
            startWaveAnimation(dot3, 300);
        }

        /**
         * Bounces a single dot up (-6dp translationY) then back down, in
         * an infinite loop, starting after startDelayMs - staggering the
         * three dots' delays creates the left-to-right wave look.
         */
        private void startWaveAnimation(View dot, long startDelayMs) {
            if (dot == null) return;

            float bounceHeightPx = dot.getResources().getDisplayMetrics().density * -6f;

            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofFloat(
                    dot, "translationY", 0f, bounceHeightPx, 0f);
            animator.setDuration(600);
            animator.setStartDelay(startDelayMs);
            animator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
            animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            animator.start();

            animators.add(animator);
        }

        /**
         * Stops all three animators - call when this row is recycled so
         * animations don't keep running on a view that's no longer
         * showing the typing bubble (RecyclerView reuses ViewHolders).
         */
        void stopAnimations() {
            for (android.animation.ObjectAnimator animator : animators) {
                animator.cancel();
            }
            animators.clear();
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof TypingBubbleHolder) {
            ((TypingBubbleHolder) holder).stopAnimations();
        }
    }


    public void addMessage(MessageDo message) {
        // If the typing bubble was showing, the sender just sent a real
        // message instead - hide the bubble first so positions stay
        // consistent, then insert the message as the new last row.
        if (typingBubbleVisible) {
            int bubblePosition = mMessageList.size();
            typingBubbleVisible = false;
            notifyItemRemoved(bubblePosition);
        }

        mMessageList.add(message);
        filtered_List = mMessageList;
        notifyItemInserted(mMessageList.size() - 1);
    }
}