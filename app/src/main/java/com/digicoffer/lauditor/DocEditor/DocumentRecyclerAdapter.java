package com.digicoffer.lauditor.DocEditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import java.util.List;

public class DocumentRecyclerAdapter extends RecyclerView.Adapter<DocumentRecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<DocumentModel> documentList;
    private final DocEditor docEditor;

    public DocumentRecyclerAdapter(Context context, List<DocumentModel> documentList, DocEditor docEditor) {
        this.context = context;
        this.documentList = documentList;
        this.docEditor = docEditor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDoc;
        TextView tvDoc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDoc = itemView.findViewById(R.id.iv_docListItem);
            // If tv_docListItem is a <include>, use findViewById within included layout
            tvDoc = itemView.findViewById(R.id.tv_docListItem);
        }
    }

    public interface EventListeners {
        void AddView(String txt, String hint);
    }

    @NonNull
    @Override
    public DocumentRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doc_editor_item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentRecyclerAdapter.ViewHolder holder, int position) {
        DocumentModel model = documentList.get(position);
        holder.tvDoc.setText(model.getName());
        holder.ivDoc.setImageResource(model.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            String name = model.getName(); // label shown in list

            switch (name) {
                case "Overview":
                case "Section":
                case "Sub Section":
                case "Sub Sub Section":
                case "Paragraph":
                    docEditor.AddView(name, "");
                    break;

                case "Numbered List":
                case "Bulleted List":
                    docEditor.AddDocListView(name, null);
                    break;

                case "Page Break":
                    docEditor.AddPageBreak(name);
                    break;

                case "Image":
                    docEditor.AddImage(name, "", name);
                    break;

                case "Table":
                    docEditor.AddTable(name,null);
                    break;

                case "Save":
                    docEditor.SaveFile();
                    break;

                case "Open":
                    docEditor.loadOpenDocument();
                    break;

                case "New":
                    docEditor.loadNewDoc();
                    break;

                case "Save As":
                    docEditor.SaveAsFile();
                    break;

                case "Delete":
                    docEditor.DeleteFile();
                    break;

                default:
                    AndroidUtils.showError("Please select the document", docEditor.getActivity());
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }
}
