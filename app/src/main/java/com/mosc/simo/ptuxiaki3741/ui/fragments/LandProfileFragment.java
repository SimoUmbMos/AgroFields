package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandInfoBinding;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

public class LandProfileFragment extends Fragment {
    public static final String TAG = "LandInfoFragment";
    private Land land;
    private ColorData color;
    private FragmentLandInfoBinding binding;
    private long snapshot;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
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

    private void initData() {
        land = null;
        color = AppValues.defaultLandColor;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLand)){
                land = getArguments().getParcelable(AppValues.argLand);
            }
        }
        if(land != null) {
            color = land.getData().getColor();
            snapshot = land.getData().getSnapshot();
        }
    }

    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(()->true);
            }
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
            binding.etLandInfoNameLayout.setError(getString(R.string.title_empty_error));
        }
    }

    private void submit(String landName, String address) {
        if(land == null){
            LandData landData = new LandData(snapshot,landName,color);
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