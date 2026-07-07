package com.digicoffer.lauditor.CommonFiles.ChatService;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.RECEIVER_EXPORTED;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.Chat_id;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.fromjid;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel;
import com.digicoffer.lauditor.LoginActivity.ViewModels.ConfirmationLogin;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.sid.element.StanzaIdElement;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class ChatConnection implements ConnectionListener {

    private static final String TAG = "RoosterConnection";

    private static ChatConnection instance;

    Context context;
    private static Context mApplicationContext = null;
    private final String mUsername;
    private final String mPassword;
    private final String mServiceName;

    public static XMPPTCPConnection mConnection;

    private static BroadcastReceiver uiThreadMessageReceiver;
    private static IncomingChatMessageListener incomingListener;
    private static NotificationManager mManager;
    RecyclerView messageListAdapter;
    private ArrayList<UnreadCountModel> unreadcountlist = new ArrayList<>();

    public static enum LoggedInState {LOGGED_IN, LOGGED_OUT;}

    public static synchronized ChatConnection getInstance(Context context) {
        if (instance == null) {
            instance = new ChatConnection(context);
        }
        return instance;
    }

    public enum ConnectionState {CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED;}

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.disconnect();
            instance = null;
        }
    }

    public ChatConnection(Context context) {
        this.context = context;
        Log.d(TAG, "RoosterConnection Constructor called.");
        mApplicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid", null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password", null);

        if (jid != null) {
            mUsername = jid + "@" + Constants.XMPP_DOMAIN;
            mServiceName = Constants.XMPP_DOMAIN;
        } else {
            mUsername = "";
            mServiceName = "";
        }
    }

    public SSLContext createContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            AndroidUtils.logMsg(e.getMessage());
            return null;
        }
    }

    public void connect() throws IOException, XMPPException, SmackException {
        Log.d(TAG, "Connecting to server " + mServiceName + " as " + mUsername);

        if (mUsername == null || mUsername.isEmpty() || mPassword == null || mPassword.isEmpty()) {
            Log.e(TAG, "connect() aborted — xmpp_jid or xmpp_password is null/empty in SharedPreferences. " +
                    "Ensure saveXmppPreferences() in LoginActivity has run before connect() is called.");
            ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
            return;
        }

        try {
            SSLContext sslContext = createContext();
            SSLEngine engine = sslContext.createSSLEngine();

            XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(Constants.XMPP_DOMAIN)
                    .setHost(Constants.XMPP_DOMAIN)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                    .setCustomSSLContext(sslContext)
                    .setSendPresence(true)
                    .setCompressionEnabled(true)
                    .build();

            setupUiThreadBroadCastMessageReceiver();

            if (mConnection != null && (mConnection.isConnected() || mConnection.isAuthenticated())) {
                Log.d(TAG, "connect() — mConnection already connected/authenticated, skipping reconnect.");
                ChatConnectionService.sConnectionState = ConnectionState.CONNECTED;
                return;
            }

            if (mConnection != null && !mConnection.isConnected()) {
                Log.d(TAG, "connect() — mConnection exists but is disconnected. Resetting for fresh connect.");
                try {
                    mConnection.disconnect();
                } catch (Exception ignored) {
                }
                mConnection = null;
            }

            if (mConnection == null) {
                mConnection = new XMPPTCPConnection(conf);
                mConnection.addConnectionListener(this);
                Log.d(TAG, "connect() — new XMPPTCPConnection created.");
            }

            ChatConnectionService.sConnectionState = ConnectionState.CONNECTING;
            Log.d(TAG, "connect() — state set to CONNECTING");

            mConnection.connect();

            mConnection.login(mUsername, mPassword);

            Log.d(TAG, "connect() — login succeeded for " + mUsername);

            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("online");
            presence.setMode(Presence.Mode.available);

            // Replaces the old no-op PresencePacketListener registration -
            // PresenceManager handles auto-accept/subscribe-back and
            // online-state tracking for incoming presence stanzas.
            PresenceManager.registerOn(mConnection);

            // One-time (per contact pair) subscribe to anyone not already
            // in the roster, so we receive their online/offline presence
            // going forward. Safe to call every connect() - it no-ops for
            // contacts already subscribed.
            PresenceManager.ensureSubscriptions(mConnection, Constants.totalchatclientlist);

            ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
            reconnectionManager.setEnabledPerDefault(true);
            reconnectionManager.enableAutomaticReconnection();

            if (incomingListener != null) {
                ChatManager.getInstanceFor(mConnection).removeIncomingListener(incomingListener);
            }

            incomingListener = new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid messageFrom, Message message, Chat chat) {
                    String from = message.getFrom().toString();
                    String contactJid = from.contains("/") ? from.split("/")[0] : from;

                    fromjid = contactJid;

                    String[] slashSplit = from.split("/");
                    String bareJid = slashSplit[0];
                    String uniqueId = slashSplit.length > 1 ? slashSplit[1] : "";

                    String username = bareJid.split("@")[0];

                    // XEP-0085 chat states (composing/paused/gone) arrive as
                    // regular messages with no body - detect and route them
                    // to TypingStateManager, then stop here. They should not
                    // trigger notifications, unread-count updates, or be
                    // added to message_list.
                    if (TypingStateManager.handleIncomingMessage(message, username)) {
                        return;
                    }

                    String finalId = uniqueId.isEmpty()
                            ? username
                            : username + "_" + uniqueId;
                    Constants.updatedFromIid = finalId;

                    String messageId = null;

                    StanzaIdElement sid = message.getExtension(StanzaIdElement.class);
                    if (sid != null) {
                        messageId = sid.getId();
                    }

                    if (messageId == null) {
                        messageId = message.getStanzaId();
                    }

                    if (messageId == null) {
                        messageId = "local-" + System.currentTimeMillis();
                    }

                    Log.e("MESSAGE_ID", messageId);
                    Constants.message_id = messageId;
                    Log.e("XMPP_MESSAGE_ID", "Received ID: " + Constants.message_id);

                    Intent intent = new Intent(ChatConnectionService.NEW_MESSAGE);
                    intent.setPackage(mApplicationContext.getPackageName());
                    intent.putExtra("message_id", Constants.message_id);
                    intent.putExtra(ChatConnectionService.BUNDLE_FROM_JID, contactJid);
                    intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY, message.getBody());
                    intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, message.getSubject());
                    mApplicationContext.sendBroadcast(intent);

                    if (!fromjid.equals(Chat_id))
                        new ChatCountApi(context).callupdatecount();

                    show_notification(contactJid.split("@")[0], message.getBody(), message.getSubject());
                }
            };

            ChatManager.getInstanceFor(mConnection).addIncomingListener(incomingListener);

            // ── XEP-0085 chat states (composing/paused/gone) ────────────────────
            // ChatManager's IncomingChatMessageListener filters out any message
            // with no <body> (see Smack's ChatManager.MESSAGE_FILTER source) -
            // composing/paused/gone stanzas have no body, so they NEVER reach
            // incomingListener above. Registering a raw stanza listener here
            // instead, filtered only by message type, so these always arrive.
            mConnection.addAsyncStanzaListener(stanza -> {
                Message msg = (Message) stanza;
                if (msg.getFrom() == null) return;

                String from = msg.getFrom().toString();
                String bareJid = from.contains("/") ? from.split("/")[0] : from;
                String guid = bareJid.contains("@") ? bareJid.split("@")[0] : bareJid;

                TypingStateManager.handleIncomingMessage(msg, guid);
            }, new org.jivesoftware.smack.filter.StanzaTypeFilter(Message.class));

        } catch (Exception e) {
            Log.e(TAG, "connect() FAILED — " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
            Log.d(TAG, "connect() — state set to DISCONNECTED after exception");

            if (e instanceof IOException) throw (IOException) e;
            if (e instanceof XMPPException) throw (XMPPException) e;
            if (e instanceof SmackException) throw (SmackException) e;
            throw new SmackException.NotConnectedException(e.getMessage());
        }
    }

    public static void loginUser(Context context, String username, String password) {
        if (instance == null) {
            instance = new ChatConnection(context);
        }

        new Thread(() -> {
            try {
                SSLContext sslContext = instance.createContext();
                XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                        .setXmppDomain(Constants.XMPP_DOMAIN)
                        .setHost(Constants.XMPP_DOMAIN)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                        .setCustomSSLContext(sslContext)
                        .setSendPresence(true)
                        .setCompressionEnabled(true)
                        .build();

                if (mConnection != null && !mConnection.isConnected()) {
                    Log.d(TAG, "loginUser() — mConnection exists but disconnected. Resetting.");
                    try {
                        mConnection.disconnect();
                    } catch (Exception ignored) {}
                    mConnection = null;
                }

                if (mConnection == null) {
                    mConnection = new XMPPTCPConnection(conf);
                    mConnection.addConnectionListener(instance);
                }

                if (mConnection.isConnected()) mConnection.disconnect();

                ChatConnectionService.sConnectionState = ConnectionState.CONNECTING;

                mConnection.connect();

                mConnection.login(username, password);

                Log.d(TAG, "loginUser() — login succeeded for " + username);

                setupUiThreadBroadCastMessageReceiver();
                Presence presence = new Presence(Presence.Type.available);
                presence.setStatus("online");
                presence.setMode(Presence.Mode.available);

                PresenceManager.registerOn(mConnection);
                PresenceManager.ensureSubscriptions(mConnection, Constants.totalchatclientlist);

                ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
                reconnectionManager.setEnabledPerDefault(true);
                reconnectionManager.enableAutomaticReconnection();

                if (incomingListener != null) {
                    ChatManager.getInstanceFor(mConnection).removeIncomingListener(incomingListener);
                }

                incomingListener = new IncomingChatMessageListener() {
                    @Override
                    public void newIncomingMessage(EntityBareJid messageFrom, Message message, Chat chat) {
                        String from = message.getFrom().toString();
                        String contactJid = from.contains("/") ? from.split("/")[0] : from;
                        fromjid = contactJid;

                        String[] slashSplit = from.split("/");
                        String bareJid = slashSplit[0];
                        String uniqueId = slashSplit.length > 1 ? slashSplit[1] : "";

                        String username2 = bareJid.split("@")[0];

                        // Same chat-state (XEP-0085) detection as in connect()'s
                        // listener - stop here for composing/paused/gone stanzas.
                        if (TypingStateManager.handleIncomingMessage(message, username2)) {
                            return;
                        }

                        String finalId = uniqueId.isEmpty()
                                ? username2
                                : username2 + "_" + uniqueId;
                        Constants.updatedFromIid = finalId;

                        String messageId = null;

                        StanzaIdElement sid = message.getExtension(StanzaIdElement.class);
                        if (sid != null) {
                            messageId = sid.getId();
                        }

                        if (messageId == null) {
                            messageId = message.getStanzaId();
                        }

                        if (messageId == null) {
                            messageId = "local-" + System.currentTimeMillis();
                        }

                        Log.e("MESSAGE_ID", messageId);
                        Constants.message_id = messageId;
                        show_notification(contactJid.split("@")[0], message.getBody(), message.getSubject());

                        Intent intent = new Intent(ChatConnectionService.NEW_MESSAGE);
                        intent.setPackage(mApplicationContext.getPackageName());
                        intent.putExtra(ChatConnectionService.BUNDLE_FROM_JID, contactJid);
                        intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY, message.getBody());
                        intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, message.getSubject());
                        mApplicationContext.sendBroadcast(intent);

                        if (!fromjid.equals(Chat_id))
                            new ChatCountApi(context).callupdatecount();
                    }
                };

                ChatManager.getInstanceFor(mConnection).addIncomingListener(incomingListener);

                // Same chat-state (XEP-0085) fix as in connect() above -
                // ChatManager filters out bodyless messages, so a raw
                // stanza listener is needed for composing/paused/gone.
                mConnection.addAsyncStanzaListener(stanza -> {
                    Message msg = (Message) stanza;
                    if (msg.getFrom() == null) return;

                    String from2 = msg.getFrom().toString();
                    String bareJid2 = from2.contains("/") ? from2.split("/")[0] : from2;
                    String guid2 = bareJid2.contains("@") ? bareJid2.split("@")[0] : bareJid2;

                    TypingStateManager.handleIncomingMessage(msg, guid2);
                }, new org.jivesoftware.smack.filter.StanzaTypeFilter(Message.class));

            } catch (Exception e) {
                Log.e(TAG, "loginUser() FAILED for " + username + " — " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
                ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
            }
        }).start();
    }

    private static void setupUiThreadBroadCastMessageReceiver() {
        if (uiThreadMessageReceiver != null) {
            try {
                mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            } catch (Exception ignored) {
            }
            uiThreadMessageReceiver = null;
        }

        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ChatConnectionService.SEND_MESSAGE.equals(intent.getAction())) {
                    sendMessage(intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(ChatConnectionService.BUNDLE_TO),
                            intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT));
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChatConnectionService.SEND_MESSAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mApplicationContext.registerReceiver(uiThreadMessageReceiver, filter, RECEIVER_EXPORTED);
        } else {
            ContextCompat.registerReceiver(mApplicationContext, uiThreadMessageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    /**
     * Sends a chat message stanza with:
     *  - a unique stanza id (required so delivery receipts / read markers
     *    can reference this specific message)
     *  - a request xmlns="urn:xmpp:receipts" element, asking the
     *    receiver's client to send back a delivery receipt (single tick -> double tick)
     * <p>
     * The request element is built with StandardExtensionElement rather
     * than a typed Smack receipts class or a custom ExtensionElement
     * subclass - StandardExtensionElement is a concrete builder Smack has
     * shipped since 4.1 specifically for adding simple custom XML, with no
     * dependency on a version-specific toXML() signature.
     */
    private static void sendMessage(String body, String toJid, String subject) {
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(toJid + "@" + Constants.XMPP_DOMAIN);
            Chat chat = ChatManager.getInstanceFor(mConnection).chatWith(jid);

            Message message = new Message(jid, Message.Type.chat);

            // Unique stanza id - required for delivery receipts and read
            // markers to reference this exact message later.
            String stanzaId = UUID.randomUUID().toString();
            message.setStanzaId(stanzaId);

            message.setBody(body);
            message.setSubject(subject);

            // XEP-0184 delivery receipt request: request xmlns="urn:xmpp:receipts"
            message.addExtension(
                    org.jivesoftware.smack.packet.StandardExtensionElement
                            .builder("request", "urn:xmpp:receipts")
                            .build());

            chat.send(message);

            Log.d(TAG, "sendMessage() — sent id=" + stanzaId + " to=" + toJid);

        } catch (XmppStringprepException | SmackException.NotConnectedException |
                 InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * XEP-0333 Chat Markers — sends a displayed read marker.
     * <p>
     * Call this once a chat is opened and the message history has loaded
     * (pass the id of the last/most recent message), and again any time a
     * new message arrives while the chat is still open. The server resets
     * the unread count for this conversation upon receiving this.
     *
     * @param toJid          bare JID local part of the other party (e.g. "guid", domain is appended)
     * @param lastMessageId  stanza id of the last message seen
     */
    public static void sendDisplayedMarker(String toJid, String lastMessageId) {
        if (toJid == null || toJid.isEmpty()
                || lastMessageId == null || lastMessageId.isEmpty()) {
            Log.d(TAG, "sendDisplayedMarker() — skipping, missing toJid or messageId");
            return;
        }

        if (mConnection == null || !mConnection.isConnected() || !mConnection.isAuthenticated()) {
            Log.e(TAG, "sendDisplayedMarker() — not connected, cannot send read marker");
            return;
        }

        try {
            EntityBareJid jid = JidCreate.entityBareFrom(toJid + "@" + Constants.XMPP_DOMAIN);

            Message message = new Message(jid, Message.Type.chat);
            message.addExtension(
                    org.jivesoftware.smack.packet.StandardExtensionElement
                            .builder("displayed", "urn:xmpp:chat-markers:0")
                            .addAttribute("id", lastMessageId)
                            .build());

            mConnection.sendStanza(message);

            Log.d(TAG, "sendDisplayedMarker() — sent id=" + lastMessageId + " to=" + toJid);

        } catch (Exception e) {
            Log.e(TAG, "sendDisplayedMarker() — failed: " + e.getMessage());
        }
    }

    /**
     * XEP-0085 Chat State Notifications — sends composing/paused/gone.
     * <p>
     * Purely real-time, client-to-client via ejabberd - no backend API,
     * no persistence. Same StandardExtensionElement technique used for
     * the receipt request / displayed marker above, for the same
     * Smack-version-safety reason.
     *
     * @param toJid bare JID local part of the other party (domain is appended)
     * @param state one of "composing", "paused", "gone" (use the constants below)
     */
    public static final String CHAT_STATE_COMPOSING = "composing";
    public static final String CHAT_STATE_PAUSED = "paused";
    public static final String CHAT_STATE_GONE = "gone";

    public static void sendChatState(String toJid, String state) {
        if (toJid == null || toJid.isEmpty() || state == null || state.isEmpty()) {
            return;
        }

        if (mConnection == null || !mConnection.isConnected() || !mConnection.isAuthenticated()) {
            Log.d(TAG, "sendChatState() — not connected, skipping");
            return;
        }

        try {
            EntityBareJid jid = JidCreate.entityBareFrom(toJid + "@" + Constants.XMPP_DOMAIN);

            Message message = new Message(jid, Message.Type.chat);
            message.addExtension(
                    org.jivesoftware.smack.packet.StandardExtensionElement
                            .builder(state, "http://jabber.org/protocol/chatstates")
                            .build());

            mConnection.sendStanza(message);

            Log.d(TAG, "sendChatState() — sent " + state + " to=" + toJid);

        } catch (Exception e) {
            Log.e(TAG, "sendChatState() — failed: " + e.getMessage());
        }
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + mServiceName);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        prefs.edit().putBoolean("xmpp_logged_in", false).apply();

        try {
            if (mConnection != null) {
                if (incomingListener != null) {
                    ChatManager.getInstanceFor(mConnection).removeIncomingListener(incomingListener);
                    incomingListener = null;
                }
                // Just closing the connection is sufficient - ejabberd detects
                // the disconnect and broadcasts "unavailable" presence to all
                // subscribed contacts automatically. No manual unavailable
                // stanza needed.
                mConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting: " + e.getMessage(), e);
        }

        // Reset local online-state and typing-state tracking so stale data
        // from this session doesn't leak into the next one.
        PresenceManager.clear();
        TypingStateManager.clear();

        mConnection = null;

        if (uiThreadMessageReceiver != null) {
            try {
                mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            } catch (Exception ignored) {
            }
            uiThreadMessageReceiver = null;
        }
    }

    @Override
    public void connected(XMPPConnection connection) {
        ChatConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "connected() — TCP connected. State = CONNECTED");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        ChatConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "authenticated() — login complete. State = CONNECTED (resumed=" + resumed + ")");
    }

    @Override
    public void connectionClosed() {
        ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "connectionClosed() — State = DISCONNECTED");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "connectionClosedOnError() — State = DISCONNECTED: " + e.toString());
    }

    public boolean isConnected() {
        return mConnection != null && mConnection.isConnected();
    }

    public boolean isAuthenticated() {
        return mConnection != null && mConnection.isAuthenticated();
    }

    @SuppressLint("NotificationPermission")
    public static void show_notification(String contact, String message, String subject) {
    }

    private static NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) mApplicationContext.getSystemService(NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public static Notification.Builder getAndroidChannelNotification(String title, String body, String notification_type, String[] notify_details) {
        PendingIntent penint;
        PendingIntent pendingIntentConfirm = null;
        String title_notification = body;
        String body_notification = "";
        if (body.contains("\n")) {
            String[] msg = body.split("\n");
            title_notification = msg[0];
            for (int i = 1; i < msg.length; i++) {
                body_notification += msg[i];
            }
        }
        if (notification_type.equals("B") || notification_type.equals("C") || notification_type.equals("P")) {
            Intent intent = new Intent(mApplicationContext, MainActivity.class);
            String biz_type = AndroidUtils.getSharedPreferenceStringData("proBizType", mApplicationContext);
            String rel_type = "Relationship";
            intent.putExtra("fragment", "Relationship");
            intent.putExtra("TYPE", notification_type);
            penint = PendingIntent.getActivity(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else if (notification_type.equals("chat")) {
            Intent intent = new Intent(mApplicationContext, MainActivity.class);
            intent.putExtra("fragment", "CHAT");
            intent.putExtra("CONTACT_JID", notify_details[0]);
            intent.putExtra("CONTACT_NAME", notify_details[1]);
            body_notification = title_notification;
            title_notification = notify_details[1];
            penint = PendingIntent.getActivity(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            Intent intent = new Intent(mApplicationContext, ConfirmationLogin.class);
            penint = PendingIntent.getActivity(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent intentConfirm = new Intent(mApplicationContext, ConfirmationLogin.class);
            intentConfirm.setAction("CONFIRM");
            intentConfirm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntentConfirm = PendingIntent.getActivity(mApplicationContext, 0, intentConfirm, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder notification = new Notification.Builder(mApplicationContext.getApplicationContext(), "com.digisec.digicoffer")
                    .setContentTitle(title_notification)
                    .setContentText(body_notification)
                    .setSmallIcon(R.mipmap.trans_logo)
                    .setAutoCancel(true)
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(body_notification))
                    .setContentIntent(penint)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);

            notification.setContentIntent(penint).getNotification();
            if (Build.VERSION.SDK_INT >= 21) {
                notification.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE);
                notification.setPriority(Notification.PRIORITY_HIGH);
            }
            return notification;
        }
        return null;
    }

    private String extractUserWithId(String from) {
        if (from == null || from.isEmpty()) return "";

        String[] slashSplit = from.split("/");
        String bareJid = slashSplit[0];
        String uniqueId = slashSplit.length > 1 ? slashSplit[1] : "";

        String username = bareJid.contains("@")
                ? bareJid.split("@")[0]
                : bareJid;

        return uniqueId.isEmpty()
                ? username
                : username + "_" + uniqueId;
    }

    public static NotificationCompat.Builder getNotificationOldMobile(String title, String message, String notification_type, String[] notify_details) {
        PendingIntent penint;
        PendingIntent pendingIntentConfirm = null;
        String title_notification = message;
        String body_notification = "";
        if (message.contains("\n")) {
            String[] msg = message.split("\n");
            title_notification = msg[0];
            for (int i = 1; i < msg.length; i++) {
                title_notification += msg[i];
            }
        }
        if (notification_type.equals("B") || notification_type.equals("C") || notification_type.equals("P")) {
            Intent intent = new Intent(mApplicationContext, MainActivity.class);
            intent.putExtra("fragment", "Relationship");
            intent.putExtra("TYPE", notification_type);
            penint = PendingIntent.getActivity(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else if (notification_type.equals("chat")) {
            Intent intent = new Intent(mApplicationContext, MainActivity.class);
            intent.putExtra("fragment", "CHAT");
            intent.putExtra("EXTRA_CONTACT_JID", notify_details[0]);
            intent.putExtra("EXTRA_CONTACT_NAME", notify_details[1]);
            intent.putExtra("EXTRA_CONTACT_TYPE", "relationship");
            penint = PendingIntent.getActivity(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            Intent intent = new Intent(mApplicationContext, ConfirmationLogin.class);
            penint = PendingIntent.getActivity(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent intentConfirm = new Intent(mApplicationContext, ConfirmationLogin.class);
            intentConfirm.setAction("CONFIRM");
            intentConfirm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntentConfirm = PendingIntent.getActivity(mApplicationContext, 0, intentConfirm, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(mApplicationContext)
                .setContentTitle(title_notification)
                .setContentText(body_notification)
                .setSmallIcon(R.mipmap.trans_logo)
                .setContentIntent(penint)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body_notification));
        if (Build.VERSION.SDK_INT >= 21) {
            notification.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE);
            notification.setPriority(Notification.PRIORITY_HIGH);
        }
        return notification;
    }

    public static void getUserStatus(String toJid) {
        mConnection.setFromMode(XMPPConnection.FromMode.valueOf(toJid));
        mConnection.getFromMode();
    }
}


// PresencePacketListener
class PresencePacketListener implements StanzaListener {
    private Context context;

    PresencePacketListener(Context context) {
        this.context = context;
    }

    @Override
    public void processStanza(Stanza packet) {
        Presence presence = (Presence) packet;
        Presence.Type presenceType = presence.getType();
        // Do sth with presence
    }
}