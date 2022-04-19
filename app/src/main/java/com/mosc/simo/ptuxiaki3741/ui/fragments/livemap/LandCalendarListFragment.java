package com.mosc.simo.ptuxiaki3741.ui.fragments.livemap;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.enums.CalendarShowFilter;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandCalendarListBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LandCalendarListFragment extends Fragment {
    //todo: complete fragment
    private FragmentLandCalendarListBinding binding;
    private String title;
    private long landId, zoneId;

    private final TreeMap<LocalDate, List<CalendarEntity>> notifications = new TreeMap<>();
    private final LinkedHashMap<LocalDate, List<CalendarEntity>> beforeData = new LinkedHashMap<>();
    private final LinkedHashMap<LocalDate, List<CalendarEntity>> afterData = new LinkedHashMap<>();
    private CalendarShowFilter showFilter;

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
        if(initData()){
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

    private void initActivity(){
        if (getActivity() == null) return;
        if (getActivity().getClass() != MainActivity.class) return;
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressed(()->true);
    }

    private boolean initData(){
        if(getArguments() != null){
            title = getArguments().getString(AppValues.argTitle, "");
            landId = getArguments().getLong(AppValues.argLandID,0);
            zoneId = getArguments().getLong(AppValues.argZoneID,0);
            return landId != 0;
        }
        return false;
    }

    private void initFragment(){
        showFilter = CalendarShowFilter.AFTER;
        if(title == null) title = "";
        binding.tvTitle.setText(title);
        binding.tvTitle.setSelected(true);
        binding.ibClose.setOnClickListener(v->goBack());
    }

    private void initViewModel(){
        if(getActivity() == null) return;
        AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
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

    private void updateCalendarList() {

    }

    private void goBack(){
        Activity activity = getActivity();
        if(activity != null)
            activity.runOnUiThread(activity::onBackPressed);
    }
}