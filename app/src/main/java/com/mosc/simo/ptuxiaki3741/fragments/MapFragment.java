package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.fragments.helpers.MapFileController;
import com.mosc.simo.ptuxiaki3741.fragments.helpers.MapImgController;
import com.mosc.simo.ptuxiaki3741.fragments.helpers.MapPointsController;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.interfaces.OnFragmentTouchEvent;
import com.mosc.simo.ptuxiaki3741.util.ui.TouchableWrapper;



public class MapFragment extends Fragment implements FragmentBackPress, OnMapReadyCallback,
        OnFragmentTouchEvent, NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraMoveListener {
    private static final String TAG = "MapFragment";
    private static final int postDelay = 100;
    private View mOriginalContentView;
    private DrawerLayout drawer;
    private MapView mapView;
    private ConstraintLayout clImgTab;
    private TextView tvImgAction;
    private ImageView imageView;
    private FloatingActionButton fabSave,fabReset,fabPlus,fabMinus;
    private GoogleMap mMap;

    private TouchableWrapper mTouchView;
    private MenuItem miLock;

    private boolean fabPlusPressed = false,
            fabMinusPressed = false,
            mapIsLocked = false;

    private MapImgController mic;
    private MapPointsController mpc;
    private MapFileController mfc;

    //initializer's
    private void init(View view, Bundle savedInstanceState) {
        mTouchView.setOnFragmentTouchEvent(this);
        initFragment(view, savedInstanceState);
        initViews(view);
        initBackgroundThread();
    }
    private void initFragment(View view, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setOnBackPressed(this);
        }
        mapView = view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        drawer = view.findViewById(R.id.drawer_layout);
        NavigationView navDrawer = view.findViewById(R.id.nav_view);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, navDrawer);
        navDrawer.setNavigationItemSelectedListener(this);
    }
    private void initViews(View view) {
        clImgTab = view.findViewById(R.id.clImgTab);
        tvImgAction = view.findViewById(R.id.tvImgAction);
        fabSave = view.findViewById(R.id.fabSave);
        fabReset = view.findViewById(R.id.fabReset);
        fabMinus = view.findViewById(R.id.fabMinus);
        fabPlus = view.findViewById(R.id.fabPlus);
        imageView = view.findViewById(R.id.imageView);

        initControllers();
        showTabMenu(false);
        initButtons();
    }
    private void initBackgroundThread() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                backgroundTask();
                handler.postDelayed(this, postDelay);
            }
        }, postDelay);
    }
    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(5);
        mMap.setMaxZoomPreference(20);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.091072, 23.545818), 20));
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraMoveListener(this);
    }
    private void initControllers() {
        mic = new MapImgController(imageView);
        mic.removeOverlayImg();
        mpc = new MapPointsController();
        mfc = new MapFileController(mic,getActivity());
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initButtons() {
        fabReset.setOnClickListener(v -> buttonReset());
        fabSave.setOnClickListener(v -> buttonSave());
        fabPlus.setOnTouchListener((v, event) -> {
            buttonPlus(event);
            return true;
        });
        fabMinus.setOnTouchListener((v, event) -> {
            buttonMinus(event);
            return true;
        });
    }

    //button's
    private void addOverlayButtonMenu(){
        mfc.addImgOverlay();
    }
    private void removeOverlayButtonMenu(){
        mic.removeOverlayImg();
    }
    private void importButtonMenu(){
        mfc.importMenuAction();
    }
    private void clearButtonMenu(){
        mpc.clearList();
        drawOnMap();
        clearControllersFlags();
    }
    private void buttonSave(){
        showTabMenu(false);
        if(mapIsLocked)
            toggleMapLock();
    }
    private void buttonReset(){
        if(mic.getImgVisible()){
            mic.resetImg();
        }else if(mpc.isActive()){
            mpc.undo();
            drawOnMap();
        }
    }
    private void buttonPlus(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTouchView.unsetOnFragmentTouchEvent();
            fabPlusPressed = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mTouchView.setOnFragmentTouchEvent(this);
            fabPlusPressed = false;
        }
    }
    private void buttonMinus(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTouchView.unsetOnFragmentTouchEvent();
            fabMinusPressed = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mTouchView.setOnFragmentTouchEvent(this);
            fabMinusPressed = false;
        }
    }

    //ui relative method's
    private void refreshUi(){
        if(drawer.isDrawerOpen(GravityCompat.END)){
            drawer.closeDrawer(GravityCompat.END);
        }
        showTabMenu(mic.getFlag() != MapImgController.FlagDisable ||
                mpc.getFlag() != MapPointsController.FlagDisable);
    }
    private void toggleDrawer() {
        showTabMenu(false);
        if(drawer.isDrawerOpen(GravityCompat.END)){
            drawer.closeDrawer(GravityCompat.END);
        }else{
            drawer.openDrawer(GravityCompat.END);
        }
    }
    private void toggleMapLock(){
        if(mMap != null){
            if(mapIsLocked){
                miLock.setIcon(R.drawable.menu_ic_unlocked);
                mTouchView.unsetOnFragmentTouchEvent();
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mapIsLocked=false;
            }else{
                miLock.setIcon(R.drawable.menu_ic_locked);
                mTouchView.setOnFragmentTouchEvent(this);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                mapIsLocked=true;
            }
        }
    }
    private void showPlusMinusButtons(boolean show){
        if(show){
            fabPlus.setVisibility(View.VISIBLE);
            fabMinus.setVisibility(View.VISIBLE);
        }else{
            fabPlus.setVisibility(View.GONE);
            fabMinus.setVisibility(View.GONE);
        }
    }
    private void showTabMenu(boolean show){
        if(show){
            clImgTab.setVisibility(View.VISIBLE);
            if(mic.getFlag() == MapImgController.FlagDisable)
                setupUiForPointAction(mpc.getFlag());
            else
                setupUiForImgAction(mic.getFlag());
        }else{
            clImgTab.setVisibility(View.GONE);
            clearControllersFlags();
        }
    }
    private void setupUiForImgAction(int imgActionFlag){
        String msg = "";
        showPlusMinusButtons(true);
        switch (imgActionFlag){
            case MapImgController.FlagMove:
                msg="Move Image";
                showPlusMinusButtons(false);
                if(!mapIsLocked)
                    toggleMapLock();
                break;
            case MapImgController.FlagZoom:
                msg="Zoom Image";
                break;
            case MapImgController.FlagRotate:
                msg="Rotate Image";
                break;
            case MapImgController.FlagAlpha:
                msg="Change Opacity of Image";
                break;
        }
        mic.setFlag(imgActionFlag);
        tvImgAction.setText(msg);
    }
    private void setupUiForPointAction(int pointActionFlag){
        String msg = "";
        showPlusMinusButtons(false);
        switch (pointActionFlag){
            case MapPointsController.FlagAddEnd:
                msg="Add to End Point";
                break;
            case MapPointsController.FlagAddBetween:
                switch (mpc.getAddBetweenStatus()){
                    case (1):
                        msg="Select first point";
                        break;
                    case (2):
                        msg="Select second point";
                        break;
                    case (3):
                        msg="Place point";
                        break;
                    case (4):
                        msg="Edit point added";
                        break;
                }
                drawOnMap();
                break;
            case MapPointsController.FlagEdit:
                msg="Edit Point";
                drawOnMap();
                break;
            case MapPointsController.FlagDelete:
                msg="Delete Point";
                drawOnMap();
                break;
        }
        mpc.setFlag(pointActionFlag);
        tvImgAction.setText(msg);
    }

    //handler's
    private boolean menuItemClick(int id) {
        switch (id){
            case (R.id.menu_item_toggle_drawer):
                toggleDrawer();
                break;
            case (R.id.menu_item_toggle_map_lock):
                toggleMapLock();
                break;
            case(R.id.toolbar_action_add_on_end):
                onPointActionClick(MapPointsController.FlagAddEnd);
                break;
            case(R.id.toolbar_action_add_between):
                onPointActionClick(MapPointsController.FlagAddBetween);
                break;
            case(R.id.toolbar_action_edit):
                onPointActionClick(MapPointsController.FlagEdit);
                break;
            case(R.id.toolbar_action_delete):
                onPointActionClick(MapPointsController.FlagDelete);
                break;
            case(R.id.toolbar_action_clean):
                clearButtonMenu();
                break;
            case(R.id.toolbar_action_add_img):
                addOverlayButtonMenu();
                break;
            case(R.id.toolbar_action_remove_img):
                removeOverlayButtonMenu();
                break;
            case(R.id.toolbar_action_move_img):
                onImgActionClick(MapImgController.FlagMove);
                break;
            case(R.id.toolbar_action_zoom_img):
                onImgActionClick(MapImgController.FlagZoom);
                break;
            case(R.id.toolbar_action_rotate_img):
                onImgActionClick(MapImgController.FlagRotate);
                break;
            case(R.id.toolbar_action_opacity_img):
                onImgActionClick(MapImgController.FlagAlpha);
                break;
            case(R.id.toolbar_action_import):
                importButtonMenu();
                break;
            default:
                return false;
        }
        refreshUi();
        return true;
    }
    private void clearControllersFlags() {
        setControllersFlag(MapImgController.FlagDisable,
                MapPointsController.FlagDisable);
    }
    private void onImgActionClick(int flagImgController) {
        if (mic.getImgVisible()) {
            setControllersFlag(flagImgController,
                    MapPointsController.FlagDisable);
        }
    }
    private void onPointActionClick(int flagPointController) {
        mpc.clearUndo();
        setControllersFlag(MapImgController.FlagDisable,
                flagPointController);
    }
    private void mapClick(LatLng latLng) {
        mpc.processClick(latLng);
        refreshUi();
        drawOnMap();
    }
    private void drawOnMap() {
        mMap.clear();
        if(mpc.getPoints().size()>0){
            mMap.addPolygon(new PolygonOptions().addAll(mpc.getPoints()));
            changeDistanceToActionBasedZoom();
            for(LatLng point : mpc.getPoints()){
                mMap.addCircle(new CircleOptions()
                        .center(point)
                        .radius(mpc.getDistanceToAction()));
            }
        }
    }
    private void changeDistanceToActionBasedZoom() {
        float zoom = mMap.getCameraPosition().zoom;
        double distance=16;
        if(zoom>16){
            distance = 20-zoom;
            distance = Math.pow(2,distance);
        }
        mpc.setDistanceToAction(distance);
    }
    private void backgroundTask() {
        if(fabPlusPressed){
            Log.d(TAG, "fabMinusPressed");
            mic.doAction(true);
        }else if(fabMinusPressed){
            Log.d(TAG, "fabMinusPressed");
            mic.doAction(false);
        }
    }
    private void setControllersFlag(int flagImgController, int flagPointController) {
        mic.setFlag(flagImgController);
        mpc.setFlag(flagPointController);
    }

    //override's
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mOriginalContentView = inflater.inflate(R.layout.fragment_map, container, false);
        if(getActivity() != null){
            mTouchView = new TouchableWrapper(getActivity());
            mTouchView.addView(mOriginalContentView);
            return mTouchView;
        }
        return mOriginalContentView;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        miLock = menu.findItem(R.id.menu_item_toggle_map_lock);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        init(v, savedInstanceState);
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        initMap(googleMap);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        menuItemClick(id);
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        refreshUi();
        showTabMenu(false);
        return menuItemClick(id);
    }
    @Override
    public void onActionDown(MotionEvent ev) {
        if(mic.getImgVisible())
            mic.initImgTouch(ev);
    }
    @Override
    public void onActionMove(MotionEvent ev) {
        if(mic.getImgVisible())
            mic.imgTouchMove(ev);
    }
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        mapClick(latLng);
    }
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mapClick(latLng);
    }
    @Override
    public void onCameraMove() {
        if(mpc.isActive()){
            drawOnMap();
        }
    }
    @Override
    public View getView() {
        return mOriginalContentView;
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public boolean onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.END)){
            drawer.closeDrawer(GravityCompat.END);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mfc.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mfc.onActivityResult(requestCode, resultCode, data);
    }
}