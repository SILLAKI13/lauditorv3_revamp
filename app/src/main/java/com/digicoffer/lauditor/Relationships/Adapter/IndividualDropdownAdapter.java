package com.digicoffer.lauditor.Relationships.Adapter;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskEmail;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskPhoneNumber;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.digicoffer.lauditor.Relationships.Model.IndividualModel;
import com.digicoffer.lauditor.R;

import java.util.ArrayList;
import java.util.List;

public class IndividualDropdownAdapter extends ArrayAdapter<IndividualModel> {

    private final LayoutInflater inflater;
    private List<IndividualModel> originalList;
    private List<IndividualModel> filteredList;

    public IndividualDropdownAdapter(@NonNull Context context, List<IndividualModel> list) {
        super(context, 0, list);
        inflater = LayoutInflater.from(context);
        this.originalList = new ArrayList<>(list);
        this.filteredList = new ArrayList<>(list);
    }

    @Override
    public int getCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    @Override
    public IndividualModel getItem(int position) {
        return filteredList != null ? filteredList.get(position) : null;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.dropdown_individual_xml, parent, false);

        IndividualModel model = getItem(position);
        if (model != null) {
            TextView tvName = view.findViewById(R.id.tv_name);
            TextView tvEmail = view.findViewById(R.id.tv_email);
            TextView tvMobile = view.findViewById(R.id.tv_mobile);

            String fullName = (model.getFirst_name() + " " + model.getLast_name()).trim();
            tvName.setText(fullName.isEmpty() ? "No Name" : fullName);
            tvEmail.setText(maskEmail(model.getEmail()));
            tvMobile.setText(maskPhoneNumber(model.getMobile()));
        }

        return view;
    }

    public void updateList(List<IndividualModel> newList) {
        this.originalList = new ArrayList<>(newList);
        this.filteredList = new ArrayList<>(newList);
        clear();
        addAll(filteredList);
        notifyDataSetChanged();
        Log.d("IndividualDropdownAdapter", "Updated list with " + filteredList.size() + " items");
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<IndividualModel> filtered = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filtered.addAll(originalList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (IndividualModel item : originalList) {
                        String name = (item.getFirst_name() + " " + item.getLast_name()).toLowerCase();
                        String email = item.getEmail().toLowerCase();

                        if (name.contains(filterPattern) || email.contains(filterPattern)) {
                            filtered.add(item);
                        }
                    }
                }

                results.values = filtered;
                results.count = filtered.size();
                Log.d("IndividualDropdownAdapter", "Filtered: " + filtered.size() + " items for query: " + constraint);
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<IndividualModel>) results.values;
                clear();
                if (filteredList != null) {
                    addAll(filteredList);
                }
                notifyDataSetChanged();
            }
        };
    }
}