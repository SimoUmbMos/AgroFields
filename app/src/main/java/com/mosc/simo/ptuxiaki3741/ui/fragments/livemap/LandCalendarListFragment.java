package com.mosc.simo.ptuxiaki3741.ui.fragments.livemap;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarShowFilter;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandCalendarListBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.CalendarAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LandCalendarListFragment extends Fragment implements FragmentBackPress {
    private static final int group_main_id = -6;
    private static final int group_date_filter_id = -5;
    private static final int group_category_filter_id = -4;
    private static final int show_new_id = -3;
    private static final int show_old_id = -2;
    private static final int show_all_id = -1;

    private FragmentLandCalendarListBinding binding;
    private CalendarAdapter adapter;
    private String title;
    private long landId, zoneId;

    private final List<CalendarCategory> categories = new ArrayList<>();

    private final TreeMap<LocalDate, List<CalendarEntity>> notifications = new TreeMap<>();
    private final LinkedHashMap<LocalDate, List<CalendarEntity>> beforeData = new LinkedHashMap<>();
    private final LinkedHashMap<LocalDate, List<CalendarEntity>> afterData = new LinkedHashMap<>();

    private CalendarShowFilter showFilter;
    private CalendarCategory selectedCategory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLandCalendarListBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        if(initArgs()){
            initData();
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

    @Override
    public boolean onBackPressed() {
        if (binding != null){
            if(binding.getRoot().isDrawerOpen(GravityCompat.END)){
                toggleDrawer(false);
                return false;
            }
        }
        return true;
    }

    private void initActivity(){
        if (getActivity() == null) return;
        if (getActivity().getClass() != MainActivity.class) return;
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressed(this);
    }

    private boolean initArgs(){
        if(getArguments() != null){
            title = getArguments().getString(AppValues.argTitle, "");
            landId = getArguments().getLong(AppValues.argLandID,-1);
            zoneId = getArguments().getLong(AppValues.argZoneID,-1);
            return landId > 0;
        }
        return false;
    }

    private void initData(){
        showFilter = CalendarShowFilter.AFTER;
        selectedCategory = null;
        if(title == null) title = "";
    }

    private void initFragment(){
        binding.tvTitle.setText(title);
        binding.tvTitle.setSelected(true);
        binding.ibClose.setOnClickListener(v->goBack());
        binding.ibMenuButton.setOnClickListener(v->toggleDrawer(true));
        binding.tvNotificationListLabel.setText(R.string.loading_list);
        binding.navCalendarMenu.setNavigationItemSelectedListener(this::onSideMenuItemSelected);
        binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        adapter = new CalendarAdapter(
                this::onDateClick,
                this::onEntityClick
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
        if(getActivity() == null) return;
        AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        viewModel.getCalendarCategories().observe(getViewLifecycleOwner(), this::onCategoriesUpdate);
        viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
    }

    private void onCategoriesUpdate(List<CalendarCategory> calendarCategories) {
        categories.clear();
        if(calendarCategories != null) categories.addAll(calendarCategories);
        onSideMenuUpdate();
        updateCalendarList();
    }

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarEntity>> n) {
        notifications.clear();
        if(n != null) {
            for(LocalDate key : n.keySet()){
                List<CalendarEntity> entities = n.get(key);
                if(entities == null) continue;

                List<CalendarEntity> temp = new ArrayList<>();
                for(CalendarEntity entity : entities){
                    if(entity.getNotification() == null || entity.getNotification().getLid() == null) continue;
                    if(landId == entity.getNotification().getLid()){
                        if(zoneId > 0){
                            if(entity.getNotification().getZid() == null) continue;
                            if(zoneId == entity.getNotification().getZid()){
                                temp.add(entity);
                            }
                        }else{
                            temp.add(entity);
                        }
                    }
                }
                if(temp.size()>0){
                    notifications.put(key,temp);
                }
            }
        }

        TreeMap<LocalDate, List<CalendarEntity>> tempData = new TreeMap<>();
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
    }

    private void onSideMenuUpdate(){
        if(binding == null) return;
        Menu sideMenu = binding.navCalendarMenu.getMenu();
        sideMenu.clear();
        sideMenu.setGroupCheckable(group_main_id,true,false);

        SubMenu subMenu1 = sideMenu.addSubMenu(
                Menu.NONE,
                group_date_filter_id,
                Menu.NONE,
                getString(R.string.date_filter_side_label)
        );
        subMenu1.setGroupCheckable(group_date_filter_id,true,true);
        MenuItem newItem = subMenu1.add(Menu.NONE,show_new_id,Menu.NONE,getString(R.string.new_events_side_label));
        MenuItem oldItem = subMenu1.add(Menu.NONE,show_old_id,Menu.NONE,getString(R.string.older_events_side_label));
        newItem.setCheckable(true);
        oldItem.setCheckable(true);

        SubMenu subMenu2 = sideMenu.addSubMenu(
                Menu.NONE,
                group_category_filter_id,
                Menu.NONE,
                getString(R.string.event_filter_side_label)
        );
        subMenu2.setGroupCheckable(group_category_filter_id,true,true);
        MenuItem allItem = subMenu2.add(Menu.NONE,show_all_id, Menu.NONE,getString(R.string.all_filter_side_label));
        allItem.setCheckable(true);
        for(int i = 0; i < categories.size(); i++){
            if(categories.get(i) == null) continue;
            MenuItem tempItem = subMenu2.add(Menu.NONE, i, Menu.NONE, categories.get(i).getName());
            tempItem.setCheckable(true);
        }

        updateSideMenuCheck();
    }

    public boolean onSideMenuItemSelected(@NonNull MenuItem item) {
        toggleDrawer(false);
        switch (item.getItemId()){
            case (show_new_id):
                showNewEvents();
                break;
            case (show_old_id):
                showOldEvents();
                break;
            case (show_all_id):
                showSubCategory(null);
                break;
            default:
                int id = item.getItemId();
                if( id > -1 && id < categories.size()) {
                    showSubCategory(categories.get(item.getItemId()));
                }
                break;
        }
        return false;
    }

    public void onDateClick(LocalDate data){

    }

    private void onEntityClick(CalendarNotification notification) {

    }

    private void toggleDrawer(boolean toggle) {
        if(binding == null) return;
        if(toggle){
            binding.getRoot().openDrawer(GravityCompat.END,true);
        }else{
            binding.getRoot().closeDrawer(GravityCompat.END,true);
        }
    }

    private void updateCalendarList() {
        LinkedHashMap<LocalDate, List<CalendarEntity>> selectedFilter;
        if(showFilter == CalendarShowFilter.AFTER){
            selectedFilter = afterData;
        }else{
            selectedFilter = beforeData;
        }
        LinkedHashMap<LocalDate, List<CalendarEntity>> displayData = new LinkedHashMap<>();

        selectedFilter.forEach((date, notifications)->{
            List<CalendarEntity> entities = new ArrayList<>();
            if(selectedCategory != null){
                for(CalendarEntity notification : notifications){
                    if(notification.getCategory().equals(selectedCategory)) {
                        entities.add(notification);
                    }
                }
            }else{
                entities.addAll(notifications);
            }
            if(entities.size() > 0) {
                displayData.put(date, entities);
            }
        });

        if(displayData.size() == 0){
            binding.tvNotificationListLabel.setText(getResources().getString(R.string.empty_list));
            binding.tvNotificationListLabel.setVisibility(View.VISIBLE);
        }else{
            binding.tvNotificationListLabel.setVisibility(View.GONE);
        }
        adapter.saveData(displayData);
    }

    private void updateSideMenuCheck() {
        MenuItem subMenuItem = binding.navCalendarMenu.getMenu().findItem(group_category_filter_id);
        if(!subMenuItem.hasSubMenu()) return;

        SubMenu subMenu2 = subMenuItem.getSubMenu();
        if(selectedCategory == null){
            for(int i = 0; i < subMenu2.size(); i++){
                MenuItem item = subMenu2.getItem(i);
                if(item.getItemId() == show_all_id){
                    if(!item.isChecked()) item.setChecked(true);
                }else{
                    item.setChecked(false);
                }
            }
        }else{
            boolean check = false;
            int index = categories.indexOf(selectedCategory);
            for(int i = 0; i < subMenu2.size(); i++){
                MenuItem item = subMenu2.getItem(i);
                if(index == item.getItemId() && !check){
                    check = true;
                    if(!item.isChecked()) item.setChecked(true);
                }else{
                    item.setChecked(false);
                }
            }
            if(!check){
                selectedCategory = null;
                MenuItem allItem = subMenu2.findItem(show_all_id);
                if(!allItem.isChecked()) allItem.setChecked(true);
            }
        }

        MenuItem newItem = binding.navCalendarMenu.getMenu().findItem(show_new_id);
        MenuItem oldItem = binding.navCalendarMenu.getMenu().findItem(show_old_id);
        if(showFilter == CalendarShowFilter.AFTER){
            if(!newItem.isChecked()) newItem.setChecked(true);
            oldItem.setChecked(false);
        }else{
            newItem.setChecked(false);
            if(!oldItem.isChecked()) oldItem.setChecked(true);
        }
    }

    private void showOldEvents() {
        if(showFilter == CalendarShowFilter.BEFORE) return;
        showFilter = CalendarShowFilter.BEFORE;
        updateSideMenuCheck();
        updateCalendarList();
    }

    private void showNewEvents() {
        if(showFilter == CalendarShowFilter.AFTER) return;
        showFilter = CalendarShowFilter.AFTER;
        updateSideMenuCheck();
        updateCalendarList();
    }

    private void showSubCategory(CalendarCategory category) {
        if(selectedCategory == category) return;
        selectedCategory = category;
        updateSideMenuCheck();
        updateCalendarList();
    }

    private void goBack(){
        Activity activity = getActivity();
        if(activity != null)
            activity.runOnUiThread(activity::onBackPressed);
    }
}