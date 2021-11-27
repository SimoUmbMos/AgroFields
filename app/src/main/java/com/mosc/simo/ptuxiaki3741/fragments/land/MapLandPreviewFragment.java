package com.mosc.simo.ptuxiaki3741.fragments.land;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.util.Log;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandMapPreviewBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class MapLandPreviewFragment extends Fragment implements FragmentBackPress {
    private static final String TAG = "MapLandPreviewFragment";
    //fixme: (code) show land zones
    private FragmentLandMapPreviewBinding binding;

    private GoogleMap mMap;

    private List<LandZone> currLandZones;
    private Land currLand;
    private User currUser;
    private boolean isHistory;

    //init relative
    private void initData(){
        currLandZones = new ArrayList<>();
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
            LandViewModel vmLand = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onUserUpdate);
            vmLand.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
            vmLand.getLandZones().observe(getViewLifecycleOwner(),this::onLandZoneUpdate);
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
        if (currLand != null && mMap != null) {
            mMap.clear();
            int strokeColor,fillColor;
            if(getContext() != null){
                strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
                fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
            }else{
                strokeColor = AppValues.strokeColor;
                fillColor = AppValues.fillColor;
            }
            PolygonOptions options = LandUtil.getPolygonOptions(
                    currLand.getData(),
                    strokeColor,
                    fillColor,
                    false
            );
            if(options != null)
                mMap.addPolygon(options);
            for(LandZone zone:currLandZones){
                options = LandUtil.getPolygonOptions(
                        new LandData(zone.getData().getBorder()),
                        strokeColor,
                        fillColor,
                        false
                );
                if(options != null)
                    mMap.addPolygon(options);
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
    private void onLandUpdate(List<Land> lands) {
        if(currLand != null){
            for(Land temp:lands){
                if(temp.getData().getId() == currLand.getData().getId() && !isHistory){
                    currLand.setData(temp.getData());
                }
            }
        }
        drawLandOnMap();
    }
    private void onLandZoneUpdate(List<LandZone> zones) {
        currLandZones.clear();
        if(currLand != null){
            for(LandZone zone:zones){
                if(zone.getData().getLid() == currLand.getData().getId()){
                    currLandZones.add(zone);
                }
            }
        }
        drawLandOnMap();
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
            if(currLand !=null){
                return currLand.getPerm().isAdmin() ||
                        currLand.getData().getCreator_id() == currUser.getId();
            }
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
                NavController nav = UIUtil.getNavController(this,R.id.MapLandPreviewFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandLandMapFragment,currLand);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }
    public void toLandHistory(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MapLandPreviewFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandHistoryFragment,currLand);
                if(nav != null)
                    nav.navigate(R.id.toLandHistory,bundle);
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
        Log.d(TAG, "onViewCreated");
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding.mvLand.onDestroy();
        binding = null;
        Log.d(TAG, "onDestroyView");
    }
    @Override public void onResume() {
        super.onResume();
        binding.mvLand.onResume();
        Log.d(TAG, "onResume");
    }
    @Override public void onPause() {
        super.onPause();
        binding.mvLand.onPause();
        Log.d(TAG, "onPause");
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        binding.mvLand.onLowMemory();
        Log.d(TAG, "onLowMemory");
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.land_map_preview_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem editItem = menu.findItem(R.id.menu_item_edit_land);
        MenuItem historyItem = menu.findItem(R.id.menu_item_history_land);
        MenuItem restoreItem = menu.findItem(R.id.menu_item_restore_land);
        if(editItem != null){
            if(currLand.getPerm().isWrite()){
                editItem.setVisible(!isHistory);
                editItem.setEnabled(!isHistory);
            }else{
                editItem.setVisible(false);
                editItem.setEnabled(false);
            }
        }
        if(historyItem != null){
            if(currLand.getPerm().isAdmin()){
                historyItem.setVisible(!isHistory);
                historyItem.setEnabled(!isHistory);
            }else{
                historyItem.setVisible(false);
                historyItem.setEnabled(false);
            }
        }
        if(restoreItem != null){
            if(currLand.getPerm().isAdmin()){
                restoreItem.setVisible(isHistory);
                restoreItem.setEnabled(isHistory);
            }else{
                restoreItem.setVisible(false);
                restoreItem.setEnabled(false);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case(R.id.menu_item_edit_land):
                toLandEdit(getActivity());
                return true;
            case(R.id.menu_item_history_land):
                toLandHistory(getActivity());
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