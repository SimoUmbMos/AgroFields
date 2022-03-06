package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.mosc.simo.ptuxiaki3741.data.models.ClusterLand;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentFileMapBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.ImportAction;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.ui.renderers.LandRendered;

import java.util.ArrayList;
import java.util.List;

public class MapFileFragment extends Fragment {
    private GoogleMap mMap;
    private ClusterManager<ClusterLand> clusterManager;

    private final List<LandData> landDataList = new ArrayList<>();
    private FragmentFileMapBinding binding;
    private LandData landData, result;
    private ImportAction action;

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
        result = null;

        if(landDataList.size() != 0) {
            landDataList.clear();
        }
        landData = null;
        action = ImportAction.NONE;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLands)){
                landDataList.addAll(
                        getArguments().getParcelableArrayList(AppValues.argLands)
                );
            }
            if(getArguments().containsKey(AppValues.argLand)){
                landData = getArguments().getParcelable(AppValues.argLand);
            }
            if(getArguments().containsKey(AppValues.argAction)){
                action = (ImportAction)
                        getArguments().getSerializable(AppValues.argAction);
            }
        }

        if(landData == null) return;
        if(landDataList.size() != 1) return;
        result = landDataList.get(0);
    }

    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(()->true);
                if(action == ImportAction.NONE){
                    mainActivity.setOverrideDoubleBack(true);
                }
            }
        }
    }

    private void initFragment(){
        switch (action){
            case IMPORT:
                binding.tvTitle.setText(getString(R.string.file_map_fragment_import_action));
                break;
            case ADD:
                binding.tvTitle.setText(getString(R.string.file_map_fragment_add_action));
                break;
            case SUBTRACT:
                binding.tvTitle.setText(getString(R.string.file_map_fragment_subtract_action));
                break;
        }
        binding.tvSelect.setVisibility(View.GONE);
        binding.ibSubmit.setVisibility(View.GONE);
        binding.ibSubmit.setOnClickListener(v->submitLand());
        binding.ibClose.setOnClickListener(v->goBack());
        if(landDataList.size() < 2) binding.ibZoom.setVisibility(View.GONE);
        binding.tvLoadingLabel.setVisibility(View.VISIBLE);
        binding.tvLoadingLabel.setText(getString(R.string.file_map_fragment_loading));
        binding.mvFileMap.getMapAsync(this::initMap);
    }

    private void initMap(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMinZoomPreference(AppValues.minZoom);
        mMap.setMaxZoomPreference(AppValues.maxZoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.075368, 23.553767),16));

        boolean isPreview = landData == null;
        mMap.getUiSettings().setRotateGesturesEnabled(isPreview);
        mMap.getUiSettings().setScrollGesturesEnabled(isPreview);
        mMap.getUiSettings().setZoomGesturesEnabled(isPreview);
        mMap.getUiSettings().setZoomControlsEnabled(isPreview);
        mMap.getUiSettings().setCompassEnabled(isPreview);

        if(getActivity() == null) return;

        binding.mvFileMap.setVisibility(View.INVISIBLE);
        clusterManager = new ClusterManager<>(getActivity(), mMap);
        LandRendered renderer = new LandRendered(getActivity(), mMap, clusterManager);
        renderer.setMinClusterSize(2);
        clusterManager.setRenderer(renderer);
        mMap.setOnCameraIdleListener(clusterManager);

        for(int i = 0; i < landDataList.size(); i++){
            landDataList.get(i).setTitle("#"+(i+1));
            clusterManager.addItem(new ClusterLand(landDataList.get(i)));
        }
        clusterManager.cluster();

        if(isPreview) {
            binding.ibZoom.setOnClickListener(v->zoomOnLands(true));
            clusterManager.setOnClusterClickListener(this::zoomOnCluster);
            clusterManager.setOnClusterItemClickListener(this::zoomOnMarker);
        }else{
            binding.tvSelect.setVisibility(View.VISIBLE);
            binding.ibSubmit.setVisibility(View.VISIBLE);
            binding.ibZoom.setOnClickListener(v->zoomOnLandsWithSelect());
            clusterManager.setOnClusterClickListener(this::zoomOnClusterWithSelect);
            clusterManager.setOnClusterItemClickListener(this::zoomOnMarkerWithSelect);
            updateUi();
        }
        mMap.setOnMapLoadedCallback(()->{
            zoomOnLands(false);
            binding.mvFileMap.setVisibility(View.VISIBLE);
            binding.tvLoadingLabel.setVisibility(View.GONE);
        });
    }

    private void zoomOnLands(boolean animate){
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
            if(animate){
                mMap.stopAnimation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
            }else{
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
            }
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
            mMap.stopAnimation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
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
            mMap.stopAnimation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private void zoomOnLandsWithSelect(){
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
            if(landDataList.size() > 1) {
                result = null;
                updateUi();
            }
            mMap.stopAnimation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),AppValues.defaultPaddingLarge));
        }
    }

    private boolean zoomOnClusterWithSelect(Cluster<ClusterLand> cluster) {
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
            if(landDataList.size() > 1) {
                result = null;
                updateUi();
            }
            mMap.stopAnimation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private boolean zoomOnMarkerWithSelect(ClusterLand marker) {
        if(mMap == null) return false;

        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : marker.getLandData().getBorder()){
            size++;
            builder.include(point);
        }
        if(size > 0){
            if(landDataList.size() > 1) {
                result = marker.getLandData();
                updateUi();
            }
            mMap.stopAnimation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPaddingLarge));
            return true;
        }
        return false;
    }

    private void submitLand() {
        if(this.landData == null || this.result == null) return;

        LandData data = new LandData(
                landData.getId(),
                landData.getSnapshot(),
                landData.getTitle(),
                new ColorData(landData.getColor().toString()),
                landData.getBorder(),
                landData.getHoles()
        );
        LandData result = null;
        switch (action){
            case ADD:
                result = LandUtil.uniteLandData(
                        data,
                        this.result
                );
                break;
            case SUBTRACT:
                result = LandUtil.subtractLandData(
                        data,
                        this.result
                );
                break;
            case IMPORT:
                result = this.result;
                break;
        }
        if(result != null){
            data.setBorder(result.getBorder());
            data.setHoles(result.getHoles());
            toLandEdit(getActivity(),data);
        }
    }

    private void updateUi() {
        if(landData == null) return;
        if( result != null ){
            binding.ibSubmit.setEnabled(true);
            binding.tvSelect.setText(result.getTitle());
        }else{
            binding.ibSubmit.setEnabled(false);
            binding.tvSelect.setText(getString(R.string.file_map_fragment_no_land_selected));
        }
    }

    private void goBack() {
        Activity activity = getActivity();
        if(activity != null) {
            activity.onBackPressed();
        }
    }

    private void toLandEdit(Activity activity, LandData landData) {
        if(activity != null)
            activity.runOnUiThread(()->{
                NavController nav = UIUtil.getNavController(this,R.id.MapFileFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argImportLand, landData);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }
}