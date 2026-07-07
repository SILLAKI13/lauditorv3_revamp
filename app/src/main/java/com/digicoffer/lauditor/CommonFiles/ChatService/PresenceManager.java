package com.digicoffer.lauditor.CommonFiles.ChatService;

import android.util.Log;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Presence / online-status tracking, per ejabberd roster subscriptions.
 * <p>
 * - On connect: fetch roster, auto-subscribe to any contact not yet
 *   subscribed (one-time per contact pair - ejabberd auto-accepts).
 * - Auto-accepts incoming subscription requests and subscribes back
 *   (mutual subscription).
 * - Tracks online state per guid across multiple device resources.
 * <p>
 * Uses Smack's built-in Roster class (org.jivesoftware.smack.roster.Roster)
 * for the roster fetch/subscribe step instead of hand-building IQ stanzas -
 * Roster has been a stable, core part of Smack since very early 4.x and is
 * the standard way to do this, so it avoids any custom IQ/ExtensionElement
 * version-compatibility risk.
 */
public class PresenceManager {

    private static final String TAG = "PresenceManager";

    // guid -> set of active device resources. A user is online if this
    // set is non-empty (handles the multi-device case).
    private static final java.util.Map<String, Set<String>> presenceMap =
            new ConcurrentHashMap<>();

    public interface PresenceChangeListener {
        void onPresenceChanged(String guid, boolean isOnline);
    }

    // Listeners registered by UI (adapters / chat screen) to get notified
    // the moment a guid's online state flips, so they can refresh just
    // that row instead of waiting for the next full reload.
    private static final Set<PresenceChangeListener> listeners = ConcurrentHashMap.newKeySet();

