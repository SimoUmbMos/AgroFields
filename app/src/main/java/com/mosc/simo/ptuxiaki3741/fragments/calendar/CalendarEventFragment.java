package com.mosc.simo.ptuxiaki3741.fragments.calendar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarNewEventBinding;
import com.mosc.simo.ptuxiaki3741.enums.CalendarEventType;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarEventFragment extends Fragment implements FragmentBackPress {
    private static final String TAG = "CalendarNewEventFragment";
    private FragmentCalendarNewEventBinding binding;
    private AppViewModel viewModel;

    private List<Land> lands;
    private Map<Long,List<LandZone>> zones;
    private List<LandZone> displayZones;

    private CalendarNotification calendarNotification;
    private boolean landsInit, zonesInit;
    private String[] calendarNotificationTypes;

    private Calendar notificationDate;
    private Land selectedLand;
    private LandZone selectedZone;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.notification_entry_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menu_item_delete_notification);
        if(item != null){
            item.setVisible(calendarNotification.getId() != 0);
            item.setEnabled(calendarNotification.getId() != 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_delete_notification){
            delete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initData(){
        lands = new ArrayList<>();
        zones = new HashMap<>();
        displayZones = new ArrayList<>();
        landsInit = false;
        zonesInit = false;

        selectedLand = new Land(getString(R.string.default_land_spinner_value));
        selectedZone = new LandZone(getString(R.string.default_zone_spinner_value));
        notificationDate = Calendar.getInstance();
        notificationDate.add(Calendar.HOUR_OF_DAY, 1);

        calendarNotification = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argNotification)){
                calendarNotification = getArguments().getParcelable(AppValues.argNotification);
            }
        }

        if(calendarNotification != null){
            notificationDate.setTime(calendarNotification.getDate());
            notificationDate.set(Calendar.SECOND,0);
            notificationDate.set(Calendar.MILLISECOND,0);
        }else{
            notificationDate.set(Calendar.SECOND,0);
            notificationDate.set(Calendar.MILLISECOND,0);
            calendarNotification = new CalendarNotification(
                    0,
                    null,
                    null,
                    "",
                    "",
                    CalendarEventType.SCHEDULE,
                    notificationDate.getTime()
            );
        }

        calendarNotificationTypes = getResources().getStringArray(R.array.notification_event_types);
    }
    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                if(calendarNotification.getId() == 0){
                    mainActivity.setToolbarTitle(getString(R.string.calendar_new_instance_fragment_title));
                }else{
                    mainActivity.setToolbarTitle(getString(R.string.calendar_edit_instance_fragment_title));
                }
                ActionBar actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.show();
                }
            }
        }
    }
    private void initFragment(){
        binding.tvSelectTitle.setText(calendarNotification.getTitle());
        binding.tvSelectMessage.setText(calendarNotification.getMessage());

        int type = calendarNotification.getType().ordinal();
        String selectedType = calendarNotificationTypes[type];
        binding.tvSelectType.setText(selectedType);
        ArrayAdapter<String> adapterTypes = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                calendarNotificationTypes);
        binding.tvSelectType.setAdapter(adapterTypes);

        binding.tvSelectLand.setOnItemClickListener((parent, v, pos, id) -> onSelectLand(pos));
        binding.tvSelectZone.setOnItemClickListener((parent, v, pos, id) -> onSelectZone(pos));

        binding.tvSelectDate.setOnClickListener(v->onOpenDatePicker());
        binding.tvSelectTime.setOnClickListener(v->onOpenTimePicker());

        binding.btnSaveNotification.setOnClickListener(v->save());
        binding.btnCancelNotification.setOnClickListener(v->goBack());

        updateDateLabel();
        updateTimeLabel();
    }
    private void initViewModel(){
        if(getActivity() != null){
            viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getLands().observe(getViewLifecycleOwner(),this::initLandSelect);
            viewModel.getLandZones().observe(getViewLifecycleOwner(),this::initZonesSelect);
        }
    }
    private void initLandSelect(List<Land> lands) {
        landsInit = true;
        this.lands.clear();
        this.lands.add(new Land(getString(R.string.default_land_spinner_value)));
        if(lands != null){
            this.lands.addAll(lands);
        }
        updateLandSelect();

        if(landsInit && zonesInit){
            landsInit = false;
            zonesInit = false;
            initFragmentFromNotification();
        }
    }
    private void initZonesSelect(Map<Long, List<LandZone>> zones) {
        zonesInit = true;
        this.zones.clear();
        if(zones != null){
            this.zones.putAll(zones);
        }
        updateZoneSelect();

        if(landsInit && zonesInit){
            landsInit = false;
            zonesInit = false;
            initFragmentFromNotification();
        }
    }
    private void initFragmentFromNotification(){
        if(calendarNotification.getLid() != null){
            for(Land land : lands){
                if(land.getData() == null) continue;
                if(land.getData().getId() == calendarNotification.getLid()){
                    selectedLand = this.lands.get(lands.indexOf(land));
                    updateLandSelect();
                    updateZoneSelect();
                    break;
                }
            }
            if(calendarNotification.getZid() != null){
                for(LandZone zone : displayZones){
                    if(zone.getData() == null) continue;
                    if(zone.getData().getId() == calendarNotification.getZid()){
                        selectedZone = this.displayZones.get(displayZones.indexOf(zone));
                        updateZoneSelect();
                        break;
                    }
                }
            }
        }
        printSelectedData();
    }

    private void onDateUpdate(long timeInMillis){
        int hour = notificationDate.get(Calendar.HOUR_OF_DAY);
        int minute = notificationDate.get(Calendar.MINUTE);
        notificationDate.setTimeInMillis(timeInMillis);
        notificationDate.set(Calendar.HOUR_OF_DAY,hour);
        notificationDate.set(Calendar.MINUTE,minute);
        updateDateLabel();
    }
    private void onTimeUpdate(MaterialTimePicker picker) {
        notificationDate.set(Calendar.HOUR_OF_DAY,picker.getHour());
        notificationDate.set(Calendar.MINUTE,picker.getMinute());
        updateTimeLabel();
    }

    private void onSelectLand(int position) {
        if(position < lands.size()){
            if(lands.get(position) != selectedLand){
                selectedLand = lands.get(position);
                selectedZone = displayZones.get(0);
            }
        }else{
            selectedLand = lands.get(0);
            selectedZone = displayZones.get(0);
        }
        updateLandSelect();
        updateZoneSelect();
        printSelectedData();
    }
    private void onSelectZone(int position) {
        if(position < displayZones.size()){
            selectedZone = displayZones.get(position);
        }else{
            selectedZone = this.displayZones.get(0);
        }
        updateZoneSelect();
        printSelectedData();
    }
    private void onOpenDatePicker(){
        MaterialDatePicker<Long> picker = DialogUtil.getDatePickerDialog(
                getString(R.string.calendar_pop_up_date_title),
                notificationDate
        );
        picker.addOnPositiveButtonClickListener(this::onDateUpdate);
        if(getActivity() != null){
            picker.show(getActivity().getSupportFragmentManager(), picker.toString());
        }
    }
    private void onOpenTimePicker(){
        MaterialTimePicker picker = DialogUtil.getTimePickerDialog(
                getString(R.string.calendar_pop_up_time_title),
                notificationDate,
                DateFormat.is24HourFormat(getContext())
        );
        picker.addOnPositiveButtonClickListener(v->onTimeUpdate(picker));
        if(getActivity() != null){
            picker.show(getActivity().getSupportFragmentManager(), picker.toString());
        }
    }

    private void updateLandSelect(){
        binding.tvSelectLand.setText(selectedLand.toString(), false);

        List<String> landStrings = new ArrayList<>();
        for(Land land : lands){
            landStrings.add(land.toString());
        }
        ArrayAdapter<String> adapterLands = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                landStrings
        );
        binding.tvSelectLand.setAdapter(adapterLands);
    }
    private void updateZoneSelect(){
        binding.tvSelectZone.setText(selectedZone.toString(), false);

        displayZones.clear();
        List<LandZone> temp = null;
        if(selectedLand.getData() != null){
            temp = zones.getOrDefault(selectedLand.getData().getId(),null);
        }
        displayZones.add(new LandZone(getString(R.string.default_zone_spinner_value)));
        if(temp != null){
            displayZones.addAll(temp);
        }

        List<String> zonesStrings = new ArrayList<>();
        for(LandZone zone : displayZones){
            zonesStrings.add(zone.toString());
        }
        ArrayAdapter<String> adapterZones = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                zonesStrings
        );
        binding.tvSelectZone.setAdapter(adapterZones);

        if(selectedLand.getData() == null){
            binding.tilSelectZone.setVisibility(View.GONE);
        }else{
            binding.tilSelectZone.setVisibility(View.VISIBLE);
        }
    }
    private void updateDateLabel() {
        String myFormat = "EEE, d MMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        binding.tvSelectDate.setText(sdf.format(notificationDate.getTime()));
    }
    private void updateTimeLabel() {
        String myFormat = "h:mm a";
        if(DateFormat.is24HourFormat(getContext())){
            myFormat = "H:mm";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        binding.tvSelectTime.setText(sdf.format(notificationDate.getTime()));
    }

    private void save(){
        long id = calendarNotification.getId();
        Long lid = null;
        Long zid = null;
        String title = "";
        String message = "";
        CalendarEventType type = CalendarEventType.SCHEDULE;
        Date date = notificationDate.getTime();

        if(binding.tvSelectTitle.getText() != null){
            title = binding.tvSelectTitle.getText().toString()
                    .trim().replaceAll("\\s+"," ");
        }
        if(binding.tvSelectMessage.getText() != null){
            message = binding.tvSelectMessage.getText().toString()
                    .trim().replaceAll("\\s+"," ");
        }
        if(binding.tvSelectType.getText() != null){
            String typeToSearch = binding.tvSelectType.getText().toString();
            for( int i = 0; i < calendarNotificationTypes.length; i++ ){
                if(calendarNotificationTypes[i].equals(typeToSearch)){
                    type = CalendarEventType.values()[i];
                    break;
                }
            }
        }

        if(selectedLand.getData() != null){
            lid = selectedLand.getData().getId();
            if(selectedZone.getData() != null){
                zid = selectedZone.getData().getId();
            }
        }

        boolean hasError = false;
        binding.tilSelectTitle.setError(null);
        binding.tilSelectMessage.setError(null);
        if(title.isEmpty()) {
            binding.tilSelectTitle.setError(getString(R.string.notification_title_error));
            hasError = true;
        }
        if(message.isEmpty()) {
            binding.tilSelectMessage.setError(getString(R.string.notification_message_error));
            hasError = true;
        }
        if(viewModel == null || hasError) return;

        calendarNotification = new CalendarNotification(id, lid, zid, title, message, type, date);
        viewModel.saveNotification(calendarNotification);
        goBack();
    }
    private void delete(){
        if(calendarNotification.getId() == 0) return;
        viewModel.removeNotification(calendarNotification);
        goBack();
    }

    private void printSelectedData(){
        Log.d(TAG, "printSelectedData: selectedLand = " + selectedLand.toString());
        Log.d(TAG, "printSelectedData: selectedZone = " + selectedZone.toString());
    }
    private void goBack(){
        if(getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}