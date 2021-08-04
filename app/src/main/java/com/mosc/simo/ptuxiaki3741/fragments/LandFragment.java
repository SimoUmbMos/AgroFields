package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.ImportActivity;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandData;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.controllers.LandFileController;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.controllers.LandImgController;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.controllers.LandPointsController;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LandMapHolder;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LandMenuHolder;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LandViewHolder;
import com.mosc.simo.ptuxiaki3741.models.ParcelablePolygon;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.interfaces.OnAction;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LandFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandFragment";
    private LandMapHolder mapHolder;
    private LandViewHolder viewHolder;
    private LandMenuHolder menuHolder;
    private LandFileController fileController;
    private LandPointsController pointsController;
    private long currLandID = -1;
    private User currUser;

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
            UserViewModel vmUsers = new ViewModelProvider(activity).get(UserViewModel.class);
            currUser = vmUsers.getCurrUser().getValue();
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                actionBar.show();
            }
        }
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        controllersAndHandlersInit(view, actionBar);
        initValues();
        viewHolderConfig();
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapHolder);
        }
    }
    private void controllersAndHandlersInit(View view, ActionBar actionBar) {
        Log.i(TAG, "controllersAndHandlersInit: start");
        pointsController = new LandPointsController();
        LandImgController imgController = new LandImgController(view.findViewById(R.id.imageView));
        fileController = new LandFileController(getActivity(), permissionResultLauncher,
                imgResultLauncher, fileResultLauncher);
        viewHolder= new LandViewHolder(view, actionBar, imgController);
        mapHolder = new LandMapHolder(viewHolder,pointsController);
        menuHolder = new LandMenuHolder(
                viewHolder,
                mapHolder,
                fileController,
                pointsController,
                imgController,
                this::save,
                this::edit
        );
        Log.i(TAG, "controllersAndHandlersInit: end");
    }
    private void initValues() {
        Land currLand = LandFragmentArgs.fromBundle(getArguments()).getLand();
        if(!Land.equals(currLand,new Land())){
            currLandID = currLand.getData().getId();
        }
        viewHolder.setTitle(currLand.getData().getTitle());
        List<LandPoint> landPointsList = new ArrayList<>(currLand.getBorder());
        Collections.sort(landPointsList);
        List<LatLng> landPoints = new ArrayList<>();
        for(LandPoint landPoint : landPointsList){
            landPoints.add(landPoint.getLatLng());
        }
        pointsController.setPoints(landPoints);
    }
    private void viewHolderConfig() {
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

    private void save() {
        if(isValidToSave()){
            saveToVM();
            navigate(toListMenu());
        }
    }
    private void edit() {
        navigate(toLandInfo());
    }

    private boolean isValidToSave() {
        return getLandPoints().size() > 2 &&
                viewHolder.getTitle().length() != 0;
    }

    private void saveToVM() {
        Land currLand = getLand();
        if(getActivity() != null){
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmLands.saveLand(currLand,currUser);
        }
    }

    private Land getLand() {
        Land currLand;
        if(currLandID >= 0){
            currLand = new Land(new LandData(currLandID,currUser.getId(),viewHolder.getTitle()),getLandPoints());
        }else{
            currLand = new Land(new LandData(currUser.getId(),viewHolder.getTitle()),getLandPoints());
        }
        return currLand;
    }

    private List<LandPoint> getLandPoints() {
        List<LatLng> points = pointsController.getPoints();
        List<LandPoint> landPoints = new ArrayList<>();
        for(int i = 0; i < points.size(); i++){
            landPoints.add(new LandPoint(i,points.get(i)));
        }
        return landPoints;
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
            intent.putExtra(ImportActivity.userName,currUser.getId());
            mapResultLauncher.launch(intent);
        }
    }

    private Intent parseFile(Intent result) {
        Intent intent = null;
        if(fileController.fileIsValid(result)){
            intent = new Intent(getContext(), ImportActivity.class);
            intent.setData(result.getData());
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
    private void onCurrUserUpdate(User user) {
        currUser = user;
        if(user != null){
            Log.d(TAG, "onUserUpdate: user not null");
        }else{
            Log.d(TAG, "onUserUpdate: user null");
        }
    }

    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.landMapFragment)
            navController.navigate(action);
    }
    private NavDirections toListMenu(){
        return LandFragmentDirections.toListMenu();
    }
    private NavDirections toLandInfo(){
        Land currLand = getLand();
        return LandFragmentDirections.toLandInfo(currLand);
    }
}
