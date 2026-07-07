package com.digicoffer.lauditor.CommonFiles.ChatService;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jivesoftware.smack.packet.Message;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XEP-0085 Chat State Notifications — tracks "is this guid currently typing"
 * dynamically for any contact, so both the chat-list page (ChatAdapter /
 * MembersAdapter) and the one-on-one chat screen (MessagesList) can read
 * the same live state.
 * <p>
 * Purely in-memory / real-time - nothing here is persisted, matching the
 * XEP-0085 spec (no backend API, no DB row for typing state).
 */
public class TypingStateManager {

    private static final String TAG = "TypingStateManager";

    // guids currently in the "composing" state.
    private static final Set<String> typingGuids = ConcurrentHashMap.newKeySet();

    public interface TypingChangeListener {
        void onTypingChanged(String guid, boolean isTyping);
    }

    // Listeners registered by UI (adapters / chat screen) to get notified
    // the moment typing state flips, so they can refresh just that row
    // instead of polling.
    private static final Set<TypingChangeListener> listeners = ConcurrentHashMap.newKeySet();

    public static void addListener(TypingChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(TypingChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners(String guid, boolean isTyping) {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (TypingChangeListener l : listeners) {
                l.onTypingChanged(guid, isTyping);
            }
        });
    }

    /**
     * Call this from the incoming-message listener in ChatConnection for
     * EVERY incoming message (body or not) - it detects and handles
     * composing/paused/gone extensions, and is a no-op for normal messages
     * that have none of these.
     *
     * @param message  the raw incoming Smack Message stanza
     * @param fromGuid the guid (local part of bare JID) the message is from
     * @return true if this message WAS a chat-state notification
     */
    public static boolean handleIncomingMessage(Message message, String fromGuid) {
        if (message == null || fromGuid == null || fromGuid.isEmpty()) return false;

        // Checked generically via getExtension(elementName, namespace) ->
        // ExtensionElement, rather than assuming a specific concrete type
        // (e.g. StandardExtensionElement) is what gets deserialized for
        // this namespace - safer across different Smack provider setups.
        org.jivesoftware.smack.packet.ExtensionElement composing =
                message.getExtension("composing", "http://jabber.org/protocol/chatstates");
        if (composing != null) {
            setTyping(fromGuid, true);
            return true;
        }

        org.jivesoftware.smack.packet.ExtensionElement paused =
                message.getExtension("paused", "http://jabber.org/protocol/chatstates");
        if (paused != null) {
            setTyping(fromGuid, false);
            return true;
        }

        org.jivesoftware.smack.packet.ExtensionElement gone =
                message.getExtension("gone", "http://jabber.org/protocol/chatstates");
        if (gone != null) {
            setTyping(fromGuid, false);
            return true;
        }

        return false;
    }

    private static void setTyping(String guid, boolean isTyping) {
        if (isTyping) {
            typingGuids.add(guid);
        } else {
            typingGuids.remove(guid);
        }

        Log.d(TAG, "guid=" + guid + " isTyping=" + isTyping);

        // Always notify, even on repeat composing stanzas (sender re-sends
        // every 3s while still typing) - harmless to re-trigger UI refresh.
        notifyListeners(guid, isTyping);
    }

    /**
     * @return true if the given guid is currently in the composing state.
     */
    public static boolean isTyping(String guid) {
        return guid != null && typingGuids.contains(guid);
    }

    /**
     * Clears all tracked typing state. Call on disconnect/logout.
     */
    public static void clear() {
        typingGuids.clear();
    }
}