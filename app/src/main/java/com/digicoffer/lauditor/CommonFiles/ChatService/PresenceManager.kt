package com.digicoffer.lauditor.CommonFiles.ChatService

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.jivesoftware.smack.StanzaListener
import org.jivesoftware.smack.filter.StanzaTypeFilter
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.json.JSONArray
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

class PresenceManager {

    interface PresenceChangeListener {
        fun onPresenceChanged(guid: String, isOnline: Boolean)
    }

    class PresenceStanzaListener(private val connection: XMPPTCPConnection) : StanzaListener {
        override fun processStanza(packet: Stanza) {
            if (packet !is Presence) return
            val presence = packet
            val type = presence.type
            val from = presence.from?.toString() ?: return
            if (from.isEmpty()) return

            if (type == Presence.Type.subscribe) {
                handleIncomingSubscribeRequest(from)
                return
            }

            updatePresenceMap(from, type)
        }

        private fun handleIncomingSubscribeRequest(fromJid: String) {
            try {
                val jid = JidCreate.from(fromJid)

                val accept = Presence(Presence.Type.subscribed)
                accept.to = jid
                connection.sendStanza(accept)

                val subscribeBack = Presence(Presence.Type.subscribe)
                subscribeBack.to = jid
                connection.sendStanza(subscribeBack)

                Log.d(TAG, "Auto-accepted + subscribed back to $fromJid")
            } catch (e: Exception) {
                Log.e(TAG, "handleIncomingSubscribeRequest() failed for $fromJid: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "PresenceManager"

        private val presenceMap: MutableMap<String, MutableSet<String>> = ConcurrentHashMap()
        private val listeners: MutableSet<PresenceChangeListener> = ConcurrentHashMap.newKeySet()

        @JvmStatic
        fun addListener(listener: PresenceChangeListener) {
            listeners.add(listener)
        }

        @JvmStatic
        fun removeListener(listener: PresenceChangeListener) {
            listeners.remove(listener)
        }

        private fun notifyListeners(guid: String, isOnline: Boolean) {
            Handler(Looper.getMainLooper()).post {
                for (l in listeners) {
                    l.onPresenceChanged(guid, isOnline)
                }
            }
        }

        @JvmStatic
        fun ensureSubscriptions(connection: XMPPTCPConnection?, allContactJidsJson: JSONArray?) {
            if (connection == null || !connection.isAuthenticated) {
                Log.e(TAG, "ensureSubscriptions() — not authenticated, skipping")
                return
            }
            if (allContactJidsJson == null) {
                Log.d(TAG, "ensureSubscriptions() — contact list is null, nothing to do")
                return
            }

            val allContactJids = ArrayList<String>()
            for (i in 0 until allContactJidsJson.length()) {
                val jid = allContactJidsJson.optString(i, "")
                if (jid.isNotEmpty()) allContactJids.add(jid)
            }

            Thread {
                try {
                    val roster = Roster.getInstanceFor(connection)
                    val alreadySubscribed = HashSet<String>()
                    for (entry in roster.entries) {
                        alreadySubscribed.add(entry.jid.asBareJid().toString())
                    }

                    for (contactJid in allContactJids) {
                        if (contactJid.isEmpty()) continue
                        if (alreadySubscribed.contains(contactJid)) continue

                        try {
                            val jid = JidCreate.from(contactJid)
                            val subscribe = Presence(Presence.Type.subscribe)
                            subscribe.to = jid
                            connection.sendStanza(subscribe)
                            Log.d(TAG, "ensureSubscriptions() — sent subscribe to $contactJid")
                        } catch (e: Exception) {
                            Log.e(TAG, "ensureSubscriptions() — failed for $contactJid: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "ensureSubscriptions() — roster fetch failed: ${e.message}")
                }
            }.start()
        }

        private fun updatePresenceMap(from: String, type: Presence.Type) {
            val bareJid = if (from.contains("/")) from.split("/")[0] else from
            val resource = if (from.contains("/")) from.split("/", limit = 2)[1] else ""
            val guid = if (bareJid.contains("@")) bareJid.split("@")[0] else bareJid

            val resources = presenceMap.computeIfAbsent(guid) { ConcurrentHashMap.newKeySet() }

            if (type == Presence.Type.unavailable) {
                resources.remove(resource)
            } else {
                resources.add(resource)
            }

            val isOnline = resources.isNotEmpty()
            Log.d(
                TAG,
                "Presence update: guid=$guid resource=$resource type=$type -> isOnline=$isOnline"
            )
            notifyListeners(guid, isOnline)
        }

        @JvmStatic
        fun isOnline(guid: String?): Boolean {
            if (guid == null) return false
            val resources = presenceMap[guid]
            return resources != null && resources.isNotEmpty()
        }

        @JvmStatic
        fun registerOn(connection: XMPPTCPConnection): PresenceStanzaListener {
            val listener = PresenceStanzaListener(connection)
            connection.addAsyncStanzaListener(listener, StanzaTypeFilter(Presence::class.java))
            Log.d(TAG, "registerOn() — presence listener registered")
            return listener
        }

        @JvmStatic
        fun clear() {
            presenceMap.clear()
        }
    }
}
