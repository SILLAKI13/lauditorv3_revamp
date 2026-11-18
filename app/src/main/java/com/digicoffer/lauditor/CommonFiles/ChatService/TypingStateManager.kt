package com.digicoffer.lauditor.CommonFiles.ChatService

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.jivesoftware.smack.packet.Message
import java.util.concurrent.ConcurrentHashMap

class TypingStateManager {

    interface TypingChangeListener {
        fun onTypingChanged(guid: String, isTyping: Boolean)
    }

    companion object {
        private const val TAG = "TypingStateManager"
        private const val TYPING_TIMEOUT_MS = 5000L

        private val typingGuids: MutableSet<String> = ConcurrentHashMap.newKeySet()
        private val listeners: MutableSet<TypingChangeListener> = ConcurrentHashMap.newKeySet()
        private val timeoutRunnables: MutableMap<String, Runnable> = ConcurrentHashMap()
        private val mainHandler = Handler(Looper.getMainLooper())

        @JvmStatic
        fun addListener(listener: TypingChangeListener) {
            listeners.add(listener)
        }

        @JvmStatic
        fun removeListener(listener: TypingChangeListener) {
            listeners.remove(listener)
        }

        private fun notifyListeners(guid: String, isTyping: Boolean) {
            mainHandler.post {
                for (l in listeners) {
                    l.onTypingChanged(guid, isTyping)
                }
            }
        }

        @JvmStatic
        fun handleIncomingMessage(message: Message?, fromGuid: String?): Boolean {
            if (message == null || fromGuid.isNullOrEmpty()) return false

            // 1. NEVER treat error stanzas as typing notifications (error stanzas echo composing payloads)
            if (message.type == Message.Type.error) {
                return false
            }

            // 2. NEVER treat messages from the logged-in user as partner typing
            if (isSelf(fromGuid)) {
                return false
            }

            // 3. If a regular message with content arrives, the user is no longer typing
            if (!message.body.isNullOrEmpty()) {
                clearTypingFor(fromGuid)
                return false
            }

            // 4. Handle Chat States (XEP-0085)
            val composing = message.getExtension<org.jivesoftware.smack.packet.ExtensionElement>(
                "composing",
                "http://jabber.org/protocol/chatstates"
            )
            if (composing != null) {
                setTyping(fromGuid, true)
                return true
            }

            val paused = message.getExtension<org.jivesoftware.smack.packet.ExtensionElement>(
                "paused",
                "http://jabber.org/protocol/chatstates"
            )
            if (paused != null) {
                setTyping(fromGuid, false)
                return true
            }

            val gone = message.getExtension<org.jivesoftware.smack.packet.ExtensionElement>(
                "gone",
                "http://jabber.org/protocol/chatstates"
            )
            if (gone != null) {
                setTyping(fromGuid, false)
                return true
            }

            return false
        }

        private fun isSelf(guid: String): Boolean {
            val myLocal = ChatConnection.mConnection?.user?.localpartOrNull?.toString()
            if (!myLocal.isNullOrEmpty() && guid.equals(myLocal, ignoreCase = true)) return true
            val myUid = Constants.UID
            if (!myUid.isNullOrEmpty() && (guid.equals(myUid, ignoreCase = true) || guid.startsWith("${myUid}_"))) return true
            val myUserId = Constants.USER_ID
            if (!myUserId.isNullOrEmpty() && guid.contains(myUserId)) return true
            return false
        }

        @JvmStatic
        fun clearTypingFor(guid: String?) {
            if (guid.isNullOrEmpty()) return
            setTyping(guid, false)
        }

        private fun setTyping(guid: String, isTyping: Boolean) {
            // Cancel any pending timeout for this guid
            timeoutRunnables.remove(guid)?.let { mainHandler.removeCallbacks(it) }

            if (isTyping) {
                typingGuids.add(guid)
                // Schedule auto-expiry after 5 seconds of inactivity
                val timeoutTask = Runnable {
                    Log.d(TAG, "Typing timeout expired for guid=$guid")
                    setTyping(guid, false)
                }
                timeoutRunnables[guid] = timeoutTask
                mainHandler.postDelayed(timeoutTask, TYPING_TIMEOUT_MS)
            } else {
                typingGuids.remove(guid)
            }

            Log.d(TAG, "guid=$guid isTyping=$isTyping")
            notifyListeners(guid, isTyping)
        }

        @JvmStatic
        fun isTyping(guid: String?): Boolean {
            return guid != null && typingGuids.contains(guid)
        }

        @JvmStatic
        fun clear() {
            for (runnable in timeoutRunnables.values) {
                mainHandler.removeCallbacks(runnable)
            }
            timeoutRunnables.clear()
            typingGuids.clear()
        }
    }
}
