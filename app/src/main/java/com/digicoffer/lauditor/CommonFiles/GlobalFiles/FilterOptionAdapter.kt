package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.digicoffer.lauditor.R
import java.util.ArrayList

class FilterOptionAdapter(
    var activity: Activity,
    var list: ArrayList<String>,
    var icons: ArrayList<Int>
) : BaseAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    internal class ViewHolder {
        var img_icon: ImageView? = null
        var tv_title: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.item_filter_option, parent, false)
            holder = ViewHolder()
            holder.img_icon = view.findViewById(R.id.img_icon)
            holder.tv_title = view.findViewById(R.id.tv_title)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        holder.tv_title?.text = list[position]
        holder.img_icon?.setImageResource(icons[position])

        return view!!
    }
}
