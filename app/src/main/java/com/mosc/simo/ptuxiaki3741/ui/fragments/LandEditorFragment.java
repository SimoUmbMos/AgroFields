package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.thuytrinh.android.collageviews.MultiTouchListener;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandMapBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.ImportAction;
import com.mosc.simo.ptuxiaki3741.data.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.data.enums.LandActionStates;
import com.mosc.simo.ptuxiaki3741.data.enums.LocationStates;
import com.mosc.simo.ptuxiaki3741.data.helpers.LocationHelper;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.data.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

public class LandEditorFragment extends Fragment implements FragmentBackPress, View.OnTouchListener {
    public static final String TAG = "LandMapEditorFragment";
    private boolean mapIsLocked = false, beforeMoveWasLocked = false;
    private int index1, index2, index3;

    private LandActionStates mapStatus;
    private LandFileState fileState;
    private ImportAction importAction;

    private FragmentLandMapBinding binding;
    private GoogleMap mMap;
    private AlertDialog dialog;
    private LoadingDialog loadingDialog;

    private LocationHelper locationHelperCamera, locationHelperPoint;
    private Marker positionMarker;

    private List<LatLng> points,startPoints;
    private List<List<LatLng>> holes,startHoles;
    private List<List<LatLng>> undoList;
    private String address;
    private LatLng startZoomLocation;
    private float startZoomLevel;
    private boolean currLocation, locationPointWasRunning, cameraInit, isSaving;
    private Land currLand;
    private ColorData color, tempColor;
    private String displayTitle;

