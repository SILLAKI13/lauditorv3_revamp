package com.digicoffer.lauditor.Chat.ViewModels;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Adapters.MembersAdapter;
import com.digicoffer.lauditor.Chat.Adapters.TeamsAdapter;
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

import java.util.ArrayList;

public class Teams extends Fragment implements AsyncTaskCompleteListener, TeamsAdapter.EventListener, MembersAdapter.OnMemberClick {

    Dialog progress_dialog;
    RecyclerView rv_Clientrelationships;
    TextInputEditText et_Search;
    TextView tv_chat;
    AlertDialog ad_dialog;

    // Empty state views
    LinearLayout layout_empty_state;
    TextView tv_empty_subtitle;

    ArrayList<ClientRelationshipsDo> Clientlist = new ArrayList<ClientRelationshipsDo>();
    ArrayList<ChildDO> child_list = new ArrayList<>();
    ArrayList<ChildDO> members_list = new ArrayList<>();

    @Override
    public void onClick(View view) {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client, container, false);
        Constants.Chat_id = "";

        rv_Clientrelationships = view.findViewById(R.id.rv_clientrelationships);
        Constants.recyclerView = rv_Clientrelationships;

        View tl_Search = view.findViewById(R.id.tl_Search);
        et_Search = tl_Search.findViewById(R.id.et_Search);

        tv_chat = view.findViewById(R.id.tv_chat);
        tv_chat.setText(R.string.list_of_teams);

