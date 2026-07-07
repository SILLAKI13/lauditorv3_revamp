package com.digicoffer.lauditor.Groups.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.Groups.Groups;
import com.digicoffer.lauditor.Groups.ViewGroupsItemClickListener;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ViewGroupsAdpater extends RecyclerView.Adapter<ViewGroupsAdpater.ViewHolder>
        implements Filterable {

    // ─────────────────────────────────────────────────────────────────────────
    // masterList  = full unfiltered data, never mutated by the filter
    // displayList = what RecyclerView actually renders; every read comes here
    // ─────────────────────────────────────────────────────────────────────────
    private final ArrayList<ViewGroupModel> masterList;
    private ArrayList<ViewGroupModel> displayList;

    private final InterfaceListener eventListener;
    private final Context mcontext;
    private final ViewGroupsItemClickListener itemClickListener;

    // Static tag used by ViewHolder to decide which layout was inflated.
    public static String mTag = "";

    private final Button btn_submit;
    private String selectedGroupId = "";
    private final Groups group;

    // -1 means nothing is currently expanded
    private int expandedPosition = -1;

    // Optional reference so notifies are posted on the RecyclerView's queue
    private RecyclerView recyclerView;

    // ─────────────────────────────────────────────────────────────────────────
    // Null-safe helper — avoids repeated ternary boilerplate
    // ─────────────────────────────────────────────────────────────────────────
    private static String safe(String s) {
        return s != null ? s : "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RecyclerView binding
    // ─────────────────────────────────────────────────────────────────────────
    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Safely collapse the currently expanded row (call from dialog dismiss etc.)
    // ─────────────────────────────────────────────────────────────────────────
    public void collapseExpanded() {
        if (expandedPosition == -1) return;
        int pos = expandedPosition;
        expandedPosition = -1;
        safeNotifyItemChanged(pos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // All notify calls deferred via post() so they never fire during a layout
    // pass (root cause of the original IndexOutOfBoundsException).
    // ─────────────────────────────────────────────────────────────────────────
    private void safeNotifyItemChanged(final int position) {
        final Runnable r = () -> {
            if (position >= 0 && position < getItemCount()) {
                notifyItemChanged(position);
            }
        };
        if (recyclerView != null) {
            recyclerView.post(r);
        } else {
            new Handler(Looper.getMainLooper()).post(r);
        }
    }

    private void safeNotifyDataSetChanged() {
        final Runnable r = this::notifyDataSetChanged;
        if (recyclerView != null) {
            recyclerView.post(r);
        } else {
            new Handler(Looper.getMainLooper()).post(r);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────
    public ViewGroupsAdpater(ArrayList<ViewGroupModel> itemsArrayList,
                             Context context,
                             InterfaceListener eventListener,
                             String tag,
                             ViewGroupsItemClickListener itemClickListener,
                             Groups groups) {
        this(itemsArrayList, context, eventListener, tag, itemClickListener, groups, null);
    }

    public ViewGroupsAdpater(ArrayList<ViewGroupModel> itemsArrayList,
                             Context context,
                             InterfaceListener eventListener,
                             String tag,
                             ViewGroupsItemClickListener itemClickListener,
                             Groups groups,
                             Button btnSubmit) {
        ArrayList<ViewGroupModel> src = (itemsArrayList != null) ? itemsArrayList : new ArrayList<>();
        this.masterList        = new ArrayList<>(src);
        this.displayList       = new ArrayList<>(src);
        this.mcontext          = context;
        this.group             = groups;
        this.eventListener     = eventListener;
        mTag                   = (tag != null) ? tag : "";
        this.itemClickListener = itemClickListener;
        this.btn_submit        = btnSubmit;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = (charSequence != null)
                        ? charSequence.toString().toLowerCase().trim()
                        : "";

                List<ViewGroupModel> result;
                if (query.isEmpty()) {
                    result = new ArrayList<>(masterList);
                } else {
                    result = new ArrayList<>();
                    for (ViewGroupModel row : masterList) {
                        String field = "CGH".equals(mTag)
                                ? safe(row.getGroup_name())
                                : safe(row.getName());
                        if (field.toLowerCase().contains(query)) {
                            result.add(row);
                        }
                    }
                }

                FilterResults fr = new FilterResults();
                fr.values = result;
                fr.count  = result.size();
                return fr;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                displayList      = new ArrayList<>((List<ViewGroupModel>) filterResults.values);
                expandedPosition = -1; // positions may have shifted — reset
                safeNotifyDataSetChanged();
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Listener interface
    // ─────────────────────────────────────────────────────────────────────────
    public interface InterfaceListener {
        void EditGroup(ViewGroupModel viewGroupModel);
        void DeleteGroup(ViewGroupModel viewGroupModel, ArrayList<ViewGroupModel> itemsArrayList);
        void CGH(ViewGroupModel viewGroupModel, ArrayList<ViewGroupModel> itemsArrayList);
        void UGM(ViewGroupModel viewGroupModel) throws JSONException;
        void GAL(ViewGroupModel viewGroupModel) throws JSONException;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onCreateViewHolder
    // ─────────────────────────────────────────────────────────────────────────
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        if ("VG".equals(mTag)) {
            layoutRes = R.layout.view_groups;
        } else if ("UGM".equals(mTag)) {
            layoutRes = R.layout.select_team_members;
        } else {
            layoutRes = R.layout.radio_button_layout;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(itemView);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onBindViewHolder — ALL data reads from displayList only
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.i("ViewGroupsAdpater", "bind pos=" + position + " tag=" + mTag);

        // Hard guard against any stale-position scenario
        if (position < 0 || position >= displayList.size()) return;

        final ViewGroupModel model = displayList.get(position);

        if ("VG".equals(mTag)) {
            bindVgRow(holder, model, position);
        } else if ("UGM".equals(mTag)) {
            bindUgmRow(holder, model);
        } else {
            bindRadioRow(holder, model);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VG row
    // ─────────────────────────────────────────────────────────────────────────
    private void bindVgRow(@NonNull final ViewHolder holder,
                           final ViewGroupModel model,
                           final int position) {

        final String itemName = safe(model.getName());

        // Blue stroke highlight
        final String hiresGroup = safe(Constants.hires_group);
        if (!hiresGroup.isEmpty() && hiresGroup.equals(itemName)) {
            holder.action_cv.setBackground(ContextCompat.getDrawable(
                    holder.itemView.getContext(), R.drawable.blue_stroke_card));
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    holder.action_cv.setBackground(ContextCompat.getDrawable(
                            holder.itemView.getContext(),
                            R.drawable.rectangular_white_background)), 5000);
        } else {
            holder.action_cv.setBackground(ContextCompat.getDrawable(
                    holder.itemView.getContext(), R.drawable.rectangular_white_background));
        }

        // Text values
        holder.tv_user_type.setText(itemName);
        holder.tv_owner_name.setText(safe(model.getOwner_name()));
        holder.tv_date.setText(safe(model.getCreated()));
        holder.created_id.setText(R.string.created_);
        holder.tv_owner_name.setTextColor(Color.BLACK);
        holder.tv_date.setTextColor(Color.BLACK);

        // Members count
        final String memberCount = safe(model.getMemberCount());
        if (!memberCount.isEmpty() && !memberCount.equals("0")) {
            holder.ll_members_count.setVisibility(View.VISIBLE);
            holder.tv_members_count.setText(memberCount);
        } else {
            holder.ll_members_count.setVisibility(View.GONE);
        }

        // Build LOCAL actions list — never share a list across items
        final ArrayList<ActionModel> itemActions = new ArrayList<>();
        final String role = safe(Constants.ROLE);

        if (itemName.equals("SuperUser") || itemName.equals("AAM")) {
            itemActions.add(new ActionModel("Update Group Members List"));
            itemActions.add(new ActionModel("Group Activity Log"));
        } else {
            if (role.equals("GH")) {
                itemActions.add(new ActionModel("Edit Group Info"));
                itemActions.add(new ActionModel("Group Activity Log"));
            } else {
                itemActions.add(new ActionModel("Edit Group Info"));
                itemActions.add(new ActionModel("Update Group Members List"));
                itemActions.add(new ActionModel("Update Group Head"));
                itemActions.add(new ActionModel("Delete Group"));
                itemActions.add(new ActionModel("Group Activity Log"));
            }
        }

        // Show / hide action ListView
        boolean isExpanded = (position == expandedPosition);

        // Always wipe stale listeners before rebinding
        holder.custom_spinner_cardview.setOnClickListener(null);
        holder.sp_action.setOnItemClickListener(null);

        if (isExpanded) {
            CommonSpinnerAdapter itemAdapter =
                    new CommonSpinnerAdapter((Activity) mcontext, itemActions);
            holder.sp_action.setAdapter(itemAdapter);
            holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));
            holder.action_list_card.setVisibility(View.VISIBLE);
            holder.sp_action.setVisibility(View.VISIBLE);
        } else {
            holder.sp_action.setAdapter(null);
            holder.action_list_card.setVisibility(View.GONE);
            holder.sp_action.setVisibility(View.GONE);
        }

        // Toggle expand / collapse
        holder.custom_spinner_cardview.setOnClickListener(v -> {
            int cur = holder.getAdapterPosition();
            if (cur == RecyclerView.NO_POSITION) return;

            int prev = expandedPosition;
            expandedPosition = (expandedPosition == cur) ? -1 : cur;

            if (prev != -1 && prev != cur) safeNotifyItemChanged(prev);
            safeNotifyItemChanged(cur);
        });

        // Action item click
        holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
            int cur = holder.getAdapterPosition();
            if (cur == RecyclerView.NO_POSITION) return;
            if (pos < 0 || pos >= itemActions.size()) return;

            final String actionName     = itemActions.get(pos).getName();
            final ArrayList<ViewGroupModel> snap = new ArrayList<>(displayList);

            expandedPosition = -1;
            safeNotifyItemChanged(cur);

            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    switch (actionName) {
                        case "Edit Group Info":
                            group.page_name("Edit Group Info");
                            eventListener.EditGroup(model);
                            break;
                        case "Delete Group":
                            group.page_name("Assign Group");
                            eventListener.DeleteGroup(model, snap);
                            break;
                        case "Update Group Members List":
                            group.page_name("Update Group Members List");
                            try { eventListener.UGM(model); }
                            catch (Exception e) { e.fillInStackTrace(); }
                            break;
                        case "Update Group Head":
                            group.page_name("Update Group Head");
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> eventListener.CGH(model, snap), 100);
                            break;
                        case "Group Activity Log":
                            group.page_name("Group Activity Log");
                            try { eventListener.GAL(model); }
                            catch (JSONException e) { e.fillInStackTrace(); }
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    AndroidUtils.showAlert(e.getMessage(), group.getActivity());
                }
            });
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UGM row (checkbox)
    // ─────────────────────────────────────────────────────────────────────────
    private void bindUgmRow(@NonNull ViewHolder holder, final ViewGroupModel model) {
        holder.cb_team_members.setOnCheckedChangeListener(null); // clear before setChecked
        holder.cb_team_members.setChecked(model.isChecked());
        holder.tv_tm_name.setText(safe(model.getName()));

        holder.cb_team_members.setOnCheckedChangeListener((btn, isChecked) -> {
            int p = holder.getAdapterPosition();
            if (p == RecyclerView.NO_POSITION || p >= displayList.size()) return;

            displayList.get(p).setChecked(isChecked);

            // Keep masterList in sync by ID
            final String matchId = safe(displayList.get(p).getId());
            if (!matchId.isEmpty()) {
                for (ViewGroupModel m : masterList) {
                    if (matchId.equals(safe(m.getId()))) {
                        m.setChecked(isChecked);
                        break;
                    }
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CGH / radio-button row
    // ─────────────────────────────────────────────────────────────────────────
    private void bindRadioRow(@NonNull ViewHolder holder, final ViewGroupModel model) {
        final String groupId   = safe(model.getGroup_id());
        final String groupName = safe(model.getGroup_name());

        holder.tv_gh_name.setText(groupName);

        holder.rb_group_selected.setOnCheckedChangeListener(null); // clear before setChecked
        holder.rb_group_selected.setChecked(groupId.equals(selectedGroupId));
        holder.rb_group_selected.setTag(groupId);

        holder.rb_group_selected.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isChecked) return;
            selectedGroupId = groupId;
            if (itemClickListener != null) itemClickListener.onClick(groupId);
            safeNotifyDataSetChanged();
            if (btn_submit != null) {
                btn_submit.setEnabled(true);
                btn_submit.setAlpha(1.0f);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Select or deselect every item in both lists. */
    public void selectOrDeselectAll(boolean isChecked) {
        for (ViewGroupModel m : masterList)  m.setChecked(isChecked);
        for (ViewGroupModel m : displayList) m.setChecked(isChecked);
        safeNotifyDataSetChanged();
    }

    /** Currently displayed (post-filter) list. */
    public ArrayList<ViewGroupModel> getDisplayList() { return displayList; }

    /** Full unfiltered list. */
    public ArrayList<ViewGroupModel> getMasterList()   { return masterList; }

    @Override
    public int getItemCount() {
        return (displayList != null) ? displayList.size() : 0;
    }

    /** Stable IDs prevent RecyclerView confusing items during animations. */
    @Override
    public long getItemId(int position) {
        if (displayList == null || position < 0 || position >= displayList.size()) {
            return RecyclerView.NO_ID;
        }
        ViewGroupModel item = displayList.get(position);
        String uid = !safe(item.getId()).isEmpty() ? item.getId() : item.getName();
        return (long) safe(uid).hashCode();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ─────────────────────────────────────────────────────────────────────────
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // VG-only views
        TextView     tv_user_type;
        TextView     tv_members;
        TextView     tv_members_count;
        CardView     action_cv;
        CardView     action_list_card;
        LinearLayout ll_members_count;

        // Views present across layouts (null if not in current layout — safe)
        TextView     tv_owner_name;
        TextView     tv_date;
        TextView     created_id;
        TextView     custom_spinner;
        TextView     tv_gh_name;
        TextView     tv_tm_name;
        LinearLayout ll_owner;
        LinearLayout check_layout;
        ImageView    custom_spinner_cardview;

        final ListView    sp_action;
        final CheckBox    cb_team_members;
        final CheckBox rb_group_selected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if ("VG".equals(mTag)) {
                tv_user_type     = itemView.findViewById(R.id.tv_group_name);
                ll_members_count = itemView.findViewById(R.id.ll_members_count);
                action_cv        = itemView.findViewById(R.id.action_cv);
                action_list_card = itemView.findViewById(R.id.action_list_card);
                tv_members       = itemView.findViewById(R.id.tv_members);
                tv_members_count = itemView.findViewById(R.id.tv_members_count);

                if (tv_members != null) {
                    tv_members.setText(R.string.number_of_members);
                    tv_members.setMaxLines(1);
                }
                if (tv_members_count != null) {
                    tv_members_count.setTextColor(
                            ContextCompat.getColor(tv_members_count.getContext(), R.color.black));
                }
            }

            // These IDs may or may not exist in the current layout;
            // findViewById returns null safely when they don't.
            tv_owner_name           = itemView.findViewById(R.id.tv_group_head);
            tv_gh_name              = itemView.findViewById(R.id.tv_gh_name);
            check_layout            = itemView.findViewById(R.id.check_layout);
            cb_team_members         = itemView.findViewById(R.id.chk_selected);
            tv_date                 = itemView.findViewById(R.id.tv_date);
            created_id              = itemView.findViewById(R.id.created_id);
            custom_spinner          = itemView.findViewById(R.id.custom_spinner);
            custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
            sp_action               = itemView.findViewById(R.id.action_list);
            rb_group_selected       = itemView.findViewById(R.id.rb_group_selected);
            tv_tm_name              = itemView.findViewById(R.id.tv_tm_name);
            ll_owner                = itemView.findViewById(R.id.ll_owner);
        }
    }
}