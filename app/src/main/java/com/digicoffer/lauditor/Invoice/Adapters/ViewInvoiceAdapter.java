package com.digicoffer.lauditor.Invoice.Adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.Invoice.Models.InvoiceModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ViewInvoiceAdapter extends RecyclerView.Adapter<ViewInvoiceAdapter.InvoiceViewHolder>
        implements Filterable {

    // ── Data ──────────────────────────────────────────────────────────────────
    ArrayList<InvoiceModel> itemsArrayList;
    ArrayList<InvoiceModel> list_item;
    Context context;
    InvoiceActionListener actionListener;

    // Track expanded position for toggle behaviour
    private int expandedPosition = -1;

    private RecyclerView recyclerView;

    // ── Setter ────────────────────────────────────────────────────────────────
    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    // ── Interface ─────────────────────────────────────────────────────────────
    public interface InvoiceActionListener {
        void onViewDetails(InvoiceModel invoiceModel);
        void onEditInvoice(InvoiceModel invoiceModel);
        void onChangeStatus(InvoiceModel invoiceModel);
        void onShareInvoice(InvoiceModel invoiceModel);
        void onDeleteInvoice(InvoiceModel invoiceModel);
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public ViewInvoiceAdapter(ArrayList<InvoiceModel> itemsArrayList,
                              Context context,
                              InvoiceActionListener actionListener) {
        this.itemsArrayList = itemsArrayList;
        this.list_item = new ArrayList<>(itemsArrayList);
        this.context = context;
        this.actionListener = actionListener;
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────────
    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice_recyclerview, parent, false);
        return new InvoiceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
        InvoiceModel model = itemsArrayList.get(position);

        try {
            // ── Client Name ───────────────────────────────────────────────────
            String displayName = model.getName() != null && !model.getName().isEmpty()
                    ? model.getName() : "";
            holder.tv_client_name.setText(displayName);

            // ── Invoice Number ────────────────────────────────────────────────
            holder.tv_invoice_no.setText(model.getInvoice_no());

            // ── Create Date ───────────────────────────────────────────────────
            holder.tv_create_date.setText(model.getDate());

            // ── Due Date ──────────────────────────────────────────────────────
            holder.tv_due_date.setText(model.getDueDate());

            // ── Created By ────────────────────────────────────────────────────
            holder.tv_created_by.setText(model.getCreatedby());

            // ── Status ────────────────────────────────────────────────────────
            String status = model.getStatus();
            if (status != null && !status.isEmpty()) {
                status = status.substring(0, 1).toUpperCase(Locale.ROOT) + status.substring(1);
            }
            holder.tv_invoice_status.setText(status);

            if (status != null && status.equalsIgnoreCase("collected")) {
                holder.tv_invoice_status.setTextColor(
                        ContextCompat.getColor(context, R.color.completed_text));
                holder.tv_invoice_status.setBackgroundResource(R.drawable.completed_badge);
            } else {
                holder.tv_invoice_status.setTextColor(
                        ContextCompat.getColor(context, R.color.scheduled_text));
                holder.tv_invoice_status.setBackgroundResource(R.drawable.scheduled_badge);
            }

            // ── Three-dot enable / disable ────────────────────────────────────
            if (model.isIsdisabled()) {
                holder.iv_action_btn.setEnabled(false);
                holder.iv_action_btn.setAlpha(0.5f);
            } else {
                holder.iv_action_btn.setEnabled(true);
                holder.iv_action_btn.setAlpha(1.0f);
            }

            // ── Build actions list ────────────────────────────────────────────
            ArrayList<ActionModel> localActions = new ArrayList<>();
            localActions.add(new ActionModel("View Details"));
            localActions.add(new ActionModel("Edit Invoice"));
            localActions.add(new ActionModel("Change Status"));
            if (model.isCan_share()) {
                localActions.add(new ActionModel("Share"));
            }
            localActions.add(new ActionModel("Delete"));

            // ── Show / hide dropdown based on expandedPosition ────────────────
            // Because the action list is inside a RelativeLayout with elevation,
            // it now floats OVER the cards below it instead of pushing them down.
            boolean isExpanded = (position == expandedPosition);
            holder.lv_invoice_actions.setVisibility(isExpanded ? VISIBLE : GONE);

            // ── Always re-set the adapter so item clicks work after rebind ────
            CommonSpinnerAdapter<ActionModel> localAdapter =
                    new CommonSpinnerAdapter<>((Activity) context, localActions);
            holder.lv_invoice_actions.setAdapter(localAdapter);
            holder.lv_invoice_actions.post(
                    () -> AndroidUtils.setDynamicHeight(holder.lv_invoice_actions));

            // ── Three-dot click: expand / collapse ────────────────────────────
            holder.iv_action_btn.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (expandedPosition == currentPos) {
                    // Collapse
                    expandedPosition = -1;
                    notifyItemChanged(currentPos);
                } else {
                    // Collapse the previously expanded item first
                    int oldPos = expandedPosition;
                    expandedPosition = currentPos;
                    if (oldPos != -1) notifyItemChanged(oldPos);
                    notifyItemChanged(currentPos);
                }
            });

            // ── Action item click ─────────────────────────────────────────────
            holder.lv_invoice_actions.setOnItemClickListener((parent, view, pos, id) -> {
                String selectedAction = localActions.get(pos).getName();
                switch (selectedAction) {
                    case "View Details":  actionListener.onViewDetails(model);   break;
                    case "Edit Invoice":  actionListener.onEditInvoice(model);   break;
                    case "Change Status": actionListener.onChangeStatus(model);  break;
                    case "Share":         actionListener.onShareInvoice(model);  break;
                    case "Delete":        actionListener.onDeleteInvoice(model); break;
                    default:
                        Log.w("ViewInvoiceAdapter", "Unknown action: " + selectedAction);
                        break;
                }
                // Collapse after selection
                expandedPosition = -1;
                notifyItemChanged(holder.getAdapterPosition());
            });

        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), context);
            Log.e("ViewInvoiceAdapter", Objects.requireNonNull(e.getMessage()));
        }
    }

    @Override
    public int getItemCount() {
        return itemsArrayList.size();
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString().toLowerCase(Locale.ROOT).trim();
                ArrayList<InvoiceModel> filtered = new ArrayList<>();
                if (query.isEmpty()) {
                    filtered.addAll(list_item);
                } else {
                    for (InvoiceModel item : list_item) {
                        boolean matchName      = item.getName()       != null && item.getName().toLowerCase(Locale.ROOT).contains(query);
                        boolean matchInvNo     = item.getInvoice_no() != null && item.getInvoice_no().toLowerCase(Locale.ROOT).contains(query);
                        boolean matchCreatedBy = item.getCreatedby()  != null && item.getCreatedby().toLowerCase(Locale.ROOT).contains(query);
                        boolean matchStatus    = item.getStatus()     != null && item.getStatus().toLowerCase(Locale.ROOT).contains(query);
                        if (matchName || matchInvNo || matchCreatedBy || matchStatus) {
                            filtered.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                results.count  = filtered.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsArrayList = (ArrayList<InvoiceModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {

        TextView  tv_client_name;
        TextView  tv_invoice_no;
        TextView  tv_create_date;
        TextView  tv_due_date;
        TextView  tv_created_by;
        TextView  tv_invoice_status;
        ImageView iv_action_btn;
        ListView  lv_invoice_actions;   // ← kept: the original CardView-styled dropdown

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_client_name     = itemView.findViewById(R.id.tv_client_name);
            tv_invoice_no      = itemView.findViewById(R.id.tv_invoice_no);
            tv_create_date     = itemView.findViewById(R.id.tv_create_date);
            tv_due_date        = itemView.findViewById(R.id.tv_due_date);
            tv_created_by      = itemView.findViewById(R.id.tv_created_by);
            tv_invoice_status  = itemView.findViewById(R.id.tv_invoice_status);
            iv_action_btn      = itemView.findViewById(R.id.iv_action_btn);
            lv_invoice_actions = itemView.findViewById(R.id.lv_invoice_actions);
            lv_invoice_actions.setNestedScrollingEnabled(false);
            lv_invoice_actions.setFocusable(false);
            lv_invoice_actions.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);
        }
    }
}