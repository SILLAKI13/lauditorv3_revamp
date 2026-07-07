package com.digicoffer.lauditor.Chat.Adapters;

import android.app.Dialog;
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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Model.ClientRelationshipsDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi;
import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi;
import com.digicoffer.lauditor.CommonFiles.ChatService.TypingStateManager;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>
        implements Filterable, AsyncTaskCompleteListener {
    // 🔥 prevents duplicate clients
    private final Map<String, ClientRelationshipsDo> clientMap = new HashMap<>();

    private ArrayList<ClientRelationshipsDo> list = new ArrayList<>();
    private ArrayList<ClientRelationshipsDo> filteredList = new ArrayList<>();
    private final Map<String, String> firmMap = new HashMap<>();

    private Context context;
    private FragmentActivity activity;
    private Dialog progressDialog;

    private ClientRelationshipsDo currentFirm;

    public interface EventListener {
        void view_users(String uid, String name, String clientType) throws JSONException;
    }

    private EventListener listener;

    // Refreshes just the affected row the instant typing state flips for
    // any guid currently in this list, rather than waiting for the next
    // unrelated notifyDataSetChanged().
    private final TypingStateManager.TypingChangeListener typingChangeListener =
            (guid, isTyping) -> {
                for (int i = 0; i < filteredList.size(); i++) {
                    if (guid.equals(filteredList.get(i).getGuid())) {
                        notifyItemChanged(i);
                        break;
                    }
                }
            };

    // Listens for ChatConnectionService.NEW_MESSAGE (broadcast by
    // ChatConnection's incoming-message listener whenever a real chat
    // message arrives) so this list's row can be updated immediately -
    // without this, a row's lastMessage/unread_count only ever updates
    // once, in the constructor's refreshConversationMeta() call, and goes
    // stale the moment any new message arrives while this screen is open.
    private BroadcastReceiver newMessageReceiver;

    public ChatAdapter(ArrayList<ClientRelationshipsDo> input,
                       EventListener listener,
                       Context context,
                       FragmentActivity activity) {

        this.context = context;
        this.activity = activity;
        this.listener = listener;

        TypingStateManager.addListener(typingChangeListener);
        registerNewMessageReceiver();

        for (ClientRelationshipsDo row : input) {

            if ("entity".equalsIgnoreCase(row.getClientType())) {
                currentFirm = row;
                callUsersApi(row);
                continue;
            }

            if (!clientMap.containsKey(row.getGuid())) {
                clientMap.put(row.getGuid(), row);
                list.add(row);
            }
        }
        filteredList = list;

        // Single call covers last-message + unread-count for every row,
        // dynamically matched by guid - no per-row MAM threads needed.
        refreshConversationMeta();
    }

    /* ---------------- API RESULT ---------------- */

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog != null) {
            AndroidUtils.dismiss_dialog(progressDialog);
        }

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (result.getBoolean("error")) return;

                JSONArray users = result.getJSONObject("data").getJSONArray("users");
                String firmName = firmMap.get(httpResult.getRequestType());

                boolean addedAny = false;
                for (int i = 0; i < users.length(); i++) {
                    JSONObject obj = users.getJSONObject(i);
                    String guid = obj.getString("guid");
                    if (!clientMap.containsKey(guid)) {

                        ClientRelationshipsDo user = new ClientRelationshipsDo();
                        user.setGuid(guid);
                        user.setName(obj.getString("name"));
                        user.setFirmName(firmName);
                        user.setClientType("consumer");

                        clientMap.put(guid, user);
                        list.add(user);
                        addedAny = true;
                    }
                }

                filteredList = list;

                if (addedAny) {
                    // New rows showed up post-construction - refresh meta so
                    // they get their last-message/unread data too.
                    refreshConversationMeta();
                } else {
                    sortChats();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void callUsersApi(ClientRelationshipsDo firm) {
        progressDialog = AndroidUtils.get_progress(activity);
        firmMap.put(firm.getRel_id(), firm.getName());

        try {
            WebServiceHelper.callHttpWebService(
                    this,
                    context,
                    WebServiceHelper.RestMethodType.GET,
                    "relationship/" + firm.getRel_id() + "/users/notify",
                    firm.getRel_id(),
                    new JSONObject().toString()
            );
        } catch (Exception e) {
            AndroidUtils.dismiss_dialog(progressDialog);
        }
    }

    /* ---------------- CONVERSATION META (single API, dynamic apply) ---------------- */

    private void refreshConversationMeta() {
        java.util.List<String> chatJids = new ArrayList<>();
        for (ClientRelationshipsDo row : list) {
            chatJids.add(row.getGuid() + com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.VitacapeExtention);
        }

        ConversationMetaApi.fetch(context, chatJids, metaMap -> {
            for (ClientRelationshipsDo row : list) {
                ConversationMetaApi.ConversationMeta meta = metaMap.get(row.getGuid());
                if (meta == null) continue;

                row.setLastMessage(meta.lastMessage);
                row.setLastMessageTime(AndroidUtils.getChatTimeText(new java.util.Date(meta.lastMessageTimestamp)));
                row.setLastMessageTimestamp(meta.lastMessageTimestamp);
                row.setUnread_count(meta.unreadCount);
            }
            sortChats();
        });
    }

    /* ---------------- NEW MESSAGE LIVE UPDATE ---------------- */

    /**
     * Registers a BroadcastReceiver for ChatConnectionService.NEW_MESSAGE,
     * broadcast by ChatConnection's incoming-message listener every time a
     * real chat message arrives (see ChatConnection.java's
     * incomingListener -> mApplicationContext.sendBroadcast(intent)).
     * <p>
     * When a message arrives for a guid present in this list, updates that
     * row's lastMessage/lastMessageTime/lastMessageTimestamp/unread_count
     * immediately and refreshes just that row - fixes the bug where a row
     * stays stuck showing stale last-message text after the contact
     * finishes typing and actually sends, since previously NOTHING updated
     * lastMessage/unread_count outside of the one-time
     * refreshConversationMeta() call in the constructor.
     */
    private void registerNewMessageReceiver() {
        newMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent == null) return;
                String action = intent.getAction();
                android.util.Log.d("ChatAdapterDebug", "onReceive fired, action=" + action);
                if (action == null || !action.equals(ChatConnectionService.NEW_MESSAGE)) return;

                String fromJidRaw = intent.getStringExtra(ChatConnectionService.BUNDLE_FROM_JID);
                String body = intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY);
                if (fromJidRaw == null) return;

                // fromJidRaw may include the domain (e.g. "guid@domain") -
                // normalize to bare guid the same way row.getGuid() expects.
                String fromGuid = fromJidRaw.contains("@")
                        ? fromJidRaw.split("@")[0]
                        : fromJidRaw;

                android.util.Log.d("ChatAdapterDebug", "fromGuid=" + fromGuid + " body=" + body + " listSize=" + list.size());

                boolean matched = false;
                for (ClientRelationshipsDo row : list) {
                    if (fromGuid.equals(row.getGuid())) {
                        matched = true;
                        row.setLastMessage(body);
                        row.setLastMessageTime(AndroidUtils.getChatTimeText(new Date()));
                        row.setLastMessageTimestamp(System.currentTimeMillis());

                        // Bump unread count by 1 - matches the existing
                        // pattern elsewhere in this file (parseUnread/
                        // increment), since this is a NEW unread message
                        // arriving while the list is on screen.
                        int currentUnread = parseUnread(row.getUnread_count());
                        row.setUnread_count(String.valueOf(currentUnread + 1));

                        sortChats();
                        break;
                    }
                }
                if (!matched) {
                    android.util.Log.d("ChatAdapterDebug", "No row matched fromGuid=" + fromGuid);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ChatConnectionService.NEW_MESSAGE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(newMessageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(context, newMessageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
        android.util.Log.d("ChatAdapterDebug", "newMessageReceiver registered, context=" + context);
    }

    /**
     * Unregisters the new-message receiver and the typing listener.
     * <p>
     * IMPORTANT: ChatAdapter has no lifecycle callback of its own (unlike a
     * Fragment/Activity with onDestroyView), so the screen that OWNS this
     * adapter (e.g. Clients.java) MUST call this explicitly - most likely
     * from its onDestroyView() - or this receiver leaks and keeps firing
     * (and referencing this adapter/its `list`/`context`) even after the
     * screen showing it is gone.
     */
    public void unregister() {
        if (newMessageReceiver != null) {
            try {
                context.unregisterReceiver(newMessageReceiver);
            } catch (Exception ignored) {
                // Already unregistered or context gone - safe to ignore.
            }
            newMessageReceiver = null;
        }
        TypingStateManager.removeListener(typingChangeListener);
    }

    /* ---------------- ADAPTER ---------------- */

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.client_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ClientRelationshipsDo row = filteredList.get(position);

        String displayName = row.getFirmName() == null
                ? row.getName()
                : row.getName() + " - " + row.getFirmName();

        holder.tv_name.setText(displayName);
        holder.person_icon.setText(displayName.substring(0, 1).toUpperCase());

        // Presence dot lives on MessagesList's header avatar only, not in
        // this list - keep it hidden here.
        holder.dot_icon.setVisibility(View.GONE);

        String unread = row.getUnread_count();
        if (unread != null && !unread.equals("0")) {
            holder.count_icon.setVisibility(View.VISIBLE);
            holder.count_icon.setText(unread);
        } else {
            holder.count_icon.setVisibility(View.GONE);
        }

        holder.ll_client_card.setOnClickListener(v -> {
            try {
                listener.view_users(row.getGuid(), displayName,row.getSource());

                // Optimistic local clear: zero the badge immediately so it
                // doesn't wait on the resetunreadcount network round-trip.
                row.setUnread_count("0");
                holder.count_icon.setVisibility(View.GONE);

                new ChatCountApi(context).callResetUnreadCount(row.getGuid());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        if (row.getLastMessage() != null && !row.getLastMessage().isEmpty()) {
            holder.ll_last_msg.setVisibility(View.VISIBLE);

            // Typing indicator (XEP-0085) takes priority over the normal
            // last-message preview while active, then reverts automatically
            // once TypingStateManager reports isTyping == false again.
            if (TypingStateManager.isTyping(row.getGuid())) {
                holder.tv_last_msg.setText("typing...");
                holder.tv_last_msg.setTextColor(
                        holder.tv_last_msg.getContext().getResources()
                                .getColor(R.color.blue));
            } else {
                holder.tv_last_msg.setText(row.getLastMessage());
                holder.tv_last_msg.setTextColor(
                        holder.tv_last_msg.getContext().getResources()
                                .getColor(R.color.material_dynamic_neutral50));
            }
            holder.tv_last_msg_time.setText(row.getLastMessageTime());
        } else {
            holder.ll_last_msg.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    /* ---------------- SEARCH ---------------- */

    @Override
    public Filter getFilter() {
        return new Filter() {
            protected FilterResults performFiltering(CharSequence cs) {
                String q = cs.toString().toLowerCase();
                ArrayList<ClientRelationshipsDo> temp = new ArrayList<>();

                for (ClientRelationshipsDo row : list) {
                    String name = row.getFirmName() == null
                            ? row.getName()
                            : row.getName() + " - " + row.getFirmName();

                    if (name.toLowerCase().contains(q)) {
                        temp.add(row);
                    }
                }
                FilterResults fr = new FilterResults();
                fr.values = temp;
                return fr;
            }

            protected void publishResults(CharSequence cs, FilterResults fr) {
                filteredList = (ArrayList<ClientRelationshipsDo>) fr.values;
                sortChats();
            }
        };
    }

    /* ---------------- SORTING ---------------- */

    private void sortChats() {

        filteredList.sort((c1, c2) -> {

            // 1️⃣ Last message time DESC
            long t1 = c1.getLastMessageTimestamp();
            long t2 = c2.getLastMessageTimestamp();
            if (t1 != t2) {
                return Long.compare(t2, t1);
            }

            // 2️⃣ Unread count DESC
            int u1 = parseUnread(c1.getUnread_count());
            int u2 = parseUnread(c2.getUnread_count());
            if (u1 != u2) {
                return Integer.compare(u2, u1);
            }

            // 3️⃣ Name fallback ASC
            return c1.getName().compareToIgnoreCase(c2.getName());
        });

        notifyDataSetChanged();
    }

    private int parseUnread(String unread) {
        try {
            return unread == null || unread.isEmpty()
                    ? 0 : Integer.parseInt(unread);
        } catch (Exception e) {
            return 0;
        }
    }

    /* ---------------- HOLDER ---------------- */

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, person_icon, count_icon, tv_last_msg, tv_last_msg_time;
        ImageView plus_icon;
        ImageView dot_icon;
        LinearLayout ll_client_card;
        RelativeLayout ll_last_msg;
        LinearLayoutCompat ll_users;

        MyViewHolder(View v) {
            super(v);
            ll_last_msg = v.findViewById(R.id.ll_last_msg);
            tv_last_msg = v.findViewById(R.id.tv_last_msg);
            tv_last_msg_time = v.findViewById(R.id.tv_last_msg_time);
            tv_name = v.findViewById(R.id.tv_name);
            person_icon = v.findViewById(R.id.person_icon);
            count_icon = v.findViewById(R.id.count_icon);
            plus_icon = v.findViewById(R.id.plus_icon);
            dot_icon = v.findViewById(R.id.dot_icon);
            ll_users = v.findViewById(R.id.ll_users);
            ll_client_card = v.findViewById(R.id.ll_client_card);
        }
    }
}