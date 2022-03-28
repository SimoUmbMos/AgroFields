package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.data.enums.LocationStates;
import com.mosc.simo.ptuxiaki3741.data.helpers.LocationHelper;
import com.mosc.simo.ptuxiaki3741.data.helpers.SnackBarHelper;
import com.mosc.simo.ptuxiaki3741.data.models.ClusterLand;
import com.mosc.simo.ptuxiaki3741.data.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLiveMapBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;
import com.mosc.simo.ptuxiaki3741.ui.renderers.LandRendered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMenuFragment extends Fragment{
    public static final String TAG = "ContactsContainerFragment";

    private FragmentLiveMapBinding binding;
    private LoadingDialog loadingDialog;
    private GoogleMap mMap;

    private AppViewModel appVM;

    private Vibrator vibrator;
    private Snackbar notificationSnackBar;
    private String notificationTitle;
    private String notificationMsg;

    private ClusterManager<ClusterLand> clusterManager;
    private long snapshot;
    private Map<Land, List<LandZone>> data;
    private boolean isInit, firstLandUpdate, firstZoneUpdate;

    private boolean cameraFollow, cameraUserMovement;
    private final Handler cameraMovingThread = new Handler();
    private Runnable cameraMovingRunnable;


    private LocationHelper locationHelper;
    private final Handler locationThread = new Handler();
    private Runnable locationRunnable;
    private Marker myLocation;
    private LatLng userLocation;

    @SuppressLint("MissingPermission")
    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::onLocationPermissionResult
    );

    private final ActivityResultLauncher<String> vibratorPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onVibratorPermissionResult
    );

    public void onLocationPermissionResult(Map<String, Boolean> results){
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

        locationHelper.setLocationPermission(locationPermission);
        locationHelper.getLastKnownLocation();
        locationHelper.start();
        if(locationPermission != LocationStates.DISABLE) {
            binding.ibCameraMode.setVisibility(View.VISIBLE);
            vibratorPermissionRequest.launch(Manifest.permission.VIBRATE);
        }
    }

    public void onVibratorPermissionResult(boolean result){
        if(result && getActivity() != null){
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLiveMapBinding.inflate(inflater, container, false);
        binding.mvLiveMap.onCreate(savedInstanceState);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        Handler handler = new Handler();
        handler.postDelayed(this::lateInitFragment,240);
    }

    @Override
    public void onDestroyView() {
        displayNotification(null, null);
        binding.mvLiveMap.onDestroy();
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mvLiveMap.onStart();
        if(locationHelper != null) locationHelper.start();
        if(locationRunnable != null){
            locationThread.post(locationRunnable);
        }
    }

    @Override
    public void onStop() {
        if(locationRunnable != null){
            locationThread.removeCallbacks(locationRunnable);
        }
        if(locationHelper != null) locationHelper.stop();
        binding.mvLiveMap.onStop();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mvLiveMap.onResume();
    }

    @Override
    public void onPause() {
        binding.mvLiveMap.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        binding.mvLiveMap.onLowMemory();
        super.onLowMemory();
    }

    private void initData() {
        myLocation = null;
        userLocation = null;
        locationHelper = null;
        vibrator = null;
        notificationSnackBar = null;

        notificationTitle = "";
        notificationMsg = "";

        data = new HashMap<>();
        clusterManager = null;
        isInit = false;
        firstLandUpdate = true;
        firstZoneUpdate = true;

        cameraFollow = false;
        cameraUserMovement = false;

        if(getArguments() != null && getArguments().containsKey(AppValues.argSnapshotKey)) {
            snapshot = getArguments().getLong(AppValues.argSnapshotKey, -1);
        }else {
            snapshot = -1;
        }
    }

    private void initActivity() {
        if (getActivity() == null) return;
        if (getActivity().getClass() != MainActivity.class) return;
        MainActivity activity = (MainActivity) getActivity();
        appVM = new ViewModelProvider(activity).get(AppViewModel.class);
        activity.setOnBackPressed(()->true);
        loadingDialog = activity.getLoadingDialog();
    }

    private void initFragment() {
        binding.ibClose.setOnClickListener(v ->goBack());
        binding.ibCameraReset.setOnClickListener(v -> zoomOnLands());
        binding.ibCameraMode.setOnClickListener(v -> {
            cameraFollow = !cameraFollow;
            isInit = true;
            if(cameraFollow){
                binding.ibCameraMode.setImageResource(R.drawable.ic_menu_close_location);
                binding.ibCameraReset.setVisibility(View.GONE);
                zoomOnUser();
            }else{
                binding.ibCameraMode.setImageResource(R.drawable.ic_menu_location);
                binding.ibCameraReset.setVisibility(View.VISIBLE);
                zoomOnLands();
                displayNotification(null,null);
            }
        });
        binding.ibCameraMode.setVisibility(View.GONE);
        binding.mvLiveMap.setVisibility(View.GONE);
        if(loadingDialog != null) loadingDialog.openDialog();
    }

    private void lateInitFragment() {
        binding.mvLiveMap.setVisibility(View.VISIBLE);
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST,r->binding.mvLiveMap.getMapAsync(this::initMap));
    }

    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(AppValues.minZoom);
        mMap.setMaxZoomPreference(AppValues.maxZoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.075368, 23.553767),16));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if(getActivity() == null) {
            goBack();
            return;
        }

        isInit = true;

        clusterManager = new ClusterManager<>(getActivity(), mMap);
        LandRendered renderer = new LandRendered(getActivity(),mMap,clusterManager);
        renderer.setMinClusterSize(2);
        clusterManager.setRenderer(renderer);
        NonHierarchicalDistanceBasedAlgorithm<ClusterLand> algorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
        algorithm.setMaxDistanceBetweenClusteredItems(60);
        clusterManager.setAlgorithm(algorithm);

        mMap.setOnCameraMoveStartedListener(reason -> {
            if(reason != GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION || cameraUserMovement){
                cameraUserMovement = false;
                if(cameraMovingRunnable != null){
                    cameraMovingThread.removeCallbacks(cameraMovingRunnable);
                    cameraMovingRunnable = null;
                }
                if(cameraFollow){
                    cameraMovingRunnable = () -> {
                        if(cameraFollow) zoomOnUser();
                        cameraMovingRunnable = null;
                    };
                }
            }
        });
        mMap.setOnCameraIdleListener(()->{
            if(cameraMovingRunnable != null) {
                cameraMovingThread.postDelayed(cameraMovingRunnable, AppValues.liveMapMillisToCameraReset);
            }
            new Handler().post(clusterManager::onCameraIdle);
        });
        clusterManager.setOnClusterItemClickListener(this::onClusterItemClick);
        clusterManager.setOnClusterClickListener(this::onClusterClick);
        if(firstLandUpdate){
            if(appVM != null){
                AsyncTask.execute(()->{
                    snapshot = appVM.setTempSnapshot(snapshot);
                    if(getActivity() != null) getActivity().runOnUiThread(this::initLandHolder);
                });
            }
        }else{
            updateMap();
        }
    }

    private void initLandHolder() {
        if (appVM != null && firstLandUpdate) {
            appVM.getTempSnapshotLands().observe(getViewLifecycleOwner(), this::onLandsUpdate);
        }
    }

    private void initZoneHolder() {
        if (appVM != null && firstZoneUpdate) {
            appVM.getTempSnapshotLandZones().observe(getViewLifecycleOwner(), this::onZonesUpdate);
        }
    }

    private void initLocation(){
        if(loadingDialog != null) loadingDialog.closeDialog();
        if(getActivity() != null){
            locationHelper = new LocationHelper(getActivity(),this::onLocationUpdate);
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void onLandsUpdate(List<Land> lands) {
        if (lands == null){
            lands = new ArrayList<>();
        }
        Map<Land, List<LandZone>> temp = new HashMap<>(data);
        data.clear();
        lands.forEach(item->{
            List<LandZone> zones = temp.getOrDefault(item,null);
            if(zones == null){
                zones = new ArrayList<>();
            }
            data.put(item,zones);
        });
        if(firstLandUpdate){
            firstLandUpdate = false;
            initZoneHolder();
        }else{
            updateMap();
        }
    }

    private void onZonesUpdate(Map<Long, List<LandZone>> data) {
        if (data == null){
            data = new HashMap<>();
        }
        data.forEach((lid,zones)->{
            if(zones == null){
                zones = new ArrayList<>();
            }
            Land land = null;
            for(Land key : this.data.keySet()){
                if(key.getData().getId() == lid){
                    land = key;
                    break;
                }
            }
            if(land != null){
                this.data.put(land,new ArrayList<>(zones));
            }
        });
        updateMap();
        if(firstZoneUpdate){
            firstZoneUpdate = false;
            initLocation();
        }
    }

    private void onLoop(boolean moveCamera) {
        if(mMap == null) return;

        if(myLocation != null){
            myLocation.remove();
            myLocation = null;
        }
        if(!cameraFollow) return;

        if(userLocation != null){
            Drawable bitmapDraw;
            try{
                bitmapDraw = ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.bg_location_marker);
            }catch (Exception e){
                bitmapDraw = null;
            }
            if(bitmapDraw != null){
                Bitmap b = UIUtil.drawableToBitmap(bitmapDraw);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 36, 36, false);
                myLocation = mMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        .zIndex(AppValues.liveMapMyLocationZIndex)
                        .draggable(false)
                );
            }else{
                myLocation = mMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .zIndex(AppValues.liveMapMyLocationZIndex)
                        .draggable(false)
                );
            }
            if(moveCamera){
                mMap.stopAnimation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation,mMap.getCameraPosition().zoom));
            }
        }

        String title = "";
        String msg = "";
        if(userLocation != null && clusterManager != null){
            for(ClusterLand item : clusterManager.getAlgorithm().getItems()){
                if(item == null || item.getZonesData() == null) continue;
                if(MapUtil.contains(userLocation,item.getLandData().getBorder())){
                    boolean onHole = false;
                    for(List<LatLng> hole : item.getLandData().getHoles()){
                        if(hole == null) continue;
                        if(MapUtil.contains(userLocation,hole)){
                            onHole = true;
                            break;
                        }
                    }
                    if(onHole) continue;

                    title = item.getTitle();
                    msg = item.getSnippet();
                    for(LandZoneData zoneData : item.getZonesData()){
                        if(zoneData == null) continue;
                        if(MapUtil.contains(userLocation,zoneData.getBorder())){
                            title = zoneData.getTitle();
                            msg = zoneData.getNote();
                            break;
                        }
                    }
                    break;
                }
            }
        }
        displayNotification(title, msg);
    }

    private void onLocationUpdate(Location l) {
        if(l == null) return;
        updateUserLocation(new LatLng(l.getLatitude(), l.getLongitude()));
    }

    private boolean onClusterItemClick(ClusterLand item) {
        if(mMap == null) return false;

        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : item.getLandData().getBorder()){
            size++;
            builder.include(point);
        }
        if(size > 0){
            mMap.stopAnimation();
            cameraUserMovement = true;
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private boolean onClusterClick(Cluster<ClusterLand> cluster) {
        if(mMap == null) return false;

        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(ClusterLand item : cluster.getItems()){
            for(LatLng point : item.getLandData().getBorder()){
                size++;
                builder.include(point);
            }
        }
        if(size > 0){
            mMap.stopAnimation();
            cameraUserMovement = true;
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private void zoomOnLands(){
        if(mMap == null) return;
        if(cameraFollow) return;
        if(myLocation != null){
            myLocation.remove();
            myLocation = null;
        }
        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Land land : data.keySet()){
            for(LatLng point : land.getData().getBorder()){
                size++;
                builder.include(point);
            }
        }
        if(size>0){
            if(isInit){
                mMap.stopAnimation();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
                isInit = false;
            } else {
                mMap.stopAnimation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
            }
        }

    }

    private void zoomOnUser(){
        if(mMap == null) return;
        if(!cameraFollow) return;

        if(userLocation != null){
            if(isInit){
                isInit = false;
                mMap.stopAnimation();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,AppValues.personZoom));
                updateUserLocation(userLocation);
            } else if(cameraMovingRunnable != null){
                mMap.stopAnimation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation,AppValues.personZoom), 120, null);
            }else{
                mMap.stopAnimation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation,AppValues.personZoom));
            }
        }
    }

    private void updateUserLocation(LatLng newPos) {
        if(locationRunnable != null){
            locationThread.removeCallbacks(locationRunnable);
            locationRunnable = null;
        }
        locationRunnable = () -> {
            if(userLocation == null){
                userLocation = newPos;
            }else if(MapUtil.distanceBetween(userLocation, newPos) > 0.001) {
                userLocation = newPos;
            }
            if(userLocation != null && userLocation.equals(newPos)) {
                onLoop(cameraFollow && cameraMovingRunnable == null);
            }
            locationRunnable = null;
        };
        locationThread.post(locationRunnable);
    }

    private void updateMap(){
        if(clusterManager == null) return;

        new Handler().post(()->{
            clusterManager.clearItems();
            data.forEach((land,zones)-> clusterManager.addItem(new ClusterLand(land,zones)));
            clusterManager.cluster();
            if(cameraFollow){
                zoomOnUser();
            }else{
                zoomOnLands();
            }
        });
    }

    private void displayNotification(String title, String msg){
        if(binding == null) return;

        if(title == null || title.isEmpty()){
            notificationTitle = "";
            notificationMsg = "";
            if(notificationSnackBar != null) {
                if(notificationSnackBar.isShown()) notificationSnackBar.dismiss();
                notificationSnackBar = null;
            }
        }else if(notificationNeedUpdate(title,msg)) {
            StringBuilder builder = new StringBuilder();
            notificationTitle = title;
            builder.append(title);
            if (msg == null || msg.isEmpty()) {
                notificationMsg = "";
            } else {
                notificationMsg = msg;
                builder.append("\n");
                builder.append(msg);
            }
            if(notificationSnackBar != null){
                notificationSnackBar.dismiss();
            }

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = binding.getRoot().getContext().getTheme();
            theme.resolveAttribute(R.attr.colorSurface, typedValue, true);
            @ColorInt int colorSurface = typedValue.data;
            Color color = Color.valueOf(colorSurface);
            colorSurface = Color.argb(AppValues.notificationAlpha,color.red(),color.green(),color.blue());
            theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true);
            @ColorInt int colorOnSurface = typedValue.data;
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            @ColorInt int colorPrimary = typedValue.data;

            notificationSnackBar = Snackbar.make(binding.llNotificationSnackBarContainer,builder.toString(),Snackbar.LENGTH_INDEFINITE);
            notificationSnackBar.setBehavior(SnackBarHelper.SnackBarNoSwipeBehavior());
            notificationSnackBar.setAction(R.string.okey,view -> {
                if(notificationSnackBar.isShown()) notificationSnackBar.dismiss();
                notificationSnackBar = null;
            });
            notificationSnackBar.setBackgroundTint(colorSurface);
            notificationSnackBar.setTextColor(colorOnSurface);
            notificationSnackBar.setActionTextColor(colorPrimary);
            notificationSnackBar.show();
            vibrate();
        }
    }

    private boolean notificationNeedUpdate(String t, String m) {
        String title = t;
        String msg = m;
        if(title == null) title = "";
        if(msg == null) msg = "";
        return !(title.equals(notificationTitle) && msg.equals(notificationMsg));
    }

    private void vibrate(){
        if(vibrator == null) return;
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private void goBack(){
        if(getActivity() != null) getActivity().onBackPressed();
    }
}