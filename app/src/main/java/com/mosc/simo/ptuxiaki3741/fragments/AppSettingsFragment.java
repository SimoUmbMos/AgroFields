package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentAppSettingsBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class AppSettingsFragment extends Fragment implements FragmentBackPress{
    private FragmentAppSettingsBinding binding;
    private SharedPreferences sharedPref;
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentAppSettingsBinding.inflate(inflater,container,false);
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
        inflater.inflate(R.menu.app_settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case (R.id.menu_item_factory_reset):
                factoryReset();
                return true;
            case (R.id.menu_item_degree_info):
                toDegreeInfo(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initData(){
        if(getActivity() != null){
            sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        }else{
            sharedPref = null;
        }
    }
    private void initActivity(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.app_settings_title));
                actionBar.show();
            }
        }
    }
    private void initFragment(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.theme_styles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.svTheme.setAdapter(adapter);
        int pos = 0;
        if(sharedPref != null){
            if(sharedPref.getBoolean(AppValues.isForceKey, false)){
                if(!sharedPref.getBoolean(AppValues.isDarkKey, false)){
                    pos=1;
                }else{
                    pos=2;
                }
            }
        }
        binding.svTheme.setSelection(pos,false);
        binding.svTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                onThemeSpinnerItemSelect(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void onThemeSpinnerItemSelect(int pos){
        if(sharedPref != null){
            SharedPreferences.Editor editor = sharedPref.edit();
            switch (pos){
                case 1:
                    editor.putBoolean(AppValues.isForceKey,true);
                    editor.putBoolean(AppValues.isDarkKey,false);
                    break;
                case 2:
                    editor.putBoolean(AppValues.isForceKey,true);
                    editor.putBoolean(AppValues.isDarkKey,true);
                    break;
                default:
                    editor.remove(AppValues.isForceKey);
                    editor.remove(AppValues.isDarkKey);
                    break;
            }
            editor.apply();
        }
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null)
            activity.checkThemeSettings();
    }

    private void factoryReset(){
        binding.svTheme.setSelection(0,false);
        if(sharedPref != null){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(AppValues.isForceKey);
            editor.remove(AppValues.isDarkKey);
            editor.apply();
        }
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null)
            activity.checkThemeSettings();
    }
    public void toDegreeInfo(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.AppSettingsFragment);
                if(nav != null)
                    nav.navigate(R.id.toDegreeInfo);
            });
    }
}