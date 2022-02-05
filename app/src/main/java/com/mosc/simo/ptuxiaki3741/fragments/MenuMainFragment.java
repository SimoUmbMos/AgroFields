package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuMainFragment extends Fragment implements FragmentBackPress {
    private FragmentMenuMainBinding binding;
    private int landsNumber;
    private int zonesNumber;

    //init
    private void initData(){
        landsNumber = 0;
        zonesNumber = 0;
    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                mainActivity.setToolbarTitle(getString(R.string.dashboard_title));
                ActionBar actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.show();
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setDisplayShowHomeEnabled(false);
                }
            }
        }
    }
    private void initViewModels() {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
            vmLands.getLandZones().observe(getViewLifecycleOwner(),this::onLandZoneUpdate);
            vmLands.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }
    private void initFragment() {
        binding.btnLands.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnZones.setOnClickListener(v -> toLandsZone(getActivity()));
        binding.btnLiveMap.setOnClickListener(v -> toLiveMap(getActivity()));
        binding.btnCalendar.setOnClickListener(v -> toCalendar(getActivity()));
        binding.btnTags.setOnClickListener(v-> toTags(getActivity()));
        LocalDate now = LocalDate.now();
        String day =  now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                " " +
                now.getDayOfMonth();
        String monthYear = new DateFormatSymbols().getShortMonths()[now.getMonth().getValue() - 1] +
                        " " +
                        now.getYear();
        binding.tvCalendarTodayDay.setText(day);
        binding.tvCalendarTodayMonthYear.setText(monthYear);
    }


    //observers
    private void onLandUpdate(List<Land> lands) {
        landsNumber = 0;
        if(lands != null){
            landsNumber = lands.size();
        }
        updateToolbar();
    }
    private void onLandZoneUpdate(Map<Long,List<LandZone>> zones) {
        zonesNumber = 0;
        if(zones != null){
            for(Long key : zones.keySet()){
                List<LandZone> temp = zones.getOrDefault(key,null);
                if(temp != null){
                    zonesNumber = zonesNumber + temp.size();
                }
            }
        }
        updateToolbar();
    }
    private void onNotificationsUpdate(Map<LocalDate, List<CalendarNotification>> notifications) {
        int todayEventsNumber = 0;
        if(notifications != null){
            List<CalendarNotification> todayEvents = notifications.getOrDefault(LocalDate.now(),null);
            if(todayEvents != null){
                todayEventsNumber = todayEvents.size();
            }
        }
        StringBuilder builder = new StringBuilder();
        if(todayEventsNumber > 999){
            builder.append("999+");
        }else{
            builder.append(todayEventsNumber);
        }
        builder.append(" ");
        if(todayEventsNumber == 1){
            builder.append(getString(R.string.singular_event_label));
        }else{
            builder.append(getString(R.string.plural_event_label));
        }
        binding.tvCalendarTodayEventCount.setText(builder.toString());
    }
    private void updateToolbar(){
        String desc = "";
        if(landsNumber != 0){
            StringBuilder builder = new StringBuilder();
            builder.append(landsNumber).append(" ");
            if(landsNumber == 1){
                builder.append(getString(R.string.singular_land_label));
            }else{
                builder.append(getString(R.string.plural_land_label));
            }
            if(zonesNumber != 0){
                builder.append("\n").append(zonesNumber).append(" ");
                if(zonesNumber == 1){
                    builder.append(getString(R.string.singular_zone_label));
                }else{
                    builder.append(getString(R.string.plural_zone_label));
                }
            }
            desc = builder.toString();
        }

        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                if(desc.isEmpty()){
                    mainActivity.setToolbarTitle(getString(R.string.dashboard_title));
                }else{
                    mainActivity.setToolbarTitle(
                            getString(R.string.dashboard_title),
                            desc
                    );
                }
            }
        }
    }

    //navigation
    public void toListMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuLands);
            });
    }
    public void toLandsZone(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuZoneLands);
            });
    }
    public void toSettings(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toAppSettings);
            });
    }
    public void toLiveMap(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toLiveMap);
            });
    }
    public void toCalendar(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toCalendar);
            });
    }
    public void toTags(@Nullable Activity activity){
        Snackbar.make(
                binding.getRoot(),
                "coming soon",
                Snackbar.LENGTH_LONG
        ).show();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModels();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_app_settings){
            toSettings(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }
}