package com.digicoffer.lauditor.DocEditor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils

class DocumentRecyclerAdapter(
    private val context: Context,
    private val documentList: List<DocumentModel>,
    private val docEditor: DocEditor
) : RecyclerView.Adapter<DocumentRecyclerAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDoc: ImageView = itemView.findViewById(R.id.iv_docListItem)
        val tvDoc: TextView = itemView.findViewById(R.id.tv_docListItem)
    }

    interface EventListeners {
        fun AddView(txt: String, hint: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.doc_editor_item_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = documentList[position]
        holder.tvDoc.text = model.name
        holder.ivDoc.setImageResource(model.iconResId)

        holder.itemView.setOnClickListener {
            val name = model.name

            when (name) {
                "Overview", "Section", "Sub Section", "Sub Sub Section", "Paragraph" -> {
                    docEditor.AddView(name, "")
                }
                "Numbered List", "Bulleted List" -> {
                    docEditor.AddDocListView(name, null)
                }
                "Page Break" -> {
                    docEditor.AddPageBreak(name)
                }
                "Image" -> {
                    docEditor.AddImage(name, "", name)
                }
                "Table" -> {
                    docEditor.AddTable(name, null)
                }
                "Save" -> {
                    docEditor.SaveFile()
                }
                "Open" -> {
                    docEditor.loadOpenDocument()
                }
                "New" -> {
                    docEditor.loadNewDoc()
                }
                "Save As" -> {
                    docEditor.SaveAsFile()
                }
                "Delete" -> {
                    docEditor.DeleteFile()
                }
                else -> {
                    AndroidUtils.showError("Please select the document", docEditor.requireActivity())
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return documentList.size
    }
}
