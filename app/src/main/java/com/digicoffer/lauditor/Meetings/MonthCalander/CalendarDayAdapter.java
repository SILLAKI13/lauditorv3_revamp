package com.digicoffer.lauditor.Meetings.MonthCalander;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Meetings.Models.Events_Do;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {
    private OnDateSelectedListener dateSelectedListener;
    private final List<CalendarItem> days;
    private CalendarItem selectedDate = null;
    private final LocalDate today = LocalDate.now();
    private boolean isInitialSelectionDone = false;
    private ArrayList<Events_Do> events_list = new ArrayList<>();
    private ArrayList<AppointmentModel> appointments_list = new ArrayList<>();
    private Meetings.FilterType currentFilter = Meetings.FilterType.ALL;

    public CalendarDayAdapter(List<CalendarItem> days, ArrayList<Events_Do> events_list, ArrayList<AppointmentModel> appointments_list, Meetings.FilterType filterType) {
        this.days = days;
        this.events_list = events_list;
        this.appointments_list = appointments_list;
        this.currentFilter = filterType;

        // Find and set the item corresponding to today's date
        for (CalendarItem item : days) {
            if (!item.isHeader() && today.equals(item.date)) {
                selectedDate = item;
                break;
            }
        }
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_cell, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, dayText;
        TextView dotView;
        LinearLayout dayCard;
        LinearLayout ll_dotView;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            dayText = itemView.findViewById(R.id.dayText);
            dotView = itemView.findViewById(R.id.dotView);
            ll_dotView = itemView.findViewById(R.id.ll_dotView);
            dayCard = itemView.findViewById(R.id.dayCard);
        }

        void bind(CalendarItem item) {
            Context context = itemView.getContext();

            if (item.isHeader()) {
                dayText.setText(item.label);
                dayText.setVisibility(View.VISIBLE);
                ll_dotView.setVisibility(View.GONE);
                dayCard.setVisibility(View.VISIBLE);
                dayCard.setBackground(null);
                return;
            }

            LocalDate date = item.date;

            if (date == null) {
                dateText.setText("");
                dotView.setVisibility(View.GONE);
                dayCard.setVisibility(View.GONE);
                return;
            }

            dayText.setVisibility(View.GONE);
            dateText.setVisibility(View.VISIBLE);
            dateText.setText(String.valueOf(date.getDayOfMonth()));
            ll_dotView.setVisibility(View.VISIBLE);
            dayCard.setVisibility(View.VISIBLE);

            // Background and text color logic
            if (date.equals(today)) {
                dayCard.setBackground(context.getDrawable(R.drawable.rounder_button_blue));
                dateText.setTextColor(Color.WHITE);
            } else {
                if (item.equals(selectedDate)) {
                    dayCard.setBackground(context.getDrawable(R.drawable.save_bg));
                    dateText.setTextColor(Color.WHITE);
                } else {
                    dayCard.setBackgroundResource(R.drawable.rectangle_light_grey);
                    dateText.setTextColor(Color.BLACK);
                }
            }

            // Count events and appointments for this date
            int totalCount = 0;
            String dateString = date.toString(); // yyyy-MM-dd

// EVENTS
            if (currentFilter == Meetings.FilterType.ALL
                    || currentFilter == Meetings.FilterType.MY_MEETINGS) {

                for (Events_Do event : events_list) {
                    if (event.getConverted_date() != null
                            && event.getConverted_date().equals(dateString)) {
                        totalCount++;
                    }
                }
            }

// APPOINTMENTS
            if (currentFilter == Meetings.FilterType.ALL
                    || currentFilter == Meetings.FilterType.APPOINTMENTS) {

                for (AppointmentModel appointment : appointments_list) {
                    try {
                        Date appointmentDate = AndroidUtils.stringToDateTimeDefault(
                                appointment.getAppointment_from(),
                                "yyyy-MM-dd'T'HH:mm:ss"
                        );

                        String converted = AndroidUtils.getDateToString(
                                appointmentDate,
                                "yyyy-MM-dd"
                        );

                        if (dateString.equals(converted)) {
                            totalCount++;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }


            // Display dot with count
            if (totalCount > 0) {
                dotView.setVisibility(View.VISIBLE);
                dotView.setText(String.valueOf(totalCount));
            } else {
                dotView.setVisibility(View.GONE);
            }

            // Default selection trigger for today's date
            if (!isInitialSelectionDone && item.equals(selectedDate)) {
                isInitialSelectionDone = true;
                if (dateSelectedListener != null) {
                    filterAndNotify(date);
                }
            }

            itemView.setOnClickListener(v -> {
                selectedDate = item;
                notifyDataSetChanged();

                if (dateSelectedListener != null) {
                    filterAndNotify(date);
                }
            });
        }
    }


    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate selectedDate, ArrayList<Events_Do> filteredEvents, ArrayList<AppointmentModel> filteredAppointments);
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }

    public void updateFilter(Meetings.FilterType newFilter) {
        this.currentFilter = newFilter;
        notifyDataSetChanged();

        // Re-trigger filter for currently selected date
        if (selectedDate != null && selectedDate.date != null && dateSelectedListener != null) {
            filterAndNotify(selectedDate.date);
        }
    }

    public void updateBothLists(ArrayList<Events_Do> newEventsList, ArrayList<AppointmentModel> newAppointmentsList) {
        this.events_list = newEventsList;
        this.appointments_list = newAppointmentsList;
        notifyDataSetChanged();

        // Re-trigger filter for currently selected date
        if (selectedDate != null && selectedDate.date != null && dateSelectedListener != null) {
            filterAndNotify(selectedDate.date);
        }
    }

    // Method to update events list
    public void updateEventsList(ArrayList<Events_Do> newEventsList) {
        this.events_list = newEventsList;
        notifyDataSetChanged();
    }

    // Method to update appointments list
    public void updateAppointmentsList(ArrayList<AppointmentModel> newAppointmentsList) {
        this.appointments_list = newAppointmentsList;
        notifyDataSetChanged();
    }

    private void filterAndNotify(LocalDate date) {
        String dateString = date.toString();

        ArrayList<Events_Do> filteredEvents = new ArrayList<>();
        ArrayList<AppointmentModel> filteredAppointments = new ArrayList<>();

        if (currentFilter == Meetings.FilterType.ALL
                || currentFilter == Meetings.FilterType.MY_MEETINGS) {

            for (Events_Do event : events_list) {
                if (event.getConverted_date() != null
                        && event.getConverted_date().equals(dateString)) {
                    filteredEvents.add(event);
                }
            }
        }

        if (currentFilter == Meetings.FilterType.ALL
                || currentFilter == Meetings.FilterType.APPOINTMENTS) {

            for (AppointmentModel appointment : appointments_list) {
                try {
                    Date d = AndroidUtils.stringToDateTimeDefault(
                            appointment.getAppointment_from(),
                            "yyyy-MM-dd'T'HH:mm:ss"
                    );

                    String converted = AndroidUtils.getDateToString(d, "yyyy-MM-dd");

                    if (dateString.equals(converted)) {
                        filteredAppointments.add(appointment);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        dateSelectedListener.onDateSelected(
                date,
                filteredEvents,
                filteredAppointments
        );
    }
}