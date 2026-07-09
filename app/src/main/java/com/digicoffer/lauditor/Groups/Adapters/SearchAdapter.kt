package com.digicoffer.lauditor.Groups.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.formatTimestamp
import com.digicoffer.lauditor.Groups.Models.SearchDo
import com.digicoffer.lauditor.R
import java.util.ArrayList

class SearchAdapter(private var itemsArrayList: ArrayList<SearchDo>) :
    RecyclerView.Adapter<SearchAdapter.MyViewHolder>(), Filterable {

    private var arrayList: ArrayList<SearchDo> = itemsArrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.searchresults, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val searchDo = arrayList[position]
        arrayList = itemsArrayList
        holder.tv_category.text = searchDo.category
        val formattedDate = formatTimestamp(searchDo.timestamp)
        holder.tv_timestamp.text = formattedDate
        holder.tv_message.text = searchDo.msg
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString() ?: ""
                if (charString.isEmpty()) {
                    arrayList = itemsArrayList
                } else {
                    val filteredList = ArrayList<SearchDo>()
                    for (row in itemsArrayList) {
                        if (AndroidUtils.isNull(row.msg).lowercase().contains(charString.lowercase())) {
                            filteredList.add(row)
                        }
                    }
                    arrayList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = arrayList.size
                filterResults.values = arrayList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                arrayList = filterResults.values as ArrayList<SearchDo>
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_category: TextView = itemView.findViewById(R.id.tv_category_name)
        val tv_timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val tv_message: TextView = itemView.findViewById(R.id.tv_message)
    }
}
