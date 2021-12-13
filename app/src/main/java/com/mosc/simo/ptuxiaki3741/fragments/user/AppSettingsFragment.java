package com.mosc.simo.ptuxiaki3741.fragments.user;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentAppSettingsBinding;
import com.mosc.simo.ptuxiaki3741.file.openxml.OpenXmlDataBaseInput;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppSettingsFragment extends Fragment implements FragmentBackPress{
    private static final String TAG = "AppSettingsFragment";
    private FragmentAppSettingsBinding binding;
    private SharedPreferences sharedPref;
    private AppViewModel viewModel;
    private Thread backThread;
    private List<Land> lands;
    private List<LandZone> zones;
    private boolean dataIsSaving;

    private final ActivityResultLauncher<String> permissionReadChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onImportDBAction
    );
    private final ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::onFileResult
    );

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
        initViewModel();
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
        if(dataIsSaving){
            Toast.makeText(
                    getContext(),
                    getString(R.string.open_xml_action_not_ended_error),
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }
        if(backThread != null){
            Toast.makeText(
                    getContext(),
                    getString(R.string.open_xml_action_not_ended_error),
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }
        return true;
    }

    private void initViewModel(){
        if(getActivity() != null){
            viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }else{
            viewModel = null;
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
    private void initData(){
        if(getActivity() != null){
            sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        }else{
            sharedPref = null;
        }
        backThread = null;
        dataIsSaving = false;
        lands = new ArrayList<>();
        zones = new ArrayList<>();
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
        binding.btnImportDB.setOnClickListener(v->onImportDBPressed());
        binding.btnExportDB.setOnClickListener(v->onExportDBPressed());
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


    private void onExportDBPressed() {
        onExportDBAction();
    }
    private void onExportDBAction() {
        if(backThread == null){
            backThread = new Thread(()->{
                boolean result = false;
                lands.clear();
                zones.clear();
                List<Land> temp1 = viewModel.getLands().getValue();
                if(temp1 != null) {
                    lands.addAll(temp1);
                }
                Map<Long,List<LandZone>> temp2 = viewModel.getLandZones().getValue();
                if(temp2 != null){
                    temp2.forEach((k,v)-> zones.addAll(v));
                }
                if(lands != null){
                    try{
                        result = FileUtil.dbExportAFileFileXLSX(lands,zones);
                    }catch (IOException e) {
                        Log.e(TAG, "onExportDBPressed: ",e);
                        result = false;
                    }
                }
                onExportDBResult(result);
                lands.clear();
                zones.clear();
                backThread = null;
            });
            backThread.start();
        }else{
            Toast.makeText(
                    getActivity(),
                    getString(R.string.open_xml_action_not_ended_error),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    private void onExportDBResult(boolean result) {
        if(getActivity() != null){
            getActivity().runOnUiThread(()->{
                if(result){
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.export_db_success),
                            Toast.LENGTH_SHORT
                    ).show();
                }else{
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.export_db_fail),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
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

    private void onImportDBPressed(){
        permissionReadChecker.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    private void onImportDBAction(boolean permission){
        if(permission){
            Intent intent = FileUtil.getFilePickerIntent();
            fileLauncher.launch(intent);
        }
    }
    private void onFileResult(ActivityResult result) {
        Intent intent = result.getData();
        int resultCode = result.getResultCode();
        if (resultCode == Activity.RESULT_OK && intent != null) {
            if(getActivity() == null)
                return;
            InputStream is;
            try {
                is = getActivity().getContentResolver().openInputStream(intent.getData());
            }catch (Exception e){
                Log.e(TAG, "onFileResult: ", e);
                is = null;
            }
            if(is == null){
                Log.d(TAG, "onFileResult: file is null");
                return;
            }
            final InputStream fis = is;
            if(backThread == null){
                backThread = new Thread(()->{
                    lands.clear();
                    zones.clear();
                    if(OpenXmlDataBaseInput.importDB(fis,lands,zones)){
                        onImportDBResult(true);
                        if(getActivity() != null){
                            getActivity().runOnUiThread(this::saveData);
                        }
                    }else{
                        onImportDBResult(false);
                    }
                    lands.clear();
                    zones.clear();
                    backThread = null;
                });
                backThread.start();
            }else{
                Toast.makeText(
                        getActivity(),
                        getString(R.string.open_xml_action_not_ended_error),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void saveData() {
        AsyncTask.execute(()->{
            Log.d(TAG, "saveData: lands size = "+lands.size());
            Log.d(TAG, "saveData: zones size = "+zones.size());
            try {
                dataIsSaving = true;
                Log.d(TAG, "saveData: dataIsSaving = true");
                for(Land land : lands){
                    viewModel.saveLand(land);
                }
                for(LandZone zone : zones){
                    viewModel.saveZone(zone);
                }
            }catch (Exception e){
                Log.e(TAG, "saveData: ", e);
            }finally {
                dataIsSaving = false;
                Log.d(TAG, "saveData: dataIsSaving = false");
            }
        });
    }

    private void onImportDBResult(boolean result) {
        if(getActivity() != null){
            getActivity().runOnUiThread(()->{
                if(result){
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.import_db_success),
                            Toast.LENGTH_SHORT
                    ).show();
                }else{
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.import_db_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }
}