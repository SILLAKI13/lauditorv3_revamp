package com.digicoffer.lauditor.Meetings.ViewModels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO;
import com.digicoffer.lauditor.Meetings.MonthCalander.CalendarMonthView;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.FilterOptionAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class Meetings extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener, CalendarMonthView.EventDetailsListener, WeeklyCalendar.EventDetailsListener {
    LinearLayoutCompat ll_view_type;
    private NewModel mViewModel;
    LinearLayout cl_meeting,ll_event_type;
    ListView sp_event_Filter;
    boolean isEventTypeSelected = true;
    private FilterType currentFilter = FilterType.ALL;
    private String selectedEventType = "All";// Default to show all
    ImageView img_filter_icon;
    LinearLayout tv_switchCreate, tv_switchView, ll_event_Filter;
    TextView tv_event_Filter, tv_create_event, tv_view_event, tv_day_view, tv_month_view;
    ArrayList<Event_Details_DO> existingList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        ll_view_type = view.findViewById(R.id.ll_view_type);
        ll_event_type = view.findViewById(R.id.ll_event_type);
        cl_meeting = view.findViewById(R.id.cl_meeting);
        ll_event_Filter = view.findViewById(R.id.ll_event_Filter);
        tv_event_Filter = ll_event_Filter.findViewById(R.id.tv_spinner_view);
        img_filter_icon = ll_event_Filter.findViewById(R.id.img_filter_icon);
        sp_event_Filter = view.findViewById(R.id.sp_event_Filter);
        tv_create_event = view.findViewById(R.id.tv_create_event);
        tv_create_event.setText(R.string.create);
        tv_view_event = view.findViewById(R.id.tv_view_calendar);
        tv_view_event.setText(R.string.view);
        tv_switchCreate = view.findViewById(R.id.tv_switchCreate);
        tv_switchView = view.findViewById(R.id.tv_switchView);
//        tv_view_event.setBackground(getContext().getDrawable(R.drawable.button_right_green_background));
        tv_day_view = view.findViewById(R.id.tv_day_view);
        tv_day_view.setText(R.string.days);
        tv_month_view = view.findViewById(R.id.tv_month_view);
        tv_month_view.setText(R.string.month);
        tv_view_event.setOnClickListener(this);
        tv_create_event.setOnClickListener(this);
        String data = "Meetings";
        setViewModelData(data);
        if (Constants.isCreate) {
            Constants.is_meeting = "Create";
            loadCreateEvent(null);
        } else {
            loadView();
        }

        AndroidUtils.setupModuleView(
                tv_switchCreate,
                getString(R.string.view_event),
                false, true, getContext(), getString(R.string.create_event),
                clickedView -> {
                    // handle click
                    loadView();
                }
        );
        AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.create_event),
                true, false, getContext(), getString(R.string.view_event),
                clickedView -> {
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(getActivity());
                    } else {
                        Constants.is_meeting = "Create";
                        loadCreateEvent(null);
                    }
                }
        );
        ArrayList<String> selectionFilterList = new ArrayList<>();
        selectionFilterList.add("My Meetings");
        selectionFilterList.add("Client Bookings");
        selectionFilterList.add("All Appointments");

        ArrayList<Integer> filterIcons = new ArrayList<>();
        filterIcons.add(R.drawable.circle_blue_bg);       // My Meetings
        filterIcons.add(R.drawable.orange_circular);      // Client Bookings
        filterIcons.add(R.drawable.grey_circular);
        FilterOptionAdapter adapter = new FilterOptionAdapter(getActivity(), selectionFilterList, filterIcons);
        sp_event_Filter.setAdapter(adapter);
        selectedEventType = selectionFilterList.get(2);
        tv_event_Filter.setText(selectedEventType);
        img_filter_icon.setImageResource(filterIcons.get(2));
        currentFilter = FilterType.ALL;
        applyFilter();
        sp_event_Filter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEventType = selectionFilterList.get(position);
                tv_event_Filter.setText(selectedEventType);
                loadSelectedEvents(selectedEventType);
                sp_event_Filter.setVisibility(GONE);
                isEventTypeSelected = true;
            }
        });
        ll_event_Filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.display_listview(isEventTypeSelected, sp_event_Filter);
                isEventTypeSelected = !isEventTypeSelected;
            }
        });
        tv_view_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadView();
            }
        });
        tv_day_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_view_type.setVisibility(View.VISIBLE);
                loadDayView();
            }
        });
        tv_month_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_view_event.setTextColor(requireContext().getColor(R.color.white));
                ll_view_type.setVisibility(View.VISIBLE);
                loadMonthView();
            }
        });
        tv_create_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.is_meeting = "Create";
                loadCreateEvent(null);
            }
        });
        return view;
    }

    private void loadSelectedEvents(String selectedEventType) {
        if (selectedEventType.equals("My Meetings")) {
            currentFilter = FilterType.MY_MEETINGS;
            img_filter_icon.setImageDrawable(getContext().getDrawable(R.drawable.circle_blue_bg));
        } else if (selectedEventType.equals("Client Bookings")) {
            currentFilter = FilterType.APPOINTMENTS;
            img_filter_icon.setImageDrawable(getContext().getDrawable(R.drawable.orange_circular));
        } else {
            currentFilter = FilterType.ALL;
            img_filter_icon.setImageDrawable(getContext().getDrawable(R.drawable.grey_circular));
        }
        applyFilter();
    }

    private void setViewModelData(String data) {
        mViewModel.setData(data);
    }

    private void loadMonthView() {
        tv_day_view.setTextColor(requireContext().getColor(R.color.black));
        tv_month_view.setTextColor(requireContext().getColor(R.color.white));
        tv_month_view.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_day_view.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_left_background));
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        CalendarMonthView nonSubmittedTimesheets = new CalendarMonthView(cl_meeting, currentFilter);
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
//        String data = "Month View";
//        setViewModelData(data);
    }

    public enum FilterType {
        ALL,           // Show both events and appointments
        MY_MEETINGS,   // Show only events
        APPOINTMENTS   // Show only appointments
    }

    private void loadDayView() {
        tv_day_view.setTextColor(requireContext().getColor(R.color.white));
        tv_month_view.setTextColor(requireContext().getColor(R.color.black));
        tv_month_view.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_day_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        WeeklyCalendar nonSubmittedTimesheets = new WeeklyCalendar(cl_meeting, currentFilter);
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
//        String data = "Week View";
//        setViewModelData(data);
    }

    //    public ArrayList<Event_Details_DO> getExcisitingArryaList() {
