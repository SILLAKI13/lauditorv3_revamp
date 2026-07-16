package com.digicoffer.lauditor.CommonFiles.ChatService

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap

class ConversationMetaApi {

    class ConversationMeta(
        @JvmField val guid: String,
        @JvmField val lastMessage: String,
        @JvmField val lastMessageTimestamp: Long,
        @JvmField val unreadCount: String
    )

    interface MetaCallback {
        fun onMetaReady(metaMap: Map<String, ConversationMeta>)
    }

    companion object {
        @JvmStatic
        fun fetch(context: Context, chatJids: List<String>, callback: MetaCallback?) {
            object : AsyncTask<Void, Void, Map<String, ConversationMeta>>() {
                override fun doInBackground(vararg voids: Void?): Map<String, ConversationMeta> {
                    val raw = fetchRaw(context, chatJids)
                    return parse(raw)
                }

                override fun onPostExecute(result: Map<String, ConversationMeta>?) {
                    callback?.onMetaReady(result ?: HashMap())
                }
            }.execute()
        }

        private fun fetchRaw(context: Context, chatJids: List<String>): String? {
            var conn: HttpURLConnection? = null
            try {
                val currentUserJid = Constants.UID + "_" + Constants.USER_ID + Constants.VitacapeExtention
                if (currentUserJid.isEmpty()) {
                    Log.e("ConversationMetaApi", "current user jid is empty, skipping meta fetch")
                    return null
                }

                val body = JSONObject()
                val jidsArray = JSONArray()
                for (jid in chatJids) {
                    jidsArray.put(jid)
                }
                body.put("chat_jids", jidsArray)
                body.put("user_jid", currentUserJid)

                Log.d("ConversationMetaApi", "POST " + Constants.CONVERSATION_META_URL)
                Log.d("ConversationMetaApi", "Request body: " + body.toString())

                conn = URL(Constants.CONVERSATION_META_URL).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN)
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use { os ->
                    val input = body.toString().toByteArray(charset("utf-8"))
                    os.write(input, 0, input.size)
                }

                val code = conn.responseCode
                if (code != 200) {
                    Log.e("ConversationMetaApi", "conversation/meta returned $code")
                    return null
                }

                val sb = StringBuilder()
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                var line: String? = br.readLine()
                while (line != null) {
                    sb.append(line)
                    line = br.readLine()
                }
                br.close()

                Log.d("ConversationMetaApi", "Response code: $code")
                Log.d("ConversationMetaApi", "Response body: $sb")

                return sb.toString()
            } catch (e: Exception) {
                Log.e("ConversationMetaApi", "Meta fetch failed: " + e.message)
                return null
            } finally {
                conn?.disconnect()
            }
        }

        private fun parse(rawResponse: String?): Map<String, ConversationMeta> {
            val map = HashMap<String, ConversationMeta>()
            if (rawResponse == null) return map

            try {
                val result = JSONObject(rawResponse)
                if (!result.optBoolean("success", false)) return map

                val data = result.getJSONArray("data")

                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val chatJid = item.optString("chat_jid", "")
                    val guid = if (chatJid.contains("@")) {
                        chatJid.substring(0, chatJid.indexOf("@"))
                    } else {
                        chatJid
                    }

                    if (guid.isEmpty()) continue

                    val preview = item.optString("last_message_preview", "")
                    val timestampMillis = parseIsoTimestamp(item.optString("last_message_timestamp", ""))
                    val unreadCount = item.optInt("unread_count", 0).toString()

                    map[guid] = ConversationMeta(guid, preview, timestampMillis, unreadCount)
                }
            } catch (e: JSONException) {
                Log.e("ConversationMetaApi", "Meta parse error: " + e.message)
            }

            Log.d("ConversationMetaApi", "Parsed map size: " + map.size + ", keys: " + map.keys)
            return map
        }

        private fun parseIsoTimestamp(iso: String?): Long {
            if (iso.isNullOrEmpty()) return 0L
            try {
                return java.time.Instant.parse(iso).toEpochMilli()
            } catch (e: Exception) {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    return sdf.parse(iso)!!.time
                } catch (ex: Exception) {
                    return 0L
                }
            }
        }
    }
}
