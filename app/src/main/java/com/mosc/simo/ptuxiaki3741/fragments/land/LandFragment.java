package com.mosc.simo.ptuxiaki3741.fragments.land;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.ImportActivity;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.helpers.LandHelper;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.fragments.land.controllers.LandFileController;
import com.mosc.simo.ptuxiaki3741.fragments.land.controllers.LandImgController;
import com.mosc.simo.ptuxiaki3741.fragments.land.controllers.LandPointsController;
import com.mosc.simo.ptuxiaki3741.fragments.land.helpers.LandFileState;
import com.mosc.simo.ptuxiaki3741.fragments.land.holders.LandMapHolder;
import com.mosc.simo.ptuxiaki3741.fragments.land.holders.LandMenuHolder;
import com.mosc.simo.ptuxiaki3741.fragments.land.holders.LandViewHolder;
import com.mosc.simo.ptuxiaki3741.models.ParcelablePolygon;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.interfaces.OnAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LandFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandFragment";
    private LandMapHolder mapHolder;
    private LandViewHolder viewHolder;
    private LandMenuHolder menuHolder;
    private LandFileController fileController;
    private LandPointsController pointsController;
    private Land currLand = null;

    private final ActivityResultLauncher<Intent> fileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::FileResult
    );
    private final ActivityResultLauncher<Intent> imgResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::ViewHolderResult
    );
    private final ActivityResultLauncher<Intent> mapResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::MapResult
    );
    private final ActivityResultLauncher<String> permissionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onRequestPermissionsResult
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_land_map, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        viewHolder.miLock = menu.findItem(R.id.menu_item_toggle_map_lock);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        menuHolder.menuItemClick(item.getItemId());
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onBackPressed() {
        if(viewHolder.drawer.isDrawerOpen(GravityCompat.END)){
            viewHolder.drawer.closeDrawer(GravityCompat.END);
            return false;
        }
        return true;
    }

    private void init(View view) {
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if (activity != null) {
            activity.setOnBackPressed(this);
            actionBar = activity.getSupportActionBar();
        }
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        controllersAndHandlersInit(view, actionBar, mapFragment);
        initValues();
        viewHolderConfig();
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapHolder);
        }
    }

    private void initValues() {
        Land land = LandFragmentArgs.fromBundle(getArguments()).getSelectedLand();
        if(land != null){
            Log.i(TAG, "initValues: land not null");
            currLand = land;
            viewHolder.setTitle(currLand.getTitle());
            LandPoint[] tempLandPoints = LandFragmentArgs.fromBundle(getArguments()).getLandPoints();
            if(tempLandPoints != null){
                Log.i(TAG, "initValues: LandPoints not null");
                List<LandPoint> landPointsList = new ArrayList<>(Arrays.asList(tempLandPoints));
                Collections.sort(landPointsList);
                List<LatLng> landPoints = new ArrayList<>();
                for(LandPoint landPoint : landPointsList){
                    landPoints.add(landPoint.toLatLng());
                }
                pointsController.setPoints(landPoints);
            }else{
                Log.i(TAG, "initValues: LandPoints null");
            }
        }else{
            Log.i(TAG, "initValues: land null");
        }
    }

    private void controllersAndHandlersInit(View view, ActionBar actionBar, SupportMapFragment mapFragment) {
        Log.i(TAG, "controllersAndHandlersInit: start");
        pointsController = new LandPointsController();
        LandImgController imgController = new LandImgController(view.findViewById(R.id.imageView));
        fileController = new LandFileController(getActivity(), permissionResultLauncher,
                imgResultLauncher, fileResultLauncher);
        viewHolder= new LandViewHolder(view, mapFragment, actionBar, imgController);
        mapHolder = new LandMapHolder(viewHolder,pointsController);
        menuHolder = new LandMenuHolder(viewHolder,mapHolder,fileController,pointsController, imgController);
        Log.i(TAG, "controllersAndHandlersInit: end");
    }
    private void viewHolderConfig() {
        viewHolder.btnSave.setOnClickListener(this::save);
        viewHolder.navDrawer.setNavigationItemSelectedListener(menuHolder);
        viewHolder.closeTabMenu();
        viewHolder.hideImgView();
        viewHolder.setOnClose(new OnAction() {
            @Override
            public void onCloseTab() {
                mapHolder.clearFlag();
            }
        });
    }

    private void save(View v) {
        Context ctx = getContext();
        if(ctx != null &&
                (pointsController.getPoints().size() > 2 && viewHolder.getTitle().length() != 0)
        ){
            LandHelper landHelper = new LandHelper(ctx);
            Land land = landHelper.saveLand(-1,"test");
            List<LandPoint> landPoints = new ArrayList<>();
            for(LatLng latLng : pointsController.getPoints()){
                landPoints.add(landHelper.saveLandPoint(
                        land,
                        pointsController.getPoints().indexOf(latLng),
                        latLng
                ));
            }
            debugSaveData(land,landPoints);
            //todo save db
        }
        navigate(toLandMenu());
    }

    private void debugSaveData(Land land, List<LandPoint> landPoints) {
        Log.i(TAG, "debugSaveData land: {\n\"ID\":\""+land.getId()+
                "\",\n\"Creator\":\""+land.getCreator_id()+
                "\",\n\"Title\":\""+land.getTitle()+"\"\n}");
        for(LandPoint landPoint : landPoints){
            Log.i(TAG, "debugSaveData landPoint: {\n\"ID\":\""+landPoint.getId()+
                    "\",\n\"Land ID\":\""+landPoint.getLid()+
                    "\",\n\"Position\":\""+landPoint.getPosition()+
                    "\",\n\"Lat\":\""+landPoint.getLat()+
                    "\",\n\"Lng\":\""+landPoint.getLng()+"\"\n}");
        }
    }

    private void FileResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK){
            onFilePicked(result.getData());
        }
    }
    private void MapResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if(result.getData() != null){
                mapHolder.onActivityResult(result.getData());
            }
        }
    }
    private void ViewHolderResult(ActivityResult result) {
        if (
                result.getResultCode() == Activity.RESULT_OK &&
                fileController.fileIsValidImg(result.getData())
        ){
            viewHolder.onActivityResult(result.getData());
        }
    }

    private void onFilePicked(Intent result) {
        Intent intent = parseFile(result);
        if(intent != null){
            mapResultLauncher.launch(intent);
        }
    }

    private Intent parseFile(Intent result) {
        Intent intent = null;
        if(fileController.fileIsValid(result)){
            intent = new Intent(getContext(), ImportActivity.class);
            List<List<LatLng>> pointsList = fileController.handleFile(result);
            if(pointsList.size() > 0){
                intent.putParcelableArrayListExtra(ImportActivity.argName,convertToParcelable(pointsList));
            }else{
                intent = null;
            }
        }
        return intent;
    }

    private ArrayList<ParcelablePolygon> convertToParcelable(List<List<LatLng>> pointsList) {
        ArrayList<ParcelablePolygon> parcelablePolygons = new ArrayList<>();
        for(List<LatLng> points : pointsList){
            parcelablePolygons.add(new ParcelablePolygon(points));
        }
        return parcelablePolygons;
    }

    public void onRequestPermissionsResult(Boolean result) {
        if(result){
            Intent intent = fileController.getFilePickerIntent();
            if(fileController.getFlag() == LandFileState.File){
                fileResultLauncher.launch(intent);
            }else if(fileController.getFlag() == LandFileState.Img){
                imgResultLauncher.launch(intent);
            }
        }
    }

    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }
    private NavDirections toLandMenu(){
        return LandFragmentDirections.toMenu();
    }
}
