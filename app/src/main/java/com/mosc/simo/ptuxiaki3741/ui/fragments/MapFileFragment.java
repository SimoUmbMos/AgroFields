package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentFileMapBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.ImportAction;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class MapFileFragment extends Fragment {
    private final List<LandData> landDataList = new ArrayList<>();
    private FragmentFileMapBinding binding;
    private LandData landData;
    private ImportAction action;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileMapBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment(savedInstanceState);
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

    private void initFragment(Bundle savedInstanceState){
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
            default:
                binding.tvTitle.setText("");
                break;
        }
        binding.ibClose.setOnClickListener(v->onButtonClosePress());
        binding.mvFileMap.onCreate(savedInstanceState);
        binding.tvLoadingLabel.setVisibility(View.VISIBLE);
        binding.tvLoadingLabel.setText(getString(R.string.file_map_fragment_loading));
        binding.mvFileMap.getMapAsync(this::initMap);
    }

    private void initMap(GoogleMap googleMap){
        boolean isPreview = landData == null;
        googleMap.getUiSettings().setRotateGesturesEnabled(isPreview);
        googleMap.getUiSettings().setScrollGesturesEnabled(isPreview);
        googleMap.getUiSettings().setZoomGesturesEnabled(isPreview);
        googleMap.getUiSettings().setZoomControlsEnabled(isPreview);
        googleMap.getUiSettings().setCompassEnabled(isPreview);
        if(!isPreview) {
            googleMap.setOnPolygonClickListener(this::onPolygonClick);
        }
        googleMap.setOnMapLoadedCallback(()->drawMap(googleMap));
    }

    private void drawMap(GoogleMap googleMap) {
        PolygonOptions options;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int builderSize = 0;
        boolean isClickable = false;
        int strokeColor , fillColor;
        if(landData != null){
            fillColor = Color.argb(
                    AppValues.defaultFillAlpha,
                    landData.getColor().getRed(),
                    landData.getColor().getGreen(),
                    landData.getColor().getBlue()
            );
            options = LandUtil.getPolygonOptions(landData,fillColor,fillColor,false);
            if(options != null)
                googleMap.addPolygon(options.zIndex(1));
            for(LatLng point:landData.getBorder()){
                builder.include(point);
                builderSize++;
            }
            isClickable = true;
        }
        for(LandData data:landDataList){
            strokeColor = Color.argb(
                    AppValues.defaultStrokeAlpha,
                    data.getColor().getRed(),
                    data.getColor().getGreen(),
                    data.getColor().getBlue()
            );
            fillColor = Color.argb(
                    AppValues.defaultFillAlpha,
                    data.getColor().getRed(),
                    data.getColor().getGreen(),
                    data.getColor().getBlue()
            );
            options = LandUtil.getPolygonOptions(data,strokeColor,fillColor,isClickable);
            if(options != null)
                googleMap.addPolygon(options.zIndex(2));
            for(LatLng point:data.getBorder()){
                builder.include(point);
                builderSize++;
            }
        }
        if(builderSize > 0)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPadding
            ));
        if(landDataList.size()>0){
            binding.tvLoadingLabel.setVisibility(View.GONE);
        }else{
            binding.tvLoadingLabel.setText(getString(R.string.file_map_fragment_error));
        }
    }

    private void onButtonClosePress() {
        Activity activity = getActivity();
        if(activity != null) {
            activity.onBackPressed();
        }
    }

    private void onPolygonClick(Polygon polygon) {
        if(landData != null){
            LandData result = null;
            switch (action){
                case ADD:
                    result = LandUtil.uniteLandData(
                            landData,
                            new LandData(polygon.getPoints(),polygon.getHoles())
                    );
                    break;
                case SUBTRACT:
                    result = LandUtil.subtractLandData(
                            landData,
                            new LandData(polygon.getPoints(),polygon.getHoles())
                    );
                    break;
                case IMPORT:
                    result = new LandData(polygon.getPoints(),polygon.getHoles());
                    break;
            }
            if(result != null){
                landData.setBorder(result.getBorder());
                landData.setHoles(result.getHoles());
                toLandEdit(getActivity());
            }
        }
    }

    private void toLandEdit(Activity activity) {
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