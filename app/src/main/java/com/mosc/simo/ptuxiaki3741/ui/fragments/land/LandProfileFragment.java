package com.mosc.simo.ptuxiaki3741.ui.fragments.land;

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
import android.widget.EditText;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandInfoBinding;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.util.ArrayList;
import java.util.List;

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
            binding.etLandInfoAddressLayout.setEnabled(true);
        }else{
            landLabel = getString(R.string.edit_land_label);
            if(binding.etLandInfoNameLayout.getEditText() != null) {
                binding.etLandInfoNameLayout.getEditText().setText(land.getData().getTitle());
            }
            if(binding.etLandInfoTagsLayout.getEditText() != null) {
                binding.etLandInfoTagsLayout.getEditText().setText(land.getData().getTags());
            }
            if(binding.etLandInfoAddressLayout.getEditText() != null){
                binding.etLandInfoAddressLayout.getEditText().setText("");
            }
            binding.etLandInfoAddressLayout.setEnabled(false);
            binding.etLandInfoAddressLayout.setVisibility(View.GONE);
        }
        binding.tvLandInfoActionLabel.setText(landLabel);
        binding.btnLandInfoSubmit.setOnClickListener(v->onSubmit());
        binding.btnLandInfoCancel.setOnClickListener(v->onCancel());
        binding.ibClose.setOnClickListener(v->goBack());
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

        EditText editTextName = binding.etLandInfoNameLayout.getEditText();
        String landName;
        if(editTextName != null && editTextName.getText() != null){
            landName = editTextName.getText().toString();
            landName = DataUtil.removeSpecialCharacters(landName);
            editTextName.setText(landName);
        }else{
            landName = "";
        }

        EditText editTextTags = binding.etLandInfoTagsLayout.getEditText();
        String landTags;
        if(editTextTags != null && editTextTags.getText() != null){
            landTags = editTextTags.getText().toString();
            landTags = DataUtil.removeSpecialCharactersCSV(landTags);
            List<String> tags = DataUtil.splitTags(landTags);
            List<String> temp = new ArrayList<>();
            for(int i = 0; i < tags.size(); i++){
                if(tags.get(i) == null || tags.get(i).isEmpty()) continue;
                temp.add(tags.get(i));
            }
            tags.clear();
            tags.addAll(temp);
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < tags.size(); i++){
                builder.append(tags.get(i));
                if(i != (tags.size() - 1)) builder.append(", ");
            }
            landTags = builder.toString();
            editTextTags.setText(landTags);
        }else{
            landTags = "";
        }

        EditText editTextAddress = binding.etLandInfoAddressLayout.getEditText();
        String address;
        if(editTextAddress != null && editTextAddress.getText() != null){
            address = editTextAddress.getText().toString();
        }else{
            address = "";
        }

        if(!landName.isEmpty()){
            binding.etLandInfoNameLayout.setError(null);
            submit(landName, landTags, address);
        }else{
            binding.etLandInfoNameLayout.setError(getString(R.string.title_empty_error));
        }
    }

    private void submit(String landName,String landTags, String address) {
        LandData landData;
        if(land == null || land.getData() == null){
            landData = new LandData(snapshot,landName,landTags,color);
            if(address.trim().isEmpty()){
                toLandMap(getActivity(),new Land(landData));
            }else{
                toLandMap(getActivity(),new Land(landData),address);
            }
        }else{
            landData = new LandData(land.getData());
            landData.setTitle(landName);
            landData.setTags(landTags);
            toLandMap(getActivity(),new Land(landData));
        }
    }

    //cancel
    public void onCancel() {
        closeKeyboard();
        goBack();
    }

    private void goBack() {
        if(getActivity() != null)
            getActivity().onBackPressed();
    }

    //navigation
    public void toLandMap(@Nullable Activity activity,Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ProfileLandFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,new Land(land));
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }

    public void toLandMap(@Nullable Activity activity, Land land, String address) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ProfileLandFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,new Land(land));
                bundle.putString(AppValues.argAddress,address);
                if(nav != null)
                    nav.navigate(R.id.toMapLandEditor,bundle);
            });
    }
}