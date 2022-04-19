package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.YearPickerDialog;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppMainFragment extends Fragment{
    private FragmentMenuMainBinding binding;
    private AppViewModel vmLands;
    private long snapshot;

    private LoadingDialog loadingDialog;
    private YearPickerDialog yearPicker;
    private AlertDialog dialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModels();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //init
    private void initData(){
        snapshot = LocalDate.now().getYear();
    }

    private void initActivity() {
        if(getActivity() == null) return;
        yearPicker = new YearPickerDialog(getActivity(), this::onSnapshotUpdate);
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setOnBackPressed(()->true);
        loadingDialog = mainActivity.getLoadingDialog();
    }

    private void initFragment() {
        binding.btnLands.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnLiveMap.setOnClickListener(v -> toLiveMap(getActivity()));
        binding.btnCalendar.setOnClickListener(v -> toCalendar(getActivity()));
        binding.ibMenuButton.setOnClickListener(v-> toSettings(getActivity()));
        binding.tvSnapshot.setOnClickListener(v-> showCalendarDialog());
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
            vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            snapshot = vmLands.getDefaultSnapshot();
            binding.tvSnapshot.setText(String.valueOf(snapshot));
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

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarEntity>> notifications) {
        int todayEventsNumber = 0;
        if(notifications != null){
            List<CalendarEntity> todayEvents = notifications.getOrDefault(LocalDate.now(),null);
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
    private void onSnapshotUpdate(Long tempYear){
        if(vmLands == null || tempYear == null) return;
        closeDialog();
        if(loadingDialog != null) loadingDialog.openDialog();
        snapshot = tempYear;
        AsyncTask.execute(()->{
            vmLands.setDefaultSnapshot(snapshot);
            snapshot = vmLands.getDefaultSnapshot();
            if(getActivity() == null) return;
            Activity activity = getActivity();
            activity.runOnUiThread(()->{
                binding.tvSnapshot.setText(String.valueOf(snapshot));
                if(loadingDialog != null) loadingDialog.closeDialog();
            });
        });
    }

    private void showCalendarDialog(){
        if(binding == null) return;
        closeDialog();
        dialog = yearPicker.getDialog();
        yearPicker.openDialog(snapshot);
    }

    private void closeDialog(){
        if(dialog == null) return;
        if(dialog.isShowing()) dialog.dismiss();
        dialog = null;
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