package com.digicoffer.lauditor.Chat.ViewModels;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Adapters.ChatAdapter;
import com.digicoffer.lauditor.Chat.Model.ChildDO;
import com.digicoffer.lauditor.Chat.Model.ClientRelationshipsDo;
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class Clients extends Fragment implements AsyncTaskCompleteListener, ChatAdapter.EventListener {

    Dialog progress_dialog;
    RecyclerView rv_Clientrelationships;
    TextInputEditText et_Search;
    AlertDialog ad_dialog;
    TextView tv_chat;

    // Empty state views
    LinearLayout layout_empty_state;
    TextView tv_empty_subtitle;

    ArrayList<ClientRelationshipsDo> Clientlist = new ArrayList<ClientRelationshipsDo>();
    ArrayList<ClientRelationshipsDo> Corporate_Client_list = new ArrayList<ClientRelationshipsDo>();
    ArrayList<ClientRelationshipsDo> Client_list = new ArrayList<ClientRelationshipsDo>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client, container, false);
        Constants.Chat_id = "";

        rv_Clientrelationships = view.findViewById(R.id.rv_clientrelationships);
        Constants.recyclerView = rv_Clientrelationships;

        View tl_Search = view.findViewById(R.id.tl_Search);
        et_Search = tl_Search.findViewById(R.id.et_Search);

        tv_chat = view.findViewById(R.id.tv_chat);
        tv_chat.setText(R.string.list_of_clients);

        // Bind empty state views
        layout_empty_state = view.findViewById(R.id.layout_empty_state);
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle);
        tv_empty_subtitle.setText("There are no clients under here.");

        et_Search.setHint(R.string.type_to_search);
        et_Search.addTextChangedListener(new DescriptionValidation(et_Search));

        callCorporateWebservice();
        return view;
    }


    @Override
    public void onClick(View view) {
    }


    // ─── Webservice calls ─────────────────────────────────────────────────────

    private void callWebservice() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/client/list?include_appointment_consumers=True",
                    "CLIENT_RELATIONSHIP",
                    postData.toString()
            );
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callCorporateWebservice() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/corporate/list",
                    "CORPORATE_RELATIONSHIP",
                    postData.toString()
            );
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }


    // ─── Webservice response ──────────────────────────────────────────────────

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {

        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {

            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());

                if (Objects.equals(httpResult.getRequestType(), "CLIENT_RELATIONSHIP")) {

                    if (!result.optBoolean("error")) {
                        JSONObject jsonObject = result.optJSONObject("data");
                        JSONArray jsonArray = jsonObject.optJSONArray("relationships");
                        et_Search.setText("");
                        loadClientRelationshipsData(jsonArray);
                    } else {
                        AndroidUtils.showValidationALert(
                                "Alert",
                                String.valueOf(result.get("msg")),
                                getContext()
                        );
                    }

                } else if (Objects.equals(httpResult.getRequestType(), "CORPORATE_RELATIONSHIP")) {

                    if (!result.optBoolean("error")) {
                        JSONArray jsonArray = result.getJSONArray("relationships");
                        et_Search.setText("");
                        loadCorporateList(jsonArray);
                        callWebservice();
                    } else {
                        AndroidUtils.showValidationALert(
                                "Alert",
                                String.valueOf(result.get("msg")),
                                getContext()
                        );
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {

            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);

            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);

            AndroidUtils.showErrorAlert(
                    httpResult.getResponseContent().toString(),
                    getActivity()
            );
        }
    }


    // ─── Data loading ─────────────────────────────────────────────────────────

    private void loadCorporateList(JSONArray jsonArray) {
        try {
            Corporate_Client_list.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                ClientRelationshipsDo clientRelationshipsDo = new ClientRelationshipsDo();
                clientRelationshipsDo.setAdminName(jsonObject.optString("adminName"));
                clientRelationshipsDo.setCanAccept(jsonObject.optBoolean("canAccept"));
                clientRelationshipsDo.setClientType(jsonObject.optString("type"));
                clientRelationshipsDo.setConsent(jsonObject.optString("consent"));
                clientRelationshipsDo.setCreated(jsonObject.optString("created"));
                clientRelationshipsDo.setGuid(jsonObject.optString("client_id"));
                clientRelationshipsDo.setId(jsonObject.optString("id"));
                clientRelationshipsDo.setAccepted(jsonObject.optBoolean("isAccepted"));
                clientRelationshipsDo.setClient(jsonObject.optBoolean("isClient"));
                clientRelationshipsDo.setEditable(jsonObject.optBoolean("isEditable"));
                clientRelationshipsDo.setName(jsonObject.optString("name"));
                clientRelationshipsDo.setRel_id(jsonObject.optString("rel_id"));
                clientRelationshipsDo.setSource(jsonObject.optString("source"));
                Corporate_Client_list.add(clientRelationshipsDo);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadClientRelationshipsData(JSONArray jsonArray) {
        try {
            Clientlist.clear();
            Client_list.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                ClientRelationshipsDo clientRelationshipsDo = new ClientRelationshipsDo();
                clientRelationshipsDo.setAdminName(jsonObject.optString("adminName"));
                clientRelationshipsDo.setCanAccept(jsonObject.optBoolean("canAccept"));
                clientRelationshipsDo.setClientType(jsonObject.optString("type"));
                clientRelationshipsDo.setConsent(jsonObject.optString("consent"));
                clientRelationshipsDo.setCreated(jsonObject.optString("created"));
                clientRelationshipsDo.setGuid(jsonObject.optString("guid"));
                clientRelationshipsDo.setId(jsonObject.optString("id"));
                clientRelationshipsDo.setAccepted(jsonObject.optBoolean("isAccepted"));
                clientRelationshipsDo.setClient(jsonObject.optBoolean("isClient"));
                clientRelationshipsDo.setEditable(jsonObject.optBoolean("isEditable"));
                clientRelationshipsDo.setName(jsonObject.optString("name"));
                clientRelationshipsDo.setRel_id(jsonObject.optString("rel_id"));
                clientRelationshipsDo.setSource(jsonObject.optString("source"));
                Clientlist.add(clientRelationshipsDo);
            }

            // Merge corporate clients into the main list
            Clientlist.addAll(Corporate_Client_list);
            loadRecycleView();

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    // ─── RecyclerView + empty state ───────────────────────────────────────────

    private void loadRecycleView() {
        applyUnreadCounts(Clientlist);
        sortClientsByUnreadCount(Clientlist);
        updateEmptyState(Clientlist.isEmpty());

        if (Clientlist.isEmpty()) {
            return;
        }

        ChatAdapter adapter = new ChatAdapter(Clientlist, this, getContext(), getActivity());
        rv_Clientrelationships.setAdapter(adapter);
        AndroidUtils.LoadingRecyclerview(rv_Clientrelationships, getContext());
        AndroidUtils.setupBottomSpacerFooter(rv_Clientrelationships, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));

        et_Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
                rv_Clientrelationships.post(() ->
                        updateEmptyState(adapter.getItemCount() == 0)
                );
            }
        });
    }

    /**
     * Toggles the RecyclerView and empty-state visibility.
     *
     * @param isEmpty true  → show empty state, hide list
     *                false → show list, hide empty state
     */
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rv_Clientrelationships.setVisibility(View.GONE);
            layout_empty_state.setVisibility(View.VISIBLE);
        } else {
            rv_Clientrelationships.setVisibility(View.VISIBLE);
            layout_empty_state.setVisibility(View.GONE);
        }
    }


    // ─── Navigation ───────────────────────────────────────────────────────────

    public void Message(ChildDO childDO) {
        try {
            String jid = childDO.getUid();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String currentJID = pref.getString("xmpp_jid", null);
            Log.d("jid+currentJID", jid + "_" + currentJID);
            if (Objects.equals(childDO.getId(), "") || childDO.getId() == null) {
                new ChatHistoryTask(currentJID, jid).execute("");
                move_message_fragment("", childDO.getName(), jid, childDO.getSource());
            } else {
                new ChatHistoryTask(childDO.getId(), jid).execute("");
                move_message_fragment(childDO.getId(), childDO.getName(), jid, childDO.getSource());
            }
        } catch (Exception e) {
            Log.d("Error:", Objects.requireNonNull(e.getMessage()));
            e.fillInStackTrace();
        }
    }

    @Override
    public void view_users(String jid, String name, String clientType) throws JSONException {
        MessagesList frag = new MessagesList(rv_Clientrelationships);
        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_CONTACT_JID", jid);
        bundle.putString("EXTRA_CONTACT_NAME", name);
        bundle.putString("EXTRA_CONTACT_TYPE", clientType);
        frag.setArguments(bundle);
        FragmentManager fragmentManager11 = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction11 = fragmentManager11.beginTransaction();
        fragmentTransaction11.replace(R.id.id_framelayout, frag);
        fragmentTransaction11.addToBackStack(null);
        fragmentTransaction11.commit();
    }

    public void Users(ClientRelationshipsDo clientRelationshipsDo, ChatAdapter.MyViewHolder holder) {
    }

    private void move_message_fragment(String tmid, String name, String jid, String source) {
        try {
            String xmpp_jid = tmid.equals("") ? jid : (jid + "_" + tmid);
            Log.d("xmpp_jid", xmpp_jid);
            MessagesList frag = new MessagesList(rv_Clientrelationships);
            Bundle bundle = new Bundle();
            bundle.putString("EXTRA_CONTACT_JID", xmpp_jid);
            bundle.putString("EXTRA_CONTACT_NAME", name);
            bundle.putString("EXTRA_CONTACT_TYPE", source);
            frag.setArguments(bundle);
            FragmentManager fragmentManager11 = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction11 = fragmentManager11.beginTransaction();
            fragmentTransaction11.replace(R.id.id_framelayout, frag);
            fragmentTransaction11.addToBackStack(null);
            fragmentTransaction11.commit();
        } catch (Exception e) {
            AndroidUtils.logMsg(e.getMessage());
        }
    }


    // ─── Async task for unread count ──────────────────────────────────────────

    class ChatHistoryTask extends AsyncTask<String, String, String> {
        String XMPP_DOMAIN = "https://" + Constants.XMPP_DOMAIN + "/";
        String url = "";
        TextView tv_count;

        ChatHistoryTask(String currentJID, String JID) {
            super();
            this.url = XMPP_DOMAIN + "unread/" + currentJID + File.separator + JID;
            this.tv_count = tv_count;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            return requestUnreadCount(url);
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (!jsonResponse.optBoolean("error")) {
                    tv_count.setText(
                            (jsonResponse.getJSONObject("data")).optString("count")
                    );
                    Log.d("count_message",
                            (jsonResponse.getJSONObject("data")).optString("count"));
                }
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    private String requestUnreadCount(String url) {
        String data = "";
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
            httpURLConnection.setRequestMethod("GET");
            int status_code = httpURLConnection.getResponseCode();
            if (status_code == 200) {
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } else {
                AndroidUtils.showAlert("Err connection, Please try again", getActivity());
            }
        } catch (Exception e) {
            AndroidUtils.logMsg(e.getMessage());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return data;
    }


    // ─── Sorting & unread helpers ─────────────────────────────────────────────

    private void sortClientsByUnreadCount(ArrayList<ClientRelationshipsDo> list) {
        java.util.Collections.sort(list, (c1, c2) -> {

            int unread1 = 0;
            int unread2 = 0;

            try {
                unread1 = Integer.parseInt(
                        c1.getUnread_count() == null || c1.getUnread_count().isEmpty()
                                ? "0" : c1.getUnread_count());
            } catch (Exception ignored) {
            }

            try {
                unread2 = Integer.parseInt(
                        c2.getUnread_count() == null || c2.getUnread_count().isEmpty()
                                ? "0" : c2.getUnread_count());
            } catch (Exception ignored) {
            }

            // Descending order (High → Low)
            return Integer.compare(unread2, unread1);
        });
    }

    private void applyUnreadCounts(ArrayList<ClientRelationshipsDo> clients) {
        for (ClientRelationshipsDo client : clients) {
            int totalUnread = 0;

            for (UnreadCountModel unread : Constants.unreadList) {
                if (unread.getFromjid() != null &&
                        unread.getFromjid().equalsIgnoreCase(client.getGuid())) {
                    try {
                        totalUnread += Integer.parseInt(unread.getCount());
                    } catch (Exception ignored) {
                    }
                }
            }

            client.setUnread_count(String.valueOf(totalUnread));
            Log.d("UnreadMap", client.getName() + " -> " + totalUnread);
        }
    }
}