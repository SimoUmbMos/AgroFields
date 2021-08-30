package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandInfoBinding;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

public class LandInfoFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandInfoFragment";
    public static final String argLand = "land";

    private Land land;
    private User currUser;
    private FragmentLandInfoBinding binding;
    private boolean isNew = false;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLandInfoBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModel();
        initView();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initData() {
        if(getArguments() != null){
            if(getArguments().containsKey(argLand)){
                land = getArguments().getParcelable(argLand);
            }else{
                land = new Land();
            }
        }else{
            land = new Land();
        }
        if(new Land().equals(land)){
            land = null;
            isNew = true;
        }else{
            isNew = false;
        }
    }
    private void initActivity(){
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if (activity != null) {
            activity.setOnBackPressed(this);
            actionBar = activity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.hide();
        }
    }
    private void initViewModel(){
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            currUser = vmUsers.getCurrUser().getValue();
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
        }
    }
    private void initView() {
        binding.etLandInfoName.setOnEditorActionListener((v, actionId, event) -> {
            if (
                    actionId == EditorInfo.IME_ACTION_DONE &&
                    binding.etLandInfoName.getText()!=null &&
                    binding.etLandInfoAddress.getText()!=null
            ) {
                onSubmit(
                        binding.etLandInfoName.getText().toString(),
                        binding.etLandInfoAddress.getText().toString()
                );
                return true;
            }
            return false;
        });
        binding.etLandInfoAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (
                    actionId == EditorInfo.IME_ACTION_DONE &&
                    binding.etLandInfoName.getText()!=null &&
                    binding.etLandInfoAddress.getText()!=null
            ) {
                onSubmit(
                        binding.etLandInfoName.getText().toString(),
                        binding.etLandInfoAddress.getText().toString()
                );
                return true;
            }
            return false;
        });
        binding.btnLandInfoSubmit.setOnClickListener(v->{
            if(
                    binding.etLandInfoName.getText()!=null &&
                    binding.etLandInfoAddress.getText()!=null
            ){
                onSubmit(
                        binding.etLandInfoName.getText().toString(),
                        binding.etLandInfoAddress.getText().toString()
                );
            }
        });

        binding.btnLandInfoCancel.setOnClickListener(v->onCancel());

        String landLabel;
        if(land == null){
            landLabel = getResources().getString(R.string.create_land_label);
            binding.etLandInfoAddress.setEnabled(true);
        }else{
            landLabel = getResources().getString(R.string.edit_land_label);
            binding.etLandInfoName.setText(land.getData().getTitle());
            binding.etLandInfoAddress.setEnabled(false);
            asyncFindAddress(getActivity(),land);
        }
        binding.tvLandInfoActionLabel.setText(landLabel);
    }

    //ui
    private void onCurrUserUpdate(User user) {
        if(user != null){
            Log.d(TAG, "onUserUpdate: user not null");
        }else{
            Log.d(TAG, "onUserUpdate: user null");
        }
        currUser = user;
    }
    private void closeKeyboard() {
        if(getActivity() != null && getActivity().getCurrentFocus() != null){
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputManager != null)
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private void asyncFindAddress(Activity activity, Land land) {
        if(activity != null){
            AsyncTask.execute(()->{
                Address address = MapUtil.findLocation(
                        activity,
                        MapUtil.getPolygonCenter(land.getData().getBorder())
                );
                if(address!= null){
                    String tempDisplay = null;
                    if(address.getLocality() != null){
                        if(address.getCountryName() != null){
                            tempDisplay = address.getLocality()+", "+address.getCountryName();
                        }else{
                            tempDisplay = address.getLocality();
                        }
                    }
                    final String finalDisplay = tempDisplay;
                    if(finalDisplay != null){
                        activity.runOnUiThread(()->binding.etLandInfoAddress.setText(finalDisplay));
                    }
                }
            });
        }
    }

    //submit
    public void onSubmit(String landName,String address) {
        closeKeyboard();
        landName = landName.replaceAll(
                "[^a-zA-Z0-9]", " ");
        landName = landName.trim().replaceAll(" +", " ");
        if(!landName.isEmpty()){
            if(isNew){
                submitAdd(landName, address);
            }else{
                submitEdit(landName);
            }
        }
    }
    private void submitAdd(String landName, String address) {
        if(currUser != null){
            LandData landData = new LandData(currUser.getId(),landName);
            toLandMap(getActivity(),new Land(landData),address);
        }
    }
    private void submitEdit(String landName) {
        land.getData().setTitle(landName);
        toLandMap(getActivity(),land);
    }

    //cancel
    public void onCancel() {
        closeKeyboard();
        finish();
    }
    private void finish() {
        if(getActivity() != null)
            getActivity().onBackPressed();
    }

    //navigation
    public void toLandMap(@Nullable Activity activity,Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandInfoFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(LandMapFragment.argLand,land);
                if(nav != null)
                    nav.navigate(R.id.landInfoToLandMap,bundle);
            });
    }
    public void toLandMap(@Nullable Activity activity, Land land, String address) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandInfoFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(LandMapFragment.argLand,land);
                bundle.putString(LandMapFragment.argAddress,address);
                if(nav != null)
                    nav.navigate(R.id.landInfoToLandMap,bundle);
            });
    }
}