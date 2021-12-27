package com.mosc.simo.ptuxiaki3741.fragments.land;

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
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapLandPreviewFragment extends Fragment implements FragmentBackPress {
    private static final String TAG = "MapLandPreviewFragment";
    private FragmentLandMapPreviewBinding binding;

    private GoogleMap mMap;

    private List<LandZone> currLandZones;
    private Land currLand;
    private boolean isHistory;

    //init relative
    private void initData(){
        currLandZones = new ArrayList<>();
        isHistory = false;
        currLand = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argIsHistory)){
                isHistory = getArguments().getBoolean(AppValues.argIsHistory);
                //FIXME: SAVE ZONES ON LAND HISTORY
            }
            if(getArguments().containsKey(AppValues.argLand)){
                currLand = getArguments().getParcelable(AppValues.argLand);
            }
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
                                " #"+ currLand.getData().getId();
                actionBar.setTitle(display);
                actionBar.show();
            }
        }
    }
    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
            if(!isHistory){
                appVM.getLandZones().observe(getViewLifecycleOwner(),this::onLandZoneUpdate);
            }
        }
    }
    private void initFragment(){
        binding.mvLand.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap) {
        binding.mvLand.setVisibility(View.INVISIBLE);
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        drawLandOnMap();
    }

    //map relative
    private void drawLandOnMap() {
        if (mMap != null) {
            mMap.clear();
            int strokeColor, fillColor;
            if(currLand != null){
                strokeColor = Color.argb(
                    AppValues.defaultStrokeAlpha,
                    currLand.getData().getColor().getRed(),
                    currLand.getData().getColor().getGreen(),
                    currLand.getData().getColor().getBlue()
                );
                fillColor = Color.argb(
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
                if(options != null)
                    mMap.addPolygon(options.zIndex(1));
                for(LandZone zone:currLandZones){
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
                    if(options != null)
                        mMap.addPolygon(options.zIndex(2));
                }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(LatLng point : currLand.getData().getBorder()){
                    builder.include(point);
                }
                binding.mvLand.setVisibility(View.VISIBLE);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        AppValues.defaultPadding
                ));
            }
        }
    }

    //observers
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
    private void onLandZoneUpdate(Map<Long,List<LandZone>> zones) {
        currLandZones.clear();
        if(currLand != null){
            List<LandZone> temp = zones.getOrDefault(currLand.getData().getId(),null);
            if(temp != null){
                currLandZones.addAll(temp);
            }
        }
        drawLandOnMap();
    }

    //restore relative
    private void restoreLand() {
        AsyncTask.execute(()->{
            //FIXME: Restore ZONES ON LAND HISTORY
            restoreToVM();
            finish(getActivity());
        });
    }
    private void restoreToVM() {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.restoreLand(currLand);
        }
    }

    //navigation
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
            editItem.setVisible(!isHistory);
            editItem.setEnabled(!isHistory);
        }
        if(historyItem != null){
            historyItem.setVisible(!isHistory);
            historyItem.setEnabled(!isHistory);
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