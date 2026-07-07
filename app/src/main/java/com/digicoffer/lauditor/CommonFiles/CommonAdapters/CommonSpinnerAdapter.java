package com.digicoffer.lauditor.CommonFiles.CommonAdapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.digicoffer.lauditor.AuditTrails.Model.SpinnerItemModal;
import com.digicoffer.lauditor.Meetings.Models.CalendarDo;
import com.digicoffer.lauditor.Meetings.Models.MinutesDO;
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO;
import com.digicoffer.lauditor.Meetings.Models.TaskDo;
import com.digicoffer.lauditor.Meetings.Models.TeamDo;
import com.digicoffer.lauditor.Relationships.Model.CountriesDO;
import com.digicoffer.lauditor.Relationships.Model.EntityModel;
import com.digicoffer.lauditor.Documents.Models.ClientsModel;
import com.digicoffer.lauditor.Documents.Models.MattersModel;
import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel;
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel;
import com.digicoffer.lauditor.TimeSheets.Models.StatusModel;
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel;
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.TimeZonesDO;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.UsersDO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CommonSpinnerAdapter<T> extends BaseAdapter implements Filterable {

    Activity context;
    ArrayList<T> listArrayData;       // currently displayed (filtered) list
    ArrayList<T> originalList;        // full unfiltered list
    private int selectedPosition = -1;
    private Set<String> suggestedItems = new HashSet<>();

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public CommonSpinnerAdapter(Activity context, ArrayList<T> listArrayData) {
        this.context       = context;
        this.listArrayData = new ArrayList<>(listArrayData);
        this.originalList  = new ArrayList<>(listArrayData);
    }
    public void setSuggestedItems(Collection<String> items) {
        this.suggestedItems = new HashSet<>(items != null ? items : new ArrayList<>());
        notifyDataSetChanged();
    }
    // ─────────────────────────────────────────────────────────────────────────
    //  Extract display label — single place, used by both getView and Filter
    // ─────────────────────────────────────────────────────────────────────────
    private String getLabel(T item) {
        if (item instanceof String)           return item.toString();
        if (item instanceof UsersDO)          return ((UsersDO) item).getName();
        if (item instanceof FirmsDo)          return ((FirmsDo) item).getName();
        if (item instanceof TimeZonesDO)      return ((TimeZonesDO) item).getNAME();
        if (item instanceof ActionModel)      return ((ActionModel) item).getName();
        if (item instanceof ViewGroupModel)   return ((ViewGroupModel) item).getGroup_name();
        if (item instanceof CountriesDO)      return ((CountriesDO) item).getName();
        if (item instanceof EntityModel)      return ((EntityModel) item).getEntityID();
        if (item instanceof MattersModel)     return ((MattersModel) item).getTitle();
        if (item instanceof ClientsModel)     return ((ClientsModel) item).getName();
        if (item instanceof TSMatterModel)    return ((TSMatterModel) item).getMattername();
        if (item instanceof TasksModel)       return ((TasksModel) item).getDisplayValue();
        if (item instanceof StatusModel)      return ((StatusModel) item).getName();
        if (item instanceof ProjectsModel)    return ((ProjectsModel) item).getProjectName();
        if (item instanceof ProjectTMModel)   return ((ProjectTMModel) item).getName();
        if (item instanceof CalendarDo)       return ((CalendarDo) item).getProjectName();
        if (item instanceof ViewMatterModel)  return ((ViewMatterModel) item).getTitle();
        if (item instanceof TaskDo)           return ((TaskDo) item).getTaskName();
        if (item instanceof TeamDo)           return ((TeamDo) item).getName();
        if (item instanceof RelationshipsDO)  return ((RelationshipsDO) item).getName();
        if (item instanceof MinutesDO)        return ((MinutesDO) item).getName();
        if (item instanceof SpinnerItemModal) return ((SpinnerItemModal) item).getName();
        return "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BaseAdapter
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        if (view == null) {
            view = context.getLayoutInflater()
                    .inflate(R.layout.spinnerdropdownview, null, true);
        }

        TextView tv = view.findViewById(R.id.spinnerDropDownTextview);
        tv.setText(getLabel(listArrayData.get(pos)));

        if (pos == selectedPosition) {
            tv.setTextColor(context.getResources().getColor(android.R.color.white));
            view.setBackgroundColor(context.getResources().getColor(R.color.green_count_color));
        } else {
            tv.setTextColor(context.getResources().getColor(android.R.color.black));
            view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        View ivSuggest = view.findViewById(R.id.iv_suggest);
        if (ivSuggest != null) {
            ivSuggest.setVisibility(
                    suggestedItems.contains(getLabel(listArrayData.get(pos)))
                            ? View.VISIBLE : View.GONE
            );
        }
        return view;
    }

    @Override public int    getCount()            { return listArrayData.size(); }
    @Override public T      getItem(int i)         { return listArrayData.get(i); }
    @Override public long   getItemId(int pos)     { return pos; }

    // ─────────────────────────────────────────────────────────────────────────
    //  Filterable
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.toString().trim().isEmpty()) {
                    results.values = new ArrayList<>(originalList);
                    results.count  = originalList.size();
                } else {
                    String query = constraint.toString().toLowerCase().trim();
                    ArrayList<T> filtered = new ArrayList<>();
                    for (T item : originalList) {
                        if (getLabel(item).toLowerCase().contains(query)) {
                            filtered.add(item);
                        }
                    }
                    results.values = filtered;
                    results.count  = filtered.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listArrayData = (ArrayList<T>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    /** Call this when the backing data changes (e.g. after an API reload). */
    public void updateData(ArrayList<T> newList) {
        originalList   = new ArrayList<>(newList);
        listArrayData  = new ArrayList<>(newList);
        selectedPosition = -1;
        notifyDataSetChanged();
    }
}