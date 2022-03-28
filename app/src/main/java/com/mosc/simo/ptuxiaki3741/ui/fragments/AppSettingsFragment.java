package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentAppSettingsBinding;
import com.mosc.simo.ptuxiaki3741.backend.file.openxml.OpenXmlDataBaseInput;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.data.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.YearPickerDialog;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppSettingsFragment extends Fragment implements FragmentBackPress{
    //todo: copy lands, zones and add history from one snapshot to another
    private static final String TAG = "AppSettingsFragment";
    private FragmentAppSettingsBinding binding;
    private LoadingDialog loadingDialog;
    private YearPickerDialog yearPicker;
    private AlertDialog dialog;

    private SharedPreferences sharedPref;
    private AppViewModel viewModel;
    private Thread backThread;
    private boolean dataIsSaving;
    private int themeId;
    private Long snapshot;
    private ArrayAdapter<String> themeAdapter;
    private int dialogChecked;


    private final ActivityResultLauncher<String> permissionWriteChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onPermissionWriteResult
    );

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
        binding = FragmentAppSettingsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
    }

    @Override
    public void onStop() {
        if(sharedPref != null){
            SharedPreferences.Editor editor = sharedPref.edit();

            if (binding.etOwnerName.getText() != null) {
                String name = binding.etOwnerName.getText().toString()
                        .trim()
                        .replaceAll("[\\t\\n\\r]+"," ")
                        .replaceAll(" +", " ");
                if(!name.isEmpty()){
                    editor.putString(AppValues.ownerName, name);
                }else{
                    editor.remove(AppValues.ownerName);
                }
            }else{
                editor.remove(AppValues.ownerName);
            }

            if (binding.etOwnerEmail.getText() != null) {
                String email = binding.etOwnerEmail.getText().toString().trim();
                if(!email.isEmpty()){
                    editor.putString(AppValues.ownerEmail, email);
                }else{
                    editor.remove(AppValues.ownerEmail);
                }
            }else{
                editor.remove(AppValues.ownerEmail);
            }

            if(snapshot != null){
                editor.putLong(AppValues.argSnapshotKey, snapshot);
            }else{
                editor.remove(AppValues.argSnapshotKey);
            }

            editor.apply();
        }

        if(viewModel != null && snapshot != null){
            AsyncTask.execute(()-> viewModel.setDefaultSnapshot(snapshot));
        }

        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        themeId=0;
        if(sharedPref != null){
            if(sharedPref.getBoolean(AppValues.isForceKey, false)){
                if(!sharedPref.getBoolean(AppValues.isDarkKey, false)){
                    themeId=1;
                }else{
                    themeId=2;
                }
            }
        }

        binding.tvTheme.setText(themeAdapter.getItem(themeId), false);
        binding.tvTheme.setSelection(binding.tvTheme.getText().length());
        binding.tvTheme.setAdapter(themeAdapter);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override public boolean onBackPressed() {
        if(dataIsSaving || backThread != null){
            showSnackBar(getString(R.string.open_xml_action_not_ended_error));
            return false;
        }
        return true;
    }

    private void initData(){
        snapshot = (long) LocalDate.now().getYear();
        sharedPref = null;
        themeId = 0;
        themeAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.theme_styles)
        );
        backThread = null;
        dataIsSaving = false;
    }

    private void initActivity(){
        if(getActivity() == null) return;
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        yearPicker = new YearPickerDialog(getActivity(), this::onSnapshotUpdate);
        viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        this.snapshot = viewModel.getDefaultSnapshot();

        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setOnBackPressed(this);
        loadingDialog = mainActivity.getLoadingDialog();
    }

    private void initFragment(){
        binding.ibClose.setOnClickListener( v -> goBack());
        binding.ibInfo.setOnClickListener(v->toDegreeInfo(getActivity()));
        binding.ibReset.setOnClickListener(v->factoryReset());
        binding.btnCopyDataToOtherSnapshot.setOnClickListener(v->showYearSelectForCopy());
        binding.btnBulkEdit.setOnClickListener(v->toBulkEdit());
        binding.btnCalendarCategories.setOnClickListener(v->toCalendarCategories());
        binding.btnImportDB.setOnClickListener(v->onImportDBPressed());
        binding.btnExportDB.setOnClickListener(v->onExportDBPressed());

        if(sharedPref != null){
            String ownerName = sharedPref.getString(AppValues.ownerName, null);
            String ownerEmail = sharedPref.getString(AppValues.ownerEmail, null);
            if(ownerName != null) binding.etOwnerName.setText(ownerName);
            if(ownerEmail != null) binding.etOwnerEmail.setText(ownerEmail);
        }

        binding.etSnapshot.setOnClickListener(v->onOpenSnapshotPicker());
        binding.etSnapshot.setText(String.valueOf(snapshot));

        binding.tvTheme.setOnItemClickListener((adapterView, view, position, id) -> {
            binding.tvTheme.clearFocus();

            if(themeId == position) return;
            themeId = position;
            onThemeSpinnerItemSelect();
        });
    }

    private void onSnapshotUpdate(Long snapshot) {
        String display;
        if(snapshot != null) {
            display = String.valueOf(snapshot);
        }else if(viewModel != null){
            display = String.valueOf(viewModel.getDefaultSnapshot());
        }else{
            display = String.valueOf(LocalDate.now().getYear());
        }
        binding.etSnapshot.setText(display);
        this.snapshot = snapshot;
    }

    private void onOpenSnapshotPicker() {
        if(yearPicker == null) return;
        if(dialog != null){
            if(dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }
        dialog = yearPicker.getDialog();
        yearPicker.openDialog(snapshot);
    }

    private void showYearSelectForCopy() {
        if(getActivity() == null) return;
        if(dialog != null){
            if(dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }
        YearPickerDialog ypDialog = new YearPickerDialog(getActivity(), this::startCopyToOtherSnapshot);
        ypDialog.openDialog(getString(R.string.select_snapshot_to_copy),snapshot);
        dialog = ypDialog.getDialog();
    }

    private void startCopyToOtherSnapshot(Long to) {
        if(loadingDialog == null || viewModel == null || to == null) return;
        if(dialog != null){
            if(dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }
        dataIsSaving = true;
        loadingDialog.openDialog();
        AsyncTask.execute(()->{
            viewModel.importFromSnapshotToAnotherSnapshot(snapshot, to);
            loadingDialog.closeDialog();
            dataIsSaving = false;
        });
    }

    private void onThemeSpinnerItemSelect(){
        binding.tvTheme.setText(themeAdapter.getItem(themeId), false);
        binding.tvTheme.setSelection(binding.tvTheme.getText().length());
        if(sharedPref != null){
            SharedPreferences.Editor editor = sharedPref.edit();
            switch (themeId){
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
        new Handler().postDelayed(this::checkThemeSettings,200);
    }

    private void checkThemeSettings(){
        if(getActivity() != null) {
            if(getActivity().getClass() == MainActivity.class) {
                MainActivity activity = (MainActivity) getActivity();
                activity.checkThemeSettings();
            }
        }
    }

    private void onExportDBPressed() {
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            dialogChecked = 0;
            String[] dataTypes = {"XLS","XLSX"};
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog)
                    .setIcon(R.drawable.ic_menu_export)
                    .setTitle(getString(R.string.file_type_select_title))
                    .setSingleChoiceItems(dataTypes, dialogChecked, (d, w) -> dialogChecked = w)
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> {
                        if(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                            permissionWriteChecker.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }else{
                            onPermissionWriteResult(true);
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    private void onExportDBActionXLSX() {
        if(backThread == null){
            if(loadingDialog != null) loadingDialog.openDialog();
            backThread = new Thread(()->{
                List<Land> lands = new ArrayList<>();
                List<LandZone> zones = new ArrayList<>();
                List<Land> temp1 = viewModel.getLands().getValue();
                if(temp1 != null) {
                    for(Land tempLand : temp1){
                        if(tempLand != null) lands.add(tempLand);
                    }
                }
                Map<Long,List<LandZone>> temp2 = viewModel.getLandZones().getValue();
                if(temp2 != null){
                    temp2.forEach((k,v)-> {
                        if(v != null)
                            zones.addAll(v);
                    });
                }
                boolean result = false;
                if(lands.size() > 0 || zones.size()>0){
                    try{
                        result = FileUtil.dbExportAFileFileXLSX(lands,zones);
                    }catch (IOException e) {
                        Log.e(TAG, "onExportDBActionXLSX: ",e);
                        result = false;
                    }
                }
                backThread = null;
                if(loadingDialog != null) loadingDialog.closeDialog();
                onExportDBResult(result);
            });
            backThread.start();
        }else{
            showSnackBar(getString(R.string.open_xml_action_not_ended_error));
        }
    }

    private void onExportDBActionXLS() {
        if(backThread == null){
            if(loadingDialog != null) loadingDialog.openDialog();
            backThread = new Thread(()->{
                List<Land> lands = new ArrayList<>();
                List<LandZone> zones = new ArrayList<>();
                List<Land> temp1 = viewModel.getLands().getValue();
                if(temp1 != null) {
                    lands.addAll(temp1);
                }
                Map<Long,List<LandZone>> temp2 = viewModel.getLandZones().getValue();
                if(temp2 != null){
                    temp2.forEach((k,v)-> zones.addAll(v));
                }
                boolean result = false;
                if(lands.size() > 0 || zones.size()>0){
                    try{
                        result = FileUtil.dbExportAFileFileXLS(lands,zones);
                    }catch (IOException e) {
                        Log.e(TAG, "onExportDBActionXLS: ",e);
                        result = false;
                    }
                }
                backThread = null;
                if(loadingDialog != null) loadingDialog.closeDialog();
                onExportDBResult(result);
            });
            backThread.start();
        }else{
            showSnackBar(getString(R.string.open_xml_action_not_ended_error));
        }
    }

    private void onExportDBResult(boolean result) {
        if(getActivity() != null){
            getActivity().runOnUiThread(()->{
                String text;
                if(result){
                    text = getString(R.string.export_db_success);
                }else{
                    text = getString(R.string.export_db_fail);
                }
                showSnackBar(text);
            });
        }
    }

    private void factoryReset(){
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

    public void goBack(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(()->getActivity().onBackPressed());
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
            String title = getString(R.string.file_backup_picker_label);
            Intent intent = FileUtil.getFilePickerIntent(title);
            fileLauncher.launch(intent);
        }
    }

    private void onFileResult(ActivityResult result) {
        Intent intent = result.getData();
        int resultCode = result.getResultCode();
        if(resultCode != Activity.RESULT_OK || intent == null) return;

        InputStream is;
        try {
            if(getActivity() != null) is = getActivity().getContentResolver().openInputStream(intent.getData());
            else is = null;
        }catch (Exception e){
            Log.e(TAG, "onFileResult: ", e);
            is = null;
        }

        if(is == null) return;

        final InputStream fis = is;
        if(backThread == null){
            if(loadingDialog != null) loadingDialog.openDialog();
            backThread = new Thread(()->{
                List<Land> lands = new ArrayList<>();
                List<LandZone> zones = new ArrayList<>();
                boolean ImportResult = false;
                if(OpenXmlDataBaseInput.importDB(fis,lands,zones)){
                    ImportResult = true;
                    saveData(lands, zones);
                }
                if(loadingDialog != null) loadingDialog.closeDialog();
                onImportDBResult(ImportResult);
                backThread = null;
            });
            backThread.start();
        }else{
            showSnackBar(getString(R.string.open_xml_action_not_ended_error));
        }
    }

    private void onPermissionWriteResult(boolean permission) {
        if(permission){
            if(dialogChecked == 0){
                onExportDBActionXLS();
            }else{
                onExportDBActionXLSX();
            }
        }
    }

    private void saveData(List<Land> lands, List<LandZone> zones) {
        try {
            dataIsSaving = true;
            Log.d(TAG, "saveData: lands size = "+lands.size());
            Log.d(TAG, "saveData: zones size = "+zones.size());
            viewModel.importLandsAndZones(lands, zones);
        } catch (Exception e) {
            Log.e(TAG, "saveData: ", e);
        } finally {
            dataIsSaving = false;
            Log.d(TAG, "saveData: dataIsSaving = false");
        }
    }

    private void onImportDBResult(boolean result) {
        if(getActivity() != null){
            getActivity().runOnUiThread(()->{
                String text;
                if(result){
                    text = getString(R.string.import_db_success);
                }else{
                    text = getString(R.string.import_db_failed);
                }
                showSnackBar(text);
            });
        }
    }

    private void showSnackBar(String text){
        Log.d(TAG, "showSnackBar: "+text);
        Snackbar s = Snackbar.make(binding.clSnackBarContainer, text, Snackbar.LENGTH_LONG);
        s.setAction(getString(R.string.okey),view -> {});
        s.show();
    }

    private void toCalendarCategories(){
        FragmentActivity activity = getActivity();
        if(activity == null) return;
        activity.runOnUiThread(()-> {
            NavController nav = UIUtil.getNavController(this,R.id.AppSettingsFragment);
            if(nav != null)
                nav.navigate(R.id.toCalendarCategories);
        });
    }

    private void toBulkEdit(){
        FragmentActivity activity = getActivity();
        if(activity == null) return;
        activity.runOnUiThread(()-> {
            NavController nav = UIUtil.getNavController(this,R.id.AppSettingsFragment);
            if(nav != null)
                nav.navigate(R.id.toBulkEditor);
        });
    }
}