    public static void addListener(PresenceChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(PresenceChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners(String guid, boolean isOnline) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            for (PresenceChangeListener l : listeners) {
                l.onPresenceChanged(guid, isOnline);
            }
        });
    }

    /**
     * Call once right after successful login/authentication.
     * Fetches the roster and subscribes to any contact JID in allContactJids
     * that isn't already subscribed (subscription == "both"/"to"/"from").
     *
     * @param connection         the active, authenticated XMPPTCPConnection
     * @param allContactJidsJson JSONArray of bare JIDs (e.g. "guid@domain"),
     *                           matching the confirmed type of
     *                           Constants.totalchatclientlist - every contact
     *                           this user should have a standing subscription to
     */
    public static void ensureSubscriptions(XMPPTCPConnection connection,
                                           org.json.JSONArray allContactJidsJson) {
        if (connection == null || !connection.isAuthenticated()) {
            Log.e(TAG, "ensureSubscriptions() — not authenticated, skipping");
            return;
        }
        if (allContactJidsJson == null) {
            Log.d(TAG, "ensureSubscriptions() — contact list is null, nothing to do");
            return;
        }

        // Snapshot to a plain List<String> up front so the background
        // thread isn't reading a JSONArray that could mutate concurrently.
        java.util.List<String> allContactJids = new java.util.ArrayList<>();
        for (int i = 0; i < allContactJidsJson.length(); i++) {
            String jid = allContactJidsJson.optString(i, "");
            if (!jid.isEmpty()) allContactJids.add(jid);
        }

        new Thread(() -> {
            try {
                Roster roster = Roster.getInstanceFor(connection);

                // Build the set of bare JIDs we're already subscribed to
                // (subscription = both/to/from - any of these means a
                // subscribe stanza was already accepted in either direction).
                Set<String> alreadySubscribed = new HashSet<>();
                for (RosterEntry entry : roster.getEntries()) {
                    alreadySubscribed.add(entry.getJid().asBareJid().toString());
                }

                for (String contactJid : allContactJids) {
                    if (contactJid == null || contactJid.isEmpty()) continue;

                    if (alreadySubscribed.contains(contactJid)) {
                        continue; // already subscribed, nothing to do
                    }

                    try {
                        Jid jid = JidCreate.from(contactJid);
                        Presence subscribe = new Presence(Presence.Type.subscribe);
                        subscribe.setTo(jid);
                        connection.sendStanza(subscribe);

                        Log.d(TAG, "ensureSubscriptions() — sent subscribe to " + contactJid);
                    } catch (Exception e) {
                        Log.e(TAG, "ensureSubscriptions() — failed for " + contactJid + ": " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "ensureSubscriptions() — roster fetch failed: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Stanza listener for incoming Presence stanzas. Register this on the
     * connection (see registerOn() below) right after login.
     * <p>
     * Handles:
     *  - type=subscribe  -> auto-accept (send subscribed) + subscribe back
     *  - type=available/unavailable -> update the online-state map
     */
    public static class PresenceStanzaListener implements StanzaListener {

        private final XMPPTCPConnection connection;

        public PresenceStanzaListener(XMPPTCPConnection connection) {
            this.connection = connection;
        }

        @Override
        public void processStanza(Stanza packet) {
            if (!(packet instanceof Presence)) return;
            Presence presence = (Presence) packet;

            Presence.Type type = presence.getType();
            String from = presence.getFrom() != null ? presence.getFrom().toString() : null;
            if (from == null || from.isEmpty()) return;

            if (type == Presence.Type.subscribe) {
                handleIncomingSubscribeRequest(from);
                return;
            }

            // available / unavailable -> update online-state map
            updatePresenceMap(from, type);
        }

        private void handleIncomingSubscribeRequest(String fromJid) {
            try {
                Jid jid = JidCreate.from(fromJid);

                // 1. Accept their request
                Presence accept = new Presence(Presence.Type.subscribed);
                accept.setTo(jid);
                connection.sendStanza(accept);

                // 2. Subscribe back (mutual)
                Presence subscribeBack = new Presence(Presence.Type.subscribe);
                subscribeBack.setTo(jid);
                connection.sendStanza(subscribeBack);

                Log.d(TAG, "Auto-accepted + subscribed back to " + fromJid);

            } catch (Exception e) {
                Log.e(TAG, "handleIncomingSubscribeRequest() failed for " + fromJid + ": " + e.getMessage());
            }
        }
    }

    /**
     * Updates the online-state map for a raw "from" JID
     * (e.g. "prof_john@domain/device_resource" or "prof_john@domain").
     */
    private static void updatePresenceMap(String from, Presence.Type type) {
        String bareJid = from.contains("/") ? from.split("/")[0] : from;
        String resource = from.contains("/") ? from.split("/", 2)[1] : "";
        String guid = bareJid.contains("@") ? bareJid.split("@")[0] : bareJid;

        Set<String> resources = presenceMap.computeIfAbsent(guid, k -> ConcurrentHashMap.newKeySet());

        if (type == Presence.Type.unavailable) {
            resources.remove(resource);
        } else {
            // Treat any non-unavailable presence (available, or null type
            // which Smack uses for plain "available") as the device coming online.
            resources.add(resource);
        }

        boolean isOnline = !resources.isEmpty();
        Log.d(TAG, "Presence update: guid=" + guid + " resource=" + resource
                + " type=" + type + " -> isOnline=" + isOnline);

        notifyListeners(guid, isOnline);
    }

    /**
     * @return true if the given guid has at least one active device resource.
     */
    public static boolean isOnline(String guid) {
        Set<String> resources = presenceMap.get(guid);
        return resources != null && !resources.isEmpty();
    }

    /**
     * Registers the presence stanza listener on the given connection.
     * Call this once, right after login/authentication succeeds.
     */
    public static PresenceStanzaListener registerOn(XMPPTCPConnection connection) {
        PresenceStanzaListener listener = new PresenceStanzaListener(connection);
        connection.addAsyncStanzaListener(listener, new StanzaTypeFilter(Presence.class));
        Log.d(TAG, "registerOn() — presence listener registered");
        return listener;
    }

    /**
     * Clears all tracked online state. Call on disconnect/logout so stale
     * state doesn't leak into the next session.
     */
    public static void clear() {
        presenceMap.clear();
    }
}