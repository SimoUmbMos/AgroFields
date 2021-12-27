package com.mosc.simo.ptuxiaki3741.fragments.zones;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentZoneEditorBinding;
import com.mosc.simo.ptuxiaki3741.enums.ZoneEditorState;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.ColorData;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZoneEditorFragment extends Fragment implements FragmentBackPress {
    private static final String TAG = "ZoneEditorFragment";
    //todo: make better ui
    //fixme: points marker are big on zoom
    //fixme: add tags
    private FragmentZoneEditorBinding binding;
    private GoogleMap mMap;
    private Polygon zonePolygon;
    private List<Circle> zonePoints;
    private AlertDialog dialog;

    private ActionBar actionBar;

    private AppViewModel vmLands;
    private LandZone zone;
    private List<LandZone> otherZones;
    private Land land;

    private ZoneEditorState state;

    private String title, note;
    private ColorData color,tempColor;
    private List<LatLng> border;
    private boolean isInit, forceBack, showNote;
    private int index1, index2, index3;

    private void initData(){
        land = null;
        zone = null;
        otherZones = new ArrayList<>();

        title = "";
        note = "";
        color = AppValues.defaultZoneColor;
        border = new ArrayList<>();
        zonePoints = new ArrayList<>();

        isInit = false;
        forceBack = false;
        showNote = false;
        state = ZoneEditorState.NormalState;
        index1 = -1;
        index2 = -1;
        index3 = -1;

        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argZone)){
                zone = getArguments().getParcelable(AppValues.argZone);
            }
            if(getArguments().containsKey(AppValues.argLand)){
                land = getArguments().getParcelable(AppValues.argLand);
            }
        }
        if(zone != null){
            title = zone.getData().getTitle();
            note = zone.getData().getNote();
            color = zone.getData().getColor();
            border.addAll(zone.getData().getBorder());
        }
    }
    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
                actionBar = activity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.show();
                }
            }
        }
    }
    private void initFragment(){
        binding.navZoneMenu.setNavigationItemSelectedListener(this::onMenuClick);
        binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.ibToggleNote.setOnClickListener(v->toggleNote());
        updateUI();
        binding.mvZonePreview.getMapAsync(this::initMap);
    }
    private void initViewModel(){
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }
    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(AppValues.countryZoom-2);
        mMap.setMaxZoomPreference(AppValues.streetZoom+2);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMapClickListener(this::onMapClick);
        binding.ibCenterCamera.setOnClickListener(v-> zoomOnLand());
        zoomOnLand();
        initObservers();
    }
    private void initObservers(){
        if(vmLands != null){
            vmLands.getLandZones().observe(getViewLifecycleOwner(),this::onZonesUpdate);
        }
    }
    private void initDrawMap(){
        if(mMap != null && land != null){
            zoomOnLand();
            mMap.clear();
            zonePolygon = null;
            zonePoints.clear();
            int strokeColor;
            int fillColor;
            if(land.getData().getBorder().size()>0){
                strokeColor = Color.argb(
                        AppValues.defaultStrokeAlpha,
                        land.getData().getColor().getRed(),
                        land.getData().getColor().getGreen(),
                        land.getData().getColor().getBlue()
                );
                fillColor = Color.argb(
                        AppValues.defaultFillAlpha,
                        land.getData().getColor().getRed(),
                        land.getData().getColor().getGreen(),
                        land.getData().getColor().getBlue()
                );
                PolygonOptions options = LandUtil.getPolygonOptions(
                        land.getData(),
                        strokeColor,
                        fillColor,
                        false
                );
                mMap.addPolygon(options.zIndex(1));
                for(LandZone tempZone:otherZones){
                    if(tempZone.getData().getBorder().size()>0){
                        strokeColor = Color.argb(
                                AppValues.defaultStrokeAlpha,
                                tempZone.getData().getColor().getRed(),
                                tempZone.getData().getColor().getGreen(),
                                tempZone.getData().getColor().getBlue()
                        );
                        fillColor = Color.argb(
                                AppValues.defaultFillAlpha,
                                tempZone.getData().getColor().getRed(),
                                tempZone.getData().getColor().getGreen(),
                                tempZone.getData().getColor().getBlue()
                        );
                        options = LandUtil.getPolygonOptions(
                                tempZone.getData(),
                                strokeColor,
                                fillColor,
                                false
                        );
                        mMap.addPolygon(options.zIndex(2));
                    }
                }
            }
        }
        if(zone == null){
            isInit = true;
            showTitleDialog();
        }
        updateUI();
    }
    private void zoomOnLand(){
        if(mMap != null && land != null){
            if(land.getData().getBorder().size()>0){
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(LatLng point: land.getData().getBorder()){
                    builder.include(point);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        AppValues.defaultPadding
                ));
            }
        }
    }

    private void saveZone(){
        toggleDrawer(false);
        if(isValidSave()){
            AsyncTask.execute(()->{
                List<LatLng> temp = new ArrayList<>(getBiggerAreaZoneIntersections(border,land.getData().getBorder()));
                border.clear();
                border.addAll(temp);
                for(LandZone zone : otherZones){
                    temp.clear();
                    temp.addAll(getBiggerAreaZoneDifference(border,zone.getData().getBorder()));
                    border.clear();
                    border.addAll(temp);
                }
                temp.clear();
                temp.addAll(MapUtil.simplify(border));
                border.clear();
                border.addAll(temp);
                if(getActivity() != null){
                    getActivity().runOnUiThread(this::updateMap);
                }
                if(isValidSave()){
                    if(zone != null){
                        zone.getData().setTitle(title);
                        zone.getData().setNote(note);
                        zone.getData().setColor(color);
                        zone.getData().setBorder(border);
                    }else{
                        zone = new LandZone(new LandZoneData(land.getData().getId(),title,note,color,border));
                    }
                    try{
                        vmLands.saveZone(zone);
                        Snackbar.make(binding.getRoot(), getString(R.string.zone_saved), Snackbar.LENGTH_LONG).show();
                    }catch (Exception e){
                        Log.e(TAG, "saveZone: ", e);
                        Snackbar.make(binding.getRoot(), getString(R.string.zone_null_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    private boolean isValidSave() {
        String error = null;
        if(title.trim().isEmpty()){
            showTitleDialog();
            error = getString(R.string.title_error);
        }else if(land == null){
            error = getString(R.string.zone_null_error);
        }else if(border.size()<=2){
            error = getString(R.string.zone_border_error);
        }else if(vmLands == null){
            error = getString(R.string.zone_null_error);
        }

        if(error != null){
            Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            return false;
        }else{
            return true;
        }
    }
    private boolean isValidTitle(String title) {
        return !title.isEmpty();
    }
    @SuppressLint("CutPasteId")
    private void showTitleDialog(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog)
                    .setTitle(getString(R.string.zone_title_label))
                    .setView(R.layout.view_edit_text)
                    .setPositiveButton(getString(R.string.zone_title_positive),null)
                    .setNegativeButton(getString(R.string.zone_title_negative),(d, w)-> {
                        d.cancel();
                        if(isInit){
                            isInit = false;
                            goBack();
                        }
                    }).create();
            dialog.show();
            EditText titleV = dialog.findViewById(R.id.etZoneTitle);
            if(titleV != null){
                titleV.setText(title);
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                EditText titleView = dialog.findViewById(R.id.etZoneTitle);
                if(titleView != null){
                    if(titleView.getText() != null){
                        String title = DataUtil.removeSpecialCharacters(
                                titleView.getText().toString()
                        );
                        if(isValidTitle(title)){
                            this.title = title;
                            updateUI();
                            dialog.dismiss();
                        }else{
                            Toast.makeText(
                                    getContext(),
                                    getString(R.string.title_error),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }else{
                    dialog.dismiss();
                }
            });
        }
    }
    @SuppressLint("CutPasteId")
    private void showNoteDialog(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog)
                    .setTitle(getString(R.string.zone_note_label))
                    .setView(R.layout.view_text_area)
                    .setPositiveButton(getString(R.string.zone_title_positive),null)
                    .setNegativeButton(getString(R.string.zone_title_negative), (d, w)-> d.cancel())
                    .create();
            dialog.show();
            EditText noteV = dialog.findViewById(R.id.etZoneNote);
            if(noteV != null){
                noteV.setText(note);
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                EditText noteView = dialog.findViewById(R.id.etZoneNote);
                if(noteView != null){
                    if(noteView.getText() != null){
                        String note = noteView.getText().toString()
                                .trim()
                                .replaceAll(" +", " ")
                                .replaceAll("\n+", "\n");
                        if(DataUtil.lineCount(note)<=2){
                            if(note.length()<=100){
                                this.note = note;
                                updateUI();
                                dialog.dismiss();
                            }else{
                                Toast.makeText(
                                        getContext(),
                                        getString(R.string.note_max_char_error),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }else{
                            Toast.makeText(
                                    getContext(),
                                    getString(R.string.note_max_line_error),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }else{
                    dialog.dismiss();
                }
            });
        }
    }
    private void showColorDialog(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            tempColor = new ColorData( color.getRed(), color.getGreen(), color.getBlue() );
            dialog = DialogUtil.getColorPickerDialog(getContext())
                    .setPositiveButton(getString(R.string.zone_title_positive),(d, w) -> {
                        color = new ColorData(
                                tempColor.getRed(),
                                tempColor.getGreen(),
                                tempColor.getBlue()
                        );
                        updateMap();
                        d.dismiss();
                    })
                    .setNegativeButton(getString(R.string.zone_title_negative),(d, w) -> d.cancel())
                    .show();

            Slider redSlider = dialog.findViewById(R.id.slRedSlider);
            Slider greenSlider = dialog.findViewById(R.id.slGreenSlider);
            Slider blueSlider = dialog.findViewById(R.id.slBlueSlider);
            FrameLayout colorBg = dialog.findViewById(R.id.flColorShower);

            if(colorBg != null){
                colorBg.setBackgroundColor(tempColor.getColor());
            }

            if(redSlider != null){
                redSlider.setValue(tempColor.getRed());
                redSlider.addOnChangeListener((range,value,user) -> {
                    tempColor.setRed(Math.round(value));
                    if(colorBg != null){
                        colorBg.setBackgroundColor(tempColor.getColor());
                    }
                });
            }

            if(greenSlider != null){
                greenSlider.setValue(tempColor.getGreen());
                greenSlider.addOnChangeListener((range,value,user) -> {
                    tempColor.setGreen(Math.round(value));
                    if(colorBg != null){
                        colorBg.setBackgroundColor(tempColor.getColor());
                    }
                });
            }

            if(blueSlider != null){
                blueSlider.setValue(tempColor.getBlue());
                blueSlider.addOnChangeListener((range,value,user) -> {
                    tempColor.setBlue(Math.round(value));
                    if(colorBg != null){
                        colorBg.setBackgroundColor(tempColor.getColor());
                    }
                });
            }

        }
    }

    private boolean onMenuClick(MenuItem item) {
        onStateUpdate(ZoneEditorState.NormalState);
        switch (item.getItemId()){
            case (R.id.menu_item_edit_zone):
                toggleDrawer(true);
                return true;
            case (R.id.toolbar_action_save_zone):
                saveZone();
                return true;
            case (R.id.toolbar_action_change_zone_name):
                showTitleDialog();
                return true;
            case (R.id.toolbar_action_change_zone_note):
                showNoteDialog();
                return true;
            case (R.id.toolbar_action_change_zone_color):
                showColorDialog();
                return true;
            case (R.id.toolbar_action_zone_add_point):
                onStateUpdate(ZoneEditorState.AddPoint);
                return true;
            case (R.id.toolbar_action_zone_add_between_points):
                if(border.size()>1){
                    onStateUpdate(ZoneEditorState.AddBetweenPoint);
                }
                return true;
            case (R.id.toolbar_action_zone_edit_point):
                onStateUpdate(ZoneEditorState.EditPoint);
                return true;
            case (R.id.toolbar_action_zone_delete_point):
                onStateUpdate(ZoneEditorState.DeletePoint);
                return true;
            default:
                return false;
        }
    }

    private void toggleDrawer(boolean toggle) {
        if(binding != null){
            if(toggle){
                binding.getRoot().openDrawer(GravityCompat.END,true);
            }else{
                binding.getRoot().closeDrawer(GravityCompat.END,false);
            }
        }
    }
    private void toggleNote() {
        if(showNote){
            showNote = false;
            binding.tvNote.setVisibility(View.GONE);
        }else{
            showNote = true;
            binding.tvNote.setVisibility(View.VISIBLE);
        }
    }

    private void onZonesUpdate(Map<Long,List<LandZone>> zones){
        otherZones.clear();
        if(land != null){
            List<LandZone> temp = zones.getOrDefault(land.getData().getId(),null);
            if(temp != null){
                otherZones.addAll(temp);
            }
        }
        if(zone != null){
            for(LandZone zoneTemp : otherZones){
                if(zone.getData().getId() == zoneTemp.getData().getId()) {
                    zone = zoneTemp;
                    break;
                }
            }
            otherZones.remove(zone);
            border.clear();
            border.addAll(zone.getData().getBorder());
        }
        initDrawMap();
        updateUI();
    }
    private void onStateUpdate(ZoneEditorState state){
        if(this.state != state){
            this.state = state;
            clearIndexBetweenPoint();
            updateUI();
            toggleDrawer(false);
        }
    }
    private void onMapClick(LatLng point) {
        if(land == null)
            return;
        boolean doMapUpdate = false;
        switch (state){
            case AddPoint:
                Log.d(TAG, "onMapClick: AddPoint");
                onAddPoint(point);
                doMapUpdate = true;
                break;
            case AddBetweenPoint:
                Log.d(TAG, "onMapClick: AddBetweenPoint");
                onAddBetweenPoint(point);
                doMapUpdate = true;
                break;
            case EditPoint:
                Log.d(TAG, "onMapClick: EditPoint");
                onEditPoint(point);
                doMapUpdate = true;
                break;
            case DeletePoint:
                Log.d(TAG, "onMapClick: DeletePoint");
                onDeletePoint(point);
                doMapUpdate = true;
                break;
        }
        if(doMapUpdate)
            updateMap();
    }
    private void onAddPoint(LatLng point) {
        border.add(point);
    }
    private void onAddBetweenPoint(LatLng point) {
        if(index1 < 0)
            selectFirstPointForBetween(point);
        else if(index2 < 0)
            selectSecondPointForBetween(point);
        else if(index3 < 0)
            placePointBetween(point);
        else
            editPointBetween(point);
        updateUIBasedOnState();
    }
    private void onEditPoint(LatLng point) {
        int index = MapUtil.closestPoint(border,point);
        if(index > -1 && index < border.size()){
            if(AppValues.distanceToMapActionKM >= MapUtil.distanceBetween(border.get(index),point)){
                border.set(index,point);
            }
        }
    }
    private void onDeletePoint(LatLng point) {
        int index = MapUtil.closestPoint(border,point);
        if(index > -1 && index < border.size()){
            if(AppValues.distanceToMapActionKM >= MapUtil.distanceBetween(border.get(index),point)){
                border.remove(index);
            }
        }
    }

    private void selectFirstPointForBetween(LatLng point) {
        index1 = MapUtil.closestPoint(border,point);
        if(index1 > -1 && index1 < border.size()){
            if(MapUtil.distanceBetween(point,border.get(index1)) > AppValues.distanceToMapActionKM){
                index1 = -1;
            }
        }else{
            index1 = -1;
        }
    }
    private void selectSecondPointForBetween(LatLng point) {
        index2 = MapUtil.closestPoint(border,point);
        if(index2 > -1 && index2 < border.size()){
            if(MapUtil.distanceBetween(point,border.get(index2)) < AppValues.distanceToMapActionKM){
                checkIndexBetween();
            }else{
                index2 = -1;
            }
        }else{
            index2 = -1;
        }
    }
    private void placePointBetween(LatLng point) {
        if(index2 == (border.size() - 1)){
            if(index1 == 0){
                index3=border.size();
                border.add(point);
            }else{
                index3=index2;
                index2++;
                border.add(index3,point);
            }
        }else{
            index3=index2;
            index2++;
            border.add(index3,point);
        }
    }
    private void editPointBetween(LatLng point) {
        border.set(index3,point);
    }
    private void checkIndexBetween() {
        boolean reset = true;
        if(index1 > -1 && index2 > -1){
            if((index1 + 1) == index2){
                reset = false;
            }else if((index1 - 1) == index2){
                int tempIndex = index1;
                index1 = index2;
                index2 = tempIndex;
                reset = false;
            }else if(index1 == 0 && index2 == (border.size() - 1)){
                reset = false;
            }else if(index1 == (border.size() - 1) && index2 == 0){
                int tempIndex = index1;
                index1 = index2;
                index2 = tempIndex;
                reset = false;
            }
        }
        if(reset)
            clearIndexBetweenPoint();
    }

    private void updateUI() {
        if(actionBar != null){
            if(zone != null){
                actionBar.setTitle(title+" #"+ zone.getData().getId());
            }else{
                if(!title.trim().isEmpty()){
                    actionBar.setTitle(title);
                }else{
                    actionBar.setTitle(getString(R.string.new_zone_bar_label));
                }
            }
        }
        binding.tvNote.setText(note);
        if(note.isEmpty()){
            binding.tvNote.setVisibility(View.GONE);
            binding.ibToggleNote.setVisibility(View.GONE);
        }else{
            binding.ibToggleNote.setVisibility(View.VISIBLE);
            if(showNote){
                binding.tvNote.setVisibility(View.VISIBLE);
            }else{
                binding.tvNote.setVisibility(View.GONE);
            }
        }
        if(mMap != null)
            updateMap();
    }
    private void updateUIBasedOnState() {
        //todo:code...
    }
    private void updateMap(){
        if(mMap != null){
            if(zonePolygon != null){
                zonePolygon.remove();
                zonePolygon=null;
                for(Circle p:zonePoints){
                    p.remove();
                }
                zonePoints.clear();
            }
            if(border.size()>0){
                int strokeColor = Color.argb(
                        AppValues.defaultStrokeAlpha,
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue()
                );
                int fillColor = Color.argb(
                        AppValues.defaultFillAlpha,
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue()
                );
                PolygonOptions options = LandUtil.getPolygonOptions(
                        new LandZoneData(border),
                        strokeColor,
                        fillColor,
                        false
                );
                zonePolygon = mMap.addPolygon(options.zIndex(3));
                for(LatLng point : border){
                    zonePoints.add(mMap.addCircle(new CircleOptions()
                            .center(point)
                            .radius(10)
                            .fillColor(strokeColor)
                            .strokeColor(strokeColor)
                            .clickable(false)
                            .zIndex(4)
                    ));
                }
            }
        }
    }

    private void clearIndexBetweenPoint(){
        index1 = -1;
        index2 = -1;
        index3 = -1;
    }

    private List<LatLng> getBiggerAreaZoneIntersections(List<LatLng> p1, List<LatLng> p2){
        List<LatLng> ans = new ArrayList<>();
        if(MapUtil.notContains(p1,p2)){
            Log.d(TAG, "getBiggerAreaZoneIntersections: don't contains");
            return ans;
        }
        List<LatLng> tempBorders = MapUtil.intersection(p1,p2);
        if(tempBorders.size()>0){
            ans.addAll(tempBorders);
        }
        return ans;
    }
    private List<LatLng> getBiggerAreaZoneDifference(List<LatLng> p1, List<LatLng> p2){
        List<LatLng> ans = new ArrayList<>(p1);
        if(MapUtil.notContains(p1,p2)){
            return ans;
        }
        List<List<LatLng>> tempBorders = MapUtil.difference(p1,p2);
        double area,max_area=0;
        int index = -1;
        for(int i =0; i<tempBorders.size(); i++){
            area = MapUtil.area(tempBorders.get(i));
            if(area>max_area){
                max_area = area;
                index = i;
            }
        }
        if(index != -1){
            ans.clear();
            ans.addAll(tempBorders.get(index));
        }
        return ans;
    }

    private void goBack(){
        forceBack = true;
        if(getActivity() != null)
            getActivity().onBackPressed();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentZoneEditorBinding.inflate(inflater,container,false);
        binding.mvZonePreview.onCreate(savedInstanceState);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModel();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding.mvZonePreview.onDestroy();
        binding = null;
    }
    @Override public void onResume() {
        super.onResume();
        binding.mvZonePreview.onResume();
    }
    @Override public void onPause() {
        super.onPause();
        binding.mvZonePreview.onPause();
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        binding.mvZonePreview.onLowMemory();
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.zone_editor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(onMenuClick(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        if(forceBack)
            return true;
        if (state != ZoneEditorState.NormalState) {
            onStateUpdate(ZoneEditorState.NormalState);
            return false;
        }
        //todo:check if saved
        return true;
    }
}