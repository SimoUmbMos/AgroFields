package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.mosc.simo.ptuxiaki3741.data.models.ClusterLand;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentFileMapBinding;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;
import com.mosc.simo.ptuxiaki3741.ui.renderers.LandRendered;

import java.util.ArrayList;
import java.util.List;

public class MapFileFragment extends Fragment {
    private FragmentFileMapBinding binding;
    private LoadingDialog loadingDialog;
    private GoogleMap mMap;

    private ClusterManager<ClusterLand> clusterManager;
    private final List<LandData> landDataList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileMapBinding.inflate(inflater,container,false);
        binding.mvFileMap.onCreate(savedInstanceState);
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
        binding.mvFileMap.onDestroy();
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mvFileMap.onResume();
    }

    @Override
    public void onPause() {
        binding.mvFileMap.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        binding.mvFileMap.onLowMemory();
        super.onLowMemory();
    }

    private void initData(){
        clusterManager = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLands)){
                landDataList.addAll(
                        getArguments().getParcelableArrayList(AppValues.argLands)
                );
            }
        }
    }

    private void initActivity(){
        if(getActivity() == null) return;
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setOnBackPressed(()->true);
        loadingDialog = mainActivity.getLoadingDialog();
        mainActivity.setOverrideDoubleBack(true);
    }

    private void initFragment(){
        binding.ibClose.setOnClickListener(v->goBack());
        if(landDataList.size() < 2) binding.ibZoom.setVisibility(View.GONE);
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST,r->binding.mvFileMap.getMapAsync(this::initMap));
    }

    private void initMap(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMinZoomPreference(AppValues.minZoom);
        mMap.setMaxZoomPreference(AppValues.maxZoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.075368, 23.553767),16));

        if(getActivity() == null) return;

        if(loadingDialog != null) loadingDialog.openDialog();

        clusterManager = new ClusterManager<>(getActivity(), mMap);
        LandRendered renderer = new LandRendered(getActivity(), mMap, clusterManager);
        renderer.setMinClusterSize(2);
        renderer.setAnimation(false);
        clusterManager.setRenderer(renderer);
        NonHierarchicalDistanceBasedAlgorithm<ClusterLand> algorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
        algorithm.setMaxDistanceBetweenClusteredItems(60);
        clusterManager.setAlgorithm(algorithm);
        mMap.setOnCameraIdleListener(()-> new Handler().post(clusterManager::onCameraIdle));

        binding.ibZoom.setOnClickListener(v->zoomOnLands());
        clusterManager.setOnClusterClickListener(this::zoomOnCluster);
        clusterManager.setOnClusterItemClickListener(this::zoomOnMarker);

        Handler handler = new Handler();
        handler.post(()->{
            for(int i = 0; i < landDataList.size(); i++){
                landDataList.get(i).setTitle("#"+(i+1));
                clusterManager.addItem(new ClusterLand(landDataList.get(i)));
            }
            clusterManager.cluster();
            zoomOnLands();
            if(loadingDialog != null) loadingDialog.closeDialog();
        });
    }

    private void zoomOnLands(){
        if(mMap == null) return;

        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LandData data : landDataList){
            for(LatLng point : data.getBorder()){
                size++;
                builder.include(point);
            }
        }
        if(size>0){
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
        }
    }

    private boolean zoomOnCluster(Cluster<ClusterLand> cluster) {
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private boolean zoomOnMarker(ClusterLand marker) {
        if(mMap == null) return false;

        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : marker.getLandData().getBorder()){
            size++;
            builder.include(point);
        }
        if(size > 0){
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private void goBack() {
        Activity activity = getActivity();
        if(activity != null) {
            activity.onBackPressed();
        }
    }
}