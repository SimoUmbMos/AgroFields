package com.mosc.simo.ptuxiaki3741.fragments.land;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.collageviews.MultiTouchListener;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandMapBinding;
import com.mosc.simo.ptuxiaki3741.enums.ImportAction;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.enums.LandActionStates;
import com.mosc.simo.ptuxiaki3741.models.ColorData;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class MapLandEditorFragment extends Fragment implements FragmentBackPress, View.OnTouchListener {
    public static final String TAG = "LandMapEditorFragment";
    //fixme: points marker are big on zoom
    //fixme: use location helper
    private boolean mapIsLocked = false, beforeMoveWasLocked = false;
    private int index1, index2, index3;

    private LandActionStates mapStatus;
    private LandFileState fileState;
    private ImportAction importAction;

    private ActionBar actionBar;
    private FragmentLandMapBinding binding;
    private GoogleMap mMap;
    private AlertDialog dialog;

    private List<LatLng> points,startPoints;
    private List<List<LatLng>> holes,startHoles;
    private List<List<LatLng>> undoList;
    private String address;
    private boolean currLocation;
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
                    if (
                            (fineLocationGranted != null && fineLocationGranted) ||
                            (coarseLocationGranted != null && coarseLocationGranted)
                    ){
                        if(mMap != null) mMap.setMyLocationEnabled(true);
                        if(getContext() != null && currLocation){
                            LocationManager locationManager = (LocationManager) getContext()
                                    .getSystemService(Context.LOCATION_SERVICE);
                            String locationProvider = LocationManager.GPS_PROVIDER;
                            moveCameraOnLocation(locationManager.getLastKnownLocation(locationProvider));
                        }
                    }else{
                        if(mMap != null) mMap.setMyLocationEnabled(false);
                    }
            }
    );
    private void moveCameraOnLocation(Location location) {
        if(location != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    ),
                    AppValues.streetZoom
            ));
        }
    }

    private void onFilePermissionResult(boolean permissionsResult) {
        toggleDrawer(false);
        if (permissionsResult) {
            Intent intent = FileUtil.getFilePickerIntent(fileState);
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
                if (result.getResultCode() == Activity.RESULT_OK &&result.getData() != null) {
                    if (FileUtil.fileIsValidImg(getContext(), result.getData())) {
                        addOverlayImg(result.getData().getData());
                    }
                }
            }
    );
    private final ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    new Thread(() -> {
                        try {
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
                        } catch (Exception e) {
                            Log.e(TAG, "fileResult: ", e);
                        }
                    }).start();
                }
            }
    );

    private LandData handleImport() {
        if (getArguments() != null) {
            LandData landData;
            if(getArguments().containsKey(AppValues.argImportLand)){
                landData = getArguments().getParcelable(AppValues.argImportLand);
            }else{
                landData = null;
            }
            return landData;
        }
        return null;
    }

    //init relative
    private boolean initData(){
        points = new ArrayList<>();
        holes = new ArrayList<>();
        startPoints = new ArrayList<>();
        startHoles = new ArrayList<>();
        currLand = null;
        address = null;
        color = AppValues.defaultLandColor;
        currLocation = false;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLand)) {
                currLand = getArguments().getParcelable(AppValues.argLand);
            }
            if(getArguments().containsKey(AppValues.argAddress)){
                address = getArguments().getString(AppValues.argAddress);
            }else{
                currLocation = true;
            }
        }

        if(currLand == null){
            toMenu(getActivity());
            return false;
        }
        if(currLand.getData() == null){
            toMenu(getActivity());
            return false;
        }

        displayTitle = currLand.getData().getTitle();
        if(currLand.getData().getId() != 0){
            displayTitle += " #"+ currLand.getData().getId();
        }

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
        if(getActivity() != null){
            if(getActivity() instanceof MainActivity){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.show();
                }
            }
        }
    }
    private void initDataToView() {
        clearFlags();
        clearUndo();
    }
    private void initViews() {
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
        binding.fabLandActionSave.setOnClickListener(v->saveAction());
        binding.fabLandActionReset.setOnClickListener(v->undo());
        binding.slAlphaSlider.addOnChangeListener((range,value,user) -> onSliderUpdate(value));

        binding.mvLand.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(AppValues.countryZoom-2);
        mMap.setMaxZoomPreference(AppValues.streetZoom+2);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMapClickListener(this::processClick);
        binding.btnLandTerrain.setOnClickListener(v -> changeMapType());
        initLocation();
        binding.mvLand.setVisibility(View.VISIBLE);
    }
    private void initLocation() {
        if(points.size() == 0){
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            moveCameraOnLocation();
        }else{
            zoomOnPoints();
        }
        drawMap();
    }

    //menu relative
    private boolean menuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case (R.id.menu_item_toggle_drawer):
                toggleDrawer(true);
                return true;
            case (R.id.menu_item_save_land):
                saveLand();
                return true;
            case (R.id.toolbar_action_toggle_map_lock):
                toggleMapLock();
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
            case (R.id.toolbar_action_opacity_img):
                setAction(LandActionStates.Alpha);
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
    private void saveLand(LandData data) {
        if(isValidToSave(data)){
            addToVM(data);
        }
    }
    private boolean isValidToSave(LandData data) {
        if(data != null){
            return data.getBorder().size() > 2;
        }
        return false;
    }
    private void addToVM(LandData data) {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.saveLand(new Land(data));
        }
    }
    private void saveLand() {
        if(isValidToSave()){
            addToVM();
            toMenu(getActivity());
        }
    }
    private boolean isValidToSave() {
        return points.size() > 2 &&
                currLand != null;
    }
    private void addToVM() {
        currLand.getData().setBorder(points);
        currLand.getData().setHoles(holes);
        currLand.getData().setColor(color);
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.saveLand(currLand);
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
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.ErrorMaterialAlertDialog)
                    .setTitle(getString(R.string.delete_land_title))
                    .setMessage(getString(R.string.delete_land_text))
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> deleteLandActions())
                    .create();
            dialog.show();
        }
    }
    private void deleteLandActions(){
        removeFromVM();
        toMenu(getActivity());
    }
    private void removeFromVM() {
        if(getActivity() != null && currLand.getData().getId() > 0){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.removeLand(currLand);
        }
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
            mMap.addPolygon(options);
            int pointsNumber = 0;
            for(List<LatLng> hole : holes){
                pointsNumber = pointsNumber + hole.size();
            }
            pointsNumber = pointsNumber + points.size();
            if(pointsNumber<100){
                for(LatLng point : points){
                    mMap.addCircle(new CircleOptions()
                            .center(point)
                            .radius(10)
                            .fillColor(strokeColor)
                            .strokeColor(strokeColor)
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
                                .clickable(false)
                        );
                    }
                }
            }
        }
    }
    private void zoomOnPoints() {
        if(points.size() > 0){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng point : points){
                builder.include(point);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPadding
            ));
        }
    }
    private void changeMapType() {
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }
    private void toggleMapLock(){
        MenuItem miLock = binding.navLandMenu.getMenu()
                .findItem(R.id.toolbar_action_toggle_map_lock);
        if(mMap != null){
            if(mapIsLocked){
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                binding.btnLandTerrain.setVisibility(View.VISIBLE);
                miLock.setIcon(R.drawable.ic_drawer_menu_unlocked);
            }else{
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                binding.btnLandTerrain.setVisibility(View.GONE);
                miLock.setIcon(R.drawable.ic_drawer_menu_map_locked);
            }
            mapIsLocked = !mapIsLocked;
        }
        if(this.mapStatus != LandActionStates.Disable && !(isImgAction(mapStatus) && mapStatus != LandActionStates.Alpha)){
            setAction(LandActionStates.Disable);
        }
    }
    private void processClick(LatLng latLng) {
        switch (mapStatus){
            case AddEnd:
                addPointToEnd(latLng);
                break;
            case AddBetween:
                addBetweenPoint(latLng);
                break;
            case Edit:
                editPoint(latLng);
                break;
            case Delete:
                deletePoint(latLng);
                break;
        }
        if(mapStatus != LandActionStates.Disable){
            drawMap();
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

    //action relative
    private void setAction(LandActionStates mapStatus){
        Log.d(TAG, "setAction: "+mapStatus.name());
        if(this.mapStatus != mapStatus){
            this.mapStatus = mapStatus;
            if(mapStatus != LandActionStates.Disable){
                toggleDrawer(false);
            }
            clearFlags();
            clearUndo();
            if(isImgAction(mapStatus)){
                if(isImgViewEnable()){
                    switch (mapStatus){
                        case EditImg:
                            setTitle(getString(R.string.edit_img));
                            break;
                        case Alpha:
                            setTitle(getString(R.string.opacity_img));
                            break;
                    }
                }else{
                    setAction(LandActionStates.Disable);
                }
            }else{
                switch (mapStatus){
                    case AddEnd:
                        setTitle(getString(R.string.create_point));
                        break;
                    case AddBetween:
                        setTitle(getString(R.string.create_between_points));
                        changeTitleBasedOnState();
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
    }

    private boolean isImgViewEnable() {
        return binding.ivLandOverlay.getVisibility() == View.VISIBLE;
    }

    private void clearFlags() {
        index1 = -1;
        index2 = -1;
        index3 = -1;
    }
    private void saveAction(){
        if(mapStatus != LandActionStates.Disable){
            if(mapStatus == LandActionStates.ResetAll){
                points.clear();
                points.addAll(startPoints);
                if(startHoles.size()>0){
                    holes.clear();
                    holes.addAll(startHoles);
                    setupSideMenu();
                }
                drawMap();
            }else if(isImgAction(mapStatus) && mapStatus != LandActionStates.Alpha){
                if(beforeMoveWasLocked){
                    toggleMapLock();
                }
            }
            setAction(LandActionStates.Disable);
        }
    }
    private void undo(){
        switch (mapStatus){
            case AddEnd:
            case AddBetween:
            case Edit:
            case Delete:
                undoPoints();
                break;
            case EditImg:
                binding.ivLandOverlay.animate()
                        .translationX(0)
                        .translationY(0)
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotation(0)
                        .setDuration(0).start();
                break;
            case Alpha:
                binding.ivLandOverlay.animate()
                        .alpha(0.5f)
                        .setDuration(0).start();
                binding.slAlphaSlider.setValue(50.0f);
                break;
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
        clearFlags();
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
        return s == LandActionStates.EditImg || s == LandActionStates.Alpha;
    }
    private void addOverlayImg(Uri data) {
        if(data != null){
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
        if(isImgViewEnable()){
            if(mapStatus == LandActionStates.Alpha){
                Log.d(TAG, "onSliderUpdate: "+value);
                binding.ivLandOverlay.animate().alpha(value/100).setDuration(0).start();
            }
        }
    }

    //ui relative
    @SuppressLint("ClickableViewAccessibility")
    private void clearTitle() {
        actionBar.setTitle(displayTitle);
        binding.clLandControls.setVisibility(View.GONE);
        binding.fabLandActionSave.setVisibility(View.GONE);
        binding.fabLandActionReset.setVisibility(View.GONE);
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
        actionBar.setTitle(s);
        binding.clLandControls.setVisibility(View.VISIBLE);
        binding.fabLandActionSave.setVisibility(View.VISIBLE);
        binding.fabLandActionReset.setVisibility(View.VISIBLE);

        if(isImgViewEnable() && isImgAction(mapStatus)){
            if(mapStatus != LandActionStates.Alpha){
                beforeMoveWasLocked = !mapIsLocked;
                if(!mapIsLocked){
                    toggleMapLock();
                }
                binding.ivLandOverlay.setOnTouchListener(new MultiTouchListener());
                Log.d(TAG, "ivLandOverlay: setOnTouchListener MultiTouchListener");
            }else{
                binding.slAlphaSlider.setVisibility(View.VISIBLE);
            }
        }
    }
    private void changeTitleBasedOnState() {
        if(mapStatus == LandActionStates.AddBetween){
            if(index1 < 0){
                actionBar.setTitle(getString(R.string.selectFirstPoint));
            }else if(index2 < 0){
                actionBar.setTitle(getString(R.string.selectSecondPoint));
            }else if(index3 < 0){
                actionBar.setTitle(getString(R.string.placePoint));
            }else{
                actionBar.setTitle(getString(R.string.editPlacedPoint));
            }
        }
    }
    private void toggleDrawer(boolean toggle) {
        if(binding != null){
            if(toggle){
                binding.getRoot().openDrawer(GravityCompat.END);
                if(isImgAction(mapStatus) && mapStatus != LandActionStates.Alpha){
                    if(beforeMoveWasLocked){
                        toggleMapLock();
                    }
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

        if(currLand != null){
            tempMenu.findItem(R.id.toolbar_action_delete_land)
                    .setEnabled(currLand.getData().getId() != 0);
        }else{
            tempMenu.findItem(R.id.toolbar_action_delete_land).setEnabled(false);
        }

        tempMenu.findItem(R.id.toolbar_action_make_land_editable).setEnabled(holes.size()>0);

        tempMenu.findItem(R.id.toolbar_action_add_on_end).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_add_between).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_edit).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_delete).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_clean).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_remove_img).setEnabled(isImgViewEnable());
        tempMenu.findItem(R.id.toolbar_action_edit_img).setEnabled(isImgViewEnable());
        tempMenu.findItem(R.id.toolbar_action_opacity_img).setEnabled(isImgViewEnable());
    }
    private void moveCameraOnLocation() {
        Activity activity = getActivity();
        if(activity != null && address != null){
            AsyncTask.execute(()->{
                Address location = MapUtil.findLocation(getContext(),address);
                if(location != null){
                    float tempZoom = AppValues.countryZoom;
                    if(location.getMaxAddressLineIndex()>-1){
                        String s = location.getAddressLine(0);
                        long count = s.chars().filter(ch -> ch == ',').count();
                        if(count == 0){
                            tempZoom = AppValues.countryZoom;
                        }else if(count == 1){
                            tempZoom = AppValues.cityZoom;
                        }else{
                            tempZoom = AppValues.streetZoom;
                        }
                    }
                    if(location.hasLatitude() && location.hasLongitude()){
                        final float zoom = tempZoom;
                        activity.runOnUiThread(()-> {
                            if(mMap != null)
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(
                                                location.getLatitude(),
                                                location.getLongitude()),
                                        zoom
                                ));
                        });
                    }
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
                        drawMap();
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

    //navigation relative
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
                bundle.putParcelable(AppValues.argLand,currLand);
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

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLandMapBinding.inflate(inflater,container,false);
        binding.mvLand.onCreate(savedInstanceState);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LandData data = handleImport();
        if(data != null){
            saveLand(data);
            toMenu(getActivity());
        }else{
            if(initData()){
                initActivity();
                initDataToView();
                initViews();
            }else{
                toMenu(getActivity());
            }
        }
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding.mvLand.onDestroy();
        binding = null;
    }
    @Override public void onResume() {
        super.onResume();
        binding.mvLand.onResume();
    }
    @Override public void onPause() {
        super.onPause();
        binding.mvLand.onPause();
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        binding.mvLand.onLowMemory();
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.land_map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(menuItemClick(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        if(binding.getRoot().isDrawerOpen(GravityCompat.END)){
            binding.getRoot().closeDrawer(GravityCompat.END);
            return false;
        }
        if(binding.clLandControls.getVisibility() != View.GONE){
            clearTitle();
            return false;
        }
        return true;
    }
    @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.performClick();
        }
        return false;
    }
}
