package com.digicoffer.lauditor.Email

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.R
import java.util.ArrayList

class Griddocument(
    private val mContext: Context,
    attachmentsArray: ArrayList<Constants.IdNameModel>?,
    private val selectedDocumentName: String?
) : BaseAdapter() {

    init {
        composAttachDocAry = attachmentsArray
    }

    override fun getCount(): Int {
        return composAttachDocAry?.size ?: 0
    }

    override fun getItem(position: Int): Any? {
        return composAttachDocAry?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val gridView: View
        val holder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(mContext)
            gridView = inflater.inflate(R.layout.grid_item, parent, false)
            holder = ViewHolder(
                gridView.findViewById(R.id.attachmentFilename),
                gridView.findViewById(R.id.attachmentImage),
                gridView.findViewById(R.id.closeIcon),
                gridView.findViewById(R.id.card_view)
            )
            gridView.tag = holder
        } else {
            gridView = convertView
            holder = gridView.tag as ViewHolder
        }

        val name = composAttachDocAry?.get(position)?.name ?: ""
        holder.attachmentFilename.text = name

        // Set the appropriate image based on the file extension
        val lowercaseName = name.lowercase()
        if (lowercaseName.endsWith(".pdf") || lowercaseName.endsWith(".ics")) {
            holder.attachmentImage.setImageResource(R.drawable.pdf_icon2)
            holder.attachmentImage.visibility = View.VISIBLE
        } else if (lowercaseName.endsWith(".png") ||
            lowercaseName.endsWith(".jpg") ||
            lowercaseName.endsWith(".jpeg")
        ) {
            holder.attachmentImage.setImageResource(R.drawable.attachment_icons)
            holder.attachmentImage.visibility = View.VISIBLE
        }

        // Set onClickListener for close icon to remove the document
        holder.closeIcon.setOnClickListener {
            Constants.composAttachDocAry?.removeAt(position)
            notifyDataSetChanged()
        }

        return gridView
    }

    private class ViewHolder(
        val attachmentFilename: TextView,
        val attachmentImage: ImageView,
        val closeIcon: ImageView,
        val card_view: CardView
    )

    companion object {
        @JvmField
        var composAttachDocAry: ArrayList<Constants.IdNameModel>? = null
    }
}
