package com.mosc.simo.ptuxiaki3741.fragments.contacts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentSelectedShareLandBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserLandPermissions;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

public class ShareLandFragment extends Fragment implements FragmentBackPress {
    private FragmentSelectedShareLandBinding binding;
    private AlertDialog dialog;

    private LandViewModel vmLands;

    private Land land;
    private User contact;
    private User currUser;
    private UserLandPermissions perm;

    private boolean initData() {
        land = null;
        contact = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.SHARE_LAND_DATA_ARG))
                land =  getArguments().getParcelable(AppValues.SHARE_LAND_DATA_ARG);
            if(getArguments().containsKey(AppValues.SHARE_LAND_USER_ARG))
                contact = getArguments().getParcelable(AppValues.SHARE_LAND_USER_ARG);
        }
        if(land != null)
            return land.getData() != null && contact != null;
        return false;
    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                ActionBar actionBar = null;
                if(mainActivity != null){
                    mainActivity.setOnBackPressed(this);
                    actionBar = mainActivity.getSupportActionBar();
                }
                if(actionBar != null){
                    actionBar.setTitle(getString(R.string.selected_share_land_list_title));
                    actionBar.show();
                }
            }
        }
    }
    private void initFragment() {
        binding.tvLandName.setText(land.getData().getTitle());
        onUserLandPermissionsUpdate(null);
        if(land.getData().getCreator_id() == contact.getId()){
            binding.ctvIsAdminLand.setEnabled(false);
            binding.ctvIsReadLand.setEnabled(false);
            binding.ctvIsWriteLand.setEnabled(false);
            binding.btnSave.setEnabled(false);
            binding.btnCancel.setEnabled(false);
        }else{
            binding.ctvIsAdminLand.setEnabled(true);
            binding.ctvIsReadLand.setEnabled(true);
            binding.ctvIsWriteLand.setEnabled(true);
            binding.btnSave.setEnabled(true);
            binding.btnCancel.setEnabled(true);
        }
        binding.ctvIsAdminLand.setOnClickListener(v->onAdminClick());
        binding.ctvIsReadLand.setOnClickListener(v->onReadClick());
        binding.ctvIsWriteLand.setOnClickListener(v->onWriteClick());
        binding.btnSave.setOnClickListener(v->onSave());
        binding.btnCancel.setOnClickListener(v->onCancel());
        binding.mvLand.getMapAsync(this::initMap);
    }
    private void initViewModel() {
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            currUser = vmUsers.getCurrUser().getValue();
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),user -> currUser = user);
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            AsyncTask.execute(()->
                    onUserLandPermissionsUpdate(vmLands.getLandPermissionForUser(contact,land))
            );
        }
    }
    private void initMap(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        DrawOnMap(googleMap);
    }

    private void DrawOnMap(GoogleMap googleMap) {
        int strokeColor,fillColor;
        if(getContext() != null){
            strokeColor = ContextCompat.getColor(getContext(), R.color.polygonStroke);
            fillColor = ContextCompat.getColor(getContext(), R.color.polygonFill);
        }else{
            strokeColor = AppValues.strokeColor;
            fillColor = AppValues.fillColor;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int builderSize = 0;
        PolygonOptions options = LandUtil.getPolygonOptions(land.getData(),strokeColor,fillColor,false);
        if(options != null)
            googleMap.addPolygon(options);
        for(LatLng point:land.getData().getBorder()){
            builder.include(point);
            builderSize++;
        }
        if(builderSize > 0)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPadding
            ));
    }

    private void onUserLandPermissionsUpdate(UserLandPermissions perm) {
        if(perm == null){
            this.perm = new UserLandPermissions(
                contact.getId(),
                land.getData().getId(),
                land.getData().getCreator_id() == contact.getId(),
                land.getData().getCreator_id() == contact.getId(),
                land.getData().getCreator_id() == contact.getId()
            );
        }else{
            this.perm = perm;
        }
        updateUI();
    }
    private void onChangeOwnerClick(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.ErrorMaterialAlertDialog)
                    .setTitle(getString(R.string.change_land_owner_alert_title))
                    .setMessage(getString(R.string.change_land_owner_alert_desc))
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> changeOwnerAction())
                    .create();
            dialog.show();
        }
    }
    private void onAdminClick() {
        this.perm.setAdmin(!this.perm.isAdmin());
        updateUI();
    }
    private void onReadClick() {
        this.perm.setRead(!this.perm.isRead());
        updateUI();
    }
    private void onWriteClick() {
        this.perm.setWrite(!this.perm.isWrite());
        updateUI();
    }
    private void onSave() {
        AsyncTask.execute(()->{
            vmLands.updateLandPermissions(perm);
            goBack();
        });
    }
    private void onCancel() {
        goBack();
    }

    private void changeOwnerAction(){
        if(vmLands != null){
            AsyncTask.execute(()-> vmLands.changeLandOwner(contact,land,this::changeOwnerResult));
        }
    }
    private void changeOwnerResult(boolean result){
        if(getActivity() != null)
            getActivity().runOnUiThread(()->{
                if(result){
                    showToast(getString(R.string.change_land_owner_result_true));
                    goBack();
                }else{
                    showToast(getString(R.string.change_land_owner_result_false));
                }
            });
    }
    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }
    private void updateUI(){
        if(getActivity() != null)
            getActivity().runOnUiThread(()->{
                if(perm != null){
                    binding.ctvIsAdminLand.setChecked(perm.isAdmin());
                    binding.ctvIsReadLand.setChecked(perm.isRead());
                    binding.ctvIsWriteLand.setChecked(perm.isWrite());
                    binding.ctvIsWriteLand.setEnabled(!perm.isAdmin() && perm.isRead());
                    binding.ctvIsReadLand.setEnabled(!perm.isAdmin());
                }
            });
    }
    private void goBack(){
        if(getActivity() != null)
            getActivity().runOnUiThread(()-> getActivity().onBackPressed());
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentSelectedShareLandBinding.inflate(inflater,container,false);
        binding.mvLand.onCreate(savedInstanceState);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        if(initData()){
            initFragment();
            initViewModel();
        }else{
            goBack();
        }
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
        inflater.inflate(R.menu.selected_share_land_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menu_item_change_land_owner);
        if(item != null){
            if(currUser != null && land != null){
                if(currUser.getId() == land.getData().getCreator_id()){
                    item.setVisible(true);
                    item.setEnabled(true);
                    return;
                }
            }
            item.setVisible(false);
            item.setEnabled(false);
        }
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_change_land_owner) {
            onChangeOwnerClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }
}