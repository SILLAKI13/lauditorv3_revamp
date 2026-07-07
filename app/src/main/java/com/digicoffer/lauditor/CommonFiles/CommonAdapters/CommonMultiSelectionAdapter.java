package com.digicoffer.lauditor.CommonFiles.CommonAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonMultiSelectionAdapter extends RecyclerView.Adapter<CommonMultiSelectionAdapter.ViewHolder> {

    private Context context;
    private List<String> allPracticeAreas;
    private List<String> filteredPracticeAreas;
    private Set<String> selectedPracticeAreas;
    private Set<String> suggestedItems = new HashSet<>();  // ← ADD THIS
    private boolean showSuggestIcon = false;               // ← ADD THIS
    private OnSelectionChangeListener listener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(List<String> selectedItems);
    }

    public CommonMultiSelectionAdapter(Context context, List<String> practiceAreas,
                                       List<String> preSelectedItems,
                                       OnSelectionChangeListener listener) {
        this.context = context;
        this.allPracticeAreas = new ArrayList<>(practiceAreas);
        this.filteredPracticeAreas = new ArrayList<>(practiceAreas);
        this.selectedPracticeAreas = new HashSet<>(preSelectedItems != null ? preSelectedItems : new ArrayList<>());
        this.listener = listener;
    }
    public void setSuggestedItems(List<String> suggested, boolean showIcon) {
        this.suggestedItems = new HashSet<>(suggested != null ? suggested : new ArrayList<>());
        this.showSuggestIcon = showIcon;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.select_multiple_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String practiceArea = filteredPracticeAreas.get(position);

        holder.tvPracticeName.setText(practiceArea);
        boolean isOtherOption = practiceArea.startsWith("+ Other");

        if (isOtherOption) {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            boolean isSelected = selectedPracticeAreas.contains(practiceArea);
            holder.checkBox.setChecked(isSelected);
            holder.checkBox.setEnabled(true);
            holder.checkBox.setAlpha(1f);
        }

        // ── iv_suggest: only visible for services + only for suggested items ──
        if (holder.ivSuggest != null) {
            boolean isSuggested = showSuggestIcon && suggestedItems.contains(practiceArea);
            holder.ivSuggest.setVisibility(isSuggested ? View.VISIBLE : View.GONE);
        }

        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                selectedPracticeAreas.add(practiceArea);
                if (listener != null) listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
            } else {
                selectedPracticeAreas.remove(practiceArea);
                if (listener != null) listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
            }
        });

        holder.itemView.setOnClickListener(v -> holder.checkBox.performClick());
    }

    public void unselectItem(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) return;

        selectedPracticeAreas.remove(itemName);

        for (int i = 0; i < filteredPracticeAreas.size(); i++) {
            if (filteredPracticeAreas.get(i).equals(itemName)) {
                notifyItemChanged(i);
                break;
            }
        }

        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
        }
    }

    public List<String> getAllItems() {
        return new ArrayList<>(allPracticeAreas); // return a copy of the backing list
    }

    public void selectItem(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) return;

        selectedPracticeAreas.add(itemName);

        for (int i = 0; i < filteredPracticeAreas.size(); i++) {
            if (filteredPracticeAreas.get(i).equals(itemName)) {
                notifyItemChanged(i);
                break;
            }
        }

        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
        }
    }

    // NEW METHOD: Update the entire data set
    public void updateData(List<String> newData) {
        if (newData == null) {
            newData = new ArrayList<>();
        }
        this.allPracticeAreas = new ArrayList<>(newData);
        this.filteredPracticeAreas = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    // NEW METHOD: Set which items are selected
    public void setSelectedItems(List<String> selectedItems) {
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }
        this.selectedPracticeAreas = new HashSet<>(selectedItems);
        notifyDataSetChanged();

        // Notify listener about the selection change
        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
        }
    }

    // NEW METHOD: Add a single selected item
    public void addSelectedItem(String item) {
        if (item == null || item.isEmpty()) return;
        selectedPracticeAreas.add(item);
        notifyDataSetChanged();

        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
        }
    }

    // NEW METHOD: Remove a single selected item
    public void removeSelectedItem(String item) {
        if (item == null || item.isEmpty()) return;
        selectedPracticeAreas.remove(item);
        notifyDataSetChanged();

        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
        }
    }

    // NEW METHOD: Clear all selected items
    public void clearAllSelected() {
        selectedPracticeAreas.clear();
        notifyDataSetChanged();

        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>());
        }
    }

    @Override
    public int getItemCount() {
        return filteredPracticeAreas.size();
    }

    public void filter(String query) {
        filteredPracticeAreas.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredPracticeAreas.addAll(allPracticeAreas);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (String item : allPracticeAreas) {
                if (item.toLowerCase().contains(lowerCaseQuery)) {
                    filteredPracticeAreas.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    public List<String> getSelectedItems() {
        return new ArrayList<>(selectedPracticeAreas);
    }

    public int getSelectedCount() {
        return selectedPracticeAreas.size();
    }

    public void clearSelection() {
        selectedPracticeAreas.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(new ArrayList<>());
        }
    }

    public void renameItem(String oldName, String newName) {
        if (oldName == null || newName == null) return;
        if (oldName.trim().isEmpty() || newName.trim().isEmpty()) return;

        // Update in allPracticeAreas
        int allIndex = allPracticeAreas.indexOf(oldName);
        if (allIndex != -1) {
            allPracticeAreas.set(allIndex, newName);
        }

        // Update in filteredPracticeAreas
        int filteredIndex = filteredPracticeAreas.indexOf(oldName);
        if (filteredIndex != -1) {
            filteredPracticeAreas.set(filteredIndex, newName);
            notifyItemChanged(filteredIndex);
        }

        // Update in selectedPracticeAreas if it was selected
        if (selectedPracticeAreas.contains(oldName)) {
            selectedPracticeAreas.remove(oldName);
            selectedPracticeAreas.add(newName);
            if (listener != null) {
                listener.onSelectionChanged(new ArrayList<>(selectedPracticeAreas));
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPracticeName;
        CheckBox checkBox;
        ImageView ivSuggest;   // ← ADD THIS

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPracticeName = itemView.findViewById(R.id.tv_tm_name);
            checkBox = itemView.findViewById(R.id.chk_selected);
            ivSuggest = itemView.findViewById(R.id.iv_suggest);  // ← ADD THIS
        }
    }
}