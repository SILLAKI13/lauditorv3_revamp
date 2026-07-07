package com.digicoffer.lauditor.Chat.ViewModels;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static androidx.constraintlayout.widget.Constraints.TAG;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.hasPermissions;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.loadWebView;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showAlertDialog;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Adapters.MessageListAdapter;
import com.digicoffer.lauditor.Chat.Model.MessageDo;
import com.digicoffer.lauditor.Chat.Model.User;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService;
import com.digicoffer.lauditor.CommonFiles.ChatService.PresenceManager;
import com.digicoffer.lauditor.CommonFiles.ChatService.TypingStateManager;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.sid.element.StanzaIdElement;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MessagesList extends Fragment {

    // ── Views ──────────────────────────────────────────────────────────────────
    RecyclerView rv_messagesList;
    ImageView ib_back_button;
    MainActivity mainActivity;
    ImageView bt_sendMessage, bt_receiveMessage;
    EditText mChatView;
    TextView tv_contactName, person_icon, tv_chat;
    TextView tv_presence_status;
    View header_presence_dot;
    Dialog progress_dialog;
    FloatingActionButton fb_chat;
    ImageView ivSearch;
    View searchLayout;
    TextInputEditText etSearchChat;
    ImageView iv_search_cancel, iv_video_call;

    // ── Data ───────────────────────────────────────────────────────────────────
    ArrayList<MessageDo> message_list = new ArrayList<>();
    ArrayList<MessageDo> time_list    = new ArrayList<>();
    MessageListAdapter adapter;

    // ── Chat identity ──────────────────────────────────────────────────────────
    private String contactJid    = "";
    private String contact_name  = "";
    private String contact_type  = "";
    private String currentJid    = "";
    private String person_name   = "";

    // XEP-0333 read markers: most recent message id seen in this chat.
    // Sent as a <displayed/> marker once history loads, and again whenever
    // a new message arrives while this chat screen is open. This is what
    // actually resets the unread count server-side.
    private String lastSeenMessageId = null;

    // ── XEP-0085 typing-state tracking ──────────────────────────────────────────
    // Throttle: composing is sent at most once every 3 seconds while the
    // user keeps typing, per spec.
    private long lastComposingSentAt = 0L;
    private static final long COMPOSING_THROTTLE_MS = 3000L;

    // True once a composing has actually been sent for the current typing
    // burst, so we know whether a paused needs to be sent when it ends.
    private boolean isCurrentlyComposing = false;

    // 1-second inactivity timer: schedules "paused" if no further keystroke
    // arrives within 1 second, per spec. Re-scheduled on every keystroke.
    private final Handler typingPauseHandler = new Handler(Looper.getMainLooper());
    private final Runnable pausedRunnable = this::sendPausedIfWasComposing;

    // ── XMPP service ──────────────────────────────────────────────────────────
    private static ChatConnection        mConnection;
    private static ChatConnectionService chatConnectionService;
    private BroadcastReceiver mBroadcastReceiver;

    // ── Reconnect guards ───────────────────────────────────────────────────────
    private boolean isReconnecting          = false;
    private boolean hasShownConnectionToast = false;

    // ── Constructor ────────────────────────────────────────────────────────────
    public MessagesList(RecyclerView rv_messagesList) {
        this.rv_messagesList = rv_messagesList;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Fragment lifecycle
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_message_list, container, false);

        // ── Bind views ─────────────────────────────────────────────────────────
        rv_messagesList  = v.findViewById(R.id.reyclerview_message_list);
        tv_contactName   = v.findViewById(R.id.tv_contactName);
        ivSearch         = v.findViewById(R.id.iv_search);
        searchLayout     = v.findViewById(R.id.search_layout);
        iv_search_cancel = v.findViewById(R.id.iv_search_cancel);
        etSearchChat     = searchLayout.findViewById(R.id.et_search_chat);
        iv_video_call    = v.findViewById(R.id.iv_video_call);
        person_icon      = v.findViewById(R.id.person_icon);
        header_presence_dot = v.findViewById(R.id.header_presence_dot);
        tv_presence_status = v.findViewById(R.id.tv_presence_status);
        tv_chat          = v.findViewById(R.id.tv_chat);
        mChatView        = v.findViewById(R.id.edittext_chatbox);
        bt_sendMessage   = v.findViewById(R.id.button_chatbox_send);
        ib_back_button   = v.findViewById(R.id.back_button);

        // ── Soft keyboard mode ─────────────────────────────────────────────────
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        tv_chat.setText(R.string.chat);
        tv_chat.setVisibility(View.GONE);

        // ── Search bar toggle ──────────────────────────────────────────────────
        ivSearch.setOnClickListener(v1 -> {
            etSearchChat.setText("");
            if (searchLayout.getVisibility() == View.VISIBLE) {
                searchLayout.setVisibility(View.GONE);
            } else {
                searchLayout.setVisibility(View.VISIBLE);
                etSearchChat.requestFocus();
            }
        });

        iv_search_cancel.setOnClickListener(v1 -> {
            etSearchChat.setText("");
            searchLayout.setVisibility(View.GONE);
        });

        // ── Video call ─────────────────────────────────────────────────────────
        iv_video_call.setOnClickListener(view -> {
            String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
            if (hasPermissions(perms, getContext())) {
                loadWebView(getContext(), getActivity());
            } else {
                showAlertDialog(view, getActivity());
            }
        });

        // ── Back button ────────────────────────────────────────────────────────
        ib_back_button.setOnClickListener(view -> loadBackPressed());

        // ── Send button enable/disable + typing-state (XEP-0085) ───────────────
        updateSendButtonState(mChatView.getText().toString());
        mChatView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable e) {
                updateSendButtonState(e.toString());

                if (e.toString().trim().isEmpty()) {
                    // Field cleared - treat as "stopped typing" immediately,
                    // no need to wait for the 1s pause timer.
                    sendPausedIfWasComposing();
                    return;
                }

                sendComposingThrottled();
                schedulePausedAfterInactivity();
            }
        });

        // ── Send message ───────────────────────────────────────────────────────
        bt_sendMessage.setOnClickListener(v1 -> {
            String text = mChatView.getText().toString().trim();
            if (text.isEmpty()) {
                AndroidUtils.showToast("Message field cannot be empty", getContext());
                return;
            }

            if (ChatConnectionService.getState()
                    .equals(ChatConnection.ConnectionState.CONNECTED)) {

                Log.d(TAG, "Sending message: " + text);

                Intent intent = new Intent(ChatConnectionService.SEND_MESSAGE);
                intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY, text);
                intent.putExtra(ChatConnectionService.BUNDLE_TO, contactJid);
                String name = Constants.USER_ID.equals("admin")
                        ? Constants.FIRM_NAME : Constants.NAME;
                String subject = name + " ##" + currentJid + "## #N#" + name + "#N#";
                intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, subject);
                requireActivity().sendBroadcast(intent);

                MessageDo chatMessage = new MessageDo();
                chatMessage.setMessage(text);
                chatMessage.setViewType(Constants.chat_SENT);
                chatMessage.setIscurrentchat(true);
                chatMessage.setCreatedAt(AndroidUtils.getDateToString(
                        Calendar.getInstance().getTime(), "MMM dd, yyyy hh:mm a"));
                User user = new User();
                user.setNickname("");
                chatMessage.setSender(user);

                if (!isMessageExist(chatMessage)) {
                    time_list.add(chatMessage);
                    adapter.addMessage(chatMessage);
                    mChatView.setText("");
                    rv_messagesList.smoothScrollToPosition(message_list.size() - 1);
                }

            } else {
                Toast.makeText(getActivity(),
                        "Client not connected. Message not sent! Please try after a few seconds.",
                        Toast.LENGTH_LONG).show();
                AndroidUtils.reconnecXMPPServer(getActivity());
            }
        });

        // ── Read bundle arguments ──────────────────────────────────────────────
        Bundle bundle = this.getArguments();
        assert bundle != null;
        contactJid   = bundle.getString("EXTRA_CONTACT_JID");
        contact_name = bundle.getString("EXTRA_CONTACT_NAME");
        contact_type = bundle.getString("EXTRA_CONTACT_TYPE");

        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        currentJid = pref.getString("xmpp_jid", null);

        pref.edit().putString("CURRENTCHAT_JID", contactJid).apply();

        Log.d("IDS..", "contactJid=" + contactJid + " currentJid=" + currentJid);

        // ── Header ─────────────────────────────────────────────────────────────
        tv_contactName.setText(contact_name);
        if (contact_name != null && !contact_name.isEmpty()) {
            person_name = contact_name.substring(0, 1);
            person_icon.setText(person_name);
        }

        // ── Presence dot (online/offline) ───────────────────────────────────────
        // Shows immediately based on whatever PresenceManager already knows,
        // then stays live via the listener below if the contact's status
        // changes while this chat is open.
        updatePresenceDot();
        PresenceManager.addListener(presenceChangeListener);
        TypingStateManager.addListener(chatTypingListener);

        // ── Video call visibility ──────────────────────────────────────────────
        if (contact_type != null && contact_type.equalsIgnoreCase("appointment")) {
            iv_video_call.setVisibility(View.GONE);
        } else {
            iv_video_call.setVisibility(View.VISIBLE);
        }

        // ── RecyclerView + adapter ─────────────────────────────────────────────
        rv_messagesList.setLayoutManager(new GridLayoutManager(getContext(), 1));
        // Set programmatically (overrides whatever recyclerview.xml has) so
        // the typing bubble's bouncing dots are never clipped by the
        // RecyclerView's own bounds/padding.
        rv_messagesList.setClipChildren(false);
        rv_messagesList.setClipToPadding(false);
        adapter = new MessageListAdapter(
                getContext(), message_list, person_name, time_list);
        rv_messagesList.setAdapter(adapter);

        // ── Search filter ──────────────────────────────────────────────────────
        etSearchChat.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
                iv_search_cancel.setImageDrawable(
                        s.toString().isEmpty()
                                ? getContext().getDrawable(R.drawable.search_groups)
                                : getContext().getDrawable(R.drawable.simple_cancel)
                );
            }
        });

        // ── Delay first MAM load by 1500 ms ───────────────────────────────────
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && !isDetached() && getActivity() != null) {
                new ChatHistoryTask().execute();
            }
        }, 1500);

        return v;
    }

    // ── Send button alpha helper ───────────────────────────────────────────────
    private void updateSendButtonState(String text) {
        boolean enabled = !text.trim().isEmpty();
        bt_sendMessage.setAlpha(enabled ? 1.0f : 0.5f);
        bt_sendMessage.setEnabled(enabled);
    }

    // ── Navigation ─────────────────────────────────────────────────────────────
    private void loadBackPressed() {
        Constants.Chat_id = "";
        Chat frag = new Chat();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.id_framelayout, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Read markers (XEP-0333) — sends <displayed/> so server resets unread count
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Sends a "displayed" read marker for the most recently seen message
     * (lastSeenMessageId) to the contact currently open in this chat.
     * Safe to call even if lastSeenMessageId is still null - the underlying
     * sendDisplayedMarker() no-ops in that case.
     */
    private void sendReadMarkerForLastSeenMessage() {
        if (lastSeenMessageId == null || lastSeenMessageId.isEmpty()) return;
        ChatConnection.sendDisplayedMarker(contactJid, lastSeenMessageId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Typing indicator (XEP-0085) — composing / paused / gone
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Sends "composing" if more than COMPOSING_THROTTLE_MS has passed since
     * the last one was sent (per spec: at most once every 3 seconds while
     * the user keeps typing).
     */
    private void sendComposingThrottled() {
        long now = System.currentTimeMillis();
        if (now - lastComposingSentAt < COMPOSING_THROTTLE_MS && isCurrentlyComposing) {
            return; // throttled - already sent recently and still composing
        }

        ChatConnection.sendChatState(contactJid, ChatConnection.CHAT_STATE_COMPOSING);
        lastComposingSentAt = now;
        isCurrentlyComposing = true;
    }

    /**
     * (Re)schedules the "paused" send for 1 second from now. Called on
     * every keystroke - each call cancels the previous pending one, so
     * "paused" only actually fires once 1 full second passes with no
     * further keystrokes.
     */
    private void schedulePausedAfterInactivity() {
        typingPauseHandler.removeCallbacks(pausedRunnable);
        typingPauseHandler.postDelayed(pausedRunnable, 1000L);
    }

    /**
     * Sends "paused" - but only if we were actually in a composing state,
     * so we don't spam paused stanzas when the user wasn't typing anyway
     * (e.g. field already empty, or chat just opened).
     */
    private void sendPausedIfWasComposing() {
        typingPauseHandler.removeCallbacks(pausedRunnable);
        if (!isCurrentlyComposing) return;

        ChatConnection.sendChatState(contactJid, ChatConnection.CHAT_STATE_PAUSED);
        isCurrentlyComposing = false;
    }

    /**
     * Sends "gone" - call when the user leaves this chat screen.
     */
    private void sendGone() {
        typingPauseHandler.removeCallbacks(pausedRunnable);
        ChatConnection.sendChatState(contactJid, ChatConnection.CHAT_STATE_GONE);
        isCurrentlyComposing = false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Presence dot (online/offline) on the header avatar
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Updates header_presence_dot's color/visibility AND the
     * tv_presence_status text ("Online"/"Offline") based on
     * PresenceManager's current knowledge of contactJid's online state.
     * Safe to call repeatedly - this is what keeps both in sync and live.
     */
    private void updatePresenceDot() {
        if (header_presence_dot == null || contactJid == null) return;

        boolean isOnline = PresenceManager.isOnline(contactJid);

        header_presence_dot.setVisibility(View.VISIBLE);
        header_presence_dot.setBackgroundResource(
                isOnline ? R.drawable.green_circular : R.drawable.red_circular);

        if (tv_presence_status != null) {
            tv_presence_status.setVisibility(View.VISIBLE);
            tv_presence_status.setText(isOnline ? "Online" : "Offline");
        }
    }

    // Refreshes the dot the instant PresenceManager reports a change for
    // the contact currently open in this chat - ignores changes for any
    // other guid, since this screen only shows one contact's status.
    private final PresenceManager.PresenceChangeListener presenceChangeListener =
            (guid, isOnline) -> {
                if (guid.equals(contactJid)) {
                    updatePresenceDot();
                }
            };

    // Shows/hides the typing bubble (three-dot indicator) at the bottom of
    // the chat thread - only reacts to typing state for the contact
    // currently open in this chat, ignores everyone else.
    private final TypingStateManager.TypingChangeListener chatTypingListener =
            (guid, isTyping) -> {
                if (guid.equals(contactJid) && adapter != null) {
                    adapter.setTypingBubbleVisible(isTyping);
                    if (isTyping) {
                        rv_messagesList.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                }
            };

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    public void onPause() {
        super.onPause();
        sendGone();
        if (getActivity() != null && mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(
                        getContext().getApplicationContext());
        pref.edit().putString("CURRENTCHAT_JID", contactJid).apply();

        if (!ChatConnectionService.getState()
                .equals(ChatConnection.ConnectionState.CONNECTED)) {
            AndroidUtils.reconnecXMPPServer(getActivity());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        super.onResume();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;

                if (action.equals(ChatConnectionService.NEW_MESSAGE)) {
                    String from = intent
                            .getStringExtra(ChatConnectionService.BUNDLE_FROM_JID)
                            .split("@")[0];
                    String body =
                            intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY);
                    // Stanza id of the just-arrived message, set by
                    // ChatConnection's incoming listener (Constants.message_id /
                    // "message_id" extra) - needed so we can send a fresh
                    // <displayed/> marker for THIS message while chat is open.
                    String incomingMessageId = intent.getStringExtra("message_id");

                    if (from.equals(contactJid)) {
                        MessageDo chatMessage = new MessageDo();
                        chatMessage.setMessage(body);
                        chatMessage.setViewType(Constants.chat_RECEIVE);
                        chatMessage.setIscurrentchat(true);
                        chatMessage.setCreatedAt(AndroidUtils.getDateToString(
                                Calendar.getInstance().getTime(), "MMM dd, yyyy hh:mm a"));
                        chatMessage.setStanzaId(incomingMessageId);
                        User user = new User();
                        user.setNickname(contact_name);
                        chatMessage.setSender(user);

                        if (!isMessageExist(chatMessage)) {
                            time_list.add(chatMessage);
                            adapter.addMessage(chatMessage);
                            rv_messagesList.smoothScrollToPosition(
                                    message_list.size() - 1);

                            // Situation 2 (XEP-0333): a new message arrived
                            // while this chat is open on screen - mark it
                            // seen immediately.
                            if (incomingMessageId != null && !incomingMessageId.isEmpty()) {
                                lastSeenMessageId = incomingMessageId;
                                sendReadMarkerForLastSeenMessage();
                            }
                        }
                    } else {
                        Log.d(TAG, "Message from other JID: " + from);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(ChatConnectionService.NEW_MESSAGE);
        requireActivity().registerReceiver(
                mBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PresenceManager.removeListener(presenceChangeListener);
        TypingStateManager.removeListener(chatTypingListener);
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    // ── Load history into RecyclerView ────────────────────────────────────────
    void load_chat_history(String resp) {
        if (getContext() == null) return;
        rv_messagesList.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter.notifyDataSetChanged();
        if (!message_list.isEmpty()) {
            rv_messagesList.smoothScrollToPosition(message_list.size() - 1);
        }

        // Situation 1 (XEP-0333): message history just finished loading.
        // Send a single "displayed" marker for the last message - this
        // implicitly marks everything before it as read too, and is what
        // resets the unread count server-side.
        sendReadMarkerForLastSeenMessage();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ChatHistoryTask — fetches last 50 messages via MAM
    // ══════════════════════════════════════════════════════════════════════════

    public class ChatHistoryTask extends AsyncTask<Void, Void, String> {

        private static final int MAX_RECONNECT_ATTEMPTS = 2;
        private int reconnectAttempts = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            new ChatUnreadCountUpdateTask(currentJid, contactJid).execute("");
            progress_dialog = AndroidUtils.get_progress(getActivity());
        }

        @Override
        protected String doInBackground(Void... voids) {

            // ── 1. Connection guard ────────────────────────────────────────────
            if (ChatConnection.mConnection == null
                    || !ChatConnection.mConnection.isConnected()
                    || !ChatConnection.mConnection.isAuthenticated()) {

                Log.e("ChatHistoryTask", "Not connected — attempt "
                        + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);

                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++;

                    if (reconnectAttempts >= 2) {
                        showConnectionLostToast();
                    }

                    scheduleReconnectOnMainThread();
                } else {
                    Log.e("ChatHistoryTask",
                            "Max reconnect attempts reached — giving up");
                    showToastOnMain("Could not connect to chat. Please try again.");
                }
                return null;
            }

            // ── 2. MAM query ───────────────────────────────────────────────────
            try {
                String contactID = contactJid + "@" + Constants.XMPP_DOMAIN;
                String currentID = currentJid  + "@" + Constants.XMPP_DOMAIN;

                Jid jid = JidCreate.entityBareFrom(contactID);

                MamManager mamManager =
                        MamManager.getInstanceFor(ChatConnection.mConnection);

                MamManager.MamQueryArgs mamQueryArgs =
                        MamManager.MamQueryArgs.builder()
                                .limitResultsToJid(jid)
                                .setResultPageSizeTo(50)
                                .queryLastPage()
                                .build();

                MamManager.MamQuery mamQuery =
                        mamManager.queryArchive(mamQueryArgs);

                List<Forwarded<Message>> forwardedList =
                        mamQuery.getPage().getForwarded();

                if (forwardedList.isEmpty()) {
                    Log.d("ChatHistoryTask", "No messages in archive");
                    return "";
                }

                // ── 3. Parse forwarded messages ────────────────────────────────
                message_list.clear();
                time_list.clear();

                String outputFormat = "MMM dd, yyyy hh:mm a";
                SimpleDateFormat sdf = new SimpleDateFormat(outputFormat, Locale.US);

                for (Forwarded<Message> forwarded : forwardedList) {

                    Message msg  = forwarded.getForwardedStanza();
                    Date    stamp = forwarded.getDelayInformation().getStamp();

                    if (msg.getBody() == null || msg.getBody().isEmpty()) continue;

                    String from = msg.getFrom() != null
                            ? msg.getFrom().toString().split("/")[0]
                            : "";

                    String timeStamp = sdf.format(stamp);

                    MessageDo timeEntry = new MessageDo();
                    timeEntry.setCreatedAt(timeStamp);
                    time_list.add(timeEntry);

                    // Resolve this message's stanza id (XEP-0359 sid first,
                    // fallback to the basic id attribute) so we can later
                    // send a <displayed/> marker referencing it.
                    String stanzaId = null;
                    StanzaIdElement sid = msg.getExtension(StanzaIdElement.class);
                    if (sid != null) {
                        stanzaId = sid.getId();
                    }
                    if (stanzaId == null) {
                        stanzaId = msg.getStanzaId();
                    }

                    MessageDo chatMessage = new MessageDo();
                    chatMessage.setMessage(msg.getBody());
                    chatMessage.setIscurrentchat(false);
                    chatMessage.setCreatedAt(timeStamp);
                    chatMessage.setStanzaId(stanzaId);

                    User user = new User();
                    if (from.equalsIgnoreCase(currentID)) {
                        chatMessage.setViewType(Constants.chat_SENT);
                        user.setNickname(Constants.NAME);
                    } else {
                        chatMessage.setViewType(Constants.chat_RECEIVE);
                        user.setNickname(contact_name);
                    }
                    chatMessage.setSender(user);
                    message_list.add(chatMessage);

                    // Track the most recent message's id - forwardedList is
                    // in chronological order from queryLastPage(), so the
                    // last one processed here is the latest message overall.
                    if (stanzaId != null && !stanzaId.isEmpty()) {
                        lastSeenMessageId = stanzaId;
                    }

                    Log.d("ChatHistoryTask", "[" + chatMessage.getViewType() + "] "
                            + msg.getBody().substring(
                            0, Math.min(30, msg.getBody().length()))
                            + " @ " + timeStamp);
                }

                dismissDialogOnMainThread();

            } catch (XmppStringprepException
                     | XMPPException.XMPPErrorException
                     | SmackException.NotConnectedException
                     | SmackException.NoResponseException
                     | SmackException.NotLoggedInException
                     | InterruptedException e) {

                Log.e("ChatHistoryTask", "XMPP error: " + e.getMessage());

                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++;
                    if (reconnectAttempts >= 2) {
                        showConnectionLostToast();
                    }
                    scheduleReconnectOnMainThread();
                } else {
                    Log.e("ChatHistoryTask",
                            "Max reconnect attempts reached — giving up");
                    showToastOnMain("Could not connect to chat. Please try again.");
                }
                return null;

            } catch (Exception e) {
                Log.e("ChatHistoryTask", "Unexpected error: " + e.getMessage());
                return null;
            }

            return "";
        }

        @Override
        protected void onPostExecute(String response) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }

            isReconnecting          = false;
            hasShownConnectionToast = false;

            if (response != null) {
                load_chat_history("");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Reconnect helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void dismissDialogOnMainThread() {
        if (getActivity() == null || isDetached()) return;
        getActivity().runOnUiThread(() -> {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        });
    }

    private void showConnectionLostToast() {
        if (hasShownConnectionToast) return;
        hasShownConnectionToast = true;
        showToastOnMain(getString(R.string.chat_cnt_lost));
    }

    private void showToastOnMain(String msg) {
        if (getActivity() == null || isDetached()) return;
        getActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show());
    }

    private void scheduleReconnectOnMainThread() {
        if (getActivity() == null || isDetached()) return;

        if (isReconnecting) {
            Log.d("ChatReconnect", "Already reconnecting — skipping duplicate.");
            return;
        }

        isReconnecting = true;
        getActivity().runOnUiThread(this::updateChatConnection);
    }

    public void updateChatConnection() {
        if (getActivity() == null || isDetached()) return;

        SharedPreferences prefs = getDefaultSharedPreferences(getActivity());
        String uid = Constants.UID;
        if (!Constants.ROLE.equalsIgnoreCase("admin")) {
            uid = uid + "_" + Constants.USER_ID;
        }
        prefs.edit()
                .putString("xmpp_jid",      uid)
                .putString("xmpp_password", Constants.TOKEN)
                .putBoolean("xmpp_logged_in", true)
                .apply();

        if (mConnection == null) {
            mConnection = new ChatConnection(getActivity());
        }
        if (chatConnectionService == null) {
            chatConnectionService = new ChatConnectionService();
        }

        new JsonTask().execute(Constants.base_URL + "user/create/");

        try {
            if (ChatConnection.mConnection != null
                    && !ChatConnection.mConnection.isConnected()) {

                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        try {
                            ChatConnection.mConnection.connect();
                            ChatConnection.mConnection.login();
                            return true;
                        } catch (Exception e) {
                            Log.e("ChatConnection", "Reconnect failed: " + e.getMessage());
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        isReconnecting          = false;
                        hasShownConnectionToast = false;

                        if (success && isAdded()
                                && !isDetached() && getActivity() != null) {
                            Log.d("ChatConnection",
                                    "Reconnect OK — retrying ChatHistoryTask");
                            new ChatHistoryTask().execute();
                        } else {
                            Log.e("ChatConnection",
                                    "Reconnect failed — not retrying");
                        }
                    }
                }.execute();

            } else {
                isReconnecting          = false;
                hasShownConnectionToast = false;
            }

        } catch (Exception e) {
            Log.e("ChatConnection", "Reconnect setup failed: " + e.getMessage());
            isReconnecting          = false;
            hasShownConnectionToast = false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Unread count update (called from ChatHistoryTask.onPreExecute)
    // ══════════════════════════════════════════════════════════════════════════

    class ChatUnreadCountUpdateTask extends AsyncTask<String, String, String> {

        String url = "";

        ChatUnreadCountUpdateTask(String currentJID, String JID) {
            super();
            this.url = "https://" + Constants.XMPP_DOMAIN
                    + "/unread-timestamp/" + currentJID + File.separator + JID;
            Log.d("GET UNREAD COUNT", this.url);
        }

        @Override
        protected String doInBackground(String... strings) {
            return requestUnreadCount(url);
        }

        @Override
        protected void onPostExecute(String response) {
            // handle response if needed
        }
    }

    private String requestUnreadCount(String url) {
        String data = "";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            Log.d("REQUEST_COUNT", String.valueOf(code));
            if (code == 200) {
                InputStream in = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);
                int ch;
                StringBuilder sb = new StringBuilder();
                while ((ch = isr.read()) != -1) sb.append((char) ch);
                data = sb.toString();
            }
        } catch (Exception e) {
            AndroidUtils.logMsg(e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return data;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  XMPP user/create registration task
    // ══════════════════════════════════════════════════════════════════════════

    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                if (mConnection == null) {
                    Log.e("JsonTask", "mConnection is null — skipping XMPP login");
                    return "";
                }
                mConnection.connect();
            } catch (IOException | SmackException | XMPPException e) {
                Log.d("Chat Error",
                        "Something went wrong while connecting: " + e.getMessage());
                e.fillInStackTrace();
                if (chatConnectionService != null) {
                    chatConnectionService.stopSelf();
                }
            }
            return "";
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Duplicate message guard
    // ══════════════════════════════════════════════════════════════════════════

    private boolean isMessageExist(MessageDo chatMessage) {
        for (MessageDo message : message_list) {
            if (message.getMessage() != null
                    && chatMessage.getMessage() != null
                    && message.getMessage().equals(chatMessage.getMessage())
                    && message.getViewType() != null
                    && message.getViewType().equals(chatMessage.getViewType())
                    && message.getSender() != null
                    && chatMessage.getSender() != null
                    && Objects.equals(message.getSender().getNickname(),
                    chatMessage.getSender().getNickname())
                    && Objects.equals(message.getCreatedAt(),
                    chatMessage.getCreatedAt())) {
                return true;
            }
        }
        return false;
    }
}