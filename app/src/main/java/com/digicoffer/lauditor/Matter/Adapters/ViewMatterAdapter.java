package com.digicoffer.lauditor.Matter.Adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Objects;

public class ViewMatterAdapter extends RecyclerView.Adapter<ViewMatterAdapter.MyViewHolder>
        implements Filterable {

    ArrayList<ViewMatterModel> itemsArrayList;
    ArrayList<ViewMatterModel> list_item;

    Context context;
    InterfaceListener eventListener;

    // ── Expand / collapse ──────────────────────────────────────────────
    private int expandedPosition = -1;
    private RecyclerView recyclerView;

    // ─────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────
    public ViewMatterAdapter(ArrayList<ViewMatterModel> itemsArrayList,
                             Context context,
                             InterfaceListener eventListener) {
        this.itemsArrayList = new ArrayList<>(itemsArrayList);
        this.list_item = new ArrayList<>(itemsArrayList);
        this.context = context;
        this.eventListener = eventListener;
    }

    // ─────────────────────────────────────────────────────────────────────
    // RecyclerView reference
    // ─────────────────────────────────────────────────────────────────────
    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Safe notify helpers
    // ─────────────────────────────────────────────────────────────────────
    private void safeNotify(int position) {
        Runnable r = () -> {
            if (position >= 0 && position < getItemCount()) {
                notifyItemChanged(position);
            }
        };
        if (recyclerView != null) recyclerView.post(r);
        else new Handler(Looper.getMainLooper()).post(r);
    }

    private void safeNotifyDataSetChanged() {
        if (recyclerView != null) {
            recyclerView.post(this::notifyDataSetChanged);
        } else {
            new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
        }
    }

    /**
     * Call from dialog onDismiss to safely collapse any open dropdown
     */
    public void collapseExpanded() {
        if (expandedPosition == -1) return;
        int pos = expandedPosition;
        expandedPosition = -1;
        safeNotify(pos);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Interface
    // ─────────────────────────────────────────────────────────────────────
    public interface InterfaceListener {
        void View_Details(ViewMatterModel viewMatterModel,
                          ArrayList<ViewMatterModel> itemsArrayList);

        void DeleteMatter(ViewMatterModel viewMatterModel,
                          ArrayList<ViewMatterModel> itemsArrayList);

        void Edit_Matter_Info(ViewMatterModel viewMatterModel);

        void Update_Group(ViewMatterModel viewMatterModel);

        void Close_Matter(ViewMatterModel viewMatterModel);

        void ReopenMatter(ViewMatterModel viewMatterModel);
    }

    // ─────────────────────────────────────────────────────────────────────
    // onCreateViewHolder
    // ─────────────────────────────────────────────────────────────────────
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_matter_recyclerview_design, parent, false);
        return new MyViewHolder(itemView);
    }

    // ─────────────────────────────────────────────────────────────────────
    // onBindViewHolder
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
        ViewMatterModel model = itemsArrayList.get(position);

        try {
            // ── Basic fields ───────────────────────────────────────────
            holder.tv_owner_name.setText(model.getOwner_name());
            holder.tv_matter_title.setText(model.getTitle());
            holder.tv_case_number.setText(model.getMatter_id());
            holder.tv_client_name.setText(model.getClient_name());
            holder.typeLayout.setVisibility(GONE);

            if (Objects.equals(Constants.MATTER_TYPE, "Legal")) {
                holder.tv_date_of_filling.setText(model.getDate_of_filling());
                if (!model.getCasetype().isEmpty()) {
                    holder.typeLayout.setVisibility(VISIBLE);
                    holder.tv_matter_type.setText(model.getCasetype());
                }
            } else {
                holder.tv_date_of_filling.setText(model.getStartdate());
                if (!model.getMatterType().isEmpty()) {
                    holder.typeLayout.setVisibility(VISIBLE);
                    holder.tv_matter_type.setText(model.getMatterType());
                }
            }

            // ── Tags ───────────────────────────────────────────────────
            JSONArray tagsArray = model.getTags_list();
            if (tagsArray != null && tagsArray.length() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tagsArray.length(); i++) {
                    String tag = tagsArray.optString(i);
                    if (!TextUtils.isEmpty(tag)) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(tag);
                    }
                }
                if (sb.length() > 0) {
                    holder.tv_tag_name.setText(sb.toString());
                    holder.tv_tag_name.setVisibility(VISIBLE);
                    holder.taglayout.setVisibility(VISIBLE);
                } else {
                    holder.tv_tag_name.setVisibility(GONE);
                    holder.taglayout.setVisibility(GONE);
                }
            } else {
                holder.tv_tag_name.setVisibility(GONE);
                holder.taglayout.setVisibility(GONE);
            }

            // ── Status chip ────────────────────────────────────────────
            holder.iv_initiated.setVisibility(GONE);
            switch (model.getStatus()) {
                case "Active":
                    holder.tv_initiated.setText("Active");
                    holder.tv_initiated.setTextColor(
                            ContextCompat.getColor(context, R.color.completed_text));
                    holder.tv_initiated.setBackgroundResource(R.drawable.completed_badge);
                    break;
                case "Closed":
                    holder.tv_initiated.setText("Closed");
                    holder.tv_initiated.setTextColor(
                            ContextCompat.getColor(context, R.color.cancelled_text));
                    holder.tv_initiated.setBackgroundResource(R.drawable.cancelled_badge);
                    break;
                default:
                    holder.tv_initiated.setText("Pending");
                    holder.tv_initiated.setTextColor(
                            ContextCompat.getColor(context, R.color.pending_text));
                    holder.tv_initiated.setBackgroundResource(R.drawable.pending_badge);
                    break;
            }

            // ── Build per-item action list ─────────────────────────────
            // ✅ LOCAL ActionModel list per item — never shared across items
            ArrayList<ActionModel> itemActions = new ArrayList<>();
            itemActions.add(new ActionModel("Edit Matter"));
            itemActions.add(new ActionModel("View Timeline"));
            if ("Closed".equals(model.getStatus())) {
                itemActions.add(new ActionModel("Reopen Matter"));
            } else if ("pending".equalsIgnoreCase(model.getStatus())
                    || "active".equalsIgnoreCase(model.getStatus())) {
                itemActions.add(new ActionModel("Close Matter"));
            }

            // ── Three-dot enable / disable ─────────────────────────────
            if (model.isIsdisabled() || !model.isIs_editable()) {
                holder.custom_spinner_cardview.setEnabled(false);
                holder.custom_spinner_cardview.setAlpha(0.5f);
            } else {
                holder.custom_spinner_cardview.setEnabled(true);
                holder.custom_spinner_cardview.setAlpha(1.0f);
            }

            // ── Expand / collapse ──────────────────────────────────────
            boolean isExpanded = (position == expandedPosition);

            // ✅ Always clear stale listeners before rebinding
            holder.custom_spinner_cardview.setOnClickListener(null);
            holder.sp_action.setOnItemClickListener(null);

            if (isExpanded) {
                // ✅ Use CommonSpinnerAdapter with ActionModel — same as ViewGroupsAdpater
                CommonSpinnerAdapter itemAdapter = new CommonSpinnerAdapter((Activity) context, itemActions);
                holder.sp_action.setAdapter(itemAdapter);
                holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));
                holder.action_list_card.setVisibility(VISIBLE);
                holder.sp_action.setVisibility(VISIBLE);
            } else {
                holder.sp_action.setAdapter(null);
                holder.action_list_card.setVisibility(GONE);
                holder.sp_action.setVisibility(GONE);
            }

            // ── Three-dot toggle ───────────────────────────────────────
            holder.custom_spinner_cardview.setOnClickListener(v -> {
                int cur = holder.getAdapterPosition();
                if (cur == RecyclerView.NO_POSITION) return;
                int prev = expandedPosition;
                expandedPosition = (expandedPosition == cur) ? -1 : cur;
                if (prev != -1 && prev != cur) safeNotify(prev);
                safeNotify(cur);
            });

            // ── Action item click ──────────────────────────────────────
            holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
                int cur = holder.getAdapterPosition();
                if (cur == RecyclerView.NO_POSITION) return;

                // ✅ Snapshot action name before collapsing
                String actionName = itemActions.get(pos).getName();

                // ✅ Collapse immediately and safely
                expandedPosition = -1;
                safeNotify(cur);

                // ✅ Dispatch AFTER collapsing — avoids re-layout race
                new Handler(Looper.getMainLooper()).post(
                        () -> dispatchAction(actionName, model));
            });

        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), context);
            Log.d("Matter Details", Objects.requireNonNull(e.getMessage()));
            e.fillInStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // dispatchAction
    // ─────────────────────────────────────────────────────────────────────
    private void dispatchAction(String action, ViewMatterModel model) {
        switch (action) {
            case "Edit Matter":
                Constants.Matter_CreateOrViewDetails = "View Timeline";
                Constants.matterDate = model.getCreated();
                eventListener.View_Details(model, itemsArrayList);
                break;
            case "View Timeline":
                Constants.Matter_CreateOrViewDetails = "Edit Matter Info";
                Constants.matterDate = model.getCreated();
                eventListener.Edit_Matter_Info(model);
                break;
            case "Close Matter":
            case "Reopen Matter":
                Constants.Matter_CreateOrViewDetails = "Close/Reopen";
                eventListener.Close_Matter(model);
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // getItemCount
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public int getItemCount() {
        return itemsArrayList == null ? 0 : itemsArrayList.size();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Filter
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence cs) {
                String q = cs.toString();
                ArrayList<ViewMatterModel> result;
                if (q.isEmpty()) {
                    result = new ArrayList<>(list_item);
                } else {
                    result = new ArrayList<>();
                    for (ViewMatterModel row : list_item) {
                        if (AndroidUtils.isNull(row.getTitle()).toLowerCase()
                                .contains(q.toLowerCase())
                                || AndroidUtils.isNull(row.getClient_name()).toLowerCase()
                                .contains(q.toLowerCase())
                                || AndroidUtils.isNull(row.getOwner_name()).toLowerCase()
                                .contains(q.toLowerCase())) {
                            result.add(row);
                        }
                    }
                }
                FilterResults fr = new FilterResults();
                fr.count = result.size();
                fr.values = result;
                return fr;
            }

            @Override
            protected void publishResults(CharSequence cs, FilterResults fr) {
                itemsArrayList = (ArrayList<ViewMatterModel>) fr.values;
                notifyDataSetChanged();
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ─────────────────────────────────────────────────────────────────────
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_matter_title, tv_case_number, tv_date_of_filling,
                tv_client_name, tv_owner_name, tv_initiated,
                filed, textView, owner, tag, tv_tag_name, matter_type, tv_matter_type;
        ImageView iv_initiated;
        ImageView custom_spinner_cardview;
        ListView sp_action;
        CardView action_list_card;
        LinearLayout action_layout, taglayout, typeLayout,ll_owner;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_matter_title = itemView.findViewById(R.id.tv_matter_title);
            tv_matter_title.setText(R.string.matter_title);
            tv_matter_title.setTextColor(Color.BLACK);

            tv_case_number = itemView.findViewById(R.id.tv_case_number);
            tv_case_number.setText(R.string.case_number);

            action_layout = itemView.findViewById(R.id.action_layout);
            taglayout = itemView.findViewById(R.id.taglayout);
            typeLayout = itemView.findViewById(R.id.typeLayout);
            custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
            ll_owner = itemView.findViewById(R.id.ll_owner);
            textView = itemView.findViewById(R.id.textView);
            textView.setText(R.string.client_);
//            textView.setTextColor(Color.BLACK);
//            textView.setTextSize(12);

            owner = itemView.findViewById(R.id.owner);
            owner.setText(R.string.owner);
//            owner.setTextColor(Color.BLACK);
//            owner.setTextSize(12);

            tag = itemView.findViewById(R.id.tag);
            tag.setText(R.string.tag_);
//            tag.setTextColor(Color.BLACK);
//            tag.setTextSize(12);
            tv_matter_type = itemView.findViewById(R.id.tv_matter_type);
            matter_type = itemView.findViewById(R.id.matter_type);

            matter_type.setText(R.string.type_);
            filed = itemView.findViewById(R.id.filed);
            filed.setText(R.string.filed);
//            filed.setTextColor(Color.BLACK);
//            filed.setTextSize(12);

            tv_date_of_filling = itemView.findViewById(R.id.tv_date_of_filling);
//            tv_date_of_filling.setTextSize(12);
//            tv_date_of_filling.setTextColor(Color.BLACK);

            tv_client_name = itemView.findViewById(R.id.tv_client_name);
            tv_client_name.setText("");
//            tv_client_name.setTextSize(12);
            tv_client_name.setMaxLines(2);
//            tv_client_name.setTextColor(Color.BLACK);
            tv_client_name.setGravity(Gravity.START);

            tv_tag_name = itemView.findViewById(R.id.tv_tag_name);
            tv_tag_name.setText("");
//            tv_tag_name.setTextSize(12);
//            tv_tag_name.setTextColor(Color.BLACK);

            tv_owner_name = itemView.findViewById(R.id.tv_owner_name);
            tv_owner_name.setText(R.string.owner_name);
//            tv_owner_name.setTextSize(12);
//            tv_owner_name.setTextColor(Color.BLACK);

            tv_initiated = itemView.findViewById(R.id.tv_initiated);
            tv_initiated.setText(R.string.pending);
//            tv_initiated.setTextSize(DynamicUtils.fifteen);

            iv_initiated = itemView.findViewById(R.id.iv_initiated);

            action_list_card = itemView.findViewById(R.id.action_list_card);
            sp_action = itemView.findViewById(R.id.list_client);
            if ("solo".equals(Constants.CATEGORY)) {
                ll_owner.setVisibility(GONE);
            } else {
                ll_owner.setVisibility(VISIBLE);
            }
        }
    }
}