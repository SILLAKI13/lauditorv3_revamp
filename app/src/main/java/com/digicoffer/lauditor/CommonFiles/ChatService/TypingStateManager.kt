package com.digicoffer.lauditor.CommonFiles.ChatService

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.jivesoftware.smack.packet.Message
import java.util.concurrent.ConcurrentHashMap

class TypingStateManager {

    interface TypingChangeListener {
        fun onTypingChanged(guid: String, isTyping: Boolean)
    }

    companion object {
        private const val TAG = "TypingStateManager"

        private val typingGuids: MutableSet<String> = ConcurrentHashMap.newKeySet()
        private val listeners: MutableSet<TypingChangeListener> = ConcurrentHashMap.newKeySet()

        @JvmStatic
        fun addListener(listener: TypingChangeListener) {
            listeners.add(listener)
        }

        @JvmStatic
        fun removeListener(listener: TypingChangeListener) {
            listeners.remove(listener)
        }

        private fun notifyListeners(guid: String, isTyping: Boolean) {
            Handler(Looper.getMainLooper()).post {
                for (l in listeners) {
                    l.onTypingChanged(guid, isTyping)
                }
            }
        }

        @JvmStatic
        fun handleIncomingMessage(message: Message?, fromGuid: String?): Boolean {
            if (message == null || fromGuid.isNullOrEmpty()) return false

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

        private fun setTyping(guid: String, isTyping: Boolean) {
            if (isTyping) {
                typingGuids.add(guid)
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
            typingGuids.clear()
        }
    }
}
