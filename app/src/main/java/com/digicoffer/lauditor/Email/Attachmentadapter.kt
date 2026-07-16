package com.digicoffer.lauditor.Email

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R

class Attachmentadapter(
    private val mContext: Context,
    private val mAttachments: List<AttachmentModel>?
) : RecyclerView.Adapter<Attachmentadapter.ViewHolder>() {

    private var listener: OnAttachmentClickListener? = null

    fun interface OnAttachmentClickListener {
        fun onAttachmentClick(position: Int)
    }

    fun setOnAttachmentClickListener(listener: OnAttachmentClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.attachment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = mAttachments?.get(position) ?: return
        holder.attachmentFilename.text = attachment.filename

        val filename = attachment.filename?.lowercase() ?: ""
        if (filename.endsWith(".pdf") || filename.endsWith(".ics")) {
            holder.attachmentImage.setImageResource(R.drawable.pdf_icon2)
        } else if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            holder.attachmentImage.setImageResource(R.drawable.pdf_icon2)
        } else {
            holder.attachmentImage.setImageResource(R.drawable.pdf_icon2)
        }

        holder.itemView.setOnClickListener {
            listener?.onAttachmentClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return mAttachments?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val attachmentImage: ImageView = itemView.findViewById(R.id.attachmentImage)
        val attachmentFilename: TextView = itemView.findViewById(R.id.attachmentFilename)
    }
}
