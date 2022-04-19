package com.mosc.simo.ptuxiaki3741.ui.fragments.calendar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.NotificationsAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarEventListBinding;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarEventListFragment extends Fragment implements FragmentBackPress {
    private static final int show_all_id = -1;

    private FragmentCalendarEventListBinding binding;
    private NotificationsAdapter adapter;
    private CalendarCategory selectedCategory;
    private List<CalendarCategory> categories;
    private List<CalendarEntity> notifications;
    private LocalDate date;

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
        selectedCategory = null;
        categories = new ArrayList<>();
        notifications = new ArrayList<>();
        date = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argDate)){
                date = (LocalDate) getArguments().getSerializable(AppValues.argDate);
            }
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
        binding.fabNewEvent.setOnClickListener(v->toNewEvent());

        binding.navCalendarMenu.setNavigationItemSelectedListener(this::onSideMenuItemSelected);

        String title = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                " " +
                date.getDayOfMonth() +
                " " +
                date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                " " +
                date.getYear();
        binding.tvTitle.setText(title);

        binding.tvNotificationsDisplay.setText(getString(R.string.loading_list));
        adapter = new NotificationsAdapter(this::toEvent);
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
            viewModel.getCalendarCategories().observe(getViewLifecycleOwner(),this::onCategoriesUpdate);
            viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }

    private void onCategoriesUpdate(List<CalendarCategory> calendarCategories) {
        categories.clear();
        if(calendarCategories != null) categories.addAll(calendarCategories);
        if(selectedCategory != null && !categories.contains(selectedCategory)) selectedCategory = null;
        updateSideMenu();
        updateCalendarList();
    }

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarEntity>> notifications) {
        this.notifications.clear();
        if(notifications != null){
            List<CalendarEntity> temp = notifications.getOrDefault(date,null);
            if(temp != null) {
                this.notifications.addAll(temp);
            }
        }
        updateCalendarList();
    }

    private boolean onSideMenuItemSelected(MenuItem item) {
        toggleNavBar(false);
        int id = item.getItemId();
        if(id == show_all_id){
            showSubCategory(null);
            return true;
        }else if(id > -1 && id < categories.size()){
            showSubCategory(categories.get(item.getItemId()));
            return true;
        }
        return false;
    }

    private void updateSideMenu() {
        if(binding == null) return;
        Menu sideMenu = binding.navCalendarMenu.getMenu();
        sideMenu.clear();
        SubMenu subMenu = sideMenu.addSubMenu(getString(R.string.event_filter_side_label));
        subMenu.add(Menu.NONE, show_all_id, Menu.NONE, getString(R.string.all_filter_side_label));
        for(int i = 0; i < categories.size(); i++){
            subMenu.add(Menu.NONE, i, Menu.NONE, categories.get(i).getName());
        }
    }

    private void updateCalendarList() {
        List<CalendarEntity> entities = new ArrayList<>();
        if(selectedCategory != null){
            for(CalendarEntity notification : notifications){
                if(notification.getCategory().equals(selectedCategory)) entities.add(notification);
            }
        }else{
            entities.addAll(notifications);
        }
        if(entities.size() == 0){
            binding.tvNotificationsDisplay.setText(getString(R.string.empty_list));
            binding.tvNotificationsDisplay.setVisibility(View.VISIBLE);
        }else{
            binding.tvNotificationsDisplay.setVisibility(View.GONE);
        }
        adapter.saveData(entities);
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

    private void showSubCategory(CalendarCategory category){
        if(selectedCategory == category) return;
        selectedCategory = category;
        updateCalendarList();
    }

    private void goBack(){
        if(getActivity() != null) getActivity().onBackPressed();
    }

    private void toEvent(CalendarNotification notification) {
        if(notification == null) return;
        if(getActivity() != null)
            getActivity().runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarEventListFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argNotification, new CalendarNotification(notification));
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent, bundle);
            });
    }

    private void toNewEvent() {
        if(getActivity() != null)
            getActivity().runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarEventListFragment);
                Bundle bundle = new Bundle();
                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                bundle.putParcelable(
                        AppValues.argNotification,
                        new CalendarNotification(
                                0,
                                AppValues.defaultCalendarCategoryID,
                                -1,
                                null,
                                null,
                                "",
                                "",
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