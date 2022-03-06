package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.models.ClusterLand;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLiveMapBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.ui.renderers.LandRendered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMenuFragment extends Fragment{
    //todo: background check location and notify by notifications
    //todo(?): add curr location with follow

    public static final String TAG = "ContactsContainerFragment";
    public static final int NotificationID = 3741;

    private NotificationManager notificationManager;
    private Notification notification;
    private String notificationTitle;
    private String notificationMsg;

    private FragmentLiveMapBinding binding;
    private GoogleMap mMap;

    private ClusterManager<ClusterLand> clusterManager;
    private Map<Land, List<LandZone>> data;
    private boolean cameraMoving, isInit, firstLandUpdate;

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
    }

    @Override
    public void onDestroyView() {
        binding.mvLiveMap.onDestroy();
        super.onDestroyView();
        binding = null;
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
        notification = null;
        notificationTitle = "";
        notificationMsg = "";

        data = new HashMap<>();
        clusterManager = null;
        cameraMoving = false;
        isInit = false;
        firstLandUpdate = true;
    }

    private void initActivity() {
        if (getActivity() != null) {
            if (getActivity().getClass() == MainActivity.class) {
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(()->true);
                notificationManager = activity.getNotificationManager();
            }
        }
    }

    private void initFragment() {
        binding.ibBack.setOnClickListener(v ->goBack());
        binding.ibZoom.setOnClickListener(v -> zoomOnLands());
        binding.mvLiveMap.getMapAsync(this::initMap);
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

        binding.mvLiveMap.setVisibility(View.INVISIBLE);
        isInit = true;

        clusterManager = new ClusterManager<>(getActivity(), mMap);
        LandRendered renderer = new LandRendered(getActivity(),mMap,clusterManager);
        renderer.setMinClusterSize(2);
        clusterManager.setRenderer(renderer);
        NonHierarchicalDistanceBasedAlgorithm<ClusterLand> algorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
        algorithm.setMaxDistanceBetweenClusteredItems(50);
        clusterManager.setAlgorithm(algorithm);


        mMap.setOnCameraMoveStartedListener(reason-> cameraMoving = true);
        mMap.setOnCameraIdleListener(()->{
            cameraMoving = false;
            clusterManager.onCameraIdle();
        });
        clusterManager.setOnClusterItemClickListener(this::onClusterItemClick);
        clusterManager.setOnClusterClickListener(this::onClusterClick);
        if(firstLandUpdate){
            initLandHolder();
        }else{
            updateMap();
        }
    }

    private void initLandHolder() {
        if (getActivity() != null) {
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getLands().observe(getViewLifecycleOwner(), this::onLandsUpdate);
        }
    }

    private void initZoneHolder() {
        if (getActivity() != null) {
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getLandZones().observe(getViewLifecycleOwner(), this::onZonesUpdate);
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
    }

    private boolean onClusterItemClick(ClusterLand item) {
        if(mMap == null) return false;

        cameraMoving = true;
        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : item.getLandData().getBorder()){
            size++;
            builder.include(point);
        }
        if(size > 0){
            mMap.stopAnimation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private boolean onClusterClick(Cluster<ClusterLand> cluster) {
        if(mMap == null) return false;

        cameraMoving = true;
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private void zoomOnLands(){
        if(mMap == null) return;

        cameraMoving = true;
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
                isInit = false;
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
                binding.mvLiveMap.setVisibility(View.VISIBLE);
            }else{
                mMap.stopAnimation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
            }
        }
    }

    private void updateMap(){
        if(clusterManager == null) return;

        clusterManager.clearItems();
        data.forEach((land,zones)-> clusterManager.addItem(new ClusterLand(land,zones)));
        clusterManager.cluster();

        zoomOnLands();
    }

    private void displayNotification(String title, String msg){
        if(notificationManager == null || getContext() == null) return;

        if(title == null || title.isEmpty()){
            if(notification == null) return;

            notificationTitle = "";
            notificationMsg = "";
            notification = null;
            notificationManager.cancel(NotificationID);
            return;
        }

        if(notificationNeedUpdate(title,msg)) {
            if (notification != null) notificationManager.cancel(NotificationID);

            notificationTitle = title;
            Notification.Builder builder = new Notification
                    .Builder(getContext(), AppValues.NotificationChannelID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notificationTitle);
            if (msg == null || msg.isEmpty()) {
                notificationMsg = "";
            } else {
                notificationMsg = msg;
                builder.setContentText(notificationMsg);
            }
            notification = builder.build();
            notificationManager.notify(NotificationID, notification);
        }
    }

    private boolean notificationNeedUpdate(String t, String m) {
        String title = t;
        String msg = m;
        if(title == null) title = "";
        if(msg == null) msg = "";
        return !(title.equals(notificationTitle) && msg.equals(notificationMsg));
    }

    private void goBack(){
        if(getActivity() != null) getActivity().onBackPressed();
    }
}