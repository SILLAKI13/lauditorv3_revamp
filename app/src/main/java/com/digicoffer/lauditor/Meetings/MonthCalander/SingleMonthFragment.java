package com.digicoffer.lauditor.Meetings.MonthCalander;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Meetings.Models.Events_Do;
import com.digicoffer.lauditor.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class SingleMonthFragment extends Fragment {
    private static final String ARG_DATE = "month_date";
    private LocalDate month;
    private CalendarDayAdapter adapter;
    private CalendarDayAdapter.OnDateSelectedListener dateSelectedListener;
    private ArrayList<Events_Do> events_list = new ArrayList<>();
    private ArrayList<AppointmentModel> appointments_list = new ArrayList<>();
    private static Meetings.FilterType currentFilter = Meetings.FilterType.ALL;

    public static SingleMonthFragment newInstance(LocalDate date,
                                                  ArrayList<Events_Do> events_list,
                                                  ArrayList<AppointmentModel> appointments_list, Meetings.FilterType filterType,
                                                  CalendarDayAdapter.OnDateSelectedListener dateSelectedListener) {
        SingleMonthFragment fragment = new SingleMonthFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date.toString());
        args.putSerializable("currentFilter", filterType);
        args.putSerializable("events_list", events_list);
        args.putSerializable("appointments_list", appointments_list);
        fragment.setArguments(args);
        fragment.setDateSelectedListener(dateSelectedListener);
        return fragment;
    }

    public void setDateSelectedListener(CalendarDayAdapter.OnDateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            month = LocalDate.parse(getArguments().getString(ARG_DATE));
            currentFilter = (Meetings.FilterType)
                    getArguments().getSerializable("currentFilter");
            events_list = (ArrayList<Events_Do>) getArguments().getSerializable("events_list");
            appointments_list = (ArrayList<AppointmentModel>) getArguments().getSerializable("appointments_list");

            // Handle null cases
            if (events_list == null) {
                events_list = new ArrayList<>();
            }
            if (appointments_list == null) {
                appointments_list = new ArrayList<>();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_month, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        List<CalendarItem> days = generateCalendarItems(month);

        // Pass both events and appointments to the adapter
        CalendarDayAdapter adapter = new CalendarDayAdapter(days, events_list, appointments_list, currentFilter);
        adapter.setOnDateSelectedListener(dateSelectedListener);
        recyclerView.setAdapter(adapter);

        return view;
    }
    public void updateFilter(Meetings.FilterType newFilter) {
        currentFilter = newFilter;
        if (adapter != null) {
            adapter.updateFilter(newFilter);
        }
    }
    private List<CalendarItem> generateCalendarItems(LocalDate date) {
        List<CalendarItem> list = new ArrayList<>();

        // Add weekday headers
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            list.add(new CalendarItem(day));
        }

        // Add date placeholders
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int startDay = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0

        // Add empty cells before first day of month
        for (int i = 0; i < startDay; i++) {
            list.add(new CalendarItem((LocalDate) null));
        }

        // Add all days of the month
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            list.add(new CalendarItem(yearMonth.atDay(i)));
        }

        // Pad remaining cells to complete the grid (7 columns × 7 rows = 49 cells including headers)
        while (list.size() < 49) {
            list.add(new CalendarItem((LocalDate) null));
        }

        return list;
    }
}