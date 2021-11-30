package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class MenuMainFragment extends Fragment implements FragmentBackPress {
    //todo: (fix) map click accuracy
    //todo: (idea) delete map
    private UserViewModel vmUsers;
    private LandViewModel vmLands;
    private List<Polygon> landPolygons,zonesPolygons;
    private List<Land> data1;
    private List<LandZone> data2;
    private GoogleMap mMap;
    private boolean drawPolygon, firstLoad;

    private FragmentMenuMainBinding binding;
    private ActionBar actionBar;

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
        actionBar = null;
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }
    private void initViewModels() {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
        }
    }
    private void initUserObservers() {
        if(vmUsers != null){
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            vmUsers.getReceivedRequestList().observe(getViewLifecycleOwner(),this::onFriendRequestListUpdate);
        }
    }
    private void initFragment() {
        binding.mainMenuAction.setVisibility(View.VISIBLE);
        binding.mainMenuAction.setText(getString(R.string.main_menu_loading));
        binding.btnLands.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnHistory.setOnClickListener(v -> toLandsZone(getActivity()));
        binding.btnContacts.setOnClickListener(v -> toUserContacts(getActivity()));
        binding.btnProfile.setOnClickListener(v -> toProfile(getActivity()));
        binding.mvLands.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.setOnMapClickListener(p -> OnMapClick());
        mMap.setOnPolygonClickListener(this::OnPolygonClick);
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
    private void onCurrUserUpdate(User user) {
        if(user != null){
            if(actionBar != null){
                actionBar.setTitle(user.getUsername());
            }
        }else{
            toLogin(getActivity());
        }
    }
    private void onFriendRequestListUpdate(List<User> requests) {
        updateRequestNotification(requests);
    }
    private void onLandUpdate(List<Land> newData) {
        AsyncTask.execute(()->{
            data1.clear();
            if(newData != null){
                for(Land temp:newData) {
                    if(temp.getPerm().isRead()){
                        data1.add(temp);
                    }
                }
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
    private void OnPolygonClick(Polygon polygon){
        drawMap(polygon);
    }
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
                int strokeColor,fillColor;
                if(getContext() != null){
                    strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
                    fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
                }else{
                    strokeColor = AppValues.strokeColor;
                    fillColor = AppValues.fillColor;
                }
                int size = 0;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(Land land:data1){
                    PolygonOptions options = LandUtil.getPolygonOptions(
                            land.getData(),
                            strokeColor,
                            fillColor,
                            true
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
                int strokeColor,fillColor;
                if(getContext() != null){
                    strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke2);
                    fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill2);
                }else{
                    strokeColor = AppValues.strokeColor2;
                    fillColor = AppValues.fillColor2;
                }
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
    private void drawMap(Polygon polygon){
        drawPolygon = true;
        if(mMap != null){
            int strokeColor,fillColor;
            if(getContext() != null){
                strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
                fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
            }else{
                strokeColor = AppValues.strokeColor;
                fillColor = AppValues.fillColor;
            }
            PolygonOptions options = LandUtil.getPolygonOptions(
                    new LandData(polygon.getPoints(),polygon.getHoles()),
                    strokeColor,
                    fillColor,
                    true
            );
            if(options != null){
                mMap.clear();
                mMap.addPolygon(options);
                mMap.addMarker(new MarkerOptions()
                        .position(MapUtil.getPolygonCenter(polygon.getPoints()))
                        .draggable(false)
                );

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(LatLng point:polygon.getPoints()){
                    builder.include(point);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        AppValues.defaultPadding
                ));
            }

        }
    }

    //ui
    private void updateRequestNotification(List<User> requests) {
        if(requests.size()>0){
            binding.mcvRequestLayout.setVisibility(View.VISIBLE);
            if(requests.size()>99){
                binding.tvRequestNumber.setText(R.string.max_request_label);
            }else{
                binding.tvRequestNumber.setText(String.valueOf(requests.size()));
            }
        }else{
            binding.mcvRequestLayout.setVisibility(View.GONE);
            binding.tvRequestNumber.setText(String.valueOf(0));
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
    public void toProfile(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toProfileUser);
            });
    }
    public void toLogin(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuMainFragment);
                if(nav != null)
                    nav.navigate(R.id.toLoading);
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
        initUserObservers();
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
        inflater.inflate(R.menu.main_menu_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_app_settings) {
            toSettings(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }
}