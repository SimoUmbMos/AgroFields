package com.mosc.simo.ptuxiaki3741.fragments.features;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLiveMapBinding;
import com.mosc.simo.ptuxiaki3741.helpers.LocationHelper;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.helpers.SensorOrientationHelper;
import com.mosc.simo.ptuxiaki3741.enums.LocationStates;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveMapFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "ContactsContainerFragment";
    public static final int NotificationID = 3741;

    private FragmentLiveMapBinding binding;
    private NotificationManager notificationManager;
    private Notification notification;
    private String notificationTitle;
    private GoogleMap mMap;
    private List<Land> lands;
    private Map<Long, List<LandZone>> zones;

    private LocationHelper locationHelper;
    private SensorOrientationHelper orientationHelper;
    private double lastZoom;
    private boolean waitUserStop;

    private Circle currPosition;
    private float currBearing;

    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            results ->{
                Boolean fineLocationGranted = results.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = results.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION,false);

                LocationStates locationPermission;

                if (fineLocationGranted != null && fineLocationGranted){
                    locationPermission = LocationStates.FINE_LOCATION;
                }else if(coarseLocationGranted != null && coarseLocationGranted){
                    locationPermission = LocationStates.COARSE_LOCATION;
                }else{
                    locationPermission = LocationStates.DISABLE;
                }

                if(locationPermission != LocationStates.DISABLE){
                    if(locationHelper != null){
                        locationHelper.setLocationPermission(locationPermission);
                        locationHelper.getLastKnownLocation();
                        locationHelper.start();
                    }
                    startAutoGesture();
                }else{
                    goBack();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLiveMapBinding.inflate(inflater, container, false);
        binding.mvLiveMap.onCreate(savedInstanceState);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewHolder();
        initFragment();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.mvLiveMap.onDestroy();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        binding.mvLiveMap.onResume();
        if(locationHelper != null) locationHelper.start();
        if(orientationHelper != null) orientationHelper.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        if(orientationHelper != null) orientationHelper.onPause();
        if(locationHelper != null) locationHelper.stop();
        binding.mvLiveMap.onPause();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mvLiveMap.onLowMemory();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initData() {
        lands = new ArrayList<>();
        zones = new HashMap<>();
        lastZoom = -1.0;
        currBearing = 0;
        waitUserStop = false;
    }
    private void initActivity() {
        if (getActivity() != null) {
            if (getActivity().getClass() == MainActivity.class) {
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
                activity.setToolbarTitle(getString(R.string.default_live_map_title));
                notificationManager = activity.getNotificationManager();
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.show();
                }
            }
            locationHelper = new LocationHelper(getActivity(),this::onUpdateLocation);
            orientationHelper = new SensorOrientationHelper(getActivity(),this::onUpdateBearing);
        }
    }
    private void initViewHolder() {
        if (getActivity() != null) {
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getLands().observe(getViewLifecycleOwner(), this::onLandsUpdate);
            appVM.getLandZones().observe(getViewLifecycleOwner(), this::onZonesUpdate);
        }
    }
    private void initFragment() {
        binding.mvLiveMap.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap) {
        binding.mvLiveMap.setVisibility(View.INVISIBLE);

        mMap = googleMap;

        mMap.setMinZoomPreference(AppValues.personZoom - 1.0f);
        mMap.setMaxZoomPreference(AppValues.personZoom + 2.0f);

        mMap.setOnCameraMoveListener(() -> {
            if(currPosition != null){
                double zoom = mMap.getCameraPosition().zoom - AppValues.personZoom + 2.0f;
                if(lastZoom < 0 || lastZoom != zoom){
                    lastZoom = zoom;
                    currPosition.setRadius(AppValues.currPositionSize / lastZoom);
                }
            }
        });
        mMap.setOnCameraMoveStartedListener(id->{
            if(id != GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION){
                waitUserStop = true;
            }
        });
        mMap.setOnCameraIdleListener(() -> {
            if(currPosition != null){
                if(waitUserStop){
                    waitUserStop = false;
                    moveCameraToCurrPosition(true);
                }else if(currPosition.getCenter() != mMap.getCameraPosition().target){
                    moveCameraToCurrPosition(true);
                }
            }
        });

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        onMapUpdate();

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void onLandsUpdate(List<Land> lands) {
        this.lands.clear();
        if (lands != null) {
            this.lands.addAll(lands);
        }
        onMapUpdate();
    }
    private void onZonesUpdate(Map<Long, List<LandZone>> zones) {
        this.zones.clear();
        if (zones != null)
            this.zones.putAll(zones);
        onMapUpdate();
    }
    private void onMapUpdate() {
        if (mMap == null) return;

        mMap.clear();
        List<LandZone> temp;
        PolygonOptions options;
        int strokeColor;
        int fillColor;
        for (Land land : lands) {
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
            options = LandUtil.getPolygonOptions(
                    land.getData(),
                    strokeColor,
                    fillColor,
                    false
            );
            if (options != null) {
                mMap.addPolygon(options.zIndex(1));
            }
            temp = zones.getOrDefault(land.getData().getId(), null);
            if (temp != null) {
                for (LandZone zone : temp) {
                    strokeColor = Color.argb(
                            AppValues.defaultStrokeAlpha,
                            zone.getData().getColor().getRed(),
                            zone.getData().getColor().getGreen(),
                            zone.getData().getColor().getBlue()
                    );
                    fillColor = Color.argb(
                            AppValues.defaultFillAlpha,
                            zone.getData().getColor().getRed(),
                            zone.getData().getColor().getGreen(),
                            zone.getData().getColor().getBlue()
                    );
                    options = LandUtil.getPolygonOptions(
                            zone.getData(),
                            strokeColor,
                            fillColor,
                            false
                    );
                    if (options != null) {
                        mMap.addPolygon(options.zIndex(2));
                    }
                }
            }
        }
    }

    private void startAutoGesture(){
        if(orientationHelper == null) return;

        if(!orientationHelper.isAutoGesture()){
            orientationHelper.setAutoGesture(true);
        }
    }

    private void onUpdateLocation(Location location) {
        if(mMap == null) return;
        if(location == null) return;

        LatLng position = new LatLng(location.getLatitude(),location.getLongitude());
        if(getActivity() != null){
            getActivity().runOnUiThread(()->{
                if(currPosition == null){
                    currBearing = location.getBearing();

                    currPosition = mMap.addCircle(new CircleOptions()
                            .center(position)
                            .radius(AppValues.currPositionSize / 2.0)
                            .strokeWidth(AppValues.currPositionSizeStroke)
                            .fillColor(AppValues.defaultPersonColorFill)
                            .strokeColor(AppValues.defaultPersonColorStroke)
                            .zIndex(3));

                    initCameraToCurrPosition();

                    binding.mvLiveMap.setVisibility(View.VISIBLE);
                }else{
                    currPosition.setCenter(position);
                    moveCameraToCurrPosition(false);
                }
            });
        }

        CheckInsidePolygon(position);
    }
    private void onUpdateBearing(float bearing) {
        if(currPosition == null) return;

        currBearing = bearing;
        if(getActivity() != null){
            getActivity().runOnUiThread(() -> moveCameraToCurrPosition(true));
        }
    }

    private void initCameraToCurrPosition() {
        if(mMap == null) return;
        if(currPosition == null) return;

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(currPosition.getCenter())
                .bearing(currBearing)
                .tilt(AppValues.defaultTilt)
                .zoom(AppValues.personZoom)
                .build()
        ));
    }
    private void moveCameraToCurrPosition(boolean rotate) {
        if(mMap == null) return;
        if(currPosition == null) return;
        if(waitUserStop){
            return;
        }

        mMap.stopAnimation();
        CameraPosition position;
        int animation;
        if (rotate) {
            position = new CameraPosition.Builder()
                    .target(currPosition.getCenter())
                    .bearing(currBearing)
                    .tilt(mMap.getCameraPosition().tilt)
                    .zoom(mMap.getCameraPosition().zoom)
                    .build();
            animation = AppValues.AnimationRotate;
        }else{
            position = new CameraPosition.Builder()
                    .target(currPosition.getCenter())
                    .bearing(mMap.getCameraPosition().bearing)
                    .tilt(mMap.getCameraPosition().tilt)
                    .zoom(mMap.getCameraPosition().zoom)
                    .build();
            animation = AppValues.AnimationMove;
        }
        mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(position),
                animation,
                null);
    }

    private void CheckInsidePolygon(LatLng position) {
        if(position == null) return;
        Land currLand = null;
        for(Land land : lands){
            if(MapUtil.contains(position,land.getData().getBorder())){
                currLand = land;
                for(List<LatLng> hole : land.getData().getHoles()) {
                    if (MapUtil.contains(position, hole)) {
                        currLand = null;
                        break;
                    }
                }
                if(currLand != null) {
                    break;
                }
            }
        }
        if(currLand != null){
            List<LandZone> currZones = this.zones.getOrDefault(currLand.getData().getId(),null);
            if(currZones != null){
                LandZone currZone = null;
                for(LandZone zone:currZones){
                    if(MapUtil.contains(position,zone.getData().getBorder())){
                        currZone = zone;
                        break;
                    }
                }
                if(currZone != null){
                    displayNotification(currZone);
                }else{
                    displayNotification(currLand);
                }
            }else{
                displayNotification(currLand);
            }
        }else{
            displayNotification(null);
        }
    }
    private void displayNotification(Object object) {
        final String title;
        final String msg;

        if(object != null){
            if(object.getClass() == Land.class){
                Land land = (Land) object;
                title = land.toString();
                msg = "";
            }else if(object.getClass() == LandZone.class){
                LandZone zone = (LandZone) object;
                title = zone.toString();
                msg = zone.getData().getNote();
            }else{
                title = "";
                msg = "";
            }
        }else{
            title = "";
            msg = "";
        }

        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                getActivity().runOnUiThread(()-> {
                    if(title.isEmpty()){
                        activity.setToolbarTitle(getString(R.string.default_live_map_title));
                    }else{
                        if(msg.isEmpty()){
                            activity.setToolbarTitle(title);
                        }else{
                            activity.setToolbarTitle(title, msg);
                        }
                    }
                });
            }
        }

        if(notificationManager != null && getContext() != null){
            if(title.isEmpty()){
                if(notification != null){
                    notificationTitle = "";
                    notificationManager.cancel(NotificationID);
                    notification = null;
                }
            }else{
                if(notification == null){
                    notificationTitle = title;
                    if(msg.isEmpty()){
                        notification = new Notification
                                .Builder(getContext(), AppValues.NotificationChannelID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(title)
                                .build();
                    }else{
                        notification = new Notification
                                .Builder(getContext(), AppValues.NotificationChannelID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(title)
                                .setContentText(msg)
                                .build();
                    }
                    notificationManager.notify(NotificationID,notification);
                }else{
                    if(!notificationTitle.equals(title)){
                        notificationTitle = title;
                        if(msg.isEmpty()){
                            notification = new Notification
                                    .Builder(getContext(), AppValues.NotificationChannelID)
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentTitle(title)
                                    .build();
                        }else{
                            notification = new Notification
                                    .Builder(getContext(), AppValues.NotificationChannelID)
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentTitle(title)
                                    .setContentText(msg)
                                    .build();
                        }
                        notificationManager.notify(NotificationID,notification);
                    }
                }
            }
        }
    }

    private void goBack(){
        Toast.makeText(
                getContext(),
                getString(R.string.live_map_permission_error),
                Toast.LENGTH_SHORT
        ).show();
        if(getActivity() != null) getActivity().onBackPressed();
    }
}