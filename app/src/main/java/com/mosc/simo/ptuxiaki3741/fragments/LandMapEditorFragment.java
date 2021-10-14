package com.mosc.simo.ptuxiaki3741.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandMapBinding;
import com.mosc.simo.ptuxiaki3741.enums.ImportAction;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.enums.LandActionStates;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class LandMapEditorFragment extends Fragment implements FragmentBackPress,View.OnTouchListener {
    public static final String TAG = "LandFragment";

    private boolean
            mapIsLocked = false,
            beforeMoveWasLocked = false;
    private float
            zoom,
            dx,
            dy,
            x,
            y;
    private int
            index1,
            index2,
            index3;

    private LandActionStates mapStatus;
    private LandFileState fileState;
    private ImportAction importAction;

    public ActionBar actionBar;
    private FragmentLandMapBinding binding;
    private GoogleMap mMap;
    private AlertDialog dialog;

    private List<LatLng> points,startPoints;
    private List<List<LatLng>> holes,startHoles;
    private List<List<LatLng>> undoList;
    private User currUser;
    private String address;
    private Land currLand;
    private String displayTitle;

    //ActivityResultLauncher relative
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onRequestPermissionsResult
    );
    private final ActivityResultLauncher<Intent> imgFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::imgFileResult
    );
    private final ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::fileResult
    );
    private final ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::importResult
    );
    private void onRequestPermissionsResult(Boolean result) {
        toggleDrawer(false);
        if(result){
            Intent intent = FileUtil.getFilePickerIntent(fileState);
            switch (fileState){
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
    private void imgFileResult(ActivityResult result) {
        if (
            result.getResultCode() == Activity.RESULT_OK &&
            result.getData() != null
        ){
            if(FileUtil.fileIsValidImg(getContext(),result.getData())){
                addOverlayImg(result.getData().getData());
            }
        }
    }
    private void fileResult(ActivityResult result) {
        if (
                result.getResultCode() == Activity.RESULT_OK &&
                        result.getData() != null
        ){
            Intent intent = FileUtil.parseFile(getContext(),result.getData());
            if(intent != null){
                intent.putExtra(AppValues.userNameImportActivity,currUser.getId());
                intent.putExtra(AppValues.actionImportActivity,importAction);
                importLauncher.launch(intent);
            }
        }
    }
    private void importResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if(result.getData() != null){
                Land tempLand = (Land) result.getData().getExtras()
                        .get(AppValues.resNameImportActivity);
                if(tempLand != null){
                    if(tempLand.getData() != null){
                        switch (importAction){
                            case IMPORT:
                                afterImportFileAction(tempLand.getData());
                                break;
                            case ADD:
                                afterAddFileAction(tempLand.getData());
                                break;
                            case SUBTRACT:
                                afterSubtractFileAction(tempLand.getData());
                                break;
                        }
                    }
                }
            }
        }
        importAction = ImportAction.NONE;
    }
    private void afterImportFileAction(LandData data){
        points.clear();
        holes.clear();
        points.addAll(data.getBorder());
        holes.addAll(data.getHoles());
        saveLand();
    }
    private void afterAddFileAction(LandData dataToAdd){
        LandData curr = new LandData(points,holes);
        LandData result = LandUtil.uniteLandData(curr,dataToAdd);
        points.clear();
        holes.clear();
        points.addAll(result.getBorder());
        holes.addAll(result.getHoles());
        saveLand();
    }
    private void afterSubtractFileAction(LandData dataToSubtract){
        LandData curr = new LandData(points,holes);
        LandData result = LandUtil.subtractLandData(curr,dataToSubtract);
        points.clear();
        holes.clear();
        points.addAll(result.getBorder());
        holes.addAll(result.getHoles());
        saveLand();
    }

    //init relative
    private void initData(){
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLandLandMapFragment)) {
                currLand = getArguments().getParcelable(AppValues.argLandLandMapFragment);
            }else {
                currLand = null;
            }
            if(getArguments().containsKey(AppValues.argAddressLandMapFragment)) {
                address = getArguments().getString(AppValues.argAddressLandMapFragment);
            }else {
                address = null;
            }
        }else{
            currLand = null;
            address = null;
        }
        points = new ArrayList<>();
        holes = new ArrayList<>();
        if(currLand == null){
            finish(getActivity());
        }

        displayTitle = currLand.getData().getTitle();
        if(currLand.getData().getId() != -1){
            displayTitle += " #"+ EncryptUtil.convert4digit(currLand.getData().getId());
        }

        points.addAll(currLand.getData().getBorder());
        if(points.size()>1)
            if(points.get(0).equals(points.get(points.size()-1)))
                points.remove(points.size()-1);

        startPoints = new ArrayList<>(points);

        holes.addAll(currLand.getData().getHoles());
        for(List<LatLng> hole : holes){
            if(hole.size()>1)
                if(hole.get(0).equals(hole.get(hole.size()-1)))
                    hole.remove(hole.size()-1);
        }
        startHoles = new ArrayList<>(holes);

        setupSideMenu();

        mapStatus = LandActionStates.Disable;
        fileState = LandFileState.Disable;
        importAction = ImportAction.NONE;
        currUser = null;
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
        if(address == null){
            asyncFindLocation();
        }
        clearFlags();
        clearUndo();
    }
    private void initViewModel() {
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onUserUpdate);
        }
    }
    private void initViews() {
        binding.clLandControls.setVisibility(View.GONE);
        binding.ivLandOverlay.setVisibility(View.GONE);
        binding.flLandTouchLayer.setVisibility(View.GONE);
        binding.navLandMenu.setNavigationItemSelectedListener(this::menuItemClick);
        binding.LandMapRoot.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MenuItem resetAll = binding.navLandMenu.getMenu().findItem(R.id.toolbar_action_clean);
        if(startPoints.size() > 0){
            resetAll.setTitle(getString(R.string.reset_points));
        }else{
            resetAll.setTitle(getString(R.string.clear_points));
        }
        clearTitle();
        binding.mvLand.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMinZoomPreference(5);
        mMap.setMaxZoomPreference(20);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMapClickListener(this::processClick);
        drawMap();
        zoomOnPoints();
        binding.btnLandTerrain.setOnClickListener(v -> changeMapType());
    }

    //land and user data getter setter
    private void onUserUpdate(User user) {
        currUser = user;
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
            case (R.id.toolbar_action_move_img):
                setAction(LandActionStates.Move);
                return true;
            case (R.id.toolbar_action_zoom_img):
                setAction(LandActionStates.Zoom);
                return true;
            case (R.id.toolbar_action_rotate_img):
                setAction(LandActionStates.Rotate);
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
    private void saveLand() {
        if(isValidToSave()){
            addToVM();
            finish(getActivity());
        }
    }
    private boolean isValidToSave() {
        return points.size() > 2 &&
                currLand != null;
    }
    private void addToVM() {
        currLand.getData().setBorder(points);
        currLand.getData().setHoles(holes);
        if(getActivity() != null){
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmLands.saveLand(currLand);
        }
    }

    //delete relative
    private void deleteLand(){
        binding.LandMapRoot.closeDrawer(GravityCompat.END,false);
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
        finish(getActivity());
    }
    private void removeFromVM() {
        if(getActivity() != null && currLand.getData().getId() >= 0){
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
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
        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    //map relative
    private void drawMap() {
        mMap.clear();
        int strokeColor,fillColor;
        if(getContext() != null){
            strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
            fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
        }else{
            strokeColor = Color.argb(192,0,0,255);
            fillColor = Color.argb(51,0,0,255);
        }
        PolygonOptions options = LandUtil.getPolygonOptions(
                new Land(new LandData(points,holes)),
                strokeColor,
                fillColor,
                false
        );
        if(options != null){
            mMap.addPolygon(options);
        }else if(address != null){
            asyncMoveCameraOnLocation(getActivity());
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
        if(this.mapStatus != LandActionStates.Disable && this.mapStatus != LandActionStates.Move){
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
            index1 = 1;
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
    @SuppressLint("ClickableViewAccessibility")
    private void setAction(LandActionStates mapStatus){
        if(this.mapStatus != mapStatus){
            this.mapStatus = mapStatus;
            if(mapStatus != LandActionStates.Disable){
                toggleDrawer(false);
            }
            clearFlags();
            clearUndo();
            switch (mapStatus){
                case Move:
                    if(binding.ivLandOverlay.getVisibility() == View.VISIBLE){
                        binding.flLandTouchLayer.setVisibility(View.VISIBLE);
                        binding.flLandTouchLayer.setOnTouchListener(this);
                        beforeMoveWasLocked = !mapIsLocked;
                        if(!mapIsLocked){
                            toggleMapLock();
                        }
                        setTitle(
                                getString(R.string.move_img),
                                save -> saveAction(),
                                reset -> undo()
                        );
                    }else{
                        setAction(LandActionStates.Disable);
                    }
                    break;
                case Zoom:
                    if(binding.ivLandOverlay.getVisibility() == View.VISIBLE){
                        setTitle(
                                getString(R.string.zoom_img),
                                save -> saveAction(),
                                reset -> undo(),
                                plus -> plusAction(),
                                minus -> minusAction()
                        );
                    }else{
                        setAction(LandActionStates.Disable);
                    }
                    break;
                case Alpha:
                    if(binding.ivLandOverlay.getVisibility() == View.VISIBLE){
                        setTitle(
                                getString(R.string.opacity_img),
                                save -> saveAction(),
                                reset -> undo(),
                                plus -> plusAction(),
                                minus -> minusAction()
                        );
                    }else{
                        setAction(LandActionStates.Disable);
                    }
                    break;
                case Rotate:
                    if(binding.ivLandOverlay.getVisibility() == View.VISIBLE){
                        setTitle(
                                getString(R.string.rotate_img),
                                save -> saveAction(),
                                reset -> undo(),
                                plus -> plusAction(),
                                minus -> minusAction()
                        );
                    }else{
                        setAction(LandActionStates.Disable);
                    }
                    break;
                case AddEnd:
                    setTitle(
                            getString(R.string.create_point),
                            save -> saveAction(),
                            reset -> undo()
                    );
                    break;
                case AddBetween:
                    setTitle(
                            getString(R.string.create_between_points),
                            save -> saveAction(),
                            reset -> undo()
                    );
                    changeTitleBasedOnState();
                    break;
                case Edit:
                    setTitle(
                            getString(R.string.edit_point),
                            save -> saveAction(),
                            reset -> undo()
                    );
                    break;
                case Delete:
                    setTitle(
                            getString(R.string.delete_point),
                            save -> saveAction(),
                            reset -> undo()
                    );
                    break;
                case ResetAll:
                    saveAction();
                    break;
            }
        }
        if(mapStatus == LandActionStates.Disable){
            clearTitle();
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void clearFlags() {
        index1 = -1;
        index2 = -1;
        index3 = -1;
        if(binding != null){
            if(binding.flLandTouchLayer.getVisibility() != View.GONE){
                binding.flLandTouchLayer.setVisibility(View.GONE);
                binding.flLandTouchLayer.setOnTouchListener(null);
            }
        }
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
            }else if(mapStatus == LandActionStates.Move && beforeMoveWasLocked){
                toggleMapLock();
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
            case Rotate:
                binding.ivLandOverlay.animate()
                        .rotation(0)
                        .setDuration(0).start();
                break;
            case Alpha:
                binding.ivLandOverlay.animate()
                        .alpha(0.5f)
                        .setDuration(0).start();
                break;
            case Zoom:
                zoom=1f;
                binding.ivLandOverlay.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(0).start();
                break;
            case Move:
                binding.ivLandOverlay.animate()
                        .translationX(0)
                        .translationY(0)
                        .setDuration(0).start();
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
    private void addOverlayImg(Uri data) {
        if(data != null){
            zoom = 1f;
            binding.ivLandOverlay.setVisibility(View.VISIBLE);
            binding.ivLandOverlay.setImageURI(data);
            binding.ivLandOverlay.animate()
                    .alpha(0.5f)
                    .rotation(0)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0)
                    .translationY(0)
                    .setDuration(0).start();
        }
    }
    private void removeOverlayImg() {
        if(binding.ivLandOverlay.getVisibility() != View.GONE){
            zoom = 1f;
            binding.ivLandOverlay.animate()
                    .alpha(0.5f)
                    .rotation(0)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0)
                    .translationY(0)
                    .setDuration(0).start();
            binding.ivLandOverlay.setImageDrawable(null);
            binding.ivLandOverlay.setVisibility(View.GONE);
        }
    }
    private void plusAction(){
        if(binding.ivLandOverlay.getVisibility() == View.VISIBLE){
            switch (mapStatus){
                case Rotate:
                    binding.ivLandOverlay.animate()
                            .rotationBy(AppValues.stepRotate)
                            .setDuration(0).start();
                    break;
                case Alpha:
                    binding.ivLandOverlay.animate()
                            .alphaBy(AppValues.stepOpacity)
                            .setDuration(0).start();
                    break;
                case Zoom:
                    if((zoom+AppValues.stepZoom)<AppValues.maxZoom){
                        zoom+=AppValues.stepZoom;
                        binding.ivLandOverlay.animate()
                                .scaleXBy(AppValues.stepZoom)
                                .scaleYBy(AppValues.stepZoom)
                                .setDuration(0).start();
                    }
                    break;
            }
        }
    }
    private void minusAction(){
        if(binding.ivLandOverlay.getVisibility() == View.VISIBLE){
            switch (mapStatus){
                case Rotate:
                    binding.ivLandOverlay.animate()
                            .rotationBy(-AppValues.stepRotate)
                            .setDuration(0).start();
                    break;
                case Alpha:
                    binding.ivLandOverlay.animate()
                            .alphaBy(-AppValues.stepOpacity)
                            .setDuration(0).start();
                    break;
                case Zoom:
                    if((zoom-AppValues.stepZoom)>AppValues.minZoom){
                        zoom-=AppValues.stepZoom;
                        binding.ivLandOverlay.animate()
                                .scaleXBy(-AppValues.stepZoom)
                                .scaleYBy(-AppValues.stepZoom)
                                .setDuration(0).start();
                    }
                    break;
            }
        }
    }
    public void imgTouchMove(MotionEvent ev) {
        float h = dx - ev.getX();
        float v = dy - ev.getY();
        x -= h;
        y -= v;
        if(
            mapStatus == LandActionStates.Move &&
                    binding.ivLandOverlay.getVisibility() == View.VISIBLE
        ){
            binding.ivLandOverlay.animate()
                    .translationX(x)
                    .translationY(y)
                    .setDuration(0).start();
        }
        initImgValues(ev);
    }
    public void initImgTouch(MotionEvent ev) {
        if( mapStatus == LandActionStates.Move ){
            initImgValues(ev);
        }
    }
    private void initImgValues(MotionEvent ev) {
        dx = ev.getX();
        dy = ev.getY();
        x = binding.ivLandOverlay.getX();
        y = binding.ivLandOverlay.getY();
    }

    //ui relative
    private void clearTitle() {
        actionBar.setTitle(displayTitle);
        binding.clLandControls.setVisibility(View.GONE);
        binding.fabLandActionSave.setVisibility(View.GONE);
        binding.fabLandActionSave.setOnClickListener(null);
        binding.fabLandActionReset.setVisibility(View.GONE);
        binding.fabLandActionReset.setOnClickListener(null);
        binding.fabLandActionPlus.setVisibility(View.GONE);
        binding.fabLandActionPlus.setOnClickListener(null);
        binding.fabLandActionMinus.setVisibility(View.GONE);
        binding.fabLandActionMinus.setOnClickListener(null);
        clearFlags();
        clearUndo();
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
    private void setTitle(
            String s,
            View.OnClickListener save,
            View.OnClickListener reset
    ) {
        if(s != null){
            actionBar.setTitle(s);
            binding.clLandControls.setVisibility(View.VISIBLE);
            binding.fabLandActionSave.setVisibility(View.VISIBLE);
            binding.fabLandActionSave.setOnClickListener(save);
            binding.fabLandActionReset.setVisibility(View.VISIBLE);
            binding.fabLandActionReset.setOnClickListener(reset);
        }else{
            actionBar.setTitle(displayTitle);
            binding.clLandControls.setVisibility(View.GONE);
            binding.fabLandActionSave.setVisibility(View.GONE);
            binding.fabLandActionSave.setOnClickListener(null);
            binding.fabLandActionReset.setVisibility(View.GONE);
            binding.fabLandActionReset.setOnClickListener(null);
        }
        binding.fabLandActionPlus.setVisibility(View.GONE);
        binding.fabLandActionPlus.setOnClickListener(null);
        binding.fabLandActionMinus.setVisibility(View.GONE);
        binding.fabLandActionMinus.setOnClickListener(null);
    }
    private void setTitle(
            String s,
            View.OnClickListener save,
            View.OnClickListener reset,
            View.OnClickListener plus,
            View.OnClickListener minus
    ) {
        if(s != null){
            actionBar.setTitle(s);
            binding.clLandControls.setVisibility(View.VISIBLE);
            binding.fabLandActionSave.setVisibility(View.VISIBLE);
            binding.fabLandActionSave.setOnClickListener(save);
            binding.fabLandActionReset.setVisibility(View.VISIBLE);
            binding.fabLandActionReset.setOnClickListener(reset);
            binding.fabLandActionPlus.setVisibility(View.VISIBLE);
            binding.fabLandActionPlus.setOnClickListener(plus);
            binding.fabLandActionMinus.setVisibility(View.VISIBLE);
            binding.fabLandActionMinus.setOnClickListener(minus);
        }else{
            actionBar.setTitle(displayTitle);
            binding.clLandControls.setVisibility(View.GONE);
            binding.fabLandActionSave.setVisibility(View.GONE);
            binding.fabLandActionSave.setOnClickListener(null);
            binding.fabLandActionReset.setVisibility(View.GONE);
            binding.fabLandActionReset.setOnClickListener(null);
            binding.fabLandActionPlus.setVisibility(View.GONE);
            binding.fabLandActionPlus.setOnClickListener(null);
            binding.fabLandActionMinus.setVisibility(View.GONE);
            binding.fabLandActionMinus.setOnClickListener(null);
        }
    }
    private void toggleDrawer(boolean toggle) {
        if(binding != null){
            if(toggle){
                binding.LandMapRoot.openDrawer(GravityCompat.END);
                if(mapStatus == LandActionStates.Move && beforeMoveWasLocked){
                    toggleMapLock();
                }
                if(this.mapStatus != LandActionStates.Disable){
                    setAction(LandActionStates.Disable);
                }
            }else{
                binding.LandMapRoot.closeDrawer(GravityCompat.END);
            }
        }
    }
    private void setupSideMenu(){
        Menu tempMenu = binding.navLandMenu.getMenu();

        tempMenu.findItem(R.id.toolbar_action_delete_land)
                .setEnabled(currLand.getData().getId() != -1);

        tempMenu.findItem(R.id.toolbar_action_make_land_editable).setEnabled(holes.size()>0);

        tempMenu.findItem(R.id.toolbar_action_add_on_end).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_add_between).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_edit).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_delete).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_clean).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_add_img).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_remove_img).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_move_img).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_zoom_img).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_rotate_img).setEnabled(!(holes.size()>0));
        tempMenu.findItem(R.id.toolbar_action_opacity_img).setEnabled(!(holes.size()>0));
    }
    private void asyncFindLocation() {
        AsyncTask.execute(()->{
            Address temp = MapUtil.findLocation(
                    getContext(),
                    MapUtil.getPolygonCenter(startPoints)
            );
            if(temp != null){
                if(temp.getLocality() != null){
                    if(temp.getCountryName() != null){
                        address = temp.getLocality()+","+temp.getCountryName();
                    }else{
                        address = temp.getLocality();
                    }
                }
            }
        });
    }
    private void asyncMoveCameraOnLocation(Activity activity) {
        if(activity != null){
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
                                        new LatLng(location.getLatitude(),location.getLongitude()),
                                        zoom
                                ));
                        });
                    }
                }
            });
        }
    }

    //navigation relative
    public void finish(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()->{
                binding.LandMapRoot.closeDrawer(GravityCompat.END,false);
                binding.clLandControls.setVisibility(View.GONE);
                activity.onBackPressed();
            });
    }
    public void toInfo(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandMapFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandInfoFragment,currLand);
                if(nav != null)
                    nav.navigate(R.id.landMapToLandInfo,bundle);
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
        initData();
        initActivity();
        initDataToView();
        initViewModel();
        initViews();
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
    @Override public boolean onTouch(View v, MotionEvent event) {
        if(binding.ivLandOverlay.getVisibility() == View.VISIBLE){
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1) {
                        imgTouchMove(event);
                    }else{
                        v.performClick();
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    if (event.getPointerCount() == 1) {
                        initImgTouch(event);
                    }else{
                        v.performClick();
                    }
                    break;
                default:
                    v.performClick();
                    break;
            }
        }
        return true;
    }
    @Override public boolean onBackPressed() {
        if(binding.LandMapRoot.isDrawerOpen(GravityCompat.END)){
            binding.LandMapRoot.closeDrawer(GravityCompat.END);
            return false;
        }
        if(binding.clLandControls.getVisibility() != View.GONE){
            clearTitle();
            return false;
        }
        return true;
    }
}
