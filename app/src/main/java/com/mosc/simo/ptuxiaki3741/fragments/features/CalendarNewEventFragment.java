package com.mosc.simo.ptuxiaki3741.fragments.features;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.LandSpinnerAdapter;
import com.mosc.simo.ptuxiaki3741.adapters.ZoneSpinnerAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarNewEventBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarNewEventFragment extends Fragment implements FragmentBackPress {
    private static final String TAG = "CalendarFragment";
    private FragmentCalendarNewEventBinding binding;
    private Calendar myCalendar;
    private List<Land> lands;
    private List<LandZone> displayZones;
    private Map<Long,List<LandZone>> zones;
    private LandSpinnerAdapter landSpinnerAdapter;
    private ZoneSpinnerAdapter zoneSpinnerAdapter;
    private Land selectedLand;
    private LandZone selectedZone;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarNewEventBinding.inflate(inflater,container,false);
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
        binding=null;
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initData(){
        lands = new ArrayList<>();
        zones = new HashMap<>();
        displayZones = new ArrayList<>();
        selectedLand = new Land(null);
        selectedZone = new LandZone(null);
        landSpinnerAdapter = new LandSpinnerAdapter(getContext(),lands);
        zoneSpinnerAdapter = new ZoneSpinnerAdapter(getContext(),displayZones);
        myCalendar = Calendar.getInstance();
    }
    private void initActivity(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.calendar_fragment_title));
                actionBar.show();
            }
        }
    }
    private void initFragment(){
        binding.svSelectedLand.setAdapter(landSpinnerAdapter);
        binding.svSelectedZone.setAdapter(zoneSpinnerAdapter);

        binding.etSelectedDate.setOnClickListener(v->onOpenDatePicker());
        binding.svSelectedLand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onLandSpinnerSelect(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.svSelectedZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onZoneSpinnerSelect(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        updateDateLabel();
        initLandSpinner(new ArrayList<>());
    }
    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getLands().observe(getViewLifecycleOwner(),this::onLandsUpdate);
            viewModel.getLandZones().observe(getViewLifecycleOwner(),this::onZonesUpdate);
        }
    }

    private void onLandsUpdate(List<Land> lands){
        initLandSpinner(lands);
    }
    private void onZonesUpdate(Map<Long,List<LandZone>> zones){
        this.zones.clear();
        if(zones != null){
            this.zones.putAll(zones);
        }
        initZonesSpinner();
    }
    private void onDateUpdate(long timeInMillis){
        Log.d(TAG, "onDateUpdate: called");
        myCalendar.setTimeInMillis(timeInMillis);
        updateDateLabel();
    }

    private void initLandSpinner(List<Land> lands) {
        this.lands.clear();
        this.lands.add(new Land(null, getString(R.string.default_land_spinner_value)));
        selectedLand = this.lands.get(0);
        if(lands != null){
            this.lands.addAll(lands);
        }
        landSpinnerAdapter.notifyDataSetChanged();
        binding.svSelectedLand.setSelection(0);

        initZonesSpinner();
    }
    private void initZonesSpinner() {
        displayZones.clear();
        displayZones.add(new LandZone(null, getString(R.string.default_zone_spinner_value)));
        if (selectedLand.getData() != null) {
            binding.svSelectedZone.setEnabled(true);
            binding.svSelectedZone.setVisibility(View.VISIBLE);
            binding.tvSelectZoneLabel.setVisibility(View.VISIBLE);
            List<LandZone> tempZones = zones.getOrDefault(selectedLand.getData().getId(),null);
            if(tempZones != null){
                displayZones.addAll(tempZones);
            }
        }else{
            binding.svSelectedZone.setEnabled(false);
            binding.svSelectedZone.setVisibility(View.GONE);
            binding.tvSelectZoneLabel.setVisibility(View.GONE);
        }
        selectedZone = this.displayZones.get(0);
        zoneSpinnerAdapter.notifyDataSetChanged();
        binding.svSelectedZone.setSelection(0);
    }

    private void onLandSpinnerSelect(int position) {
        if(position < lands.size()){
            selectedLand = lands.get(position);
        }else{
            selectedLand = this.lands.get(0);
        }
        initZonesSpinner();
        printData("onLandSpinnerSelect");
    }
    private void onZoneSpinnerSelect(int position) {
        if(position < displayZones.size()){
            selectedZone = displayZones.get(position);
        }else{
            selectedZone = this.displayZones.get(0);
        }
        printData("onZoneSpinnerSelect");
    }
    private void onOpenDatePicker(){
        if(getActivity() != null){
            MaterialDatePicker<Long> picker = DialogUtil.getDatePickerDialog(
                    getString(R.string.calendar_pop_up_title),
                    myCalendar.getTimeInMillis()
            );
            picker.addOnPositiveButtonClickListener(this::onDateUpdate);
            picker.show(getActivity().getSupportFragmentManager(), picker.toString());
        }
    }

    private void updateDateLabel() {
        Log.d(TAG, "updateDateLabel: called");
        String myFormat = "EEE, d MMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        binding.etSelectedDate.setText(sdf.format(myCalendar.getTime()));
    }
    private void printData(String TAG) {
        if(selectedLand.getData() != null){
            Log.d(CalendarNewEventFragment.TAG, TAG+": selected land = " + selectedLand.toString());
        }else{
            Log.d(CalendarNewEventFragment.TAG, TAG+": selected land = null");
        }
        if(selectedZone.getData() != null){
            Log.d(CalendarNewEventFragment.TAG, TAG+": selected zone = " + selectedZone.toString());
        }else{
            Log.d(CalendarNewEventFragment.TAG, TAG+": selected zone = null");
        }
    }
}