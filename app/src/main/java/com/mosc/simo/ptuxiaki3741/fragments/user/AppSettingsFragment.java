package com.mosc.simo.ptuxiaki3741.fragments.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
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
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.RealPathUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppSettingsFragment extends Fragment implements FragmentBackPress{
    private static final String TAG = "AppSettingsFragment";
    private FragmentAppSettingsBinding binding;
    private SharedPreferences sharedPref;
    private boolean alreadyRunning;
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
        alreadyRunning = false;
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
        if(!alreadyRunning){
            alreadyRunning = true;
            new Thread(()->{
                boolean result;
                try{
                    result = FileUtil.createDbExportAFileFileXLSX(getContext());
                }catch (IOException e) {
                    Log.e(TAG, "onExportDBPressed: ",e);
                    result = false;
                }
                onExportDBResult(result);
                alreadyRunning = false;
            }).start();
        }
    }
    private void onImportDBPressed(){
        Intent intent = FileUtil.getFilePickerIntent();
        fileLauncher.launch(intent);
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

    private void onFileResult(ActivityResult result) {
        Intent intent = result.getData();
        int resultCode = result.getResultCode();
        if (resultCode == Activity.RESULT_OK && intent != null) {
            if(getActivity() == null)
                return;
            File file = RealPathUtil.getFile(getActivity(),intent.getData());

            if(file == null){
                Log.d(TAG, "onFileResult: file is null");
                return;
            }
            if(!file.exists()){
                Log.d(TAG, "onFileResult: file don't exist");
                return;
            }

            if(!alreadyRunning){
                alreadyRunning = true;
                new Thread(()->{
                    List<Land> lands = new ArrayList<>();
                    List<LandZone> zones = new ArrayList<>();
                    List<Contact> contacts = new ArrayList<>();
                    if(OpenXmlDataBaseInput.importDB(file,lands,zones,contacts)){
                        for(Land land : lands){
                            Log.d(TAG, "land: "+
                                    land.getData().getTitle()+" "+
                                    land.getData().getColor().toString()
                            );
                        }
                        for(LandZone zone : zones){
                            Log.d(TAG, "zone: "+
                                    zone.getData().getLid()+" "+
                                    zone.getData().getTitle()+" "+
                                    zone.getData().getColor().toString()
                            );
                        }
                        for(Contact contact : contacts){
                            Log.d(TAG, "contact: "+
                                    contact.getUsername()+" "+
                                    contact.getEmail()+" "+
                                    contact.getPhone()
                            );
                        }
                    }
                    alreadyRunning = false;
                }).start();
            }
        }
    }
}