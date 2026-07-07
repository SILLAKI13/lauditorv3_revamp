package com.digicoffer.lauditor.TimeSheets.Adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel;
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.MyViewHolder> implements Filterable {

    private final Context context;
    private final ArrayList<ProjectsModel> originalProjectList;
    private final ArrayList<ProjectTMModel> originalProjectTmList;
    private ArrayList<ProjectsModel> filteredProjectList;
    private String filterQuery = "";
    private boolean isDestroyed = false;

    public ProjectAdapter(ArrayList<ProjectsModel> projectList, Context context, ArrayList<ProjectTMModel> projecttmList) {
        this.context = context;
        this.originalProjectList = new ArrayList<>(projectList);
        this.originalProjectTmList = new ArrayList<>(projecttmList);
        this.filteredProjectList = new ArrayList<>(projectList);
    }

    public void markAdapterDestroyed() {
        isDestroyed = true;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.projects_recyclerview, parent, false);
        return new MyViewHolder(view);
    }

    public void updateData(ArrayList<ProjectsModel> updatedProjectList, ArrayList<ProjectTMModel> updatedTmList) {
        synchronized (this) {
            originalProjectList.clear();
            originalProjectList.addAll(updatedProjectList);

            originalProjectTmList.clear();
            originalProjectTmList.addAll(updatedTmList);

            filteredProjectList.clear();
            filteredProjectList.addAll(updatedProjectList);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (position >= filteredProjectList.size()) return;
        ProjectsModel project = filteredProjectList.get(position);

        holder.tv_project_case_number.setText(project.getCaseNo());
        holder.tv_project_name.setText(project.getProjectName());

        // --- Set Clients ---
        JSONArray clientsArray = project.getClientNames();
        if (clientsArray != null && clientsArray.length() > 0) {
            StringBuilder clientsBuilder = new StringBuilder();
            for (int i = 0; i < clientsArray.length(); i++) {
                try {
                    clientsBuilder.append(clientsArray.getString(i));
                    if (i != clientsArray.length() - 1) clientsBuilder.append(", ");
                } catch (JSONException e) {
                    Log.e("ProjectAdapter", "Error parsing client name", e);
                }
            }
            holder.tv_client_name.setText(clientsBuilder.toString());
        } else {
            holder.tv_client_name.setText("");
        }

        // --- Setup Team Members RecyclerView ---
        holder.rv_tm_projects.setLayoutManager(new GridLayoutManager(context, 1));
        ArrayList<ProjectTMModel> teamList = new ArrayList<>();
        try {
            JSONArray teamMembersArray = project.getTeamMembers();
            if (teamMembersArray != null) {
                for (int i = 0; i < teamMembersArray.length(); i++) {
                    ProjectTMModel projectTMModel = new ProjectTMModel();
                    projectTMModel.setBillableHours(teamMembersArray.optJSONObject(i).optString("billableHours", "0"));
                    projectTMModel.setName(teamMembersArray.optJSONObject(i).optString("name", ""));
                    projectTMModel.setNonBillablehours(teamMembersArray.optJSONObject(i).optString("nonBillablehours", "0"));
                    projectTMModel.setTotal(teamMembersArray.optJSONObject(i).optString("total", "0"));
                    teamList.add(projectTMModel);
                }
            }
        } catch (Exception e) {
            Log.e("ProjectAdapter", "Failed to parse team members for project: " + project.getProjectName(), e);
        }

        holder.projectTMAdapter = new ProjectTMAdapter(context, teamList);
        holder.rv_tm_projects.setAdapter(holder.projectTMAdapter);
    }

    @Override
    public int getItemCount() {
        return filteredProjectList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = (constraint != null) ? constraint.toString().toLowerCase().trim() : "";
                ArrayList<ProjectsModel> filteredList = new ArrayList<>();

                synchronized (ProjectAdapter.this) {
                    if (query.isEmpty()) {
                        // ✅ Restore original list when search is empty
                        filteredList.addAll(originalProjectList);
                    } else {
                        for (ProjectsModel project : originalProjectList) {
                            if (project == null) continue;
                            boolean matches = false;

                            if (project.getProjectName() != null &&
                                    project.getProjectName().toLowerCase().contains(query)) {
                                matches = true;
                            } else if (jsonArrayContains(project.getClientNames(), query) ||
                                    jsonArrayContains(project.getTeamMembers(), query)) {
                                matches = true;
                            }

                            if (matches) filteredList.add(project);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (isDestroyed) return; // Adapter no longer active
                filteredProjectList = (ArrayList<ProjectsModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private boolean jsonArrayContains(JSONArray array, String query) {
        if (array == null) return false;
        for (int i = 0; i < array.length(); i++) {
            try {
                Object item = array.get(i);
                if (item instanceof String) {
                    if (((String) item).toLowerCase().contains(query)) return true;
                } else if (item instanceof JSONObject) {
                    JSONObject obj = (JSONObject) item;
                    if (obj.has("name") && obj.optString("name").toLowerCase().contains(query)) return true;
                    if (obj.has("billableHours") && obj.optString("billableHours").toLowerCase().contains(query)) return true;
                    if (obj.has("nonBillablehours") && obj.optString("nonBillablehours").toLowerCase().contains(query)) return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_project_case_number, tv_project_name, tv_client_label, tv_client_name;
        RecyclerView rv_tm_projects;
        ProjectTMAdapter projectTMAdapter;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_project_case_number = itemView.findViewById(R.id.tv_project_case_number);
            tv_project_name = itemView.findViewById(R.id.tv_project_name);
            rv_tm_projects = itemView.findViewById(R.id.rv_tm_projects);
            tv_client_name = itemView.findViewById(R.id.tv_client_name);
            tv_client_label = itemView.findViewById(R.id.tv_client_label);
            tv_client_label.setText(R.string.clients_);
        }
    }
}

