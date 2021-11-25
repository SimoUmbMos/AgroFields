package com.mosc.simo.ptuxiaki3741.fragments.land;

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

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandInfoBinding;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class ProfileLandFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandInfoFragment";
//fixme: cant use greek characters on land name
//todo: (idea) address->curr loc
    private Land land;
    private User currUser;
    private FragmentLandInfoBinding binding;

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
        initFragment();
        initViewModel();
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
            if(getArguments().containsKey(AppValues.argLandInfoFragment)){
                land = getArguments().getParcelable(AppValues.argLandInfoFragment);
            }else{
                land = null;
            }
        }else{
            land = null;
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
    private void initFragment() {
        String landLabel;
        if(land == null){
            landLabel = getString(R.string.create_land_label);
            binding.etLandInfoAddress.setEnabled(true);
        }else{
            binding.etLandInfoName.setEnabled(land.getPerm().isWrite());
            binding.etLandInfoAddress.setEnabled(land.getPerm().isWrite());
            binding.btnLandInfoSubmit.setEnabled(land.getPerm().isWrite());
            binding.btnLandInfoCancel.setEnabled(land.getPerm().isWrite());

            landLabel = getString(R.string.edit_land_label);
            binding.etLandInfoName.setText(land.getData().getTitle());
            binding.etLandInfoAddress.setText("");
            binding.etLandInfoAddress.setEnabled(false);
            binding.etLandInfoAddressLayout.setVisibility(View.GONE);
            asyncFindAddress(getActivity(),land);
        }
        binding.tvLandInfoActionLabel.setText(landLabel);
        binding.btnLandInfoSubmit.setOnClickListener(v->onSubmit());
        binding.btnLandInfoCancel.setOnClickListener(v->onCancel());
    }
    private void initViewModel(){
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
        }
    }

    //ui
    private void onCurrUserUpdate(User user) {
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
    public void onSubmit() {
        closeKeyboard();
        String landName = "";
        String address = "";
        if(binding.etLandInfoName.getText()!=null){
            landName = binding.etLandInfoName.getText().toString();
        }
        if(binding.etLandInfoAddress.getText()!=null){
            address = binding.etLandInfoAddress.getText().toString();
        }

        landName = landName.replaceAll(
                "[^a-zA-Z0-9]", " ");
        landName = landName.trim().replaceAll(" +", " ");
        if(!landName.isEmpty()){
            submit(landName, address);
        }
    }
    private void submit(String landName, String address) {
        if(land == null){
            if(currUser != null){
                LandData landData = new LandData(currUser.getId(),landName);
                toLandMap(getActivity(),new Land(landData),address);
            }
        }else{
            land.getData().setTitle(landName);
            toLandMap(getActivity(),land);
        }
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
                NavController nav = UIUtil.getNavController(this,R.id.ProfileLandFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandLandMapFragment,land);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }
    public void toLandMap(@Nullable Activity activity, Land land, String address) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ProfileLandFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandLandMapFragment,land);
                bundle.putString(AppValues.argAddressLandMapFragment,address);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }
}