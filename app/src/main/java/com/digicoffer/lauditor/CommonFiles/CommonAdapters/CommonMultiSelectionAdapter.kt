package com.digicoffer.lauditor.CommonFiles.CommonAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import java.util.ArrayList
import java.util.HashSet

class CommonMultiSelectionAdapter(
    private val context: Context,
    practiceAreas: List<String>?,
    preSelectedItems: List<String>?,
    private val listener: OnSelectionChangeListener?
) : RecyclerView.Adapter<CommonMultiSelectionAdapter.ViewHolder>() {

    private var allPracticeAreas: ArrayList<String> = if (practiceAreas != null) ArrayList(practiceAreas) else ArrayList()
    private var filteredPracticeAreas: ArrayList<String> = if (practiceAreas != null) ArrayList(practiceAreas) else ArrayList()
    private var selectedPracticeAreas: HashSet<String> = if (preSelectedItems != null) HashSet(preSelectedItems) else HashSet()
    private var suggestedItems: HashSet<String> = HashSet()
    private var showSuggestIcon = false

    interface OnSelectionChangeListener {
        fun onSelectionChanged(selectedItems: List<String>)
    }

    fun setSuggestedItems(suggested: List<String>?, showIcon: Boolean) {
        this.suggestedItems = if (suggested != null) HashSet(suggested) else HashSet()
        this.showSuggestIcon = showIcon
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.select_multiple_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val practiceArea = filteredPracticeAreas[position]

        holder.tvPracticeName.text = practiceArea
        val isOtherOption = practiceArea.startsWith("+ Other")

        if (isOtherOption) {
            holder.checkBox.visibility = View.GONE
            holder.checkBox.isChecked = false
        } else {
            holder.checkBox.visibility = View.VISIBLE
            val isSelected = selectedPracticeAreas.contains(practiceArea)
            holder.checkBox.isChecked = isSelected
            holder.checkBox.isEnabled = true
            holder.checkBox.alpha = 1f
        }

        if (holder.ivSuggest != null) {
            val isSuggested = showSuggestIcon && suggestedItems.contains(practiceArea)
            holder.ivSuggest.visibility = if (isSuggested) View.VISIBLE else View.GONE
        }

        holder.checkBox.setOnClickListener {
            if (holder.checkBox.isChecked) {
                selectedPracticeAreas.add(practiceArea)
            } else {
                selectedPracticeAreas.remove(practiceArea)
            }
            listener?.onSelectionChanged(ArrayList(selectedPracticeAreas))
        }

        holder.itemView.setOnClickListener { holder.checkBox.performClick() }
    }

    fun unselectItem(itemName: String?) {
        if (itemName.isNullOrBlank()) return
        selectedPracticeAreas.remove(itemName)
        for (i in filteredPracticeAreas.indices) {
            if (filteredPracticeAreas[i] == itemName) {
                notifyItemChanged(i)
                break
            }
        }
        listener?.onSelectionChanged(ArrayList(selectedPracticeAreas))
    }

    fun getAllItems(): List<String> {
        return ArrayList(allPracticeAreas)
    }

    fun selectItem(itemName: String?) {
        if (itemName.isNullOrBlank()) return
        selectedPracticeAreas.add(itemName)
        for (i in filteredPracticeAreas.indices) {
            if (filteredPracticeAreas[i] == itemName) {
                notifyItemChanged(i)
                break
            }
        }
        listener?.onSelectionChanged(ArrayList(selectedPracticeAreas))
    }

    fun updateData(newData: List<String>?) {
        val data = newData ?: ArrayList()
        this.allPracticeAreas = ArrayList(data)
        this.filteredPracticeAreas = ArrayList(data)
        notifyDataSetChanged()
    }

    fun setSelectedItems(selectedItems: List<String>?) {
        val items = selectedItems ?: ArrayList()
        this.selectedPracticeAreas = HashSet(items)
        notifyDataSetChanged()
        listener?.onSelectionChanged(ArrayList(selectedPracticeAreas))
    }

    fun addSelectedItem(item: String?) {
        if (item.isNullOrEmpty()) return
        selectedPracticeAreas.add(item)
        notifyDataSetChanged()
        listener?.onSelectionChanged(ArrayList(selectedPracticeAreas))
    }

    fun removeSelectedItem(item: String?) {
        if (item.isNullOrEmpty()) return
        selectedPracticeAreas.remove(item)
        notifyDataSetChanged()
        listener?.onSelectionChanged(ArrayList(selectedPracticeAreas))
    }

    fun clearAllSelected() {
        selectedPracticeAreas.clear()
        notifyDataSetChanged()
        listener?.onSelectionChanged(ArrayList())
    }

    override fun getItemCount(): Int {
        return filteredPracticeAreas.size
    }

    fun filter(query: String?) {
        filteredPracticeAreas.clear()
        if (query.isNullOrBlank()) {
            filteredPracticeAreas.addAll(allPracticeAreas)
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            for (item in allPracticeAreas) {
                if (item.lowercase().contains(lowerCaseQuery)) {
                    filteredPracticeAreas.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<String> {
        return ArrayList(selectedPracticeAreas)
    }

    fun getSelectedCount(): Int {
        return selectedPracticeAreas.size
    }

    fun clearSelection() {
        selectedPracticeAreas.clear()
        notifyDataSetChanged()
        listener?.onSelectionChanged(ArrayList())
    }

    fun renameItem(oldName: String?, newName: String?) {
        if (oldName.isNullOrBlank() || newName.isNullOrBlank()) return

        val allIndex = allPracticeAreas.indexOf(oldName)
        if (allIndex != -1) {
            allPracticeAreas[allIndex] = newName
        }

        val filteredIndex = filteredPracticeAreas.indexOf(oldName)
        if (filteredIndex != -1) {
            filteredPracticeAreas[filteredIndex] = newName
            notifyItemChanged(filteredIndex)
        }

        if (selectedPracticeAreas.contains(oldName)) {
            selectedPracticeAreas.remove(oldName)
            selectedPracticeAreas.add(newName)
            listener?.onSelectionChanged(ArrayList(selectedPracticeAreas))
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPracticeName: TextView = itemView.findViewById(R.id.tv_tm_name)
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_selected)
        val ivSuggest: ImageView? = itemView.findViewById(R.id.iv_suggest)
    }
}
