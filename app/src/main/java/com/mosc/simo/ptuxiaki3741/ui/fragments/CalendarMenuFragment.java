package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.CalendarAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarShowFilter;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import kotlin.Unit;

public class CalendarMenuFragment extends Fragment implements FragmentBackPress {
    private static final int show_new_id = -3;
    private static final int show_old_id = -2;
    private static final int show_all_id = -1;

    private List<CalendarCategory> categories;

    private TreeMap<LocalDate, List<CalendarNotification>> notifications;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> beforeData;
    private LinkedHashMap<LocalDate, List<CalendarNotification>> afterData;

    private FragmentCalendarBinding binding;
    private YearMonth currentMonth;
    private DayOfWeek[] daysOfWeek;
    private CalendarAdapter adapter;
    private SharedPreferences sharedPref;
    private boolean listView;

    private CalendarShowFilter showFilter;
    private CalendarCategory selectedCategory;

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
        Handler handler = new Handler();
        handler.postDelayed(this::initViewModel,240);
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
        categories = new ArrayList<>();
        selectedCategory = null;
        showFilter = CalendarShowFilter.AFTER;
        beforeData = new LinkedHashMap<>();
        afterData = new LinkedHashMap<>();

        notifications = new TreeMap<>();
        currentMonth = YearMonth.now();
        daysOfWeek = daysOfWeekFromLocale();
        listView = true;
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
        binding.tvCalendarListLabel.setText(getResources().getString(R.string.loading_label));
        binding.ibClose1.setOnClickListener( view -> goBack());
        binding.ibClose2.setOnClickListener( view -> goBack());
        binding.ibGridView.setOnClickListener(v->{
            toggleDrawer(false);
            onListViewUpdate(false);
        });
        binding.ibListView.setOnClickListener(v->{
            toggleDrawer(false);
            onListViewUpdate(true);
        });
        binding.ibFilters.setOnClickListener(v-> toggleShowFilter());
        binding.fabNewEvent.setOnClickListener(v->toNewEvent());

        binding.navCalendarMenu.setNavigationItemSelectedListener(this::onSideMenuItemSelected);
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
                container.setOnClick(v->toEventList(day.getDate()));
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
                this::toEventList,
                this::toEvent
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
            viewModel.getCalendarCategories().observe(getViewLifecycleOwner(), this::onCategoriesUpdate);
            viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }

    private void onCategoriesUpdate(List<CalendarCategory> calendarCategories) {
        categories.clear();
        if(calendarCategories != null) categories.addAll(calendarCategories);
        updateSideMenu();
        updateCalendarList();
    }

    public boolean onSideMenuItemSelected(@NonNull MenuItem item) {
        toggleDrawer(false);
        switch (item.getItemId()){
            case (show_new_id):
                showNewEvents();
                return true;
            case (show_old_id):
                showOldEvents();
                return true;
            case (show_all_id):
                showSubCategory(null);
                return true;
            default:
                int id = item.getItemId();
                if( id > -1 && id < categories.size()) {
                    showSubCategory(categories.get(item.getItemId()));
                    return true;
                }
                return false;
        }
    }

    private void updateSideMenu(){
        if(binding == null) return;
        Menu sideMenu = binding.navCalendarMenu.getMenu();
        sideMenu.clear();

        SubMenu subMenu1 = sideMenu.addSubMenu(getString(R.string.date_filter_side_label));
        subMenu1.add(Menu.NONE,show_new_id,Menu.NONE,getString(R.string.new_events_side_label));
        subMenu1.add(Menu.NONE,show_old_id,Menu.NONE,getString(R.string.older_events_side_label));

        SubMenu subMenu2 = sideMenu.addSubMenu(getString(R.string.event_filter_side_label));
        subMenu2.add(Menu.NONE,show_all_id, Menu.NONE,getString(R.string.all_filter_side_label));
        for(int i = 0; i < categories.size(); i++){
            subMenu2.add(Menu.NONE, i, Menu.NONE, categories.get(i).getName());
        }
    }

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarNotification>> n) {
        notifications.clear();
        if(n != null) notifications.putAll(n);

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

    private void updateView() {
        if(listView){
            binding.clListView.setVisibility(View.VISIBLE);
            binding.clGridView.setVisibility(View.GONE);
        }else{
            binding.clListView.setVisibility(View.GONE);
            binding.clGridView.setVisibility(View.VISIBLE);
        }
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

    private void onListViewUpdate(boolean isList){
        listView = isList;
        if(sharedPref != null){
            SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
            sharedPrefEditor.putBoolean(AppValues.argListView,listView);
            sharedPrefEditor.apply();
        }
        updateView();
    }

    private void toggleShowFilter(){
        if(listView){
            toggleDrawer(true);
        }
    }

    private void showOldEvents(){
        if(showFilter == CalendarShowFilter.BEFORE) return;
        showFilter = CalendarShowFilter.BEFORE;
        updateCalendarList();
    }

    private void showNewEvents(){
        if(showFilter == CalendarShowFilter.AFTER) return;
        showFilter = CalendarShowFilter.AFTER;
        updateCalendarList();
    }

    private void showSubCategory(CalendarCategory category){
        if(selectedCategory == category) return;
        selectedCategory = category;
        updateCalendarList();
    }

    private void updateCalendarList() {
        LinkedHashMap<LocalDate, List<CalendarNotification>> displayList = new LinkedHashMap<>();
        if(showFilter == CalendarShowFilter.BEFORE){
            displayList.putAll(beforeData);
        }else{
            displayList.putAll(afterData);
        }

        LinkedHashMap<LocalDate, List<CalendarEntity>> listData = new LinkedHashMap<>();

        displayList.forEach((date, notifications)->{
            List<CalendarEntity> entities = new ArrayList<>();
            for(CalendarNotification notification : notifications){
                if(selectedCategory == null){
                    CalendarCategory temp = getCategory(notification.getCategoryID());
                    if(temp != null) entities.add(new CalendarEntity(temp, notification));
                }else if(selectedCategory.getId() == notification.getCategoryID()) {
                    entities.add(new CalendarEntity(selectedCategory, notification));
                }
            }
            if(entities.size() > 0) listData.put(date, entities);
        });


        if(listData.size() == 0){
            binding.tvCalendarListLabel.setText(getResources().getString(R.string.empty_list));
            binding.tvCalendarListLabel.setVisibility(View.VISIBLE);
        }else{
            binding.tvCalendarListLabel.setVisibility(View.GONE);
        }
        adapter.saveData(listData);
    }

    private CalendarCategory getCategory(long categoryID) {
        for(CalendarCategory category : categories){
            if(category.getId() == categoryID) return category;
        }
        return null;
    }

    private void goBack(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(()->getActivity().onBackPressed());
    }

    private void toNewEvent() {
        if(getActivity() != null)
            getActivity().runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent);
            });
    }

    private void toEvent(CalendarNotification event) {
        if(getActivity() != null)
            getActivity().runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argNotification, event);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent, bundle);
            });
    }

    private void toEventList(LocalDate localDate) {
        if(localDate == null) return;
        if(getActivity() != null)
            getActivity().runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppValues.argDate, localDate);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEventsList, bundle);
            });
    }
}