        // Bind empty state views
        layout_empty_state = view.findViewById(R.id.layout_empty_state);
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle);
        tv_empty_subtitle.setText("There are no teams under here.");

        et_Search.addTextChangedListener(new DescriptionValidation(et_Search));
        et_Search.setHint(R.string.type_to_search);

        callViewGroupsWebservice();
        return view;
    }


    // ─── Webservice calls ─────────────────────────────────────────────────────

    private void callWebservice() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "chatusers",
                    "Team_Chat",
                    postData.toString()
            );
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callViewGroupsWebservice() {
        try {
            JSONObject postdata = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/members",
                    "Get Members",
                    postdata.toString()
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

                if ("Team_Chat".equals(httpResult.getRequestType())) {

                    if (!result.getBoolean("error")) {
                        JSONArray jsonArray = result.getJSONArray("groups");
                        Constants.teamResArray = jsonArray;
                        et_Search.setText("");
                        loadClientRelationshipsData(jsonArray);
                    } else {
                        AndroidUtils.showValidationALert(
                                "Alert",
                                String.valueOf(result.get("msg")),
                                getContext()
                        );
                    }

                } else if ("Get Members".equals(httpResult.getRequestType())) {

                    JSONObject data = result.getJSONObject("data");
                    JSONArray users = data.getJSONArray("users");
                    loadMembers(users);
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


    // ─── Members data loading ─────────────────────────────────────────────────

    private void loadMembers(JSONArray users) throws JSONException {
        members_list.clear();

        for (int i = 0; i < users.length(); i++) {
            JSONObject jsonObject = users.getJSONObject(i);

            ChildDO member = new ChildDO();
            member.setId(jsonObject.getString("id"));
            member.setName(jsonObject.getString("name"));
            member.setGuid(jsonObject.getString("guid"));
            member.setUnread_count("");

            members_list.add(member);
        }

        loadMembersRecyclerView();
    }

    private void loadMembersRecyclerView() {
        applyUnreadCounts(members_list);
        sortClientsByUnreadCount(members_list);
        updateEmptyState(members_list.isEmpty());

        if (members_list.isEmpty()) {
            return;
        }

        MembersAdapter adapter = new MembersAdapter(
                members_list,
                getContext(),
                this
        );

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
                adapter.getFilter().filter(et_Search.getText().toString());
                rv_Clientrelationships.post(() ->
                        updateEmptyState(adapter.getItemCount() == 0)
                );
            }
        });
    }

    @Override
    public void onMemberClick(ChildDO member) {
        MessagesList frag = new MessagesList(rv_Clientrelationships);

        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_CONTACT_JID", member.getGuid());
        bundle.putString("EXTRA_CONTACT_NAME", member.getName());
        bundle.putString("EXTRA_CONTACT_TYPE", "relationship");

        frag.setArguments(bundle);

        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.id_framelayout, frag)
                .addToBackStack(null)
                .commit();
    }


    // ─── Teams data loading ───────────────────────────────────────────────────

    private void loadClientRelationshipsData(JSONArray jsonArray) {
        try {
            Clientlist.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                ClientRelationshipsDo clientRelationshipsDo = new ClientRelationshipsDo();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                clientRelationshipsDo.setId(jsonObject.getString("id"));
                clientRelationshipsDo.setName(jsonObject.getString("groupName"));
                clientRelationshipsDo.setClientType("Team");
                clientRelationshipsDo.setUsers(jsonObject.getJSONArray("users"));
                clientRelationshipsDo.setUnread_count("");

                JSONArray userArray = jsonObject.getJSONArray("users");
                clientRelationshipsDo.setUsers(userArray);

                // Collect GUIDs for all users in this group
                ArrayList<String> userGuids = new ArrayList<>();
                for (int j = 0; j < userArray.length(); j++) {
                    JSONObject jsonuser = userArray.getJSONObject(j);
                    userGuids.add(jsonuser.getString("guid"));
                }

                // Find the first matching unread count from any user in the group
                String unreadCountStr = "";
                String matchedGuid = null;
                for (String guid : userGuids) {
                    for (UnreadCountModel unreadCount : Constants.unreadList) {
                        if (guid.equals(unreadCount.getFromjid())) {
                            unreadCountStr = unreadCount.getCount();
                            matchedGuid = guid;
                            break;
                        }
                    }
                    if (matchedGuid != null) break;
                }

                clientRelationshipsDo.setGuid(matchedGuid != null ? matchedGuid : "");
                clientRelationshipsDo.setUnread_count(unreadCountStr);

                Clientlist.add(clientRelationshipsDo);
                Constants.teamMapChatList.put(clientRelationshipsDo.getId(), child_list);
            }

            loadRecycleView();

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    // ─── RecyclerView + empty state ───────────────────────────────────────────

    private void loadRecycleView() {
        updateEmptyState(Clientlist.isEmpty());

        if (Clientlist.isEmpty()) {
            return;
        }

        rv_Clientrelationships.setLayoutManager(new GridLayoutManager(getContext(), 1));
        final TeamsAdapter adapter = new TeamsAdapter(Clientlist, this, getContext(), getActivity());

        Log.d("Clientlist", String.valueOf(Clientlist.size()));
        rv_Clientrelationships.setAdapter(adapter);
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

    @Override
    public void Message(ChildDO childDO) {
        try {
            String jid = childDO.getUid();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String currentJID = pref.getString("xmpp_jid", null);
            Log.d("jid+currentJID", jid + "_" + currentJID);
            if (childDO.getId() == "" || childDO.getId() == null) {
                move_message_fragment("", childDO.getName(), jid);
            } else {
                move_message_fragment(childDO.getId(), childDO.getName(), jid);
            }
        } catch (Exception e) {
            Log.d("Error:", e.getMessage());
            e.fillInStackTrace();
        }
    }

    @Override
    public void view_users(String jid, String name) throws JSONException {
        MessagesList frag = new MessagesList(rv_Clientrelationships);
        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_CONTACT_JID", jid);
        bundle.putString("EXTRA_CONTACT_NAME", name);
        bundle.putString("EXTRA_CONTACT_TYPE", "relationship");
        frag.setArguments(bundle);
        FragmentManager fragmentManager11 = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction11 = fragmentManager11.beginTransaction();
        fragmentTransaction11.replace(R.id.id_framelayout, frag);
        fragmentTransaction11.addToBackStack(null);
        fragmentTransaction11.commit();
    }

    @Override
    public void Users(ClientRelationshipsDo clientRelationshipsDo, TeamsAdapter.MyViewHolder holder) {
    }

    private void move_message_fragment(String tmid, String name, String jid) {
        try {
            String xmpp_jid = tmid.equals("") ? jid : (jid + "_" + tmid);
            Log.d("xmpp_jid", xmpp_jid);
            MessagesList frag = new MessagesList(rv_Clientrelationships);
            Bundle bundle = new Bundle();
            bundle.putString("EXTRA_CONTACT_JID", xmpp_jid);
            bundle.putString("EXTRA_CONTACT_NAME", name);
            bundle.putString("EXTRA_CONTACT_TYPE", "relationship");
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


    // ─── Sorting & unread helpers ─────────────────────────────────────────────

    private void sortClientsByUnreadCount(ArrayList<ChildDO> list) {
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

    private void applyUnreadCounts(ArrayList<ChildDO> members) {
        for (ChildDO client : members) {
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