    //ActivityResultLauncher relative
    private final ActivityResultLauncher<String> permissionReadChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onFilePermissionResult
    );
    @SuppressLint("MissingPermission")
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

                if(cameraInit){
                    cameraInit = false;
                    if(points.size() == 0 && currLocation && locationHelperCamera != null){
                        locationHelperCamera.setLocationPermission(locationPermission);
                        locationHelperCamera.getLastKnownLocation();
                    }
                }
                if(locationHelperPoint != null){
                    locationHelperPoint.setLocationPermission(locationPermission);
                    if(mapStatus == LandActionStates.AddLocation){
                        if(locationPermission != LocationStates.DISABLE){
                            locationHelperPoint.getLastKnownLocation();
                            locationHelperPoint.start();
                        }else{
                            setAction(LandActionStates.Disable);
                        }
                    }
                }
            }
    );

    private void onFilePermissionResult(boolean permissionsResult) {
        toggleDrawer(false);
        if (permissionsResult) {
            String title;
            if(fileState == LandFileState.Img){
                title = getString(R.string.file_img_picker_label);
            }else{
                title = getString(R.string.file_picker_label);
            }
            Intent intent = FileUtil.getFilePickerIntent(fileState, title);
            switch (fileState) {
                case Img:
                    imgFileLauncher.launch(intent);
                    break;
                case File_Import:
                case File_Add:
                case File_Subtract:
                    fileLauncher.launch(intent);
                    break;
            }
        }
        fileState = LandFileState.Disable;
    }
    private final ActivityResultLauncher<Intent> imgFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                Log.d(TAG, "imgFileLauncher: called");
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (FileUtil.fileIsValidImg(getContext(), result.getData())) {
                        addOverlayImg(result.getData().getData());
                    }
                }
            }
    );
    private final ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                Log.d(TAG, "fileLauncher: called");
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    new Thread(() -> {
                        ArrayList<LandData> data = FileUtil.handleFile(
                                getContext(),
                                result.getData()
                        );
                        if (data.size() > 0) {
                            Bundle args = new Bundle();
                            args.putParcelableArrayList(
                                    AppValues.argLands,
                                    data
                            );
                            args.putParcelable(
                                    AppValues.argLand,
                                    currLand.getData()
                            );
                            args.putSerializable(
                                    AppValues.argAction,
                                    importAction
                            );
                            toImport(getActivity(), args);
                        }
                    }).start();
                }
            }
    );

    //init relative
    private boolean initData(){
        points = new ArrayList<>();
        holes = new ArrayList<>();
        startPoints = new ArrayList<>();
        startHoles = new ArrayList<>();
        startZoomLocation = null;
        startZoomLevel = AppValues.countryZoom;
        locationPointWasRunning = false;
        isSaving = false;

        if(getArguments() == null) return false;
        if(getArguments().containsKey(AppValues.argLand)) {
            currLand = getArguments().getParcelable(AppValues.argLand);
        }else{
            return false;
        }
        if(currLand == null || currLand.getData() == null){
            return false;
        }

        if(getArguments().containsKey(AppValues.argAddress)){
            address = getArguments().getString(AppValues.argAddress);
            currLocation = false;
        }else{
            address = null;
            currLocation = true;
        }

        displayTitle = currLand.getData().toString();

        points.addAll(currLand.getData().getBorder());
        if(points.size()>1)
            if(points.get(0).equals(points.get(points.size()-1)))
                points.remove(points.size()-1);

        startPoints.addAll(points);

        holes.addAll(currLand.getData().getHoles());
        for(List<LatLng> hole : holes){
            if(hole.size()>1)
                if(hole.get(0).equals(hole.get(hole.size()-1)))
                    hole.remove(hole.size()-1);
        }
        startHoles.addAll(holes);

        color = currLand.getData().getColor();

        mapStatus = LandActionStates.Disable;
        fileState = LandFileState.Disable;
        importAction = ImportAction.NONE;
        return true;
    }
    private void initActivity() {
        if(getActivity() == null) return;
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setOnBackPressed(this);
        loadingDialog = mainActivity.getLoadingDialog();
        locationHelperCamera = new LocationHelper(getActivity(),this::moveCameraOnLocation);
        locationHelperPoint = new LocationHelper(getActivity(), this::onLocationUpdate);
    }
    private void initViews() {
        clearFlags();
        clearUndo();

        binding.ibClose.setOnClickListener( v -> goBack() );
        binding.ibSave.setOnClickListener( v -> saveLand() );
        binding.ibToggleMenu.setOnClickListener(v -> toggleDrawer(true) );

        binding.clLandControls.setVisibility(View.GONE);
        binding.ivLandOverlay.setVisibility(View.GONE);
        Log.d(TAG, "ivLandOverlay: GONE");
        MenuItem resetAll = binding.navLandMenu.getMenu().findItem(R.id.toolbar_action_clean);
        if(startPoints.size() > 0){
            resetAll.setTitle(getString(R.string.reset_points));
        }else{
            resetAll.setTitle(getString(R.string.clear_points));
        }
        clearTitle();
        setupSideMenu();

        binding.navLandMenu.setNavigationItemSelectedListener(this::menuItemClick);
        binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.ibActionSave.setOnClickListener(v->saveAction());
        binding.ibActionReset.setOnClickListener(v->undo());
        binding.slAlphaSlider.addOnChangeListener((range,value,user) -> onSliderUpdate(value));

        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST,r->binding.mvLand.getMapAsync(this::initMap));
    }
    @SuppressLint("PotentialBehaviorOverride")
    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        cameraInit = true;
        mMap.setMinZoomPreference(AppValues.minZoom);
        mMap.setMaxZoomPreference(AppValues.maxZoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.075368, 23.553767),16));
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapClickListener(this::processClick);
        mMap.setOnMarkerClickListener(marker -> {
            processClick(marker.getPosition());
            return true;
        });
        if(points.size() > 0){
            cameraInit = false;
            drawMap();
            zoomOnPoints();
        }else{
            initLocation();
        }
    }
    private void initLocation() {
        if(!currLocation){
            cameraInit = false;
            moveCameraOnLocation();
        }else{
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    //menu relative
    private boolean menuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case (R.id.toolbar_action_toggle_map_lock):
                toggleMapLock();
                return true;
            case (R.id.toolbar_action_toggle_map_road_label):
                changeMapType();
                return true;
            case (R.id.toolbar_action_edit_land_info):
                toInfo(getActivity());
                return true;
            case (R.id.toolbar_action_edit_land_color):
                showColorDialog();
                return true;
            case (R.id.toolbar_action_add_on_end):
                setAction(LandActionStates.AddEnd);
                return true;
            case (R.id.toolbar_action_add_between):
                setAction(LandActionStates.AddBetween);
                return true;
            case (R.id.toolbar_action_add_location):
                setAction(LandActionStates.AddLocation);
                return true;
            case (R.id.toolbar_action_edit):
                setAction(LandActionStates.Edit);
                return true;
            case (R.id.toolbar_action_delete):
                setAction(LandActionStates.Delete);
                return true;
            case (R.id.toolbar_action_clean):
                setAction(LandActionStates.ResetAll);
                return true;
            case (R.id.toolbar_action_add_img):
                toImportImg();
                return true;
            case (R.id.toolbar_action_remove_img):
                removeOverlayImg();
                return true;
            case (R.id.toolbar_action_edit_img):
                setAction(LandActionStates.EditImg);
                return true;
            case (R.id.toolbar_action_import):
                toImportFile();
                return true;
            case (R.id.toolbar_action_add_to_land):
                toImportAddFile();
                return true;
            case (R.id.toolbar_action_remove_from_land):
                toImportSubtractFile();
                return true;
            case (R.id.toolbar_action_delete_land):
                deleteLand();
                return true;
            case(R.id.toolbar_action_make_land_editable):
                makeLandEditable();
                return true;
            default:
                toggleDrawer(false);
                return false;
        }
    }

    //save relative
    private void saveLand() {
        Log.d(TAG, "saveLand: called");
        binding.getRoot().closeDrawer(GravityCompat.END);
        if(isValidToSave()){
            Log.d(TAG, "isValidToSave: true");
            binding.ibSave.setEnabled(false);
            binding.ibToggleMenu.setEnabled(false);
            isSaving = true;
            if( getActivity() == null) return;
            AsyncTask.execute(()->{
                if(loadingDialog != null) loadingDialog.openDialog();
                addToVM(getActivity());
                if(loadingDialog != null) loadingDialog.closeDialog();
                toMenu(getActivity());
            });
        }else{
            Log.d(TAG, "isValidToSave: false");
        }
    }
    private boolean isValidToSave() {
        return points.size() > 2 &&
                currLand != null &&
                currLand.getData() != null;
    }
    private void addToVM(@NonNull ViewModelStoreOwner activity) {
        Log.d(TAG, "addToVM: called");
        currLand.getData().setBorder(points);
        currLand.getData().setHoles(holes);
        currLand.getData().setColor(color);
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(activity).get(AppViewModel.class);
            vmLands.saveLand(currLand);
            Log.d(TAG, "vmLands.saveLand: called");
        }
    }

    //delete relative
    private void deleteLand(){
        binding.getRoot().closeDrawer(GravityCompat.END,false);
        showDeleteLandDialog();
    }
    private void showDeleteLandDialog(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Error)
                    .setIcon(R.drawable.ic_menu_delete)
                    .setTitle(getString(R.string.delete_land_title))
                    .setMessage(getString(R.string.delete_land_text))
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> deleteLandActions())
                    .create();
            dialog.show();
        }
    }
    private void deleteLandActions(){
        AsyncTask.execute(()->{
            if(removeFromVM()){
                toMenu(getActivity());
            }
        });
    }
    private boolean removeFromVM() {
        if(getActivity() == null) return false;
        if(currLand.getData().getId() <= 0) return true;
        AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        return vmLands.removeLand(currLand);
    }

    //file relative
    public void importFile(LandFileState state) {
        fileState = state;
        switch (fileState){
            case File_Import:
                importAction = ImportAction.IMPORT;
                break;
            case File_Add:
                importAction = ImportAction.ADD;
                break;
            case File_Subtract:
                importAction = ImportAction.SUBTRACT;
                break;
            default:
                importAction = ImportAction.NONE;
                break;
        }
        permissionReadChecker.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    //map relative
    private void drawMap() {
        LatLng currPosition = null;
        if(positionMarker != null){
            currPosition = positionMarker.getPosition();
        }
        mMap.clear();
        LandData tempLandData = new LandData(color,points,holes);
        int strokeColor = Color.argb(
                AppValues.defaultStrokeAlpha,
                tempLandData.getColor().getRed(),
                tempLandData.getColor().getGreen(),
                tempLandData.getColor().getBlue()
        );
        int fillColor = Color.argb(
                AppValues.defaultFillAlpha,
                tempLandData.getColor().getRed(),
                tempLandData.getColor().getGreen(),
                tempLandData.getColor().getBlue()
        );
        PolygonOptions options = LandUtil.getPolygonOptions(
                tempLandData,
                strokeColor,
                fillColor,
                false
        );
        if(options != null){
            mMap.addPolygon(options.zIndex(AppValues.liveMapLandZIndex));
            int pointsNumber = 0;
            for(List<LatLng> hole : holes){
                pointsNumber = pointsNumber + hole.size();
            }
            pointsNumber = pointsNumber + points.size();
            if(pointsNumber<100 && holes.size() == 0){
                for(LatLng point : points){
                    mMap.addCircle(new CircleOptions()
                            .center(point)
                            .radius(10)
                            .fillColor(strokeColor)
                            .strokeColor(strokeColor)
                            .zIndex(AppValues.liveMapZoneZIndex)
                            .clickable(false)
                    );
                }
                for(List<LatLng> hole : holes){
                    for(LatLng point : hole){
                        mMap.addCircle(new CircleOptions()
                                .center(point)
                                .radius(10)
                                .fillColor(strokeColor)
                                .strokeColor(strokeColor)
                                .zIndex(AppValues.liveMapZoneZIndex)
                                .clickable(false)
                        );
                    }
                }
            }
        }
        if(currPosition != null){
            Drawable bitmapDraw;
            try{
                bitmapDraw = ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.bg_location_marker);
            }catch (Exception e){
                bitmapDraw = null;
            }
            if(bitmapDraw != null){
                Bitmap b = UIUtil.drawableToBitmap(bitmapDraw);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 36, 36, false);
                positionMarker = mMap.addMarker(new MarkerOptions()
                        .position(currPosition)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        .zIndex(AppValues.liveMapMyLocationZIndex)
                        .draggable(false)
                );
            }else{
                positionMarker = mMap.addMarker(new MarkerOptions()
                        .position(currPosition)
                        .zIndex(AppValues.liveMapMyLocationZIndex)
                        .draggable(false)
                );
            }
        }
    }
    private void drawPositionMarker(LatLng currLocation) {
        if(positionMarker == null){
            Drawable bitmapDraw;
            try{
                bitmapDraw = ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.bg_location_marker);
            }catch (Exception e){
                bitmapDraw = null;
            }
            if(bitmapDraw != null){
                Bitmap b = UIUtil.drawableToBitmap(bitmapDraw);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 36, 36, false);
                positionMarker = mMap.addMarker(new MarkerOptions()
                        .position(currLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        .zIndex(AppValues.liveMapMyLocationZIndex)
                        .draggable(false)
                );
            }else{
                positionMarker = mMap.addMarker(new MarkerOptions()
                        .position(currLocation)
                        .zIndex(AppValues.liveMapMyLocationZIndex)
                        .draggable(false)
                );
            }
        }else{
            positionMarker.setPosition(currLocation);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                currLocation,
                mMap.getCameraPosition().zoom
        ));
    }
    private void cleanPositionMarker(){
        if(points.size()>0){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng point : points){
                builder.include(point);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPadding
            ));
        }else if(startZoomLocation != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startZoomLocation,startZoomLevel));
        }
        startZoomLocation = null;
        if(positionMarker != null){
            positionMarker.remove();
            positionMarker = null;
        }
    }
    private void zoomOnPoints() {
        if(points.size() <= 0) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : points){
            builder.include(point);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                builder.build(),
                AppValues.defaultPadding
        ));
    }
    private void changeMapType() {
        MenuItem miAction = binding.navLandMenu.getMenu()
                .findItem(R.id.toolbar_action_toggle_map_road_label);
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            miAction.setTitle(getString(R.string.hide_road_name));
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            miAction.setTitle(getString(R.string.show_road_name));
        }
        if(this.mapStatus != LandActionStates.Disable){
            setAction(LandActionStates.Disable);
        }
        binding.getRoot().closeDrawer(GravityCompat.END);
    }
    private void toggleMapLock(){
        if(mMap == null) return;
        MenuItem miLock = binding.navLandMenu.getMenu()
                .findItem(R.id.toolbar_action_toggle_map_lock);
        mMap.getUiSettings().setRotateGesturesEnabled(mapIsLocked);
        mMap.getUiSettings().setScrollGesturesEnabled(mapIsLocked);
        mMap.getUiSettings().setZoomGesturesEnabled(mapIsLocked);
        mMap.getUiSettings().setZoomControlsEnabled(mapIsLocked);
        mMap.getUiSettings().setCompassEnabled(mapIsLocked);
        if(mapIsLocked){
            miLock.setIcon(R.drawable.ic_drawer_menu_unlocked);
        }else{
            miLock.setIcon(R.drawable.ic_drawer_menu_map_locked);
        }
        mapIsLocked = !mapIsLocked;
        if(this.mapStatus != LandActionStates.Disable && !isImgAction(mapStatus)){
            setAction(LandActionStates.Disable);
        }
        binding.getRoot().closeDrawer(GravityCompat.END);
    }
    private void processClick(LatLng latLng) {
        switch (mapStatus){
            case AddEnd:
                addPointToEnd(latLng);
                drawMap();
                break;
            case AddBetween:
                addBetweenPoint(latLng);
                drawMap();
                break;
            case AddLocation:
                if(positionMarker != null){
                    addPointToEnd(positionMarker.getPosition());
                    drawMap();
                }
                break;
            case Edit:
                editPoint(latLng);
                drawMap();
                break;
            case Delete:
                deletePoint(latLng);
                drawMap();
                break;
        }
    }
    private void addPointToEnd(LatLng latLng) {
        addPointsToUndo();
        points.add(latLng);
    }
    private void editPoint(LatLng latLng) {
        int index = MapUtil.closestPoint(points,latLng);
        if(index > -1 && index < points.size()){
            if(AppValues.distanceToMapActionKM >= MapUtil.distanceBetween(points.get(index),latLng)){
                addPointsToUndo();
                points.set(index,latLng);
            }
        }
    }
    private void deletePoint(LatLng latLng) {
        int index = MapUtil.closestPoint(points,latLng);
        if(index > -1 && index < points.size()){
            if(AppValues.distanceToMapActionKM >= MapUtil.distanceBetween(points.get(index),latLng)){
                addPointsToUndo();
                points.remove(index);
            }
        }
    }
    private void addBetweenPoint(LatLng latLng) {
        if(index1 < 0)
            selectFirstPointForBetween(latLng);
        else if(index2 < 0)
            selectSecondPointForBetween(latLng);
        else if(index3 < 0)
            placePointBetween(latLng);
        else
            editPointBetween(latLng);
        changeTitleBasedOnState();
    }
    private void selectFirstPointForBetween(LatLng latLng) {
        index1 = MapUtil.closestPoint(points,latLng);
        if(index1 > -1 && index1 < points.size()){
            if(MapUtil.distanceBetween(latLng,points.get(index1)) > AppValues.distanceToMapActionKM){
                index1 = -1;
            }
        }else{
            index1 = -1;
        }
    }
    private void selectSecondPointForBetween(LatLng latLng) {
        index2 = MapUtil.closestPoint(points,latLng);
        if(index2 > -1 && index2 < points.size()){
            if(MapUtil.distanceBetween(latLng,points.get(index2)) < AppValues.distanceToMapActionKM){
                checkIndexBetween();
            }else{
                index2 = -1;
            }
        }else{
            index2 = -1;
        }
    }
    private void placePointBetween(LatLng latLng) {
        addPointsToUndo();
        if(index2 == (points.size() - 1)){
            if(index1 == 0){
                index3=points.size();
                points.add(latLng);
            }else{
                index3=index2;
                index2++;
                points.add(index3,latLng);
            }
        }else{
            index3=index2;
            index2++;
            points.add(index3,latLng);
        }
    }
    private void editPointBetween(LatLng latLng) {
        addPointsToUndo();
        points.set(index3,latLng);
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
            }else if(index1 == 0 && index2 == (points.size() - 1)){
                reset = false;
            }else if(index1 == (points.size() - 1) && index2 == 0){
                int tempIndex = index1;
                index1 = index2;
                index2 = tempIndex;
                reset = false;
            }
        }
        if(reset)
            clearFlags();
    }
    private void onLocationUpdate(Location location){
        if(getActivity() != null && location != null){
            getActivity().runOnUiThread(()->drawPositionMarker(new LatLng(location.getLatitude(),location.getLongitude())));
        }
    }

    //action relative
    private void setAction(LandActionStates mapStatus){
        Log.d(TAG, "setAction: "+mapStatus.name());
        if(this.mapStatus != mapStatus){
            if(this.mapStatus == LandActionStates.AddLocation){
                cleanPositionMarker();
            }else if(isImgAction(this.mapStatus) && beforeMoveWasLocked){
                toggleMapLock();
                beforeMoveWasLocked = false;
            }
            this.mapStatus = mapStatus;
            if(mapStatus != LandActionStates.Disable){
                toggleDrawer(false);
            }
            clearFlags();
            clearUndo();
            if(isImgAction(mapStatus)){
                if(isImgViewEnable()){
                    setTitle(getString(R.string.edit_img));
                }else{
                    setAction(LandActionStates.Disable);
                }
            }else{
                switch (mapStatus){
                    case AddEnd:
                        setTitle(getString(R.string.add_point));
                        break;
                    case AddBetween:
                        setTitle(getString(R.string.add_between_points));
                        changeTitleBasedOnState();
                        break;
                    case AddLocation:
                        if(mMap != null){
                            startZoomLocation = mMap.getCameraPosition().target;
                            startZoomLevel = mMap.getCameraPosition().zoom;
                        } else {
                            setAction(LandActionStates.Disable);
                            return;
                        }
                        setTitle(getString(R.string.add_by_location));
                        if(locationHelperPoint.getLocationPermission() == LocationStates.DISABLE){
                            locationPermissionRequest.launch(new String[] {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            });
                        }else{
                            locationHelperPoint.getLastKnownLocation();
                            locationHelperPoint.start();
                        }
                        break;
                    case Edit:
                        setTitle(getString(R.string.edit_point));
                        break;
                    case Delete:
                        setTitle(getString(R.string.delete_point));
                        break;
                    case ResetAll:
                        saveAction();
                        break;
                }
            }
        }
        if(mapStatus == LandActionStates.Disable){
            clearTitle();
        }
        drawMap();
    }
    private boolean isImgViewEnable() {
        return binding.ivLandOverlay.getVisibility() == View.VISIBLE;
    }
    private void clearFlags() {
        index1 = -1;
        index2 = -1;
        index3 = -1;
        locationHelperPoint.stop();
    }
    private void saveAction(){
        Log.d(TAG, "saveAction: called");
        if(mapStatus == LandActionStates.Disable) return;
        Log.d(TAG, "saveAction: not disable");
        if(isImgAction(mapStatus) && beforeMoveWasLocked){
            Log.d(TAG, "saveAction: isImgAction && beforeMoveWasLocked");
            toggleMapLock();
            beforeMoveWasLocked = false;
        }else if(mapStatus == LandActionStates.ResetAll){
            Log.d(TAG, "saveAction: ResetAll");
            points.clear();
            points.addAll(startPoints);
            if(startHoles.size()>0){
                holes.clear();
                holes.addAll(startHoles);
                setupSideMenu();
            }
            drawMap();
        }
        setAction(LandActionStates.Disable);
    }
    private void undo(){
        if(isImgAction(mapStatus)){
            binding.ivLandOverlay.animate()
                    .translationX(0)
                    .translationY(0)
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotation(0)
                    .alpha(0.5f)
                    .setDuration(0).start();
            binding.slAlphaSlider.setValue(50.0f);
        }else{
            undoPoints();
        }
    }
    private void addPointsToUndo(){
        undoList.add(new ArrayList<>(points));
    }
    private void undoPoints(){
        points.clear();
        points.addAll(undoList.get(undoList.size()-1));
        if(undoList.size() > 1){
            undoList.remove(undoList.size()-1);
        }
        if(mapStatus != LandActionStates.AddLocation){
            clearFlags();
        }
        changeTitleBasedOnState();
        drawMap();
    }
    private void clearUndo(){
        if(undoList == null){
            undoList = new ArrayList<>();
        }else{
            undoList.clear();
        }
        undoList.add(new ArrayList<>(points));
    }
    private void makeLandEditable(){
        if(holes.size()>0){
            holes.clear();
            setupSideMenu();
            drawMap();
        }
    }

    //img relative
    private boolean isImgAction(LandActionStates s) {
        return s == LandActionStates.EditImg;
    }
    private void addOverlayImg(Uri data) {
        if(data == null) return;
        binding.slAlphaSlider.setValue(50.0f);
        binding.ivLandOverlay.setVisibility(View.VISIBLE);
        Log.d(TAG, "ivLandOverlay: VISIBLE");
        binding.ivLandOverlay.setImageURI(data);
        binding.ivLandOverlay.animate()
                .alpha(0.5f)
                .rotation(0)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0)
                .translationY(0)
                .setDuration(0).start();
        setupSideMenu();
    }
    private void removeOverlayImg() {
        if(isImgViewEnable()){
            binding.ivLandOverlay.setImageDrawable(null);
            binding.ivLandOverlay.setVisibility(View.GONE);
            Log.d(TAG, "ivLandOverlay: GONE");
            setupSideMenu();
        }
    }
    private void onSliderUpdate(float value){
        if(isImgViewEnable() && isImgAction(mapStatus)){
            Log.d(TAG, "onSliderUpdate: "+value);
            binding.ivLandOverlay.animate().alpha(value/100).setDuration(0).start();
        }
    }

    //ui relative
    @SuppressLint("ClickableViewAccessibility")
    private void clearTitle() {
        binding.tvTitle.setText(displayTitle);
        binding.clLandControls.setVisibility(View.GONE);
        binding.ibActionSave.setVisibility(View.GONE);
        binding.ibActionReset.setVisibility(View.GONE);
        binding.slAlphaSlider.setVisibility(View.GONE);

        if(isImgViewEnable()){
            binding.ivLandOverlay.setOnTouchListener(this);
            Log.d(TAG, "ivLandOverlay: setOnTouchListener this");
        }
        clearFlags();
        clearUndo();
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setTitle(String s) {
        binding.tvTitle.setText(s);

        binding.clLandControls.setVisibility(View.VISIBLE);
        binding.ibActionSave.setVisibility(View.VISIBLE);
        binding.ibActionReset.setVisibility(View.VISIBLE);

        if(isImgViewEnable() && isImgAction(mapStatus)){
            beforeMoveWasLocked = !mapIsLocked;
            if(!mapIsLocked){
                toggleMapLock();
            }

            binding.slAlphaSlider.setVisibility(View.VISIBLE);
            binding.ivLandOverlay.setOnTouchListener(new MultiTouchListener());
            Log.d(TAG, "ivLandOverlay: setOnTouchListener MultiTouchListener");
        }
    }
    private void changeTitleBasedOnState() {
        if(mapStatus == LandActionStates.AddBetween){
            if(index1 < 0){
                binding.tvTitle.setText(getString(R.string.add_between_points_select_point_1));
            }else if(index2 < 0){
                binding.tvTitle.setText(getString(R.string.add_between_points_select_point_2));
            }else if(index3 < 0){
                binding.tvTitle.setText(getString(R.string.add_between_points_place_new_point));
            }else{
                binding.tvTitle.setText(getString(R.string.add_between_points_edit_new_point));
            }
        }
    }
    private void toggleDrawer(boolean toggle) {
        if(binding != null){
            if(toggle){
                binding.getRoot().openDrawer(GravityCompat.END);
                if(isImgAction(mapStatus) && beforeMoveWasLocked){
                    toggleMapLock();
                    beforeMoveWasLocked = false;
                }
                if(this.mapStatus != LandActionStates.Disable){
                    setAction(LandActionStates.Disable);
                }
            }else{
                binding.getRoot().closeDrawer(GravityCompat.END);
            }
        }
    }
    private void setupSideMenu(){
        Menu tempMenu = binding.navLandMenu.getMenu();

        MenuItem deleteLand = tempMenu.findItem(R.id.toolbar_action_delete_land);
        if(deleteLand != null){
            if(currLand != null){
                deleteLand.setEnabled(currLand.getData().getId() != 0);
            }else{
                deleteLand.setEnabled(false);
            }
        }

        MenuItem editableLand = tempMenu.findItem(R.id.toolbar_action_make_land_editable);
        if(editableLand != null){
            editableLand.setEnabled(holes.size()>0);
        }

        tempMenu.findItem(R.id.toolbar_action_add_on_end).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_add_between).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_edit).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_delete).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_clean).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_remove_img).setEnabled(isImgViewEnable());
        tempMenu.findItem(R.id.toolbar_action_edit_img).setEnabled(isImgViewEnable());
    }
    private void moveCameraOnLocation() {
        if(getActivity() == null) return;
        if(address == null) return;
        if(mMap == null) return;

        Activity activity = getActivity();
        AsyncTask.execute(()->{
            Address location = MapUtil.findLocation(activity,address);
            if(location == null) return;

            final float zoom;
            if(location.getMaxAddressLineIndex()>-1){
                String s = location.getAddressLine(0);
                long count = s.chars().filter(ch -> ch == ',').count();
                if(count == 0){
                    zoom = AppValues.countryZoom;
                }else if(count == 1){
                    zoom = AppValues.cityZoom;
                }else{
                    zoom = AppValues.streetZoom;
                }
            }else{
                zoom = AppValues.countryZoom;
            }
            if(location.hasLatitude() && location.hasLongitude()){
                final LatLng pos = new LatLng(location.getLatitude(),location.getLongitude());
                activity.runOnUiThread(()-> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( pos, zoom )));
            }
        });
    }
    private void moveCameraOnLocation(Location location) {
        currLocation = false;
        if(location != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude())
                    , AppValues.streetZoom
            ));
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
                        drawMap();
                        d.dismiss();
                    })
                    .setNegativeButton(getString(R.string.cancel),(d, w) -> d.cancel())
                    .show();

            DialogUtil.setupColorDialog(dialog, tempColor);

        }
    }

    //navigation relative
    public void goBack(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(()->getActivity().onBackPressed());
    }
    public void toMenu(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()->{
                NavController nav = UIUtil.getNavController(this,R.id.MapLandEditorFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuLands);
            });
    }
    public void toInfo(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MapLandEditorFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,new Land(currLand));
                if(nav != null)
                    nav.navigate(R.id.toProfileLand,bundle);
            });
    }
    public void toImport(Activity activity, Bundle args) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MapLandEditorFragment);
                if(nav != null)
                    nav.navigate(R.id.toMapFile,args);
            });
    }
    private void toImportFile(){
        importFile(LandFileState.File_Import);
    }
    private void toImportAddFile(){
        importFile(LandFileState.File_Add);
    }
    private void toImportSubtractFile(){
        importFile(LandFileState.File_Subtract);
    }
    private void toImportImg(){
        importFile(LandFileState.Img);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        binding = FragmentLandMapBinding.inflate(inflater,container,false);
        binding.mvLand.onCreate(savedInstanceState);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        if(initData()){
            initViews();
        }else{
            toMenu(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mvLand.onResume();
        if(locationPointWasRunning){
            locationPointWasRunning = false;
            locationHelperPoint.start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mvLand.onStart();
    }

    @Override
    public void onStop() {
        binding.mvLand.onStop();
        super.onStop();
    }

    @Override
    public void onPause() {
        if(locationHelperPoint != null){
            if(locationHelperPoint.isRunning()){
                locationPointWasRunning = true;
                locationHelperPoint.stop();
            }
        }
        binding.mvLand.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        binding.mvLand.onDestroy();
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        binding.mvLand.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public boolean onBackPressed() {
        if(isSaving) return false;
        if(binding.getRoot().isDrawerOpen(GravityCompat.END)){
            binding.getRoot().closeDrawer(GravityCompat.END);
            return false;
        }
        if(mapStatus != LandActionStates.Disable){
            setAction(LandActionStates.Disable);
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.performClick();
        }
        return false;
    }
}
