package com.digicoffer.lauditor.Chat.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Model.ChildDO;
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONException;

import java.util.ArrayList;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.MyViewHolder> {


    ArrayList<ChildDO> filteredList = new ArrayList<>();
    //    ChildAdapter.EventListener context;
    Context cContext;
    ChildDO childDo;
    EventListener context;
    String relationship_id;
    View view;
    Activity Mactivity;

    public ChildAdapter(ArrayList<ChildDO> child_list, Context context, EventListener mcontext, Activity activity) {
        this.filteredList = child_list;
        this.cContext = context;
        this.context = mcontext;
        this.Mactivity = activity;
    }

    public interface EventListener {
        void Message(ChildDO childDO);

        //        void Copy(ChildDo childDo);
        void view_users(String uid, String name) throws JSONException;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub, parent, false);
        return new ChildAdapter.MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ChildAdapter.MyViewHolder holder, int position) {
        ChildDO childDO = filteredList.get(position);
        holder.tv_name.setText(childDO.getName());
        Constants.Chat_id = "";
        int totalUnreadCount = 0;
        for (UnreadCountModel unreadCount : Constants.unreadList) {
            if (unreadCount.getFromjid().contains(childDO.getGuid())) {
                totalUnreadCount += Integer.parseInt(unreadCount.getCount());
            }
        }

        // Set the total unread count for the client
        childDO.setUnread_count(String.valueOf(totalUnreadCount));
        if (!childDO.getName().isEmpty()) {
            String person_name = childDO.getName().substring(0, 1);
            holder.person_icon.setText(person_name);
        }
        holder.count_icon.setBackground(cContext.getDrawable(R.drawable.red_circular));
        if (!childDO.getUnread_count().isEmpty()) {
            int unreadcount = Integer.parseInt(childDO.getUnread_count());
            Log.d("unread_count_value", "" + unreadcount);
            if (unreadcount > 0) {
                holder.count_icon.setText(childDO.getUnread_count());
                holder.count_icon.setVisibility(View.VISIBLE);
            } else {
                holder.count_icon.setVisibility(View.GONE);
            }
        } else {
            holder.count_icon.setVisibility(View.GONE);
        }
//        holder.ll_users.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("Message", childDO.getName());
//                context.Message(childDO);
//                try {
//                    context.view_users(childDO.getGuid(), childDO.getName());
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
        holder.ll_users.setBackground(cContext.getDrawable(R.drawable.rectangular_white_background));
        holder.tv_name.setTextColor(Color.BLACK);
        holder.ll_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("Message", childDO.getName());
                    context.Message(childDO);
                    holder.ll_users.setBackground(cContext.getDrawable(R.drawable.radiobutton_centre_green_background));
                    holder.tv_name.setTextColor(Color.WHITE);
                    context.view_users(childDO.getGuid(), childDO.getName());
                    new ChatCountApi(cContext).callResetUnreadCount(childDO.getGuid());
                    Constants.Chat_id = childDO.getGuid() + Constants.VitacapeExtention;
//                        notifyDataSetChanged();
                } catch (JSONException e) {
                    e.fillInStackTrace();
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, person_icon;
        LinearLayoutCompat ll_users, ll_clients;
        TextView count_icon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            count_icon = itemView.findViewById(R.id.count_icon);
            person_icon = itemView.findViewById(R.id.person_icon);
            tv_name = itemView.findViewById(R.id.tv_name);
            ll_users = itemView.findViewById(R.id.ll_clients);
            ll_clients = itemView.findViewById(R.id.ll_clients);
        }
    }
}
