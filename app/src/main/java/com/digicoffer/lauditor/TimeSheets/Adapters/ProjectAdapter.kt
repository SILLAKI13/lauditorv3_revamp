package com.digicoffer.lauditor.TimeSheets.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class ProjectAdapter(
    projectList: ArrayList<ProjectsModel>,
    private val context: Context,
    projecttmList: ArrayList<ProjectTMModel>
) : RecyclerView.Adapter<ProjectAdapter.MyViewHolder>(), Filterable {

    private val originalProjectList: ArrayList<ProjectsModel> = ArrayList(projectList)
    private val originalProjectTmList: ArrayList<ProjectTMModel> = ArrayList(projecttmList)
    private var filteredProjectList: ArrayList<ProjectsModel> = ArrayList(projectList)
    private var isDestroyed = false

    fun markAdapterDestroyed() {
        isDestroyed = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.projects_recyclerview, parent, false)
        return MyViewHolder(view)
    }

    fun updateData(updatedProjectList: ArrayList<ProjectsModel>, updatedTmList: ArrayList<ProjectTMModel>) {
        synchronized(this) {
            originalProjectList.clear()
            originalProjectList.addAll(updatedProjectList)

            originalProjectTmList.clear()
            originalProjectTmList.addAll(updatedTmList)

            filteredProjectList.clear()
            filteredProjectList.addAll(updatedProjectList)
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position >= filteredProjectList.size) return
        val project = filteredProjectList[position]

        holder.tv_project_case_number.text = project.caseNo
        holder.tv_project_name.text = project.projectName

        // --- Set Clients ---
        val clientsArray = project.clientNames
        if (clientsArray != null && clientsArray.length() > 0) {
            val clientsBuilder = StringBuilder()
            for (i in 0 until clientsArray.length()) {
                try {
                    clientsBuilder.append(clientsArray.getString(i))
                    if (i != clientsArray.length() - 1) clientsBuilder.append(", ")
                } catch (e: JSONException) {
                    Log.e("ProjectAdapter", "Error parsing client name", e)
                }
            }
            holder.tv_client_name.text = clientsBuilder.toString()
        } else {
            holder.tv_client_name.text = ""
        }

        // --- Setup Team Members RecyclerView ---
        holder.rv_tm_projects.layoutManager = GridLayoutManager(context, 1)
        val teamList = ArrayList<ProjectTMModel>()
        try {
            val teamMembersArray = project.teamMembers
            if (teamMembersArray != null) {
                for (i in 0 until teamMembersArray.length()) {
                    val projectTMModel = ProjectTMModel().apply {
                        billableHours = teamMembersArray.optJSONObject(i)?.optString("billableHours", "0")
                        name = teamMembersArray.optJSONObject(i)?.optString("name", "")
                        nonBillablehours = teamMembersArray.optJSONObject(i)?.optString("nonBillablehours", "0")
                        total = teamMembersArray.optJSONObject(i)?.optString("total", "0")
                    }
                    teamList.add(projectTMModel)
                }
            }
        } catch (e: Exception) {
            Log.e("ProjectAdapter", "Failed to parse team members for project: " + project.projectName, e)
        }

        holder.projectTMAdapter = ProjectTMAdapter(context, teamList)
        holder.rv_tm_projects.adapter = holder.projectTMAdapter
    }

    override fun getItemCount(): Int {
        return filteredProjectList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = if (constraint != null) constraint.toString().lowercase().trim() else ""
                val filteredList = ArrayList<ProjectsModel>()

                synchronized(this@ProjectAdapter) {
                    if (query.isEmpty()) {
                        filteredList.addAll(originalProjectList)
                    } else {
                        for (project in originalProjectList) {
                            if (project == null) continue
                            var matches = false

                            if (project.projectName != null &&
                                project.projectName!!.lowercase().contains(query)
                            ) {
                                matches = true
                            } else if (jsonArrayContains(project.clientNames, query) ||
                                jsonArrayContains(project.teamMembers, query)
                            ) {
                                matches = true
                            }

                            if (matches) filteredList.add(project)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                if (isDestroyed) return
                filteredProjectList = results.values as ArrayList<ProjectsModel>
                notifyDataSetChanged()
            }
        }
    }

    private fun jsonArrayContains(array: JSONArray?, query: String): Boolean {
        if (array == null) return false
        for (i in 0 until array.length()) {
            try {
                val item = array.get(i)
                if (item is String) {
                    if (item.lowercase().contains(query)) return true
                } else if (item is JSONObject) {
                    if (item.has("name") && item.optString("name").lowercase().contains(query)) return true
                    if (item.has("billableHours") && item.optString("billableHours").lowercase().contains(query)) return true
                    if (item.has("nonBillablehours") && item.optString("nonBillablehours").lowercase().contains(query)) return true
                }
            } catch (ignored: Exception) {
            }
        }
        return false
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_project_case_number: TextView = itemView.findViewById(R.id.tv_project_case_number)
        var tv_project_name: TextView = itemView.findViewById(R.id.tv_project_name)
        var tv_client_label: TextView = itemView.findViewById(R.id.tv_client_label)
        var tv_client_name: TextView = itemView.findViewById(R.id.tv_client_name)
        var rv_tm_projects: RecyclerView = itemView.findViewById(R.id.rv_tm_projects)
        var projectTMAdapter: ProjectTMAdapter? = null

        init {
            tv_client_label.setText(R.string.clients_)
        }
    }
}
