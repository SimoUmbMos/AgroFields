package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandMapPreviewBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

public class LandMapPreviewFragment extends Fragment implements FragmentBackPress {
    //todo: (idea) make save memo based on user
    private FragmentLandMapPreviewBinding binding;

    private GoogleMap mMap;

    private Land currLand;
    private User currUser;
    private boolean isHistory;

    //init relative
    private void initData(){
        Bundle args = getArguments();
        if(args != null){
            if(args.containsKey(AppValues.argIsHistoryLandMapPreviewFragment)){
                isHistory = args.getBoolean(AppValues.argIsHistoryLandMapPreviewFragment);
            }else{
                isHistory = false;
            }
            if(args.containsKey(AppValues.argLandLandMapPreviewFragment)){
                currLand = args.getParcelable(AppValues.argLandLandMapPreviewFragment);
            }else{
                currLand = null;
            }
        }else{
            isHistory = false;
            currLand = null;
        }
    }
    private void initActivity(){
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
            activity.setOnBackPressed(this);
            if(currLand == null){
                activity.onBackPressed();
                return;
            }
            ActionBar actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                String display = currLand.getData().getTitle()+
                                " #"+ EncryptUtil.convert4digit(currLand.getData().getId());
                actionBar.setTitle(display);
                actionBar.show();
            }
        }
    }
    private void initViewModel(){
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onUserUpdate);
        }
    }
    private void initFragment(){
        binding.mvLand.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMinZoomPreference(10);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        drawLandOnMap();
    }

    //map relative
    private void drawLandOnMap() {
        if (currLand != null) {
            int strokeColor,fillColor;
            if(getContext() != null){
                strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
                fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
            }else{
                strokeColor = Color.argb(192,0,0,255);
                fillColor = Color.argb(51,0,0,255);
            }
            PolygonOptions options = MapUtil.getPolygonOptions(
                    currLand,
                    strokeColor,
                    fillColor,
                    false
            );
            if(options != null)
                mMap.addPolygon(options);
            if(!isHistory){
                mMap.addMarker(new MarkerOptions()
                        .position(MapUtil.getPolygonCenter(currLand.getData().getBorder()))
                );
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng point : currLand.getData().getBorder()){
                builder.include(point);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPadding
            ));
        }
    }

    //observers
    private void onUserUpdate(User user) {
        currUser = user;
    }

    //restore relative
    private void restoreLand() {
        if(isValidToRestore()){
            restoreToVM();
            finish(getActivity());
        }
    }
    private boolean isValidToRestore() {
        if(currUser != null){
            return currLand.getData().getCreator_id() == currUser.getId();
        }
        return false;
    }
    private void restoreToVM() {
        if(getActivity() != null){
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmLands.restoreLand(currLand);
        }
    }

    //navigation
    public void toLandEdit(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandMapPreviewFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandLandMapFragment,currLand);
                if(nav != null)
                    nav.navigate(R.id.landPreviewToLandMap,bundle);
            });
    }
    public void finish(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(activity::onBackPressed);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLandMapPreviewBinding.inflate(inflater,container,false);
        binding.mvLand.onCreate(savedInstanceState);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModel();
        initFragment();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding.mvLand.onDestroy();
        binding = null;
    }
    @Override public void onResume() {
        super.onResume();
        binding.mvLand.onResume();
    }
    @Override public void onPause() {
        super.onPause();
        binding.mvLand.onPause();
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        binding.mvLand.onLowMemory();
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.land_map_preview_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem editItem = menu.findItem(R.id.menu_item_edit_land);
        MenuItem restoreItem = menu.findItem(R.id.menu_item_restore_land);
        if(editItem != null){
            editItem.setVisible(!isHistory);
            editItem.setEnabled(!isHistory);
        }
        if(restoreItem != null){
            restoreItem.setVisible(isHistory);
            restoreItem.setEnabled(isHistory);
        }
        super.onPrepareOptionsMenu(menu);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case(R.id.menu_item_edit_land):
                toLandEdit(getActivity());
                return true;
            case(R.id.menu_item_restore_land):
                restoreLand();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override public boolean onBackPressed() {
        return true;
    }
}