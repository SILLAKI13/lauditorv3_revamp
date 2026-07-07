package com.digicoffer.lauditor.Members.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.Members.Members;
import com.digicoffer.lauditor.Members.MembersModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> implements Filterable {

    ArrayList<MembersModel> itemsArrayList;
    ArrayList<MembersModel> list_item;
    Context mcontext;
    Members members;
    private int expandedPosition = -1;
    EventListener eventListener;

    public MembersAdapter(ArrayList<MembersModel> members_list) {
        this.itemsArrayList = new ArrayList<>(members_list);
        this.list_item = new ArrayList<>(members_list);
    }

    public MembersAdapter(ArrayList<MembersModel> itemsArrayList, Context context, EventListener listener, Members members) {
        this.itemsArrayList = new ArrayList<>(itemsArrayList);
        this.list_item = new ArrayList<>(itemsArrayList);
        this.mcontext = context;
        this.eventListener = listener;
        this.members = members;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                ArrayList<MembersModel> resultList;
                if (charString.isEmpty()) {
                    resultList = new ArrayList<>(list_item);
                } else {
                    resultList = new ArrayList<>();
                    for (MembersModel row : list_item) {
                        if (AndroidUtils.isNull(row.getName()).toLowerCase().contains(charString.toLowerCase())
                                || AndroidUtils.isNull(row.getDesignation()).toLowerCase().contains(charString.toLowerCase())
                                || AndroidUtils.isNull(row.getEmail()).toLowerCase().contains(charString.toLowerCase())) {
                            resultList.add(row);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = resultList.size();
                filterResults.values = resultList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsArrayList = (ArrayList<MembersModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface EventListener {
        void EditMember(MembersModel membersModel);

        void UpdateGroupAccess(MembersModel membersModel) throws JSONException;

        void ResetPassword(MembersModel membersModel);

        void DeleteMember(MembersModel membersModel);

        void Upgrade(MembersModel membersModel);
    }

    @NonNull
    @Override
    public MembersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_members, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MembersModel membersModel = itemsArrayList.get(position);

        ArrayList<ActionModel> localActionsList = new ArrayList<>();
        if (Constants.ROLE.equals("GH")) {
            localActionsList.add(new ActionModel("Edit Member Info"));
            localActionsList.add(new ActionModel("Delete Member"));
            localActionsList.add(new ActionModel("Reset Password"));
            localActionsList.add(new ActionModel("Upgrade as Practice Partner"));
        } else {
            localActionsList.add(new ActionModel("Edit Member Info"));
            localActionsList.add(new ActionModel("Update Group Access"));
            localActionsList.add(new ActionModel("Reset Password"));
            localActionsList.add(new ActionModel("Delete Member"));
            localActionsList.add(new ActionModel("Upgrade as Practice Partner"));
        }

        if (membersModel.isIsdisabled()) {
            holder.custom_spinner_cardview.setEnabled(false);
            holder.custom_spinner_cardview.setAlpha(0.5f);
        } else {
            holder.custom_spinner_cardview.setEnabled(true);
            holder.custom_spinner_cardview.setAlpha(1.0f);
        }

        // Set member name
        holder.tv_member_name.setText(isValidString(membersModel.getName()) ? membersModel.getName() : "");

        // Set designation with label
        holder.tv_member_type.setText("Designation : ");
        holder.tv_litigation.setText(isValidString(membersModel.getDesignation()) ? membersModel.getDesignation() : "");

        // Set currency with label
        holder.tv_currency_type.setText("Currency : ");
        String symbol = getCurrencySymbolFromString(isValidString(membersModel.getCurrency()) ? membersModel.getCurrency() : "");
        holder.tv_currency.setText((isValidString(membersModel.getCurrency()) ? membersModel.getCurrency() : ""));

        // Set rate with label
        holder.tv_rate_label.setText("Rate : ");
        holder.tv_rate_value.setText(isValidString(membersModel.getDefaultRate()) ? symbol + membersModel.getDefaultRate() : "0");

        // Set email with label
        holder.tv_email_label.setText("Email : ");
        holder.tv_email_id.setText(isValidString(membersModel.getEmail()) ? membersModel.getEmail() : "");

        CommonSpinnerAdapter spinner_adapter = new CommonSpinnerAdapter((Activity) mcontext, localActionsList);
        holder.sp_action.setAdapter(spinner_adapter);
        AndroidUtils.setDynamicHeight(holder.sp_action);

        boolean isExpanded = position == expandedPosition;
        holder.action_list_card.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.sp_action.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.custom_spinner_cardview.setOnClickListener(null);
        holder.sp_action.setOnItemClickListener(null);

        holder.custom_spinner_cardview.setOnClickListener(v -> {
            int previousExpanded = expandedPosition;
            expandedPosition = (expandedPosition == position) ? -1 : position;

            if (members != null && members.rv_view_members != null) {
                members.rv_view_members.post(() -> {
                    if (previousExpanded != -1) notifyItemChanged(previousExpanded);
                    notifyItemChanged(position);
                });
            } else {
                if (previousExpanded != -1) notifyItemChanged(previousExpanded);
                notifyItemChanged(position);
            }
        });

        holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
            spinner_adapter.setSelectedPosition(pos);
            String selectedAction = localActionsList.get(pos).getName().trim();

            expandedPosition = -1;
            if (members != null && members.rv_view_members != null) {
                members.rv_view_members.post(() -> notifyItemChanged(position));
            } else {
                notifyItemChanged(position);
            }

            switch (selectedAction) {
                case "Edit Member Info":
                    if (members != null) members.model_name("Edit Member");
                    if (eventListener != null) eventListener.EditMember(membersModel);
                    break;

                case "Update Group Access":
                    if (members != null) members.model_name("Update Group Access");
                    if (eventListener != null) {
                        try {
                            eventListener.UpdateGroupAccess(membersModel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "Reset Password":
                    if (eventListener != null) eventListener.ResetPassword(membersModel);
                    break;

                case "Delete Member":
                    if (members != null) members.model_name("Delete Member");
                    if (eventListener != null) eventListener.DeleteMember(membersModel);
                    break;

                case "Upgrade as Practice Partner":
                    if (members != null) members.model_name("Upgrade as Practice Partners");
                    if (eventListener != null) eventListener.Upgrade(membersModel);
                    break;
            }
        });
    }

    private boolean isValidString(String value) {
        return value != null && !value.equals("null") && !value.trim().isEmpty();
    }

    @Override
    public int getItemCount() {
        return itemsArrayList == null ? 0 : itemsArrayList.size();
    }

    private static final Map<String, String> currencySymbols = new HashMap<String, String>() {{
        put("USD", "$");
        put("EUR", "€");
        put("JPY", "¥");
        put("GBP", "£");
        put("AUD", "A$");
        put("CAD", "C$");
        put("CHF", "CHF");
        put("KWD", "KD");
        put("BHD", "BD");
        put("INR", "₹");
    }};

    private String getCurrencySymbolFromString(String currencyString) {
        if (currencyString == null) return "";
        int start = currencyString.indexOf("(");
        int end = currencyString.indexOf(")");
        if (start != -1 && end != -1 && end > start) {
            String code = currencyString.substring(start + 1, end).toUpperCase();
            String symbol = currencySymbols.get(code);
            return symbol != null ? symbol : code;
        }
        return currencyString;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView designation, tv_member_name, tv_member_type, tv_litigation, tv_currency_type, tv_currency;
        TextView tv_rate_label, tv_rate_value, tv_email_label, tv_email_id;
        ListView sp_action;
        ImageView custom_spinner_cardview;
        CardView action_list_card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_member_name = itemView.findViewById(R.id.tv_member_name);
            designation = itemView.findViewById(R.id.designation);
            designation.setText("Designation :");
            tv_member_type = itemView.findViewById(R.id.tv_currency_type);
            tv_litigation = itemView.findViewById(R.id.tv_litigation);
            tv_currency_type = itemView.findViewById(R.id.tv_currency_type);
            tv_currency = itemView.findViewById(R.id.tv_currency);
            tv_rate_label = itemView.findViewById(R.id.tv_rate_label);
            tv_rate_value = itemView.findViewById(R.id.tv_rate_value);
            tv_email_label = itemView.findViewById(R.id.tv_email_label);
            tv_email_id = itemView.findViewById(R.id.tv_email_id);
            custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
            sp_action = itemView.findViewById(R.id.list_client);
            action_list_card = itemView.findViewById(R.id.action_list_card);
        }
    }
}