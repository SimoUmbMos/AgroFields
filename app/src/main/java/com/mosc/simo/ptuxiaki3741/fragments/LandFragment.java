package com.mosc.simo.ptuxiaki3741.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.ImportActivity;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.enums.LandActionStates;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LandMapViewHolder;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.ParcelablePolygon;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LandFragment extends Fragment implements FragmentBackPress{
    public static final String TAG = "LandFragment";
    public static final double distanceToMapActionKM = 15;
    private static final float stepOpacity = 0.03f,
            stepRotate = 1f,
            stepZoom = 0.03f,
            maxZoom = 7f,
            minZoom = 0.1f;
    public static final int defaultPadding = 64;
    public ActionBar actionBar;
    private LandMapViewHolder viewHolder;
    private GoogleMap mMap;

    private LandActionStates mapStatus;
    private LandFileState fileState;
    private List<LatLng> points,startPoints;
    private List<List<LatLng>> undoList;
    private User currUser;
    private long currLandID;
    private String title;
    private float zoom;
    private int index1, index2, index3;
    //todo undo

    //ActivityResultLauncher relative
    private final ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::fileResult
    );
    private final ActivityResultLauncher<Intent> imgFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::imgFileResult
    );
    private final ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::importResult
    );
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onRequestPermissionsResult
    );
    private void fileResult(ActivityResult result) {
        if (
                result.getResultCode() == Activity.RESULT_OK &&
                result.getData() != null
        ){
            Intent intent = FileUtil.parseFile(getContext(),result.getData());
            if(intent != null){
                intent.putExtra(ImportActivity.userName,currUser.getId());
                importLauncher.launch(intent);
            }
        }
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
    private void importResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if(result.getData() != null){
                ParcelablePolygon polygon = (ParcelablePolygon) result.getData().getExtras()
                        .get(ImportActivity.resName);
                points.clear();
                points.addAll(polygon.getPoints());
                drawMap();
                zoomOnPoints();
            }
        }
    }
    private void onRequestPermissionsResult(Boolean result) {
        toggleDrawer(false);
        if(result){
            Intent intent = FileUtil.getFilePickerIntent(fileState);
            switch (fileState){
                case File:
                    fileLauncher.launch(intent);
                    break;
                case Img:
                    imgFileLauncher.launch(intent);
                    break;
            }
        }
        fileState = LandFileState.Disable;
    }

    // overrides
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_land_map, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initData();
        initViewModel();
        initViews(view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(menuItemClick(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onBackPressed() {
        if(viewHolder.drawer.isDrawerOpen(GravityCompat.END)){
            viewHolder.drawer.closeDrawer(GravityCompat.END);
            return false;
        }
        return true;
    }

    //init relative
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
    private void initData() {
        Land currLand = LandFragmentArgs.fromBundle(getArguments()).getLand();
        points = new ArrayList<>();
        if(!new Land().equals(currLand)){
            currLandID = currLand.getData().getId();
            title = currLand.getData().getTitle();
            if(currLand.getBorder() != null){
                points.addAll(getPointsFromLandBorder(currLand.getBorder()));
            }
        }else{
            currLandID = -1;
            title = "";
        }
        startPoints = new ArrayList<>(points);
        mapStatus = LandActionStates.Disable;
        fileState = LandFileState.Disable;
        currUser = null;
        clearFlags();
        clearUndo();
    }
    private void initViewModel() {
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::setCurrUser);
        }
    }
    private void initViews(View view) {
        viewHolder = new LandMapViewHolder(view);
        viewHolder.navDrawer.setNavigationItemSelectedListener(this::menuItemClick);
        if(startPoints.size() > 0){
            viewHolder.resetAll.setTitle(getString(R.string.reset_points));
        }else{
            viewHolder.resetAll.setTitle(getString(R.string.clear_points));
        }
        clearTitle();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::initMap);
        }
    }
    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setMinZoomPreference(5);
        googleMap.setMaxZoomPreference(20);
        googleMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setOnMapClickListener(this::processClick);
        googleMap.setOnMapLongClickListener(this::processClick);
        googleMap.setOnMapLoadedCallback(this::mapFullLoaded);
        viewHolder.terrainButton.setOnClickListener(v -> changeMapType());
    }
    private void mapFullLoaded() {
        if(points.size() > 0){
            drawMap();
            zoomOnPoints();
        }
    }

    //land and user data getter setter
    private void setCurrUser(User user) {
        currUser = user;
    }
    private List<LatLng> getPointsFromLandBorder(List<LandPoint> border) {
        List<LandPoint> landPointsList = new ArrayList<>(border);
        Collections.sort(landPointsList);
        List<LatLng> landPoints = new ArrayList<>();
        for(LandPoint landPoint : landPointsList){
            landPoints.add(landPoint.getLatLng());
        }
        return landPoints;
    }
    private Land getLand() {
        Land currLand;
        if(currLandID >= 0){
            currLand = new Land(new LandData(currLandID,currUser.getId(),title),getLandPoints());
        }else{
            currLand = new Land(new LandData(currUser.getId(),title),getLandPoints());
        }
        return currLand;
    }
    private List<LandPoint> getLandPoints() {
        List<LandPoint> landPoints = new ArrayList<>();
        for(int i = 0; i < points.size(); i++){
            landPoints.add(new LandPoint(i,points.get(i)));
        }
        return landPoints;
    }

    //menu relative
    private boolean menuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case (R.id.menu_item_toggle_drawer):
                toggleDrawer(true);
                return true;
            case (R.id.menu_item_save_land):
                save();
                return true;
            case (R.id.toolbar_action_toggle_map_lock):
                toggleMapLock();
                return true;
            case (R.id.toolbar_action_edit_land_info):
                navigate(toInfo());
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
            default:
                toggleDrawer(false);
                return false;
        }
    }

    //navigation relative
    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        if(
                navController.getCurrentDestination() == null ||
                navController.getCurrentDestination().getId() == R.id.landMapFragment
        )
            navController.navigate(action);
    }
    private NavDirections toMenu(){
        return LandFragmentDirections.toListMenu();
    }
    private NavDirections toInfo(){
        return LandFragmentDirections.toLandInfo(getLand());
    }
    private void toImportFile(){
        importFile(LandFileState.File);
    }
    private void toImportImg(){
        importFile(LandFileState.Img);
    }

    //save relative
    private void save() {
        if(isValidToSave()){
            saveToVM();
            navigate(toMenu());
        }
    }
    private boolean isValidToSave() {
        return
                points.size() > 2 &&
                !title.trim().isEmpty() &&
                currUser != null;
    }
    private void saveToVM() {
        Land currLand = getLand();
        if(getActivity() != null){
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmLands.saveLand(currLand,currUser);
        }
    }

    //file relative
    public void importFile(LandFileState state) {
        fileState = state;
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        permissionLauncher.launch(permission);
    }

    //map relative
    private void drawMap() {
        mMap.clear();
        if(points.size() > 0){
            mMap.addPolygon(new PolygonOptions().addAll(points));
        }
    }
    private void zoomOnPoints() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : points){
            builder.include(point);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                builder.build(),
                defaultPadding
        ));
    }
    private void changeMapType() {
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }
    private void toggleMapLock(){
        if(mMap != null){
            if(viewHolder.terrainButton.getVisibility() == View.GONE){
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                viewHolder.terrainButton.setVisibility(View.VISIBLE);
                viewHolder.miLock.setIcon(R.drawable.menu_ic_unlocked);
            }else{
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                viewHolder.terrainButton.setVisibility(View.GONE);
                viewHolder.miLock.setIcon(R.drawable.menu_ic_locked);
            }
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
            if(distanceToMapActionKM >= MapUtil.distanceBetween(points.get(index),latLng)){
                addPointsToUndo();
                points.set(index,latLng);
            }
        }
    }
    private void deletePoint(LatLng latLng) {
        int index = MapUtil.closestPoint(points,latLng);
        if(index > -1 && index < points.size()){
            if(distanceToMapActionKM >= MapUtil.distanceBetween(points.get(index),latLng)){
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
            if(MapUtil.distanceBetween(latLng,points.get(index1)) > distanceToMapActionKM){
                index1 = -1;
            }
        }else{
            index1 = 1;
        }
    }
    private void selectSecondPointForBetween(LatLng latLng) {
        index2 = MapUtil.closestPoint(points,latLng);
        if(index2 > -1 && index2 < points.size()){
            if(MapUtil.distanceBetween(latLng,points.get(index2)) < distanceToMapActionKM){
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
        toggleDrawer(false);
        if(this.mapStatus != mapStatus){
            this.mapStatus = mapStatus;
            clearFlags();
            clearUndo();
            switch (mapStatus){
                case Move:
                    if(viewHolder.imageView.getVisibility() == View.VISIBLE){
                        setTitle(
                                getResources().getString(R.string.move_img),
                                save -> setAction(LandActionStates.Disable),
                                reset -> undo()
                        );
                        //todo add touch control on move
                    }else{
                        setAction(LandActionStates.Disable);
                    }
                    break;
                case Zoom:
                    if(viewHolder.imageView.getVisibility() == View.VISIBLE){
                        setTitle(
                                getResources().getString(R.string.zoom_img),
                                save -> setAction(LandActionStates.Disable),
                                reset -> undo(),
                                plus -> plusAction(),
                                minus -> minusAction()
                        );
                    }else{
                        setAction(LandActionStates.Disable);
                    }
                    break;
                case Alpha:
                    if(viewHolder.imageView.getVisibility() == View.VISIBLE){
                        setTitle(
                                getResources().getString(R.string.opacity_img),
                                save -> setAction(LandActionStates.Disable),
                                reset -> undo(),
                                plus -> plusAction(),
                                minus -> minusAction()
                        );
                    }else{
                        setAction(LandActionStates.Disable);
                    }
                    break;
                case Rotate:
                    if(viewHolder.imageView.getVisibility() == View.VISIBLE){
                        setTitle(
                                getResources().getString(R.string.rotate_img),
                                save -> setAction(LandActionStates.Disable),
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
                            getResources().getString(R.string.create_point),
                            save -> setAction(LandActionStates.Disable),
                            reset -> undo()
                    );
                    break;
                case AddBetween:
                    setTitle(
                            getResources().getString(R.string.create_between_points),
                            save -> setAction(LandActionStates.Disable),
                            reset -> undo()
                    );
                    changeTitleBasedOnState();
                    break;
                case Edit:
                    setTitle(
                            getResources().getString(R.string.edit_point),
                            save -> setAction(LandActionStates.Disable),
                            reset -> undo()
                    );
                    break;
                case Delete:
                    setTitle(
                            getResources().getString(R.string.delete_point),
                            save -> setAction(LandActionStates.Disable),
                            reset -> undo()
                    );
                    break;
                case ResetAll:
                    points.clear();
                    points.addAll(startPoints);
                    drawMap();
                    this.mapStatus = LandActionStates.Disable;
                case Disable:
                    clearTitle();
                    break;
            }
        }
    }
    private void clearFlags() {
        index1 = -1;
        index2 = -1;
        index3 = -1;
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
                viewHolder.imageView.animate()
                        .rotation(0)
                        .setDuration(0).start();
                break;
            case Alpha:
                viewHolder.imageView.animate()
                        .alpha(0.5f)
                        .setDuration(0).start();
                break;
            case Zoom:
                zoom=1f;
                viewHolder.imageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(0).start();
                break;
            case Move:
                viewHolder.imageView.animate()
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

    //img relative
    private void addOverlayImg(Uri data) {
        if(data != null){
            zoom = 1f;
            viewHolder.imageView.setVisibility(View.VISIBLE);
            viewHolder.imageView.setImageURI(data);
            viewHolder.imageView.animate()
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
        if(viewHolder.imageView.getVisibility() != View.GONE){
            zoom = 1f;
            viewHolder.imageView.animate()
                    .alpha(0.5f)
                    .rotation(0)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0)
                    .translationY(0)
                    .setDuration(0).start();
            viewHolder.imageView.setImageDrawable(null);
            viewHolder.imageView.setVisibility(View.GONE);
        }
    }
    private void plusAction(){
        if(viewHolder.imageView.getVisibility() == View.VISIBLE){
            switch (mapStatus){
                case Rotate:
                    viewHolder.imageView.animate()
                            .rotationBy(stepRotate)
                            .setDuration(0).start();
                    break;
                case Alpha:
                    viewHolder.imageView.animate()
                            .alphaBy(stepOpacity)
                            .setDuration(0).start();
                    break;
                case Zoom:
                    if((zoom+stepZoom)<maxZoom){
                        zoom+=stepZoom;
                        viewHolder.imageView.animate()
                                .scaleXBy(stepZoom)
                                .scaleYBy(stepZoom)
                                .setDuration(0).start();
                    }
                    break;
            }
        }
    }
    private void minusAction(){
        if(viewHolder.imageView.getVisibility() == View.VISIBLE){
            switch (mapStatus){
                case Rotate:
                    viewHolder.imageView.animate()
                            .rotationBy(-stepRotate)
                            .setDuration(0).start();
                    break;
                case Alpha:
                    viewHolder.imageView.animate()
                            .alphaBy(-stepOpacity)
                            .setDuration(0).start();
                    break;
                case Zoom:
                    if((zoom-stepZoom)>minZoom){
                        zoom-=stepZoom;
                        viewHolder.imageView.animate()
                                .scaleXBy(-stepZoom)
                                .scaleYBy(-stepZoom)
                                .setDuration(0).start();
                    }
                    break;
            }
        }
    }

    //ui relative
    private void clearTitle() {
        clearFlags();
        clearUndo();
        actionBar.setTitle(title);
        viewHolder.clTab.setVisibility(View.GONE);
        viewHolder.fabSave.setVisibility(View.GONE);
        viewHolder.fabReset.setVisibility(View.GONE);
        viewHolder.fabPlus.setVisibility(View.GONE);
        viewHolder.fabMinus.setVisibility(View.GONE);
        viewHolder.fabPlus.setOnClickListener(null);
        viewHolder.fabMinus.setOnClickListener(null);
        viewHolder.fabReset.setOnClickListener(null);
        viewHolder.fabSave.setOnClickListener(null);
    }
    private void changeTitleBasedOnState() {
        if(mapStatus == LandActionStates.AddBetween){
            if(index1 < 0){
                actionBar.setTitle(title + " - " +"select first point");
            }else if(index2 < 0){
                actionBar.setTitle(title + " - " +"select second point");
            }else if(index3 < 0){
                actionBar.setTitle(title + " - " +"place point");
            }else{
                actionBar.setTitle(title + " - " +"edit placed point");
            }
        }
    }
    private void setTitle(
            String s,
            View.OnClickListener save,
            View.OnClickListener reset
    ) {
        if(s != null){
            actionBar.setTitle(title + "-" +s);
            viewHolder.clTab.setVisibility(View.VISIBLE);
            viewHolder.fabSave.setVisibility(View.VISIBLE);
            viewHolder.fabReset.setVisibility(View.VISIBLE);
            viewHolder.fabReset.setOnClickListener(reset);
            viewHolder.fabSave.setOnClickListener(save);
        }else{
            actionBar.setTitle(title);
            viewHolder.clTab.setVisibility(View.GONE);
            viewHolder.fabSave.setVisibility(View.GONE);
            viewHolder.fabReset.setVisibility(View.GONE);
            viewHolder.fabReset.setOnClickListener(null);
            viewHolder.fabSave.setOnClickListener(null);
        }
        viewHolder.fabPlus.setVisibility(View.GONE);
        viewHolder.fabMinus.setVisibility(View.GONE);
        viewHolder.fabPlus.setOnClickListener(null);
        viewHolder.fabMinus.setOnClickListener(null);
    }
    private void setTitle(
            String s,
            View.OnClickListener save,
            View.OnClickListener reset,
            View.OnClickListener plus,
            View.OnClickListener minus
    ) {
        if(s != null){
            actionBar.setTitle(title + "-" +s);
            viewHolder.clTab.setVisibility(View.VISIBLE);
            viewHolder.fabSave.setVisibility(View.VISIBLE);
            viewHolder.fabReset.setVisibility(View.VISIBLE);
            viewHolder.fabPlus.setVisibility(View.VISIBLE);
            viewHolder.fabMinus.setVisibility(View.VISIBLE);
            viewHolder.fabSave.setOnClickListener(save);
            viewHolder.fabReset.setOnClickListener(reset);
            viewHolder.fabPlus.setOnClickListener(plus);
            viewHolder.fabMinus.setOnClickListener(minus);
        }else{
            actionBar.setTitle(title);
            viewHolder.clTab.setVisibility(View.GONE);
            viewHolder.fabSave.setVisibility(View.GONE);
            viewHolder.fabReset.setVisibility(View.GONE);
            viewHolder.fabPlus.setVisibility(View.GONE);
            viewHolder.fabMinus.setVisibility(View.GONE);
            viewHolder.fabReset.setOnClickListener(null);
            viewHolder.fabSave.setOnClickListener(null);
            viewHolder.fabPlus.setOnClickListener(null);
            viewHolder.fabMinus.setOnClickListener(null);
        }
    }
    private void toggleDrawer(boolean toggle) {
        if(viewHolder != null){
            if(toggle){
                viewHolder.drawer.openDrawer(GravityCompat.END);
            }else{
                viewHolder.drawer.closeDrawer(GravityCompat.END);
            }
        }
    }

    /*public static final String TAG = "LandFragment";
    private LandMapHolder mapHolder;
    private LandViewHolder viewHolder;
    private LandMenuHolder menuHolder;
    private LandFileController fileController;
    private LandPointsController pointsController;
    private long currLandID = -1;
    private User currUser;

    private final ActivityResultLauncher<Intent> fileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::FileResult
    );
    private final ActivityResultLauncher<Intent> imgResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::ViewHolderResult
    );
    private final ActivityResultLauncher<Intent> mapResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::MapResult
    );
    private final ActivityResultLauncher<String> permissionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onRequestPermissionsResult
    );


    private void init(View view) {
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if (activity != null) {
            activity.setOnBackPressed(this);
            UserViewModel vmUsers = new ViewModelProvider(activity).get(UserViewModel.class);
            currUser = vmUsers.getCurrUser().getValue();
            actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                actionBar.show();
            }
        }
        controllersAndHandlersInit(view, actionBar);
        initValues();
        viewHolderConfig();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapHolder);
        }
    }
    private void controllersAndHandlersInit(View view, ActionBar actionBar) {
        Log.i(TAG, "controllersAndHandlersInit: start");
        pointsController = new LandPointsController();
        LandImgController imgController = new LandImgController(view.findViewById(R.id.imageView));
        fileController = new LandFileController(getActivity(), permissionResultLauncher,
                imgResultLauncher, fileResultLauncher);
        viewHolder= new LandViewHolder(view, actionBar, imgController);
        mapHolder = new LandMapHolder(viewHolder,pointsController);
        menuHolder = new LandMenuHolder(
                viewHolder,
                mapHolder,
                fileController,
                pointsController,
                imgController,
                this::save,
                this::edit
        );
        Log.i(TAG, "controllersAndHandlersInit: end");
    }
    private void initValues() {
        Land currLand = LandFragmentArgs.fromBundle(getArguments()).getLand();
        if(!new Land().equals(currLand)){
            currLandID = currLand.getData().getId();
        }
        viewHolder.setTitle(currLand.getData().getTitle());
        List<LandPoint> landPointsList = new ArrayList<>(currLand.getBorder());
        Collections.sort(landPointsList);
        List<LatLng> landPoints = new ArrayList<>();
        for(LandPoint landPoint : landPointsList){
            landPoints.add(landPoint.getLatLng());
        }
        pointsController.setPoints(landPoints);
    }
    private void viewHolderConfig() {
        viewHolder.navDrawer.setNavigationItemSelectedListener(menuHolder);
        viewHolder.closeTabMenu();
        viewHolder.hideImgView();
        viewHolder.setOnClose(new OnAction() {
            @Override
            public void onCloseTab() {
                mapHolder.clearFlag();
            }
        });
    }

    private void save() {
        if(isValidToSave()){
            saveToVM();
            navigate(toListMenu());
        }
    }
    private void edit() {
        navigate(toLandInfo());
    }







    private void FileResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK){
            onFilePicked(result.getData());
        }
    }
    private void MapResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if(result.getData() != null){
                mapHolder.onActivityResult(result.getData());
            }
        }
    }
    private void ViewHolderResult(ActivityResult result) {
        if (
                result.getResultCode() == Activity.RESULT_OK &&
                fileController.fileIsValidImg(result.getData())
        ){
            viewHolder.onActivityResult(result.getData());
        }
    }

    private void onFilePicked(Intent result) {
        Log.d(TAG, "onFilePicked: "+result.getData().getPath());
        Intent intent = parseFile(result);
        if(intent != null){
            intent.putExtra(ImportActivity.userName,currUser.getId());
            mapResultLauncher.launch(intent);
        }
    }

    private Intent parseFile(Intent result) {
        Intent intent = null;
        if(fileController.fileIsValid(result)){
            intent = new Intent(getContext(), ImportActivity.class);
            intent.setData(result.getData());
        }
        return intent;
    }

    public void onRequestPermissionsResult(Boolean result) {
        if(result){
            Intent intent = fileController.getFilePickerIntent();
            if(fileController.getFlag() == LandFileState.File){
                fileResultLauncher.launch(intent);
            }else if(fileController.getFlag() == LandFileState.Img){
                imgResultLauncher.launch(intent);
            }
        }
    }

    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.landMapFragment)
            navController.navigate(action);
    }
    private NavDirections toListMenu(){
        return LandFragmentDirections.toListMenu();
    }
    private NavDirections toLandInfo(){
        Land currLand = getLand();
        return LandFragmentDirections.toLandInfo(currLand);
    }*/
}
