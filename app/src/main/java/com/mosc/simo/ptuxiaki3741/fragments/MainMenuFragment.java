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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class MainMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG ="MenuFragment";
    public static final int defaultPadding = 16;

    private UserViewModel vmUsers;
    private LandViewModel vmLands;
    private List<User> friendRequests;
    private List<Land> lands;
    private GoogleMap mMap;
    private User currUser;
    private boolean drawPolygon;

    private FragmentMenuMainBinding binding;
    private ActionBar actionBar;
    private Menu menu;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModels();
        initObservers();
        initFragment();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.request_menu, menu);
        this.menu = menu;
        updateMenu();
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_request) {
            toRequestMenu(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    //init
    private void initData(){
        friendRequests = new ArrayList<>();
        lands = new ArrayList<>();
        drawPolygon = false;
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
    private void initObservers() {
        if(vmUsers != null){
            currUser = vmUsers.getCurrUser().getValue();
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            vmUsers.getFriendRequestList().observe(getViewLifecycleOwner(),this::onFriendRequestListUpdate);
        }
        if(vmLands != null){
            onLandUpdate(vmLands.getLands().getValue());
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
        }
    }
    private void initFragment() {
        binding.btnLands.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnHistory.setOnClickListener(v -> toLandHistory(getActivity()));
        binding.btnContacts.setOnClickListener(v -> toUserContacts(getActivity()));
        binding.btnProfile.setOnClickListener(v -> toProfile(getActivity()));

        if(UIUtil.isGooglePlayServicesAvailable(getActivity())){
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mainMenuMap);
            if(mapFragment != null){
                mapFragment.getMapAsync(this::initMap);
            }
        }
    }
    private void initMap(GoogleMap googleMap){
        binding.mainMenuMap.setVisibility(View.INVISIBLE);
        binding.mainMenuAction.setVisibility(View.VISIBLE);
        binding.mainMenuAction.setText(getString(R.string.main_menu_loading));
        mMap = googleMap;
        
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
        googleMap.setOnMapClickListener(this::OnMapClick);
        googleMap.setOnPolygonClickListener(this::OnPolygonClick);
        googleMap.setOnMapLoadedCallback(this::mapFullLoaded);
    }

    //menu
    private void updateMenu() {
        if(menu != null && getContext() != null){
            MenuItem item = menu.findItem(R.id.menu_item_request);
            if(item != null){
                if(friendRequests.size() > 0){
                    item.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_add_request_notification));
                }else{
                    item.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_add_request));
                }
            }
        }
    }

    //observers
    private void onCurrUserUpdate(User user) {
        currUser = user;
        if(currUser != null){
            if(actionBar != null){
                actionBar.setTitle(currUser.getUsername());
            }
        }else{
            toLogin(getActivity());
        }
    }
    private void onFriendRequestListUpdate(List<User> requests) {
        friendRequests.clear();
        if(requests != null)
            friendRequests.addAll(requests);

        updateMenu();
    }
    private void onLandUpdate(List<Land> landList) {
        lands.clear();
        if(landList != null){
            lands.addAll(landList);
        }
        drawMap();
    }

    //map
    private void mapFullLoaded(){
        binding.mainMenuMap.setVisibility(View.VISIBLE);
        binding.mainMenuAction.setVisibility(View.GONE);
        binding.mainMenuAction.setText(getString(R.string.main_menu_no_lands));
        drawMap();
    }
    private void OnPolygonClick(Polygon polygon){
        Log.d(TAG, "OnPolygonClick: ");
        drawMap(polygon);
    }
    private void OnMapClick(LatLng point){
        Log.d(TAG, "OnMapClick: ");
        if(drawPolygon)
            drawMap();
    }
    private void drawMap(){
        drawPolygon = false;
        if(mMap != null){
            mMap.clear();
            if(lands.size()>0){
                binding.mainMenuMap.setVisibility(View.VISIBLE);
                binding.mainMenuAction.setVisibility(View.GONE);
                int strokeColor,fillColor;
                if(getContext() != null){
                    strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
                    fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
                }else{
                    strokeColor = Color.rgb(0,0,255);
                    fillColor = Color.argb(51,0,0,255);
                }
                int size = 0;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(Land land:lands){
                    mMap.addPolygon(new PolygonOptions()
                            .addAll(land.getData().getBorder())
                            .clickable(true)
                            .strokeColor(strokeColor)
                            .fillColor(fillColor)
                            .zIndex(0.5f)
                    );
                    for(LatLng point: land.getData().getBorder()){
                        builder.include(point);
                        size++;
                    }
                }
                if(size > 0){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                            builder.build(),
                            defaultPadding
                    ));
                }else{
                    binding.mainMenuMap.setVisibility(View.GONE);
                    binding.mainMenuAction.setVisibility(View.VISIBLE);
                }
            }else{
                binding.mainMenuMap.setVisibility(View.GONE);
                binding.mainMenuAction.setVisibility(View.VISIBLE);
            }
        }
    }
    private void drawMap(Polygon polygon){
        drawPolygon = true;
        if(mMap != null){
            mMap.clear();
            int strokeColor,fillColor;
            if(getContext() != null){
                strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
                fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
            }else{
                strokeColor = Color.argb(192,0,0,255);
                fillColor = Color.argb(51,0,0,255);
            }
            mMap.addPolygon(new PolygonOptions()
                    .addAll(polygon.getPoints())
                    .strokeColor(strokeColor)
                    .fillColor(fillColor)
                    .clickable(true)
            );
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
                    defaultPadding
            ));
        }
    }

    //navigation
    public void toLandHistory(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToLandHistory);
            });
    }
    public void toListMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToListMenu);
            });
    }
    public void toProfile(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToUserProfile);
            });
    }
    public void toLogin(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToLogin);
            });
    }
    public void toUserContacts(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToUserContacts);
            });
    }
    public void toRequestMenu(@Nullable Activity activity) {
        if(activity != null){
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null){
                    nav.navigate(R.id.mainMenuToUserRequests);
                }
            });
        }
    }
}