package com.digicoffer.lauditor.Chat.Adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Model.ChildDO;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi;
import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi;
import com.digicoffer.lauditor.CommonFiles.ChatService.TypingStateManager;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import java.util.ArrayList;
import java.util.Date;

public class MembersAdapter
        extends RecyclerView.Adapter<MembersAdapter.MemberVH>
        implements Filterable {

    public interface OnMemberClick {
        void onMemberClick(ChildDO member);
    }

    private ArrayList<ChildDO> members;
    private ArrayList<ChildDO> filtered_list = new ArrayList<>();
    private Context context;
    private OnMemberClick listener;

    private final TypingStateManager.TypingChangeListener typingChangeListener =
            (guid, isTyping) -> {
                for (int i = 0; i < filtered_list.size(); i++) {
                    if (guid.equals(filtered_list.get(i).getGuid())) {
                        notifyItemChanged(i);
                        break;
                    }
                }
            };

    // Listens for ChatConnectionService.NEW_MESSAGE so this list's row can
    // be updated immediately when a real message arrives, instead of
    // staying stuck on stale lastMessage/unread_count - same fix as
    // ChatAdapter, see its registerNewMessageReceiver() for full rationale.
    private BroadcastReceiver newMessageReceiver;

    public MembersAdapter(ArrayList<ChildDO> members,
                          Context context,
                          OnMemberClick listener) {

        this.members = members;
        this.filtered_list = new ArrayList<>(members);
        this.context = context;
        this.listener = listener;

        TypingStateManager.addListener(typingChangeListener);
        registerNewMessageReceiver();

        // Single call covers last-message + unread-count for every member,
        // dynamically matched by guid - no per-row MAM threads needed.
        refreshConversationMeta();
    }

    /* ---------------- ADAPTER ---------------- */

    @NonNull
    @Override
    public MemberVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_list, parent, false);
        return new MemberVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberVH holder, int position) {
        ChildDO member = filtered_list.get(position);

        holder.tv_name.setText(member.getName());
        holder.person_icon.setText(
                member.getName().substring(0, 1).toUpperCase()
        );
        String unread = member.getUnread_count();
        if (unread != null && !unread.equals("0")) {
            holder.count_icon.setVisibility(View.VISIBLE);
            holder.count_icon.setText(unread);
        } else {
            holder.count_icon.setVisibility(View.GONE);
        }
        holder.plus_icon.setVisibility(View.GONE);
        holder.ll_users.setVisibility(View.GONE);

        holder.ll_client_card.setOnClickListener(v -> {
            listener.onMemberClick(member);

            // Optimistic local clear: zero the badge immediately so it
            // doesn't wait on the resetunreadcount network round-trip.
            member.setUnread_count("0");
            holder.count_icon.setVisibility(View.GONE);

            new ChatCountApi(context).callResetUnreadCount(member.getGuid());
        });

        if (member.getLastMessage() != null && !member.getLastMessage().isEmpty()) {
            holder.ll_last_msg.setVisibility(View.VISIBLE);

            if (TypingStateManager.isTyping(member.getGuid())) {
                holder.tv_last_msg.setText("typing...");
                holder.tv_last_msg.setTextColor(
                        holder.tv_last_msg.getContext().getResources()
                                .getColor(R.color.blue));
            } else {
                holder.tv_last_msg.setText(member.getLastMessage());
                holder.tv_last_msg.setTextColor(
                        holder.tv_last_msg.getContext().getResources()
                                .getColor(R.color.material_dynamic_neutral50));
            }
            holder.tv_last_msg_time.setText(member.getLastMessageTime());
        } else {
            holder.ll_last_msg.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return filtered_list.size();
    }

    /* ---------------- SEARCH ---------------- */

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String search = charSequence.toString().trim().toLowerCase();
                ArrayList<ChildDO> tempList = new ArrayList<>();

                if (search.isEmpty()) {
                    tempList.addAll(members);
                } else {
                    for (ChildDO row : members) {
                        String name = AndroidUtils.isNull(row.getName()).toLowerCase();
                        String firm = AndroidUtils.isNull(row.getFirmName()).toLowerCase();

                        if (name.contains(search) || firm.contains(search)) {
                            tempList.add(row);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = tempList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered_list.clear();
                filtered_list.addAll((ArrayList<ChildDO>) results.values);
                sortMembers();
            }
        };
    }

    /* ---------------- CONVERSATION META (single API, dynamic apply) ---------------- */

    private void refreshConversationMeta() {
        java.util.List<String> chatJids = new ArrayList<>();
        for (ChildDO member : members) {
            chatJids.add(member.getGuid() + com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.VitacapeExtention);
        }

        ConversationMetaApi.fetch(context, chatJids, metaMap -> {
            for (ChildDO member : members) {
                ConversationMetaApi.ConversationMeta meta = metaMap.get(member.getGuid());
                if (meta == null) continue;

                member.setLastMessage(meta.lastMessage);
                member.setLastMessageTime(AndroidUtils.getChatTimeText(new java.util.Date(meta.lastMessageTimestamp)));
                member.setLastMessageTimestamp(meta.lastMessageTimestamp);
                member.setUnread_count(meta.unreadCount);
            }
            sortMembers();
        });
    }

    /* ---------------- NEW MESSAGE LIVE UPDATE ---------------- */

    /**
     * Registers a BroadcastReceiver for ChatConnectionService.NEW_MESSAGE.
     * See ChatAdapter.registerNewMessageReceiver() for the full rationale -
     * identical fix applied here for the Team Members list.
     */
    private void registerNewMessageReceiver() {
        newMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent == null) return;
                String action = intent.getAction();
                android.util.Log.d("MembersAdapterDebug", "onReceive fired, action=" + action);
                if (action == null || !action.equals(ChatConnectionService.NEW_MESSAGE)) return;

                String fromJidRaw = intent.getStringExtra(ChatConnectionService.BUNDLE_FROM_JID);
                String body = intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY);
                if (fromJidRaw == null) return;

                String fromGuid = fromJidRaw.contains("@")
                        ? fromJidRaw.split("@")[0]
                        : fromJidRaw;

                android.util.Log.d("MembersAdapterDebug", "fromGuid=" + fromGuid + " body=" + body + " membersSize=" + members.size());

                boolean matched = false;
                for (ChildDO member : members) {
                    if (fromGuid.equals(member.getGuid())) {
                        matched = true;
                        member.setLastMessage(body);
                        member.setLastMessageTime(AndroidUtils.getChatTimeText(new Date()));
                        member.setLastMessageTimestamp(System.currentTimeMillis());

                        int currentUnread = parseUnread(member.getUnread_count());
                        member.setUnread_count(String.valueOf(currentUnread + 1));

                        sortMembers();
                        break;
                    }
                }
                if (!matched) {
                    android.util.Log.d("MembersAdapterDebug", "No member matched fromGuid=" + fromGuid);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ChatConnectionService.NEW_MESSAGE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(newMessageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(context, newMessageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
        android.util.Log.d("MembersAdapterDebug", "newMessageReceiver registered, context=" + context);
    }

    /**
     * Unregisters the new-message receiver and the typing listener.
     * IMPORTANT: must be called explicitly by the screen that owns this
     * adapter (e.g. Teams.java), most likely from onDestroyView() - see
     * ChatAdapter.unregister()'s javadoc for the full explanation.
     */
    public void unregister() {
        if (newMessageReceiver != null) {
            try {
                context.unregisterReceiver(newMessageReceiver);
            } catch (Exception ignored) {
            }
            newMessageReceiver = null;
        }
        TypingStateManager.removeListener(typingChangeListener);
    }

    /* ---------------- SORTING ---------------- */

    private void sortMembers() {

        filtered_list.sort((m1, m2) -> {

            // 1️⃣ Last message received DESC
            long t1 = m1.getLastMessageTimestamp();
            long t2 = m2.getLastMessageTimestamp();

            if (t1 != t2) {
                return Long.compare(t2, t1);
            }

            // 2️⃣ Unread count DESC
            int unread1 = parseUnread(m1.getUnread_count());
            int unread2 = parseUnread(m2.getUnread_count());

            if (unread1 != unread2) {
                return Integer.compare(unread2, unread1);
            }

            // 3️⃣ Name fallback ASC
            return m1.getName().compareToIgnoreCase(m2.getName());
        });

        notifyDataSetChanged();
    }

    private int parseUnread(String unread) {
        try {
            return unread == null ? 0 : Integer.parseInt(unread);
        } catch (Exception e) {
            return 0;
        }
    }

    /* ---------------- HOLDER ---------------- */

    static class MemberVH extends RecyclerView.ViewHolder {
        TextView tv_name, person_icon, tv_last_msg, tv_last_msg_time, count_icon;
        ImageView plus_icon;
        LinearLayoutCompat ll_users;
        LinearLayout ll_client_card;
        RelativeLayout ll_last_msg;

        MemberVH(View v) {
            super(v);
            ll_last_msg = v.findViewById(R.id.ll_last_msg);
            tv_last_msg = v.findViewById(R.id.tv_last_msg);
            tv_last_msg_time = v.findViewById(R.id.tv_last_msg_time);
            tv_name = v.findViewById(R.id.tv_name);
            person_icon = v.findViewById(R.id.person_icon);
            plus_icon = v.findViewById(R.id.plus_icon);
            count_icon = v.findViewById(R.id.count_icon);
            ll_users = v.findViewById(R.id.ll_users);
            ll_client_card = v.findViewById(R.id.ll_client_card);
        }
    }
}