//        return  existingList;
//    }
//    public void loadEditEvent(ArrayList<Event_Details_DO> event_details_list) {
//
//        existingList = event_details_list;
//        Log.d("EventList",existingList.toString());
//        Fragment childFragment = new EditEvent();
//        Bundle args = new Bundle();
//
//        FragmentManager childFragmentManager = getChildFragmentManager();
//        childFragmentManager.beginTransaction().add(R.id.child_container_timesheets, childFragment).commit();
//    }
    void loadView() {
        tv_switchView.setVisibility(VISIBLE);
        tv_switchCreate.setVisibility(GONE);
        ll_view_type.setVisibility(View.VISIBLE);
        ll_event_type.setVisibility(VISIBLE);
        tv_create_event.setTextColor(requireContext().getColor(R.color.black));
        tv_view_event.setTextColor(requireContext().getColor(R.color.white));
        tv_view_event.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_create_event.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        loadDayView();
    }

    void loadCreateEvent(ArrayList<Event_Details_DO> event_details_list) {
        tv_switchView.setVisibility(GONE);
        tv_switchCreate.setVisibility(VISIBLE);
        ll_event_type.setVisibility(GONE);
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(getActivity());
        } else {
            tv_view_event.setTextColor(requireContext().getColor(R.color.black));
            tv_create_event.setTextColor(requireContext().getColor(R.color.white));
            tv_view_event.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
            tv_create_event.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            CreateEvent nonSubmittedTimesheets = new CreateEvent(this, event_details_list);
            ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ll_view_type.setVisibility(View.GONE);
            ft.commit();
        }
//        String data = "Create Event";
//        setViewModelData(data);
    }

    private void applyFilter() {
        // Get the current fragment (either WeeklyCalendar or CalendarMonthView)
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.child_container_timesheets);

        if (currentFragment instanceof WeeklyCalendar) {
            ((WeeklyCalendar) currentFragment).applyFilter(currentFilter);
        } else if (currentFragment instanceof CalendarMonthView) {
            ((CalendarMonthView) currentFragment).applyFilter(currentFilter);
        }
    }

    public FilterType getCurrentFilter() {
        return currentFilter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_view_calendar://ID - tv_view_calender is named as tv_view_event.
                loadView();
                ll_view_type.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_create_event:
                ll_view_type.setVisibility(View.GONE);
                Constants.is_meeting = "Create";
                loadCreateEvent(null);
                break;
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {

    }

    @Override
    public void onEventDetailsPassed(ArrayList<Event_Details_DO> event_details_list, String calendar_Type) {
        Constants.is_meeting = "Edit";
        AndroidUtils.updateModuleTitle(
                tv_switchCreate,
                getString(R.string.edit_event)
        );
        loadCreateEvent(event_details_list);
    }

    public void loadViewEvent(String calendar_type) {
        Fragment fragment = new Fragment();
        if (Objects.equals(calendar_type, "Monthly")) {
            fragment = new MonthlyCalendar(cl_meeting);
        } else if (Objects.equals(calendar_type, "Weekly")) {
            fragment = new WeeklyCalendar(cl_meeting, currentFilter);
        } else {
            fragment = new WeeklyCalendar(cl_meeting, currentFilter);
        }
//        editEventFragment.setEventDetailsList(event_details_list);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.child_container_timesheets, fragment)
                .commit();
    }
}
