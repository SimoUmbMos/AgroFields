package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.NotificationsAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarEventListBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarEventType;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarSubFilter;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarEventListFragment extends Fragment implements FragmentBackPress {
    private FragmentCalendarEventListBinding binding;
    private NotificationsAdapter adapter;
    private List<CalendarNotification> notifications;
    private List<CalendarNotification> display;
    private CalendarSubFilter subFilter;
    private LocalDate date;
    private boolean firstStart;
    private String[] typesString;
    private Integer[] typesColor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarEventListBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(initData()){
            initActivity();
            initFragment();
            initViewModel();
        }else{
            goBack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean initData(){
        subFilter = CalendarSubFilter.ALL;
        firstStart = true;
        notifications = new ArrayList<>();
        display = new ArrayList<>();
        date = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argDate)){
                date = (LocalDate) getArguments().getSerializable(AppValues.argDate);
            }
        }

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

        return date != null;
    }

    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
            }
        }
    }

    private void initFragment(){
        binding.ibClose.setOnClickListener(v->goBack());
        binding.ibFilter.setOnClickListener(v->toggleNavBar(true));
        binding.fabNewEvent.setOnClickListener(v->toNewEvent(getActivity()));

        binding.navCalendarMenu.setNavigationItemSelectedListener(this::onSideMenuItemSelected);
        setupSideMenu();

        String title = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                " " +
                date.getDayOfMonth() +
                " " +
                date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                " " +
                date.getYear();
        binding.tvTitle.setText(title);

        binding.tvNotificationsDisplay.setText(getString(R.string.loading_list));
        adapter = new NotificationsAdapter(
                typesString,
                typesColor,
                this::onNotificationClick
        );
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvNotificationsList.setHasFixedSize(true);
        binding.rvNotificationsList.setLayoutManager(layoutManager);
        binding.rvNotificationsList.setAdapter(adapter);
    }

    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarNotification>> notifications) {
        this.notifications.clear();
        List<CalendarNotification> temp = notifications.getOrDefault(date,null);
        if(temp != null) {
            this.notifications.addAll(temp);
        }
        updateCalendarList();
    }

    private void onNotificationClick(CalendarNotification notification) {
        toEvent(getActivity(), notification);
    }

    private void onUpdateUi() {
        if(display.size() > 0){
            binding.tvNotificationsDisplay.setVisibility(View.GONE);
        }else{
            binding.tvNotificationsDisplay.setVisibility(View.VISIBLE);
        }
        if(firstStart){
            firstStart = false;
            binding.tvNotificationsDisplay.setText(getString(R.string.empty_list));
        }
    }

    private void toggleNavBar(boolean toggle) {
        if(binding != null){
            if(toggle){
                binding.getRoot().openDrawer(GravityCompat.END,true);
            }else{
                binding.getRoot().closeDrawer(GravityCompat.END,true);
            }
        }
    }

    private void goBack(){
        if(getActivity() != null) getActivity().onBackPressed();
    }

    private void setupSideMenu(){
        if(binding == null) return;

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

    public boolean onSideMenuItemSelected(@NonNull MenuItem item) {
        toggleNavBar(false);
        switch (item.getItemId()){
            case (R.id.menu_item_filter_all):
                showAllEvents();
                return true;
            case (R.id.menu_item_filter_schedule):
                showScheduleEvents();
                return true;
            case (R.id.menu_item_filter_plant):
                showPlantEvents();
                return true;
            case (R.id.menu_item_filter_cultivate):
                showCultivateEvents();
                return true;
            case (R.id.menu_item_filter_fertilize):
                showFertilizeEvents();
                return true;
            case (R.id.menu_item_filter_spray):
                showSprayEvents();
                return true;
            case (R.id.menu_item_filter_harvest):
                showHarvestEvents();
                return true;
            default:
                return false;
        }
    }

    private void updateCalendarList() {
        this.display.clear();
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
        if(type != null){
            List<CalendarNotification> filter = new ArrayList<>();
            for(CalendarNotification event : notifications){
                if(event.getType() == type) filter.add(event);
            }
            display.addAll(filter);
        }else{
            display.addAll(notifications);
        }
        adapter.saveData(display);
        onUpdateUi();
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

    private void toEvent(@Nullable Activity activity, CalendarNotification notification) {
        if(notification == null) return;
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarEventListFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argNotification, notification);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent, bundle);
            });
    }

    private void toNewEvent(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarEventListFragment);
                Bundle bundle = new Bundle();
                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                bundle.putParcelable(
                        AppValues.argNotification,
                        new CalendarNotification(
                                0,
                                -1,
                                null,
                                null,
                                "",
                                "",
                                CalendarEventType.SCHEDULE,
                                calendar.getTime()
                        )
                );
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent, bundle);
            });
    }

    @Override
    public boolean onBackPressed() {
        if(binding.getRoot().isDrawerOpen(GravityCompat.END)){
            toggleNavBar(false);
            return false;
        }
        return true;
    }
}