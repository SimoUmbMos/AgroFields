package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentZoneEditorBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.LocationStates;
import com.mosc.simo.ptuxiaki3741.data.enums.ZoneEditorState;
import com.mosc.simo.ptuxiaki3741.data.helpers.LocationHelper;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.data.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZoneEditorFragment extends Fragment implements FragmentBackPress {
    private static final String TAG = "ZoneEditorFragment";
    private FragmentZoneEditorBinding binding;
    private GoogleMap mMap;
    private Polygon zonePolygon;
    private List<Circle> zonePoints;
    private AlertDialog dialog;
    private LoadingDialog loadingDialog;

    private AppViewModel vmLands;
    private LandZone zone;
    private List<LandZone> otherZones;
    private Land land;

    private ZoneEditorState state;

    private List<List<LatLng>> undoList;

    private LocationHelper locationHelperPoint;
    private Marker positionMarker;
    private boolean locationPointWasRunning;

    private String title, note;
    private ColorData color,tempColor;
    private List<LatLng> border;
    private boolean forceBack, showNote, mapLoaded, doubleBackToExit, isSaving;
    private int index1, index2, index3;

    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION,false);

                LocationStates locationPermission;
                if (fineLocationGranted != null && fineLocationGranted){
                    locationPermission = LocationStates.FINE_LOCATION;
                }else if(coarseLocationGranted != null && coarseLocationGranted){
                    locationPermission = LocationStates.COARSE_LOCATION;
                }else{
                    locationPermission = LocationStates.DISABLE;
                }

                if(locationHelperPoint != null){
                    locationHelperPoint.setLocationPermission(locationPermission);
                    if(state == ZoneEditorState.AddLocation){
                        if(locationPermission != LocationStates.DISABLE){
                            locationHelperPoint.getLastKnownLocation();
                            locationHelperPoint.start();
                        }else{
                            onStateUpdate(ZoneEditorState.NormalState);
                        }
                    }
                }
            }
    );

    private void initData(){
        positionMarker = null;
        locationPointWasRunning = false;

        land = null;
        zone = null;
        otherZones = new ArrayList<>();

        title = "";
        note = "";
        color = AppValues.defaultZoneColor;
        border = new ArrayList<>();
        zonePoints = new ArrayList<>();

        doubleBackToExit = false;
        mapLoaded = false;
        forceBack = false;
        showNote = false;
        isSaving = false;
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
        if(zone != null && zone.getData() != null){
            LandZoneData data = new LandZoneData(zone.getData());
            zone = new LandZone(data);
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
            }
            loadingDialog = new LoadingDialog(getActivity());
            locationHelperPoint = new LocationHelper(getActivity(),this::onLocationUpdate);
        }
    }

    private void initFragment(){
        binding.ibClose.setOnClickListener( v -> goBack());
        binding.ibEditMenu.setOnClickListener(v->{
            onStateUpdate(ZoneEditorState.NormalState);
            toggleDrawer(true);
        });
        binding.navZoneMenu.setNavigationItemSelectedListener(this::onMenuClick);
        binding.ibUndo.setOnClickListener(v->undo());
        binding.btnClearState.setOnClickListener(v-> onStateUpdate(ZoneEditorState.NormalState));
        binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.ibToggleNote.setOnClickListener(v->toggleNote());
        updateUI();
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST,r->binding.mvZonePreview.getMapAsync(this::initMap));
    }

    private void initViewModel(){
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }

    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(AppValues.minZoom);
        mMap.setMaxZoomPreference(AppValues.maxZoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.075368, 23.553767),16));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        binding.ibCenterCamera.setOnClickListener(v-> {
            if(state != ZoneEditorState.AddLocation){
                zoomOnLand();
            }else{
                if(positionMarker != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            positionMarker.getPosition(),
                            mMap.getCameraPosition().zoom
                    ));
                }else{
                    zoomOnLand();
                }
            }
        });
        zoomOnLand();
        drawLand();
        mMap.setOnMapLoadedCallback(this::MapLoaded);
    }

    @SuppressLint("PotentialBehaviorOverride")
    private void MapLoaded() {
        mapLoaded = true;
        mMap.setOnMapClickListener(this::onMapClick);
        mMap.setOnMarkerClickListener(marker -> {
            onMapClick(marker.getPosition());
            return true;
        });
        initObservers();
    }

    private void initObservers(){
        if(vmLands != null){
            vmLands.getLandZones().observe(getViewLifecycleOwner(),this::onZonesUpdate);
        }
    }

    private void initDrawMap(){
        if(mMap != null) mMap.clear();
        drawLand();
        drawOtherZones();
        updateUI();
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void drawLand(){
        if(mMap == null) return;
        if(land == null) return;
        zoomOnLand();
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
        }
    }

    private void drawOtherZones(){
        if(mMap == null) return;
        int strokeColor;
        int fillColor;
        PolygonOptions options;
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
        String sb = null;
        toggleDrawer(false);
        if(needSave()){
            if(isValidSave() && getActivity() != null){
                isSaving = true;
                binding.ibEditMenu.setEnabled(false);
                AsyncTask.execute(()->{
                    if(loadingDialog != null) loadingDialog.openDialog();
                    List<LatLng> temp = new ArrayList<>(MapUtil.getBiggerAreaZoneIntersections(border,land.getData().getBorder()));
                    border.clear();
                    border.addAll(temp);
                    if(border.size() != 0){
                        for(LandZone zone : otherZones){
                            if(zone == null || zone.getData() == null || zone.getData().getBorder() == null) continue;
                            if(MapUtil.containsAll(border,zone.getData().getBorder())){
                                border.clear();
                                break;
                            }
                            temp.clear();
                            temp.addAll(MapUtil.getBiggerAreaZoneDifference(border,zone.getData().getBorder()));
                            border.clear();
                            border.addAll(temp);
                            if(border.size() == 0) break;
                        }
                    }
                    if(border.size() != 0){
                        for(List<LatLng> hole : land.getData().getHoles()){
                            if(hole == null) continue;
                            if(MapUtil.containsAll(border,hole)){
                                border.clear();
                                break;
                            }
                            List<LatLng> tempBorder = new ArrayList<>(MapUtil.getBiggerAreaZoneDifference(border,hole));
                            border.clear();
                            border.addAll(tempBorder);
                            if(border.size() == 0) break;
                        }
                    }
                    String snackDisplay;
                    if(isValidSave()){
                            if(zone != null){
                                zone.getData().setTitle(title);
                                zone.getData().setNote(note);
                                zone.getData().setColor(color);
                                zone.getData().setBorder(border);
                            }else{
                                zone = new LandZone(new LandZoneData(land.getData().getSnapshot(),land.getData().getId(),title,note,color,border));
                            }
                            try{
                                vmLands.saveZone(zone);
                                snackDisplay = getString(R.string.zone_saved);
                            }catch (Exception e){
                                Log.e(TAG, "saveZone: ", e);
                                snackDisplay = getString(R.string.zone_null_error);
                            }
                    }else{
                        snackDisplay = getString(R.string.zone_no_valid);
                    }
                    showSnackBar(snackDisplay);
                    getActivity().runOnUiThread(()->{
                        updateMap();
                        binding.ibEditMenu.setEnabled(true);
                        isSaving = false;
                        if(loadingDialog != null) loadingDialog.closeDialog();
                    });
                });
            }else{
                sb =  getString(R.string.zone_no_valid);
            }
        }else{
            sb = getString(R.string.zone_no_need_save);
        }
        if(sb != null){
            showSnackBar(sb);
        }
    }

    private boolean needSave() {
        if(zone != null){
            if(!zone.getData().getTitle().equals(title)){
                return true;
            }
            if(!zone.getData().getNote().equals(note)){
                return true;
            }
            if(!zone.getData().getColor().toString().equals(color.toString())){
                return true;
            }
            if(zone.getData().getBorder().size() == border.size()){
                for(int i = 0; i < border.size(); i++){
                    if(zone.getData().getBorder().get(i) != border.get(i)) {
                        return true;
                    }
                }
                return false;
            }else{
                return true;
            }
        }
        return true;
    }

    private boolean isValidSave() {
        String error = null;
        if(title.trim().isEmpty()){
            showTitleDialog();
            error = getString(R.string.title_empty_error);
        }else if(land == null){
            error = getString(R.string.zone_null_error);
        }else if(border.size()<=2){
            error = getString(R.string.zone_border_error);
        }else if(vmLands == null){
            error = getString(R.string.zone_null_error);
        }

        if(error != null){
            showSnackBar(error);
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
                    .setIcon(R.drawable.ic_menu_edit)
                    .setTitle(getString(R.string.zone_title_label))
                    .setView(R.layout.view_edit_text)
                    .setPositiveButton(getString(R.string.submit),null)
                    .setNegativeButton(getString(R.string.cancel),(d, w)-> {
                        d.cancel();
                        if(zone == null && title.isEmpty()){
                            forceBack = true;
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
                            if(getContext() != null){
                                showSnackBar(getString(R.string.title_empty_error));
                            }else{
                                dialog.dismiss();
                            }
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
                    .setIcon(R.drawable.ic_menu_edit)
                    .setTitle(getString(R.string.zone_note_label))
                    .setView(R.layout.view_text_area)
                    .setPositiveButton(getString(R.string.submit),null)
                    .setNegativeButton(getString(R.string.cancel), (d, w)-> d.cancel())
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
                        String bar = null;
                        if(DataUtil.lineCount(note)<=2){
                            if(note.length()<=100){
                                this.note = note;
                                updateUI();
                                dialog.dismiss();
                            }else{
                                bar = getString(R.string.note_max_char_error);
                            }
                        }else{
                            bar = getString(R.string.note_max_line_error);
                        }
                        if(bar != null){
                            showSnackBar(bar);
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
                    .setPositiveButton(getString(R.string.submit),(d, w) -> {
                        color = new ColorData(
                                tempColor.getRed(),
                                tempColor.getGreen(),
                                tempColor.getBlue()
                        );
                        updateMap();
                        d.dismiss();
                    })
                    .setNegativeButton(getString(R.string.cancel),(d, w) -> d.cancel())
                    .show();

            DialogUtil.setupColorDialog(dialog, tempColor);

        }
    }

    private boolean onMenuClick(MenuItem item) {
        if( !mapLoaded ) return false;
        switch (item.getItemId()){
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
            case (R.id.toolbar_action_zone_add_point_location):
                onStateUpdate(ZoneEditorState.AddLocation);
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
        showNote = !showNote;
        if(showNote){
            setToolbarTitle(binding.tvTitle.getText().toString(), note);
        }else{
            setToolbarTitle(binding.tvTitle.getText().toString());
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
            if(this.state == ZoneEditorState.AddLocation){
                locationHelperPoint.stop();
                cleanPositionMarker();
            }
            this.state = state;
            clearUndo();
            clearIndexBetweenPoint();
            updateUI();
            toggleDrawer(false);
            if(state == ZoneEditorState.AddLocation){
                if(locationHelperPoint.getLocationPermission() == LocationStates.DISABLE){
                    locationPermissionRequest.launch(new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                }else{
                    locationHelperPoint.getLastKnownLocation();
                    locationHelperPoint.start();
                }
            }
        }
    }

    private void onLocationUpdate(Location location){
        if(getActivity() != null && location != null){
            getActivity().runOnUiThread(()->
                    drawPositionMarker(new LatLng(location.getLatitude(),location.getLongitude()))
            );
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
            case AddLocation:
                if(positionMarker != null){
                    onAddPoint(positionMarker.getPosition());
                    doMapUpdate = true;
                }
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
        addPointsToUndo();
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
                addPointsToUndo();
                border.set(index,point);
            }
        }
    }

    private void onDeletePoint(LatLng point) {
        int index = MapUtil.closestPoint(border,point);
        if(index > -1 && index < border.size()){
            if(AppValues.distanceToMapActionKM >= MapUtil.distanceBetween(border.get(index),point)){
                addPointsToUndo();
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
        addPointsToUndo();
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
        addPointsToUndo();
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

    private void drawPositionMarker(LatLng currLocation) {
        if(positionMarker == null){
            positionMarker = mMap.addMarker(new MarkerOptions()
                    .position(currLocation)
                    .draggable(false)
            );
        }else{
            positionMarker.setPosition(currLocation);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                currLocation,
                mMap.getCameraPosition().zoom
        ));
    }

    private void cleanPositionMarker(){
        if(positionMarker != null) positionMarker.remove();
        positionMarker = null;

        if(land == null) return;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int size = 0;
        for(LatLng point: land.getData().getBorder()){
            builder.include(point);
            size++;
        }
        for(LatLng point: border){
            builder.include(point);
            size++;
        }
        if(size > 0){
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPadding
            ));
        }
    }

    private void updateUI() {
        updateUIBasedOnState();
        if(note.isEmpty()){
            binding.ibToggleNote.setVisibility(View.GONE);
        }else{
            binding.ibToggleNote.setVisibility(View.VISIBLE);
            if(showNote){
                setToolbarTitle(binding.tvTitle.getText().toString(), note);
            }else{
                setToolbarTitle(binding.tvTitle.getText().toString());
            }
        }
        if(mMap != null) {
            updateMap();
        }
        if( zone == null && title.isEmpty() && mapLoaded ){
            showTitleDialog();
        }
        if( state == ZoneEditorState.NormalState){
            binding.clControl.setVisibility(View.GONE);
        }else{
            binding.clControl.setVisibility(View.VISIBLE);
        }
    }

    private void updateUIBasedOnState() {
        switch (state){
            case AddPoint:
                setToolbarTitle(getString(R.string.add_point));
                break;
            case AddLocation:
                setToolbarTitle(getString(R.string.add_by_location));
                break;
            case AddBetweenPoint:
                String display;
                if(index1 != -1){
                    if(index2 != -1){
                        if(index3 != -1){
                            display = getString(R.string.add_between_points_edit_new_point);
                        }else{
                            display = getString(R.string.add_between_points_place_new_point);
                        }
                    }else{
                        display = getString(R.string.add_between_points_select_point_2);
                    }
                }else{
                    display = getString(R.string.add_between_points_select_point_1);
                }
                setToolbarTitle(display);
                break;
            case EditPoint:
                setToolbarTitle(getString(R.string.edit_point));
                break;
            case DeletePoint:
                setToolbarTitle(getString(R.string.delete_point));
                break;
            case NormalState:
            default:
                if(zone != null){
                    setToolbarTitle("#"+ zone.getData().getId()+" "+title);
                }else{
                    if(!title.trim().isEmpty()){
                        setToolbarTitle(title);
                    }else{
                        setToolbarTitle(getString(R.string.new_zone_bar_label));
                    }
                }
                break;
        }
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

                zonePolygon = mMap.addPolygon(new PolygonOptions().addAll(border).clickable(false).strokeColor(strokeColor).fillColor(fillColor).zIndex(3));
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

    private void undo(){
        border.clear();
        border.addAll(undoList.get(undoList.size()-1));
        if(undoList.size() > 1){
            undoList.remove(undoList.size()-1);
        }
        if(state == ZoneEditorState.AddBetweenPoint && undoList.size() == 1){
            clearIndexBetweenPoint();
        }
        updateUI();
    }

    private void addPointsToUndo(){
        undoList.add(new ArrayList<>(border));
    }

    private void clearUndo(){
        if(undoList == null){
            undoList = new ArrayList<>();
        }else{
            undoList.clear();
        }
        undoList.add(new ArrayList<>(border));
    }

    private void clearIndexBetweenPoint(){
        index1 = -1;
        index2 = -1;
        index3 = -1;
    }

    private void setToolbarTitle(String title){
        binding.tvTitle.setText(title);
        binding.tvNote.setVisibility(View.GONE);
        binding.tvNote.setText("");
    }

    private void setToolbarTitle(String title, String subTitle){
        binding.tvTitle.setText(title);
        binding.tvNote.setVisibility(View.VISIBLE);
        binding.tvNote.setText(subTitle);
    }

    private void showSnackBar(String text){
        Log.d(TAG, "showSnackBar: "+text);
        Snackbar s = Snackbar.make(binding.clSnackBarContainer, text, Snackbar.LENGTH_LONG);
        s.setAction(getString(R.string.okey),view -> {});
        s.show();
    }

    private void goBack(){
        forceBack = true;
        if(getActivity() != null)
            getActivity().onBackPressed();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        binding.mvZonePreview.onDestroy();
        super.onDestroyView();
        binding = null;
    }

    @Override public void onResume() {
        super.onResume();
        binding.mvZonePreview.onResume();
        if(locationPointWasRunning){
            locationPointWasRunning = false;
            locationHelperPoint.start();
        }
    }

    @Override public void onPause() {
        if(locationHelperPoint != null){
            if(locationHelperPoint.isRunning()){
                locationPointWasRunning = true;
                locationHelperPoint.stop();
            }
        }
        binding.mvZonePreview.onPause();
        super.onPause();
    }

    @Override public void onLowMemory() {
        binding.mvZonePreview.onLowMemory();
        super.onLowMemory();
    }

    @Override public boolean onBackPressed() {
        if(forceBack) return true;
        if(isSaving) return false;
        if (state != ZoneEditorState.NormalState) {
            onStateUpdate(ZoneEditorState.NormalState);
            return false;
        }
        if(needSave()){
            if (doubleBackToExit) {
                if(dialog != null){
                    dialog.dismiss();
                }
                return true;
            }
            doubleBackToExit = true;
            new Handler().postDelayed(() -> doubleBackToExit=false, AppValues.doubleTapBack);

            CharSequence display;
            if(zone != null){
                display = getText(R.string.zone_need_save_edit);
            }else{
                display = getText(R.string.zone_need_save_new);
            }
            showSnackBar(display.toString());
            return false;
        }
        if(dialog != null){
            dialog.dismiss();
        }
        return true;
    }
}