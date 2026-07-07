package com.digicoffer.lauditor.Meetings.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Meetings.Models.Day;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklyCalendarAdapter extends RecyclerView.Adapter<WeeklyCalendarAdapter.DayViewHolder> {

    private List<Day> days;
    private OnDaySelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    Context context;

    public WeeklyCalendarAdapter(List<Day> days, OnDaySelectedListener listener, Context context) {
        this.days = days;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_cell, parent, false);
        return new DayViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Day day = days.get(position);

        // Count both events and appointments
        int totalCount = 0;

        // Count events from EventDay list
        if (day.getEvents() != null && day.getEvents().size() > 0) {
            totalCount = day.getEvents().size();
        }

        // Show event/appointment count if available
        if (totalCount > 0) {
            holder.dot.setVisibility(View.VISIBLE);
            holder.dot.setText(String.valueOf(totalCount));
        } else {
            holder.dot.setVisibility(View.GONE);
            holder.dot.setText("");
        }

        // Parse and display the day
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        try {
            Date parsedDate = dateFormat.parse(day.getDate());
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
            String new_day = dayFormat.format(parsedDate);
            holder.textDay.setText(new_day);
        } catch (ParseException e) {
            Log.e("DateParsing", "Error parsing date: " + day.getDate(), e);
        }

        // Handle selection visuals
        holder.dayCard.setOnClickListener(view -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition);
            }
            Constants.isfwd_or_isbwd = false;
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onDaySelected(day);
            }
        });

        if (day.isToday()) {
            holder.dayCard.setBackground(context.getDrawable(R.drawable.rounder_button_blue));
            holder.textDay.setTextColor(Color.WHITE);
        } else {
            holder.dayCard.setBackground(context.getDrawable(R.drawable.rectangle_light_grey));
            holder.textDay.setTextColor(Color.BLACK);
            if (selectedPosition == position) {
                if (!Constants.isfwd_or_isbwd) {
                    holder.dayCard.setBackground(context.getDrawable(R.drawable.rectangular_button_green_count));
                    holder.textDay.setTextColor(Color.WHITE);
                }
            }
        }
    }



    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        public TextView textDay;
        public TextView dot;
        LinearLayout dayCard;

        public DayViewHolder(View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.dateText);
            dot = itemView.findViewById(R.id.dotView);
            dayCard = itemView.findViewById(R.id.dayCard);
        }
    }

    // Interface to handle day selection
    public interface OnDaySelectedListener {
        void onDaySelected(Day day);
    }
}