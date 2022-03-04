package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandMapPreviewBinding;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LandPreviewFragment extends Fragment {
    private final List<Polygon> mapZones = new ArrayList<>();
    private Polygon mapLand;
    private FragmentLandMapPreviewBinding binding;

    private GoogleMap mMap;
    private List<LandZone> currLandZones;
    private Land currLand;
    private boolean isHistory;

    //init relative
    private void initData(){
        isHistory = false;
        currLand = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argIsHistory)){
                isHistory = getArguments().getBoolean(AppValues.argIsHistory);
            }
            if(getArguments().containsKey(AppValues.argLand)){
                currLand = getArguments().getParcelable(AppValues.argLand);
            }
            if(getArguments().containsKey(AppValues.argZones)){
                currLandZones = getArguments().getParcelableArrayList(AppValues.argZones);
            }
        }
        if(currLandZones == null) currLandZones = new ArrayList<>();
    }

    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(()->true);
            }
        }
    }

    private void initMenu(){
        binding.tvTitle.setText(currLand.toString());
        binding.ibEdit.setEnabled(!isHistory);
        binding.fabZonesMenu.setEnabled(!isHistory);
        binding.ibHistory.setEnabled(!isHistory);
        binding.ibRestore.setEnabled(isHistory);
        if(isHistory){
            binding.ibEdit.setVisibility(View.GONE);
            binding.fabZonesMenu.setVisibility(View.GONE);
            binding.ibHistory.setVisibility(View.GONE);
            binding.ibRestore.setVisibility(View.VISIBLE);
        }else{
            binding.ibEdit.setVisibility(View.VISIBLE);
            binding.fabZonesMenu.setVisibility(View.VISIBLE);
            binding.ibHistory.setVisibility(View.VISIBLE);
            binding.ibRestore.setVisibility(View.GONE);
        }
    }

    private void initFragment(){
        binding.ibEdit.setOnClickListener( v -> toLandEdit(getActivity()) );
        binding.ibHistory.setOnClickListener( v -> toLandHistory(getActivity()) );
        binding.ibRestore.setOnClickListener( v -> restoreLand() );
        binding.fabZonesMenu.setOnClickListener( v -> toZonesMenu(getActivity()) );
        binding.mvLand.getMapAsync(this::initMap);
    }

    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(AppValues.countryZoom-1);
        mMap.setMaxZoomPreference(AppValues.streetZoom+1);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if(isHistory){
            drawLandOnMap();
            drawZonesOnMap();
        }else{
            initViewModel();
        }
    }

    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
            appVM.getLandZones().observe(getViewLifecycleOwner(),this::onLandZoneUpdate);
        }
    }

    //map draw relative
    private void drawLandOnMap() {
        if(mMap == null) return;

        if(mapLand != null) {
            mapLand.remove();
            mapLand = null;
        }

        int strokeColor = Color.argb(
                AppValues.defaultStrokeAlpha,
                currLand.getData().getColor().getRed(),
                currLand.getData().getColor().getGreen(),
                currLand.getData().getColor().getBlue()
        );
        int fillColor = Color.argb(
                AppValues.defaultFillAlpha,
                currLand.getData().getColor().getRed(),
                currLand.getData().getColor().getGreen(),
                currLand.getData().getColor().getBlue()
        );

        PolygonOptions options = LandUtil.getPolygonOptions(
                currLand.getData(),
                strokeColor,
                fillColor,
                false
        );
        if(options == null) return;

        mapLand = mMap.addPolygon(options.zIndex(1));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : mapLand.getPoints()){
            builder.include(point);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                builder.build(),
                AppValues.defaultPadding
        ));

    }

    private void drawZonesOnMap() {
        if(mMap == null) return;

        for(Polygon zone : mapZones){
            zone.remove();
        }
        mapZones.clear();

        for(LandZone zone:currLandZones){
            int strokeColor = Color.argb(
                    AppValues.defaultStrokeAlpha,
                    zone.getData().getColor().getRed(),
                    zone.getData().getColor().getGreen(),
                    zone.getData().getColor().getBlue()
            );
            int fillColor = Color.argb(
                    AppValues.defaultFillAlpha,
                    zone.getData().getColor().getRed(),
                    zone.getData().getColor().getGreen(),
                    zone.getData().getColor().getBlue()
            );
            PolygonOptions options = LandUtil.getPolygonOptions(
                    zone.getData(),
                    strokeColor,
                    fillColor,
                    false
            );
            if(options != null) {
                mapZones.add(mMap.addPolygon(options.zIndex(2)));
            }
        }
    }

    //observers
    private void onLandUpdate(List<Land> lands) {
        for(Land temp:lands){
            if(temp.getData() == null) continue;
            if(temp.getData().getId() == currLand.getData().getId()){
                currLand.setData(temp.getData());
                break;
            }
        }
        drawLandOnMap();
    }

    private void onLandZoneUpdate(Map<Long,List<LandZone>> zones) {
        currLandZones.clear();
        List<LandZone> temp = zones.getOrDefault(currLand.getData().getId(),null);
        if(temp != null){
            currLandZones.addAll(temp);
        }
        drawZonesOnMap();
    }

    //restore relative
    private void restoreLand() {
        AsyncTask.execute(()->{
            restoreToVM();
            goBack(getActivity());
        });
    }

    private void restoreToVM() {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.restoreLand(currLand, currLandZones);
        }
    }

    //navigation
    public void goBack(@Nullable Activity activity) {
        if(activity != null) activity.runOnUiThread(activity::onBackPressed);
    }

    public void toLandEdit(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MapLandPreviewFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,currLand);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }

    public void toZonesMenu(@Nullable Activity activity) {
        if(currLand == null) return;
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this, R.id.MapLandPreviewFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand, currLand);
                if(nav != null){
                    nav.navigate(R.id.toZonesLandSelected,bundle);
                }
            });
    }

    public void toLandHistory(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MapLandPreviewFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,currLand);
                if(nav != null)
                    nav.navigate(R.id.toLandHistory,bundle);
            });
    }

    //overrides
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        binding = FragmentLandMapPreviewBinding.inflate(inflater,container,false);
        binding.mvLand.onCreate(savedInstanceState);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        if(currLand != null){
            initMenu();
            initFragment();
        }else{
            goBack(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mvLand.onResume();
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
}