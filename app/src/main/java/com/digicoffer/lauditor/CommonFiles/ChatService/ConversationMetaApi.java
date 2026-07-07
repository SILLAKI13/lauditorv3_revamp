package com.digicoffer.lauditor.CommonFiles.ChatService;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Single source of truth for conversation list data.
 * <p>
 * One GET call to /conversation/meta returns last-message + unread-count
 * info for every conversation in one shot. This class fetches it once and
 * hands back a Map keyed by guid (the local part of chat_jid, before the
 * "@" domain) so any adapter (ChatAdapter, MembersAdapter, etc.) can look
 * up a row's latest state without spawning per-row MAM threads.
 * <p>
 * Replaces: per-row MAM "fetchLastMessageFromMam" calls AND the old
 * ChatCountApi.callgetunreadcountlist()/handleConversationMetaResponse()
 * read-side parsing. ChatCountApi keeps only the write-side POST calls
 * (resetunreadcount / updateunreadcount) since /conversation/meta is GET-only.
 */
public class ConversationMetaApi {

    public static class ConversationMeta {
        public String guid;              // local part of chat_jid
        public String lastMessage;
        public long lastMessageTimestamp;
        public String unreadCount;

        public ConversationMeta(String guid, String lastMessage,
                                long lastMessageTimestamp, String unreadCount) {
            this.guid = guid;
            this.lastMessage = lastMessage;
            this.lastMessageTimestamp = lastMessageTimestamp;
            this.unreadCount = unreadCount;
        }
    }

    public interface MetaCallback {
        void onMetaReady(Map<String, ConversationMeta> metaMap);
    }

    /**
     * Fetches /conversation/meta and returns a guid -> ConversationMeta map.
     * Runs off the main thread; callback fires on the main thread.
     *
     * @param chatJids the full list of chat JIDs (e.g. Constants.totalchatlist)
     *                 to request meta for - this is a POST body field, the
     *                 server only returns data for JIDs you send.
     */
    public static void fetch(Context context, java.util.List<String> chatJids, MetaCallback callback) {
        new AsyncTask<Void, Void, Map<String, ConversationMeta>>() {
            @Override
            protected Map<String, ConversationMeta> doInBackground(Void... voids) {
                String raw = fetchRaw(context, chatJids);
                return parse(raw);
            }

            @Override
            protected void onPostExecute(Map<String, ConversationMeta> result) {
                if (callback != null) {
                    callback.onMetaReady(result != null ? result : new HashMap<>());
                }
            }
        }.execute();
    }

    private static String fetchRaw(Context context, java.util.List<String> chatJids) {
        HttpURLConnection conn = null;
        try {
            // Built the same way ChatCountApi.callResetUnreadCount() builds
            // "tojid" - Constants.UID + "_" + Constants.USER_ID + VitacapeExtention.
            String currentUserJid = Constants.UID + "_" + Constants.USER_ID + Constants.VitacapeExtention;

            if (currentUserJid.isEmpty()) {
                Log.e("ConversationMetaApi", "current user jid is empty, skipping meta fetch");
                return null;
            }

            // Real payload confirmed from network capture:
            // POST body = { "chat_jids": [...], "user_jid": "..." }
            JSONObject body = new JSONObject();
            JSONArray jidsArray = new JSONArray();
            for (String jid : chatJids) {
                jidsArray.put(jid);
            }
            body.put("chat_jids", jidsArray);
            body.put("user_jid", currentUserJid);

            Log.d("ConversationMetaApi", "POST " + Constants.CONVERSATION_META_URL);
            Log.d("ConversationMetaApi", "Request body: " + body.toString());

            conn = (HttpURLConnection) new URL(Constants.CONVERSATION_META_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = body.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                Log.e("ConversationMetaApi", "conversation/meta returned " + code);
                return null;
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            Log.d("ConversationMetaApi", "Response code: " + code);
            Log.d("ConversationMetaApi", "Response body: " + sb.toString());

            return sb.toString();

        } catch (Exception e) {
            Log.e("ConversationMetaApi", "Meta fetch failed: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Parses the raw /conversation/meta JSON into a guid-keyed map.
     * guid = chat_jid local part, i.e. everything before "@".
     * This is dynamic - works for any guid present in the response,
     * no hardcoded mapping needed.
     */
    private static Map<String, ConversationMeta> parse(String rawResponse) {
        Map<String, ConversationMeta> map = new HashMap<>();
        if (rawResponse == null) return map;

        try {
            JSONObject result = new JSONObject(rawResponse);
            if (!result.optBoolean("success", false)) return map;

            JSONArray data = result.getJSONArray("data");

            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);

                String chatJid = item.optString("chat_jid", "");
                String guid = chatJid.contains("@")
                        ? chatJid.substring(0, chatJid.indexOf("@"))
                        : chatJid;

                if (guid.isEmpty()) continue;

                String preview = item.optString("last_message_preview", "");
                long timestampMillis = parseIsoTimestamp(item.optString("last_message_timestamp", ""));
                String unreadCount = String.valueOf(item.optInt("unread_count", 0));

                map.put(guid, new ConversationMeta(guid, preview, timestampMillis, unreadCount));
            }

        } catch (JSONException e) {
            Log.e("ConversationMetaApi", "Meta parse error: " + e.getMessage());
        }

        Log.d("ConversationMetaApi", "Parsed map size: " + map.size() + ", keys: " + map.keySet());
        return map;
    }

    private static long parseIsoTimestamp(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            // Fallback for devices/APIs where java.time isn't available
            try {
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return sdf.parse(iso).getTime();
            } catch (Exception ex) {
                return 0L;
            }
        }
    }
}