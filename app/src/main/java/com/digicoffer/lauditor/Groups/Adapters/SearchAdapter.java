package com.digicoffer.lauditor.Groups.Adapters;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.formatTimestamp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.SearchDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> implements Filterable {
    ArrayList<SearchDo>arrayList = new ArrayList<>();
    ArrayList<SearchDo> itemsArrayList = new ArrayList<>();

    public SearchAdapter(ArrayList<SearchDo> arrayList) {
        this.arrayList = arrayList;
        this.itemsArrayList = arrayList;
    }

    @NonNull
    @Override

    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchresults, parent, false);
        return new SearchAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SearchDo searchDo = arrayList.get(position);
        arrayList = itemsArrayList;
        holder.tv_category.setText(searchDo.getCategory());
        String formattedDate = formatTimestamp(searchDo.getTimestamp());
        holder.tv_timestamp.setText(formattedDate);
        holder.tv_message.setText(searchDo.getMsg());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    arrayList = itemsArrayList;
                } else {
                    ArrayList<SearchDo> filteredList = new ArrayList<>();
                    for (SearchDo row : itemsArrayList) {
//                            if (row.isChecked()){
//                                row.setChecked(false);
//                            }else
//                            {
//                                row.setChecked(true  );
//                            }
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (AndroidUtils.isNull(row.getMsg()).toLowerCase().contains(charString.toLowerCase()) ) {
                            filteredList.add(row);
                        }
                    }
                    arrayList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.count = arrayList.size();
                filterResults.values = arrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                arrayList = (ArrayList<SearchDo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_category,tv_timestamp,tv_message;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_category = itemView.findViewById(R.id.tv_category_name);
            tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
            tv_message = itemView.findViewById(R.id.tv_message);
        }
    }
}
