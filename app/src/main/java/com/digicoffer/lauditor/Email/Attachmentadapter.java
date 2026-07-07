package com.digicoffer.lauditor.Email;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;

import java.util.List;

public class Attachmentadapter extends RecyclerView.Adapter<Attachmentadapter.ViewHolder> {
    private Context mContext;
    private List<AttachmentModel> mAttachments;
    private OnAttachmentClickListener listener;

    public interface OnAttachmentClickListener {
        void onAttachmentClick(int position);
    }

    public void setOnAttachmentClickListener(OnAttachmentClickListener listener) {
        this.listener = listener;
    }

    public Attachmentadapter(Context context, List<AttachmentModel> attachments) {
        this.mContext = context;
        this.mAttachments = attachments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.attachment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttachmentModel attachment = mAttachments.get(position);

        holder.attachmentFilename.setText(attachment.getFilename());

        String filename = attachment.getFilename().toLowerCase();
        if (filename.endsWith(".pdf") || filename.endsWith(".ics")) {
            holder.attachmentImage.setImageResource(R.drawable.pdf_icon2);
        } else if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            holder.attachmentImage.setImageResource(R.drawable.pdf_icon2);
        } else {
            holder.attachmentImage.setImageResource(R.drawable.pdf_icon2);
//            holder.attachmentImage.setVisibility(View.GONE); // Hide for unknown types
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAttachmentClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAttachments != null ? mAttachments.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView attachmentImage;
        TextView attachmentFilename;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            attachmentImage = itemView.findViewById(R.id.attachmentImage);
            attachmentFilename = itemView.findViewById(R.id.attachmentFilename);
        }
    }
}
