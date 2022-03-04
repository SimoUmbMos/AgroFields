package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppMainFragment extends Fragment{
    private FragmentMenuMainBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initFragment();
        initViewModels();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() == null) return;
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        binding.tvTitle.setText(
                sharedPref.getString(
                        AppValues.ownerName,
                        getResources().getString(R.string.app_name)
                )
        );
    }

    //init
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(()->true);
            }
        }
    }

    private void initFragment() {
        binding.btnLands.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnLiveMap.setOnClickListener(v -> toLiveMap(getActivity()));
        binding.btnCalendar.setOnClickListener(v -> toCalendar(getActivity()));
        binding.ibMenuButton.setOnClickListener(v-> toSettings(getActivity()));
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

    private void initViewModels() {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            binding.tvSnapshot.setText(vmLands.getDefaultSnapshot().toString());
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
            vmLands.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }

    //observers
    private void onLandUpdate(List<Land> lands) {
        int landsNumber = 0;
        if(lands != null){
            landsNumber = lands.size();
        }
        String descLands = "";
        if(landsNumber != 0){
            StringBuilder builder = new StringBuilder();
            builder.append(landsNumber).append(" ");
            if(landsNumber == 1){
                builder.append(getString(R.string.singular_land_label));
            }else{
                builder.append(getString(R.string.plural_land_label));
            }
            descLands = builder.toString();
        }
        binding.tvLandsNumber.setText(descLands);
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

    //navigation
    public void toListMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuLands);
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
}