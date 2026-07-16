package com.digicoffer.lauditor.Relationships.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.IndividualModel
import java.util.ArrayList

class IndividualDropdownAdapter(context: Context, list: List<IndividualModel>) :
    ArrayAdapter<IndividualModel>(context, 0, list) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalList: List<IndividualModel> = ArrayList(list)
    private var filteredList: List<IndividualModel> = ArrayList(list)

    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): IndividualModel? {
        return if (position in filteredList.indices) filteredList[position] else null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.dropdown_individual_xml, parent, false)
        val model = getItem(position)
        if (model != null) {
            val tvName = view.findViewById<TextView>(R.id.tv_name)
            val tvEmail = view.findViewById<TextView>(R.id.tv_email)
            val tvMobile = view.findViewById<TextView>(R.id.tv_mobile)

            val firstName = model.first_name ?: ""
            val lastName = model.last_name ?: ""
            val fullName = "$firstName $lastName".trim()
            tvName.text = if (fullName.isEmpty()) "No Name" else fullName
            tvEmail.text = AndroidUtils.maskEmail(model.email ?: "")
            tvMobile.text = AndroidUtils.maskPhoneNumber(model.mobile ?: "")
        }
        return view
    }

    fun updateList(newList: List<IndividualModel>) {
        this.originalList = ArrayList(newList)
        this.filteredList = ArrayList(newList)
        clear()
        addAll(filteredList)
        notifyDataSetChanged()
        Log.d("IndividualDropdownAdapter", "Updated list with ${filteredList.size} items")
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filtered: MutableList<IndividualModel> = ArrayList()

                if (constraint.isNullOrEmpty()) {
                    filtered.addAll(originalList)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in originalList) {
                        val firstName = item.first_name ?: ""
                        val lastName = item.last_name ?: ""
                        val name = "$firstName $lastName".lowercase()
                        val email = (item.email ?: "").lowercase()

                        if (name.contains(filterPattern) || email.contains(filterPattern)) {
                            filtered.add(item)
                        }
                    }
                }

                results.values = filtered
                results.count = filtered.size
                Log.d("IndividualDropdownAdapter", "Filtered: ${filtered.size} items for query: $constraint")
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = (results?.values as? List<IndividualModel>) ?: ArrayList()
                clear()
                addAll(filteredList)
                notifyDataSetChanged()
            }
        }
    }
}
