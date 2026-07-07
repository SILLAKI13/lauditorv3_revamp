package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.digicoffer.lauditor.R;

import java.util.ArrayList;

public class FilterOptionAdapter extends BaseAdapter {

    Activity activity;
    ArrayList<String> list;     // Titles
    ArrayList<Integer> icons;   // Drawable IDs

    public FilterOptionAdapter(Activity activity, ArrayList<String> list, ArrayList<Integer> icons) {
        this.activity = activity;
        this.list = list;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView img_icon;
        TextView tv_title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_filter_option, parent, false);
            holder = new ViewHolder();
            holder.img_icon = convertView.findViewById(R.id.img_icon);
            holder.tv_title = convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_title.setText(list.get(position));
        holder.img_icon.setImageResource(icons.get(position));

        return convertView;
    }
}

