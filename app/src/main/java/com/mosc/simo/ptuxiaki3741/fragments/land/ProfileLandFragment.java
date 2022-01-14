package com.mosc.simo.ptuxiaki3741.fragments.land;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
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
import com.mosc.simo.ptuxiaki3741.models.ColorData;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class ProfileLandFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandInfoFragment";
    //fixme: add tags
    private Land land;
    private ColorData color;
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
        land = null;
        color = AppValues.defaultLandColor;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLand)){
                land = getArguments().getParcelable(AppValues.argLand);
            }
        }
        if(land != null)
            color = land.getData().getColor();
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
            landLabel = getString(R.string.edit_land_label);
            binding.etLandInfoName.setText(land.getData().getTitle());
            binding.etLandInfoAddress.setText("");
            binding.etLandInfoAddress.setEnabled(false);
            binding.etLandInfoAddressLayout.setVisibility(View.GONE);
        }
        binding.tvLandInfoActionLabel.setText(landLabel);
        binding.btnLandInfoSubmit.setOnClickListener(v->onSubmit());
        binding.btnLandInfoCancel.setOnClickListener(v->onCancel());
    }

    //ui
    private void closeKeyboard() {
        if(getActivity() != null && getActivity().getCurrentFocus() != null){
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputManager != null)
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
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
        landName = DataUtil.removeSpecialCharacters(landName);
        binding.etLandInfoName.setText(landName);
        if(!landName.isEmpty()){
            binding.etLandInfoNameLayout.setError(null);
            submit(landName, address);
        }else{
            binding.etLandInfoNameLayout.setError(getString(R.string.title_error));
        }
    }
    private void submit(String landName, String address) {
        if(land == null){
            LandData landData = new LandData(landName,color);
            if(address.trim().isEmpty()){
                toLandMap(getActivity(),new Land(landData));
            }else{
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
                bundle.putParcelable(AppValues.argLand,land);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }
    public void toLandMap(@Nullable Activity activity, Land land, String address) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ProfileLandFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,land);
                bundle.putString(AppValues.argAddress,address);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }
}