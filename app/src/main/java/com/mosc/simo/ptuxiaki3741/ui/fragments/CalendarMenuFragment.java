package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.CalendarAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarEventType;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarShowFilter;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarSubFilter;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.ui.views.DayViewContainer;
import com.mosc.simo.ptuxiaki3741.ui.views.MonthHeaderContainer;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import kotlin.Unit;

public class CalendarMenuFragment extends Fragment implements FragmentBackPress {
    private FragmentCalendarBinding binding;
    private YearMonth currentMonth;
    private DayOfWeek[] daysOfWeek;
    private TreeMap<LocalDate, List<CalendarNotification>> notifications;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> beforeData;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> afterData;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> listData;
    private CalendarAdapter adapter;
    private CalendarShowFilter showFilter;
    private CalendarSubFilter subFilter;
    private SharedPreferences sharedPref;
    private boolean listView;
    private String[] typesString;
    private Integer[] typesColor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
    public boolean onBackPressed() {
        if(binding.getRoot().isDrawerOpen(GravityCompat.END)){
            toggleDrawer(false);
            return false;
        }
        return true;
    }

    private void initData(){
        showFilter = CalendarShowFilter.AFTER;
        subFilter = CalendarSubFilter.ALL;
        beforeData = new LinkedHashMap<>();
        afterData = new LinkedHashMap<>();
        listData = new LinkedHashMap<>();
        notifications = new TreeMap<>();
        currentMonth = YearMonth.now();
        daysOfWeek = daysOfWeekFromLocale();
        listView = true;

        typesString = getResources().getStringArray(R.array.notification_event_types);
        typesColor = new Integer[6];
        if(getContext() != null){
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getContext().getTheme();
            theme.resolveAttribute(R.attr.colorEventSchedule, typedValue, true);
            typesColor[0] = typedValue.data;
            theme.resolveAttribute(R.attr.colorEventPlant, typedValue, true);
            typesColor[1] = typedValue.data;
            theme.resolveAttribute(R.attr.colorEventCultivate, typedValue, true);
            typesColor[2] = typedValue.data;
            theme.resolveAttribute(R.attr.colorEventFertilize, typedValue, true);
            typesColor[3] = typedValue.data;
            theme.resolveAttribute(R.attr.colorEventSpray, typedValue, true);
            typesColor[4] = typedValue.data;
            theme.resolveAttribute(R.attr.colorEventHarvest, typedValue, true);
            typesColor[5] = typedValue.data;
        }else{
            Arrays.fill(typesColor, null);
        }
    }

    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
                listView = sharedPref.getBoolean(AppValues.argListView,true);
            }
        }
    }

    private void initFragment(){
        binding.ibClose1.setOnClickListener( view -> goBack());
        binding.ibClose2.setOnClickListener( view -> goBack());
        binding.ibGridView.setOnClickListener(v->{
            toggleDrawer(false);
            onListViewUpdate(false);
        });
        binding.ibListView.setOnClickListener(v->{
            toggleDrawer(false);
            onListViewUpdate(true);
            setupSideMenu();
        });
        binding.ibFilters.setOnClickListener(v-> toggleShowFilter());

        binding.navCalendarMenu.setNavigationItemSelectedListener(this::onSideMenuItemSelected);
        setupSideMenu();
        binding.fabNewEvent.setOnClickListener(v->toNewEvent(getActivity()));
        binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
                typesString,
                typesColor,
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

    private void setupSideMenu(){
        if(binding == null) return;
        MenuItem oldEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_old_event);
        MenuItem newEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_new_event);

        if(oldEventItem == null) return;
        if(newEventItem == null) return;


        if(showFilter == CalendarShowFilter.AFTER){
            oldEventItem.setEnabled(true);
            newEventItem.setEnabled(false);
        }else{
            oldEventItem.setEnabled(false);
            newEventItem.setEnabled(true);
        }

        MenuItem allEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_filter_all);
        MenuItem schEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_filter_schedule);
        MenuItem plnEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_filter_plant);
        MenuItem cltEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_filter_cultivate);
        MenuItem frtEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_filter_fertilize);
        MenuItem sprEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_filter_spray);
        MenuItem hrvEventItem = binding.navCalendarMenu.getMenu().findItem(R.id.menu_item_filter_harvest);

        if(allEventItem == null) return;
        if(schEventItem == null) return;
        if(plnEventItem == null) return;
        if(cltEventItem == null) return;
        if(frtEventItem == null) return;
        if(sprEventItem == null) return;
        if(hrvEventItem == null) return;


        allEventItem.setEnabled(true);
        schEventItem.setEnabled(true);
        plnEventItem.setEnabled(true);
        cltEventItem.setEnabled(true);
        frtEventItem.setEnabled(true);
        sprEventItem.setEnabled(true);
        hrvEventItem.setEnabled(true);

        switch (subFilter){
            case SCHEDULE:
                schEventItem.setEnabled(false);
                break;
            case PLANT:
                plnEventItem.setEnabled(false);
                break;
            case CULTIVATE:
                cltEventItem.setEnabled(false);
                break;
            case FERTILIZE:
                frtEventItem.setEnabled(false);
                break;
            case SPRAY:
                sprEventItem.setEnabled(false);
                break;
            case HARVEST:
                hrvEventItem.setEnabled(false);
                break;
            case ALL:
            default:
                allEventItem.setEnabled(false);
                break;
        }
    }

    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }

    public boolean onSideMenuItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case (R.id.menu_item_old_event):
                toggleDrawer(false);
                showOldEvents();
                return true;
            case (R.id.menu_item_new_event):
                toggleDrawer(false);
                showNewEvents();
                return true;
            case (R.id.menu_item_filter_all):
                toggleDrawer(false);
                showAllEvents();
                return true;
            case (R.id.menu_item_filter_schedule):
                toggleDrawer(false);
                showScheduleEvents();
                return true;
            case (R.id.menu_item_filter_plant):
                toggleDrawer(false);
                showPlantEvents();
                return true;
            case (R.id.menu_item_filter_cultivate):
                toggleDrawer(false);
                showCultivateEvents();
                return true;
            case (R.id.menu_item_filter_fertilize):
                toggleDrawer(false);
                showFertilizeEvents();
                return true;
            case (R.id.menu_item_filter_spray):
                toggleDrawer(false);
                showSprayEvents();
                return true;
            case (R.id.menu_item_filter_harvest):
                toggleDrawer(false);
                showHarvestEvents();
                return true;
            default:
                return false;
        }
    }

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarNotification>> n) {
        notifications.clear();
        notifications.putAll(n);

        TreeMap<LocalDate, List<CalendarNotification>> tempData = new TreeMap<>();
        beforeData.clear();
        for(LocalDate key : notifications.descendingKeySet()){
            if(key.isBefore(LocalDate.now())){
                beforeData.put(key,notifications.get(key));
            }else{
                tempData.put(key,notifications.get(key));
            }
        }

        afterData.clear();
        for(LocalDate key : tempData.keySet()){
            afterData.put(key,tempData.get(key));
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
            subFilter = CalendarSubFilter.ALL;
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

    private void updateCalendarList() {
        LinkedHashMap<LocalDate, List<CalendarNotification>> displayList = new LinkedHashMap<>();
        if(showFilter == CalendarShowFilter.BEFORE){
            displayList.putAll(beforeData);
        }else{
            displayList.putAll(afterData);
        }

        CalendarEventType type;
        switch (subFilter){
            case PLANT:
                type = CalendarEventType.PLANT;
                break;
            case SPRAY:
                type = CalendarEventType.SPRAY;
                break;
            case HARVEST:
                type = CalendarEventType.HARVEST;
                break;
            case SCHEDULE:
                type = CalendarEventType.SCHEDULE;
                break;
            case CULTIVATE:
                type = CalendarEventType.CULTIVATE;
                break;
            case FERTILIZE:
                type = CalendarEventType.FERTILIZE;
                break;
            case ALL:
            default:
                type = null;
                break;
        }

        listData.clear();
        if(type != null){
            LinkedHashMap<LocalDate, List<CalendarNotification>> filter = new LinkedHashMap<>();
            displayList.forEach((date, notifications)->{
                List<CalendarNotification> temp = new ArrayList<>();
                for(CalendarNotification notification : notifications){
                    if(notification.getType() == type) temp.add(notification);
                }
                if(temp.size() > 0){
                    filter.put(date, temp);
                }
            });
            listData.putAll(filter);
        }else{
            listData.putAll(displayList);
        }

        if(listData.size() == 0){
            binding.tvCalendarListLabel.setVisibility(View.VISIBLE);
        }else{
            binding.tvCalendarListLabel.setVisibility(View.GONE);
        }
        adapter.saveData(listData);
    }

    private void updateCalendarGrid() {
        binding.tvCalendarGridLabel.setVisibility(View.VISIBLE);
        binding.headerTextView.setText(getCurrentMonthYearTitle());
        binding.calendarView.setupAsync(
                currentMonth,
                currentMonth,
                daysOfWeek[0],
                ()->{
                    binding.tvCalendarGridLabel.setVisibility(View.GONE);
                    return Unit.INSTANCE;
                }
        );
    }

    private void updateView() {
        if(listView){
            binding.clListView.setVisibility(View.VISIBLE);
            binding.clGridView.setVisibility(View.GONE);
        }else{
            binding.clListView.setVisibility(View.GONE);
            binding.clGridView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleShowFilter(){
        if(listView){
            toggleDrawer(true);
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

    private void goBack(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(()->getActivity().onBackPressed());
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

    private void toggleDrawer(boolean toggle) {
        if(binding != null){
            if(toggle){
                binding.getRoot().openDrawer(GravityCompat.END,true);
            }else{
                binding.getRoot().closeDrawer(GravityCompat.END,true);
            }
        }
    }

    private void showOldEvents(){
        if(showFilter == CalendarShowFilter.BEFORE) return;
        showFilter = CalendarShowFilter.BEFORE;
        updateCalendarList();
        setupSideMenu();
    }

    private void showNewEvents(){
        if(showFilter == CalendarShowFilter.AFTER) return;
        showFilter = CalendarShowFilter.AFTER;
        updateCalendarList();
        setupSideMenu();
    }

    private void showAllEvents(){
        if(subFilter == CalendarSubFilter.ALL) return;
        subFilter = CalendarSubFilter.ALL;
        updateCalendarList();
        setupSideMenu();
    }

    private void showScheduleEvents(){
        if(subFilter == CalendarSubFilter.SCHEDULE) return;
        subFilter = CalendarSubFilter.SCHEDULE;
        updateCalendarList();
        setupSideMenu();
    }

    private void showPlantEvents(){
        if(subFilter == CalendarSubFilter.PLANT) return;
        subFilter = CalendarSubFilter.PLANT;
        updateCalendarList();
        setupSideMenu();
    }

    private void showCultivateEvents(){
        if(subFilter == CalendarSubFilter.CULTIVATE) return;
        subFilter = CalendarSubFilter.CULTIVATE;
        updateCalendarList();
        setupSideMenu();
    }

    private void showFertilizeEvents(){
        if(subFilter == CalendarSubFilter.FERTILIZE) return;
        subFilter = CalendarSubFilter.FERTILIZE;
        updateCalendarList();
        setupSideMenu();
    }

    private void showSprayEvents(){
        if(subFilter == CalendarSubFilter.SPRAY) return;
        subFilter = CalendarSubFilter.SPRAY;
        updateCalendarList();
        setupSideMenu();
    }

    private void showHarvestEvents(){
        if(subFilter == CalendarSubFilter.HARVEST) return;
        subFilter = CalendarSubFilter.HARVEST;
        updateCalendarList();
        setupSideMenu();
    }
}