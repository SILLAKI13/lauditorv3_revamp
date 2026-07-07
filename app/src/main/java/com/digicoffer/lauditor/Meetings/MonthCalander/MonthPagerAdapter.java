package com.digicoffer.lauditor.Meetings.MonthCalander;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Meetings.Models.Events_Do;

import java.time.LocalDate;
import java.util.ArrayList;

public class MonthPagerAdapter extends FragmentStateAdapter {
    public static final int START_POSITION = 120;
    private final LocalDate baseDate;
    private ArrayList<Events_Do> events_list = new ArrayList<>();
    private ArrayList<AppointmentModel> appointments_list = new ArrayList<>();
    private long dataVersion = 0;
    private Meetings.FilterType filterType = Meetings.FilterType.ALL;
    private final CalendarDayAdapter.OnDateSelectedListener dateSelectedListener;

    public MonthPagerAdapter(@NonNull Fragment fragment, LocalDate baseDate,
                             ArrayList<Events_Do> events_list,
                             ArrayList<AppointmentModel> appointments_list, Meetings.FilterType filterType,
                             CalendarDayAdapter.OnDateSelectedListener listener) {
        super(fragment);
        this.baseDate = baseDate;
        this.dateSelectedListener = listener;
        this.events_list = events_list;
        this.appointments_list = appointments_list;
        this.filterType = filterType;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        LocalDate targetMonth = baseDate.plusMonths(position - START_POSITION);
        return SingleMonthFragment.newInstance(targetMonth, events_list, appointments_list, filterType, dateSelectedListener);
    }

    @Override
    public int getItemCount() {
        return 240;
    }

    // === KEY PART ===
    @Override
    public long getItemId(int position) {
        // Combine position with dataVersion to force refresh when data changes
        return position + dataVersion * 1000L;
    }

    @Override
    public boolean containsItem(long itemId) {
        // Accept all item IDs in valid range for current dataVersion
        long position = itemId % 1000L;
        return position >= 0 && position < getItemCount();
    }
    public void updateFilter(Meetings.FilterType newFilter) {
        this.filterType = newFilter;
        dataVersion++; // Force fragment refresh with new filter
        notifyDataSetChanged();
    }
    public void updateEventsList(ArrayList<Events_Do> newList) {
        this.events_list = newList;
        dataVersion++; // force fragments to be considered "new"
        notifyDataSetChanged();
    }

    public void updateAppointmentsList(ArrayList<AppointmentModel> newList) {
        this.appointments_list = newList;
        dataVersion++; // force fragments to be considered "new"
        notifyDataSetChanged();
    }

    // Update both lists at once to avoid double refresh
    public void updateBothLists(ArrayList<Events_Do> newEventsList, ArrayList<AppointmentModel> newAppointmentsList) {
        this.events_list = newEventsList;
        this.appointments_list = newAppointmentsList;
        dataVersion++; // force fragments to be considered "new"
        notifyDataSetChanged();
    }
}