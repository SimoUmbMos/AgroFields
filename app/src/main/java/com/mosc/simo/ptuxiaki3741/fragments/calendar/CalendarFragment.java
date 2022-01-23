package com.mosc.simo.ptuxiaki3741.fragments.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.CalendarAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarBinding;
import com.mosc.simo.ptuxiaki3741.enums.CalendarShowFilter;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.views.DayViewContainer;
import com.mosc.simo.ptuxiaki3741.views.MonthHeaderContainer;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import kotlin.Unit;

public class CalendarFragment extends Fragment implements FragmentBackPress {
    private FragmentCalendarBinding binding;
    private ActionBar actionBar;
    private YearMonth currentMonth;
    private DayOfWeek[] daysOfWeek;
    private TreeMap<LocalDate, List<CalendarNotification>> notifications;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> beforeData;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> afterData;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> listData;
    private CalendarAdapter adapter;
    private CalendarShowFilter showFilter;
    private SharedPreferences sharedPref;
    private boolean listView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentCalendarBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModel();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem grid = menu.findItem(R.id.menu_item_grid_view);
        MenuItem list = menu.findItem(R.id.menu_item_list_view);
        grid.setVisible(listView);
        list.setVisible(!listView);
        grid.setEnabled(listView);
        list.setEnabled(!listView);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case (R.id.menu_item_grid_view):
                onListViewUpdate(false);
                return true;
            case (R.id.menu_item_list_view):
                onListViewUpdate(true);
                return true;
            case (R.id.menu_item_filter):
                toggleShowFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initData(){
        showFilter = CalendarShowFilter.AFTER;
        beforeData = new LinkedHashMap<>();
        afterData = new LinkedHashMap<>();
        listData = new LinkedHashMap<>();
        notifications = new TreeMap<>();
        currentMonth = YearMonth.now();
        daysOfWeek = daysOfWeekFromLocale();
        listView = false;
    }
    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.setTitle(getString(R.string.calendar_fragment_grid_title));
                    actionBar.show();
                }
                sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
                listView = sharedPref.getBoolean(AppValues.argListView,false);
            }
        }
    }
    private void initFragment(){
        binding.fabNewEvent.setOnClickListener(v->toNewEvent(getActivity()));

        binding.headerPreviousMonth.setOnClickListener(v->{
            currentMonth = currentMonth.minusMonths(1);
            updateCalendarGrid();
        });
        binding.headerNextMonth.setOnClickListener(v->{
            currentMonth = currentMonth.plusMonths(1);
            updateCalendarGrid();
        });
        binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }
            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.setText(String.valueOf(day.getDate().getDayOfMonth()));
                container.setOnClick(v->onDayClick(day.getDate()));
                LocalDate localDate2 = LocalDate.now();
                container.setToday(localDate2.equals(day.getDate()));
                if(day.getOwner() == DayOwner.THIS_MONTH){
                    container.setEnable(true);
                    List<CalendarNotification> temp =
                            notifications.getOrDefault(day.getDate(),null);
                    if(temp != null){
                        container.setBadgeCount(temp.size());
                    }else{
                        container.setBadgeCount(0);
                    }
                }else{
                    container.setEnable(false);
                }
            }
        });
        binding.calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderContainer>() {
            @NonNull
            @Override
            public MonthHeaderContainer create(@NonNull View view) {
                return new MonthHeaderContainer(view);
            }

            @Override
            public void bind(@NonNull MonthHeaderContainer container, @NonNull CalendarMonth month) {
                container.setDaysHeader(daysOfWeek);
            }
        });

        adapter = new CalendarAdapter(
                listData,
                this::onDayClick,
                this::onEventClick
        );
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rcCalendarList.setHasFixedSize(true);
        binding.rcCalendarList.setLayoutManager(layoutManager);
        binding.rcCalendarList.setAdapter(adapter);
        updateView();
    }
    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarNotification>> n) {
        notifications.clear();
        notifications.putAll(n);

        beforeData.clear();
        for(LocalDate key : notifications.descendingKeySet()){
            if(key.isBefore(LocalDate.now())){
                beforeData.put(key,notifications.get(key));
            }
        }

        afterData.clear();
        for(LocalDate key : notifications.keySet()){
            if(!key.isBefore(LocalDate.now())){
                afterData.put(key,notifications.get(key));
            }
        }

        updateCalendarList();
        updateCalendarGrid();
    }
    private void onListViewUpdate(boolean isList){
        listView = isList;
        if(sharedPref != null){
            SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
            sharedPrefEditor.putBoolean(AppValues.argListView,listView);
            sharedPrefEditor.apply();
        }
        if(getActivity() != null){
            getActivity().invalidateOptionsMenu();
        }
        if(listView){
            showFilter = CalendarShowFilter.AFTER;
            updateCalendarList();
        }
        updateView();
    }
    private void onDayClick(LocalDate date){
        toEventList(getActivity(), date);
    }
    private void onEventClick(CalendarNotification event){
        toEvent(getActivity(), event);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateCalendarList() {
        listData.clear();
        if(showFilter == CalendarShowFilter.BEFORE){
            listData.putAll(beforeData);
        }else{
            listData.putAll(afterData);
        }
        adapter.notifyDataSetChanged();
        if(listData.size() == 0){
            binding.rcCalendarList.setVisibility(View.GONE);
            binding.tvCalendarListLabel.setVisibility(View.VISIBLE);
        }else{
            binding.rcCalendarList.setVisibility(View.VISIBLE);
            binding.tvCalendarListLabel.setVisibility(View.GONE);
        }
    }
    private void updateCalendarGrid() {
        binding.loadingView.setVisibility(View.VISIBLE);
        binding.headerTextView.setText(getCurrentMonthYearTitle());
        binding.calendarView.setupAsync(
                currentMonth,
                currentMonth,
                daysOfWeek[0],
                ()->{
                    binding.loadingView.setVisibility(View.GONE);
                    return Unit.INSTANCE;
                }
        );
    }
    private void updateView() {
        if(listView){
            binding.clListView.setVisibility(View.VISIBLE);
            binding.clGridView.setVisibility(View.GONE);
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.calendar_fragment_list_title));
            }
        }else{
            binding.clListView.setVisibility(View.GONE);
            binding.clGridView.setVisibility(View.VISIBLE);
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.calendar_fragment_grid_title));
            }
        }
    }
    private void toggleShowFilter(){
        if(listView){
            if(showFilter == CalendarShowFilter.AFTER){
                showFilter = CalendarShowFilter.BEFORE;
            }else{
                showFilter = CalendarShowFilter.AFTER;
            }
            updateCalendarList();
        }
    }

    private DayOfWeek[] daysOfWeekFromLocale() {
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            DayOfWeek[] days = new DayOfWeek[7];
            int index = 0,size = 0;
            for(int i = 0; i< daysOfWeek.length;i++){
                if(firstDayOfWeek == daysOfWeek[i]){
                    index = i;
                    break;
                }
            }
            for(int i = index; i < daysOfWeek.length;i++){
                days[size++] = daysOfWeek[i];
            }
            for(int i = 0; i < index; i++){
                days[size++] = daysOfWeek[i];
            }
            System.arraycopy(days, 0, daysOfWeek, 0, days.length);
        }
        return daysOfWeek;
    }
    private String getCurrentMonthYearTitle() {
        String month = new DateFormatSymbols().getShortMonths()[currentMonth.getMonth().getValue()-1];
        return String.format(Locale.getDefault(), "%s %d", month, currentMonth.getYear());
    }

    private void toNewEvent(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent);
            });
    }
    private void toEvent(@Nullable Activity activity, CalendarNotification event) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argNotification, event);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent, bundle);
            });
    }
    private void toEventList(@Nullable Activity activity, LocalDate localDate) {
        if(localDate == null) return;
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppValues.argDate, localDate);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEventsList, bundle);
            });
    }
}