package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class MenuMainFragment extends Fragment implements FragmentBackPress {
    //todo: (fix) map click accuracy
    //todo: (idea) delete map
    private AppViewModel vmLands;
    private List<Polygon> landPolygons,zonesPolygons;
    private List<Land> data1;
    private List<LandZone> data2;
    private GoogleMap mMap;
    private boolean drawPolygon, firstLoad;

    private FragmentMenuMainBinding binding;

    //init
    private void initData(){
        landPolygons = new ArrayList<>();
        zonesPolygons = new ArrayList<>();
        data1 = new ArrayList<>();
        data2 = new ArrayList<>();
        drawPolygon = false;
        firstLoad = true;
    }
    private void initActivity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.hide();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }
    private void initViewModels() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }
    private void initFragment() {
        binding.mainMenuAction.setVisibility(View.VISIBLE);
        binding.mainMenuAction.setText(getString(R.string.main_menu_loading));
        binding.btnLands.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnHistory.setOnClickListener(v -> toLandsZone(getActivity()));
        binding.btnContacts.setOnClickListener(v -> toUserContacts(getActivity()));
        binding.btnSettings.setOnClickListener(v -> toSettings(getActivity()));
        binding.mvLands.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.setOnMapClickListener(p -> OnMapClick());
        if(binding != null){
            binding.mvLands.setVisibility(View.INVISIBLE);
        }
        initLandObservers();
    }
    private void initLandObservers() {
        if(vmLands != null){
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
            vmLands.getLandZones().observe(getViewLifecycleOwner(),this::onLandZoneUpdate);
        }
    }


    //observers
    private void onLandUpdate(List<Land> newData) {
        AsyncTask.execute(()->{
            data1.clear();
            if(newData != null){
                data1.addAll(newData);
            }
            if(getActivity() != null)
                getActivity().runOnUiThread(()->{
                    drawMapLands();
                    if(firstLoad){
                        firstLoad = false;
                        binding.mainMenuAction.setText(getString(R.string.main_menu_no_lands));
                    }
                });
        });
    }
    private void onLandZoneUpdate(List<LandZone> newData) {
        AsyncTask.execute(()->{
            data2.clear();
            if(newData != null){
                data2.addAll(newData);
            }
            if(getActivity() != null)
                getActivity().runOnUiThread(this::drawMapZones);
        });
    }

    //map
    private void OnMapClick(){
        if(drawPolygon) {
            drawMapLands();
            drawMapZones();
        }
    }
    private void drawMapLands(){
        drawPolygon = false;
        if(mMap != null){
            if(landPolygons.size()>0){
                for(Polygon polygon:landPolygons){
                    polygon.remove();
                }
                landPolygons.clear();
            }
            if(data1.size()>0){
                binding.mvLands.setVisibility(View.VISIBLE);
                binding.mainMenuAction.setVisibility(View.GONE);

                int strokeColor = Color.argb(
                        AppValues.defaultStrokeAlpha,
                        AppValues.defaultLandColor.getRed(),
                        AppValues.defaultLandColor.getGreen(),
                        AppValues.defaultLandColor.getBlue()
                );
                int fillColor = Color.argb(
                        AppValues.defaultFillAlpha,
                        AppValues.defaultLandColor.getRed(),
                        AppValues.defaultLandColor.getGreen(),
                        AppValues.defaultLandColor.getBlue()
                );
                int size = 0;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(Land land:data1){
                    PolygonOptions options = LandUtil.getPolygonOptions(
                            land.getData(),
                            strokeColor,
                            fillColor,
                            false
                    );
                    if(options != null){
                        landPolygons.add(mMap.addPolygon(options.zIndex(1)));
                    }
                    for(LatLng point: land.getData().getBorder()){
                        builder.include(point);
                        size++;
                    }
                }
                if(size > 0){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                            builder.build(),
                            AppValues.defaultPadding
                    ));
                }else{
                    binding.mvLands.setVisibility(View.INVISIBLE);
                    binding.mainMenuAction.setVisibility(View.VISIBLE);
                }
            }else{
                binding.mvLands.setVisibility(View.INVISIBLE);
                binding.mainMenuAction.setVisibility(View.VISIBLE);
            }
        }
    }
    private void drawMapZones(){
        drawPolygon = false;
        if(mMap != null){
            if(zonesPolygons.size()>0){
                for(Polygon polygon:zonesPolygons){
                    polygon.remove();
                }
                zonesPolygons.clear();
            }
            if(data2.size()>0){
                int strokeColor = Color.argb(
                        AppValues.defaultStrokeAlpha,
                        AppValues.defaultZoneColor.getRed(),
                        AppValues.defaultZoneColor.getGreen(),
                        AppValues.defaultZoneColor.getBlue()
                );
                int fillColor = Color.argb(
                        AppValues.defaultFillAlpha,
                        AppValues.defaultZoneColor.getRed(),
                        AppValues.defaultZoneColor.getGreen(),
                        AppValues.defaultZoneColor.getBlue()
                );

                for(LandZone zone:data2){
                    PolygonOptions options = LandUtil.getPolygonOptions(
                            zone.getData(),
                            strokeColor,
                            fillColor,
                            false
                    );
                    if(options != null){
                        zonesPolygons.add(mMap.addPolygon(options.zIndex(2)));
                    }
                }
            }
        }
    }

    //navigation
    public void toListMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuLands);
            });
    }
    public void toLandsZone(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuZoneLands);
            });
    }
    public void toSettings(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toAppSettings);
            });
    }
    public void toUserContacts(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuContacts);
            });
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        binding.mvLands.onCreate(savedInstanceState);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModels();
        initFragment();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding.mvLands.onDestroy();
        binding = null;
    }
    @Override public void onResume() {
        super.onResume();
        binding.mvLands.onResume();
    }
    @Override public void onPause() {
        super.onPause();
        binding.mvLands.onPause();
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        binding.mvLands.onLowMemory();
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onBackPressed() {
        return true;
    }
}