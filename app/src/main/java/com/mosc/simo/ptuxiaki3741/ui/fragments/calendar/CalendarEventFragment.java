package com.mosc.simo.ptuxiaki3741.ui.fragments.calendar;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarNewEventBinding;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.data.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarEventFragment extends Fragment{
    private FragmentCalendarNewEventBinding binding;
    private LoadingDialog dialog;

    private static final String TAG = "CalendarNewEventFragment";
    private AppViewModel viewModel;

    private List<Land> lands;
    private Map<Long,List<LandZone>> zones;
    private List<Long> snapshots;
    private List<CalendarCategory> calendarCategories;
    private List<LandZone> displayZones;

    private CalendarNotification calendarNotification;
    private boolean landsInit, zonesInit;
    private long snapshot;

    private Calendar notificationDate;
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

    private void initData(){
        lands = new ArrayList<>();
        zones = new HashMap<>();
        snapshots = new ArrayList<>();
        calendarCategories = new ArrayList<>();
        displayZones = new ArrayList<>();
        landsInit = false;
        zonesInit = false;

        selectedLand = new Land(getString(R.string.default_land_spinner_value));
        selectedZone = new LandZone(getString(R.string.default_zone_spinner_value));
        notificationDate = Calendar.getInstance();
        notificationDate.add(Calendar.HOUR_OF_DAY, 1);
        notificationDate.set(Calendar.SECOND,0);
        notificationDate.set(Calendar.MILLISECOND,0);

        calendarNotification = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argNotification)){
                calendarNotification = getArguments().getParcelable(AppValues.argNotification);
            }
        }

        if(calendarNotification != null){
            notificationDate.setTime(calendarNotification.getDate());
        }else{
            calendarNotification = new CalendarNotification(
                    0,
                    AppValues.defaultCalendarCategoryID,
                    -1,
                    null,
                    null,
                    "",
                    "",
                    notificationDate.getTime()
            );
        }
        snapshot = calendarNotification.getSnapshot();
    }

    private void initActivity(){
        if(getActivity() == null) return;
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setOnBackPressed(()->true);
        dialog = mainActivity.getLoadingDialog();
    }

    private void initFragment(){
        binding.ibClose.setOnClickListener(view -> goBack());
        binding.ibDelete.setOnClickListener(v->delete());

        if(calendarNotification.getId() == 0){
            binding.ibDelete.setVisibility(View.GONE);
            binding.tvTitle.setText(getString(R.string.calendar_new_instance_fragment_title));
        }else{
            binding.ibDelete.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(getString(R.string.calendar_edit_instance_fragment_title));
        }

        binding.tvSelectTitle.setText(calendarNotification.getTitle());
        binding.tvSelectMessage.setText(calendarNotification.getMessage());

        binding.tvSelectType.setText("",false);
        ArrayAdapter<String> calendarCategoriesAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>());
        binding.tvSelectType.setAdapter(calendarCategoriesAdapter);

        binding.tvSelectType.setOnItemClickListener((parent, v, pos, id) -> onSelectType(pos));
        binding.tvSelectSnapshot.setOnItemClickListener((parent, v, pos, id) -> onSelectSnapshot(pos));
        binding.tvSelectLand.setOnItemClickListener((parent, v, pos, id) -> onSelectLand(pos));
        binding.tvSelectZone.setOnItemClickListener((parent, v, pos, id) -> onSelectZone(pos));

        binding.tvSelectDate.setOnClickListener(v->onOpenDatePicker());
        binding.tvSelectTime.setOnClickListener(v->onOpenTimePicker());

        binding.btnSave.setOnClickListener(v->save());
        binding.btnCancel.setOnClickListener(v->goBack());

        updateDateLabel();
        updateTimeLabel();
    }

    private void initViewModel(){
        if(getActivity() == null) return;
        viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        AsyncTask.execute(()->{
            snapshot = viewModel.setTempSnapshot(snapshot);
            getActivity().runOnUiThread(()->{
                viewModel.getSnapshots().observe(getViewLifecycleOwner(),this::onSnapshotsUpdate);
                viewModel.getCalendarCategories().observe(getViewLifecycleOwner(),this::onCategoriesUpdate);
                viewModel.getTempSnapshotLands().observe(getViewLifecycleOwner(),this::onLandsUpdate);
                viewModel.getTempSnapshotLandZones().observe(getViewLifecycleOwner(),this::onZonesUpdate);
            });
        });
    }

    private void onCategoriesUpdate(List<CalendarCategory> calendarCategories) {
        this.calendarCategories.clear();
        if(calendarCategories != null) this.calendarCategories.addAll(calendarCategories);
        updateCalendarCategories();
    }

    private void onSnapshotsUpdate(List<Long> snapshots) {
        this.snapshots.clear();
        this.snapshots.add(snapshot);
        if(snapshots != null){
            for(Long snapshot : snapshots){
                if(snapshot == null) continue;
                if(!this.snapshots.contains(snapshot)) this.snapshots.add(snapshot);
            }
        }
        updateSnapshots();
    }

    private void onLandsUpdate(List<Land> lands) {
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

    private void onZonesUpdate(Map<Long, List<LandZone>> zones) {
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

    private void onSelectType(int position) {
        if(position < 0 || position >= calendarCategories.size() ) return;
        binding.tvSelectType.setText(calendarCategories.get(position).getName(),false);
    }

    private void onSelectSnapshot(int position) {
        if(position < 0 || position >= snapshots.size() ) return;
        if(snapshot == snapshots.get(position)) return;
        if(dialog != null) dialog.openDialog();
        snapshot = snapshots.get(position);
        binding.tvSelectSnapshot.setText(String.valueOf(snapshot),false);
        onSelectLand(0);
        AsyncTask.execute(()->{
            snapshot = viewModel.setTempSnapshot(snapshot);
            if(dialog != null) dialog.closeDialog();
        });
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

    private void updateCalendarCategories() {
        String currSelected = "";
        if(binding.tvSelectType.getText() != null) currSelected = binding.tvSelectType.getText().toString();
        List<String> categoriesDisplay = new ArrayList<>();
        for(CalendarCategory category : calendarCategories){
            categoriesDisplay.add(category.getName());
            if(category.getId() == calendarNotification.getCategoryID()) {
                currSelected = category.getName();
            }
        }
        if(currSelected.isEmpty() && calendarCategories.size() > 0){
            currSelected = calendarCategories.get(0).getName();
        }
        ArrayAdapter<String> calendarCategoriesAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                categoriesDisplay);
        binding.tvSelectType.setText(currSelected,false);
        binding.tvSelectType.setAdapter(calendarCategoriesAdapter);
        if(categoriesDisplay.size() > 2){
            binding.tvSelectType.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.dropDownHeight));
        }else{
            binding.tvSelectType.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void updateSnapshots() {
        List<String> snapshots = new ArrayList<>();
        for(long snapshot : this.snapshots){
            snapshots.add(String.valueOf(snapshot));
        }
        ArrayAdapter<String> snapshotAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                snapshots);
        binding.tvSelectSnapshot.setText(String.valueOf(snapshot),false);
        binding.tvSelectSnapshot.setAdapter(snapshotAdapter);
        if(snapshots.size() > 2){
            binding.tvSelectSnapshot.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.dropDownHeight));
        }else{
            binding.tvSelectSnapshot.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
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

        if(adapterLands.getCount() > 2){
            binding.tvSelectLand.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.dropDownHeight));
        }else{
            binding.tvSelectLand.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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

        if(adapterZones.getCount() > 2){
            binding.tvSelectZone.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.dropDownHeight));
        }else{
            binding.tvSelectZone.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
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
        if(dialog != null) dialog.openDialog();

        long id = calendarNotification.getId();
        long categoryID = AppValues.defaultCalendarCategoryID;
        Long lid = null;
        Long zid = null;
        String title = "";
        String message = "";
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
            String selectedCat = binding.tvSelectType.getText().toString();
            for(CalendarCategory category : calendarCategories){
                if(category.getName().equals(selectedCat)){
                    categoryID = category.getId();
                    break;
                }
            }
        }
        if( selectedLand != null && selectedLand.getData() != null){
            lid = selectedLand.getData().getId();
            snapshot = selectedLand.getData().getSnapshot();
            if( selectedZone != null && selectedZone.getData() != null && selectedZone.getData().getLid() == lid){
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
        if(viewModel == null || hasError) {
            if(dialog != null) dialog.closeDialog();
            return;
        }

        calendarNotification = new CalendarNotification(id, categoryID, snapshot, lid, zid, title, message, date);
        AsyncTask.execute(()->{
            viewModel.saveNotification(calendarNotification);
            if(dialog != null) dialog.closeDialog();
            goBack();
        });
    }

    private void delete(){
        if(calendarNotification.getId() == 0) return;
        AsyncTask.execute(()->{
            viewModel.removeNotification(calendarNotification);
            goBack();
        });
    }

    private void printSelectedData(){
        Log.d(TAG, "printSelectedData: selectedLand = " + selectedLand.toString());
        Log.d(TAG, "printSelectedData: selectedZone = " + selectedZone.toString());
    }

    private void goBack(){
        if(getActivity() != null) {
            getActivity().runOnUiThread(getActivity()::onBackPressed);
        }
    }
}