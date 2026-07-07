package com.digicoffer.lauditor.Chat.Adapters;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Model.ChildDO;
import com.digicoffer.lauditor.Chat.Model.ClientRelationshipsDo;
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TeamsAdapter extends RecyclerView.Adapter<TeamsAdapter.MyViewHolder> implements Filterable, ChildAdapter.EventListener {
    private TeamsAdapter.EventListener context;
    ArrayList<ChildDO> child_list = new ArrayList<>();
    private Context frag_context;
    LinearLayoutCompat ll_clients;
    MyViewHolder new_holder;
    int item_position = 0;
    private boolean isExpandable;
    ClientRelationshipsDo relationshipsDoRow;
    String User_Name = "";
    TextView tv_name_users;
    Activity activity;
    Dialog progress_dialog;
    ArrayList<ClientRelationshipsDo> filtered_list = new ArrayList<>();
    ArrayList<ClientRelationshipsDo> list_item = new ArrayList<>();


    public interface EventListener {
        void Message(ChildDO childDO);

        void view_users(String uid, String name) throws JSONException;

        void Users(ClientRelationshipsDo clientRelationshipsDo, MyViewHolder holder);

    }

    public TeamsAdapter(ArrayList<ClientRelationshipsDo> contactsList, EventListener mcontext, Context context, FragmentActivity activity) {
        this.filtered_list = filtered_list;
        this.list_item = contactsList;
        this.filtered_list = contactsList;
        this.context = mcontext;
        this.frag_context = context;
        this.activity = activity;
        isExpandable = false;
        this.child_list = child_list;
    }

    @NonNull
    @Override
    public TeamsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent, false);
        return new TeamsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamsAdapter.MyViewHolder holder, int position) {
        ClientRelationshipsDo clientRelationshipsDo = filtered_list.get(position);
        Constants.Chat_id = "";
        int totalUnreadCount = 0;
        for (UnreadCountModel unreadCount : Constants.unreadList) {
            if (unreadCount.getFromjid().equals(clientRelationshipsDo.getGuid())) {
                totalUnreadCount += Integer.parseInt(unreadCount.getCount());
            }
        }

        // Set the total unread count for the client
        clientRelationshipsDo.setUnread_count(String.valueOf(totalUnreadCount));
//        clientRelationshipsDo.setRecyclerview_position(position);
//        filtered_list.set(position, clientRelationshipsDo);
        boolean isExpandable = clientRelationshipsDo.isExpanded();
//                for (int i=0;i<filtered_list.size();i++){
//                    ClientRelationshipsDo clientRelationshipsDo1 = filtered_list.get(i);
        Log.d("Position", String.valueOf(clientRelationshipsDo.getRecyclerview_position()) + String.valueOf(clientRelationshipsDo.getName()));
//                }
        new_holder = holder;
        holder.tv_name.setText(clientRelationshipsDo.getName());
        if (!clientRelationshipsDo.getName().isEmpty()) {
            String person_name = clientRelationshipsDo.getName().substring(0, 1);
            holder.person_icon.setText(person_name);
        }
        holder.ll_users.setVisibility(View.VISIBLE);

        Log.d("Client_Type", "" + clientRelationshipsDo.getClientType().toString());
//        if (clientRelationshipsDo.getClientType().equalsIgnoreCase("Consumer")) {
//            holder.plus_icon.setVisibility(View.GONE);
//        } else {

        if (clientRelationshipsDo.getUsers().length() > 0) {
            holder.plus_icon.setVisibility(View.VISIBLE);
        } else {
            holder.plus_icon.setVisibility(View.GONE);
        }
//        }
        holder.ll_users.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        if (isExpandable) {
            holder.plus_icon.setBackgroundResource(R.drawable.minus_icon_small_chat);

        } else {
            holder.plus_icon.setBackgroundResource(R.drawable.plus_icon_xl_chat);
        }
        holder.plus_icon.setOnClickListener(v -> {


            if (clientRelationshipsDo.isExpanded()) {
                // If already expanded, remove the sub-items and update the button icon
                child_list.clear();
                clientRelationshipsDo.setExpanded(false);


            } else {

                clientRelationshipsDo.setExpanded(true);
                context.Users(clientRelationshipsDo, holder);
//                relationshipsDoRow = clientRelationshipsDo;
//                if (clientRelationshipsDo.getClientType().equalsIgnoreCase("Team")) {
//
//                    child_list.clear();
//                    try {
//                        for (int j = 0; j < clientRelationshipsDo.getUsers().length(); j++) {
//                            ChildDO childDO1 = new ChildDO();
//                            JSONObject jsonuser = clientRelationshipsDo.getUsers().getJSONObject(j);
//                            childDO1.setGuid(jsonuser.getString("guid"));
//                            childDO1.setName(jsonuser.getString("name"));
//
//                            child_list.add(childDO1);
//                        }
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    loadChildList();
//
//                }

            }
            notifyItemChanged(holder.getAdapterPosition());
        });
        if (!clientRelationshipsDo.getUnread_count().isEmpty()) {
//            holder.dot_icon.setVisibility(View.VISIBLE);
            int unreadcount = Integer.parseInt(clientRelationshipsDo.getUnread_count());
            Log.d("unread_count_value", "" + unreadcount);
            if (unreadcount > 0) {
                holder.dot_icon.setVisibility(View.VISIBLE);
            } else {
                holder.dot_icon.setVisibility(View.GONE);
            }
        } else {
            holder.dot_icon.setVisibility(View.GONE);
        }
        try {
            child_list.clear();
            for (int i = 0; i < clientRelationshipsDo.getUsers().length(); i++) {
                ChildDO childDO1 = new ChildDO();
                JSONObject jsonuser = clientRelationshipsDo.getUsers().getJSONObject(i);
                childDO1.setGuid(jsonuser.getString("guid"));
                childDO1.setName(jsonuser.getString("name"));
                child_list.add(childDO1);
            }
            // Update unread count in Clientlist based on unreadcountlist
            for (ChildDO client : child_list) {
                for (UnreadCountModel unreadCount : Constants.unreadList) {
                    if (unreadCount.getFromjid().equals(client.getGuid())) {
                        client.setUnread_count(unreadCount.getCount());
                        Log.d("teamunread_list", client.getGuid() + " count: " + client.getUnread_count());
                        break;
                    } else {
                        client.setUnread_count("");
                    }
                }
            }
            loadChildList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void getUserInTeam(JSONArray users, String teamId, String name) {
        try {
            for (int j = 0; j < users.length(); j++) {
                ChildDO childDO1 = new ChildDO();
                JSONObject jsonuser = users.getJSONObject(j);
                childDO1.setGuid(jsonuser.getString("guid"));
                childDO1.setName(jsonuser.getString("name"));
                child_list.add(childDO1);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        loadChildList();
//        for (int i = 0; i < Constants.teamResArray.length(); i++) {
//            ClientRelationshipsDo clientRelationshipsDo = new ClientRelationshipsDo();
//            JSONObject jsonObject = null;
//            try {
//                jsonObject = Constants.teamResArray.getJSONObject(i);
//
//                if (teamId.equalsIgnoreCase(jsonObject.getString("id"))) {
//
//                    JSONArray user = jsonObject.getJSONArray("users");
////                    ChildDO childDO = new ChildDO();
////                    childDO.setGuid(teamId);
////                    childDO.setName(name+" "+"(Admin)");
//////                    childDO.setUid(teamId);
////                    Log.d("Child_Position", String.valueOf(relationshipsDoRow.getGuid())+relationshipsDoRow.getName());
////                    child_list.add(childDO);
//
//                    for (int j = 0; j < user.length(); j++) {
//                        ChildDO childDO1 = new ChildDO();
//                        JSONObject jsonuser = user.getJSONObject(j);
//                        childDO1.setGuid(jsonuser.getString("guid"));
//                        childDO1.setName(jsonuser.getString("name"));
////                    childDO1.setId(jsonuser.getString("id"));
////                    childDO1.setUid(uid);
//                        child_list.add(childDO1);
//                    }
//                    loadChildList();
//                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    private void loadChildList() {

//        notifyItemChanged(new_holder.getAdapterPosition());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(frag_context, LinearLayoutManager.VERTICAL, false);
        ChildAdapter childRecyclerViewAdapter = new ChildAdapter(child_list, new_holder.rv_users.getContext(), this, activity);
        new_holder.rv_users.setAdapter(childRecyclerViewAdapter);
        new_holder.rv_users.setLayoutManager(layoutManager);
        //new_holder.rv_users.setHasFixedSize(true);
//        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filtered_list = list_item;

                } else {
                    ArrayList<ClientRelationshipsDo> filteredList = new ArrayList<>();
                    for (ClientRelationshipsDo row : list_item) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (AndroidUtils.isNull(row.getName()).toLowerCase().contains(charString.toLowerCase()) || AndroidUtils.isNull(row.getClientType()).toLowerCase().contains(charString.toLowerCase()) || AndroidUtils.isNull(row.getCreated()).toLowerCase().contains(charString.toLowerCase()) || AndroidUtils.isNull(row.getConsent()).toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    filtered_list = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered_list;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence cha, FilterResults filterResults) {
                filtered_list = (ArrayList<ClientRelationshipsDo>) filterResults.values;
                notifyDataSetChanged();
            }

        };
    }

    @Override
    public int getItemCount() {
        return filtered_list.size();
    }

    @Override
    public void Message(ChildDO childDO) {
        context.Message(childDO);
    }

    @Override
    public void view_users(String uid, String name) throws JSONException {
        try {
            context.view_users(uid, name);
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, person_icon;
        ImageView plus_icon, dot_icon;
        LinearLayoutCompat ll_users;
        LinearLayout ll_client_card;
        RecyclerView rv_users;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            person_icon = itemView.findViewById(R.id.person_icon);
            plus_icon = itemView.findViewById(R.id.plus_icon);
            dot_icon = itemView.findViewById(R.id.dot_icon);
            ll_users = itemView.findViewById(R.id.ll_users);
            rv_users = itemView.findViewById(R.id.rv_users);
            ll_client_card = itemView.findViewById(R.id.ll_client_card);
        }
    }
}