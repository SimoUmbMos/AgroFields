package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppLoadingFragment extends Fragment {
    public static final String TAG = "LoadingFragment";
    private Intent intent;
    private int oldYear;
    private AppViewModel appVM;

    private final ActivityResultLauncher<String> permissionReadChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::handleFileAction
    );

    public AppLoadingFragment(){
        super(R.layout.fragment_loading);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = initActivity();
        if(intent != null){
            handleFile(intent);
        }else{
            initViewModel();
        }
    }

    private Intent initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(()->true);
                return mainActivity.getIntentIfCalledByFile();
            }
        }
        return null;
    }

    private void initViewModel(){
        Log.d(TAG, "initViewModel: called");
        if(getActivity() == null) return;

        appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        appVM.getSnapshots().observe(getViewLifecycleOwner(), this::onUpdate);

        AsyncTask.execute(()-> appVM.setDefaultSnapshot(LocalDate.now().getYear()));
    }

    private void onUpdate(List<Long> snapshots) {
        if(snapshots == null || getActivity() == null) return;
        Activity activity = getActivity();
        AsyncTask.execute(()->{
            if(isNewYear()){
                showDialogNewYearImport(activity);
            }else{
                toMenu(activity);
            }
        });
    }

    private boolean isNewYear() {
        if(getActivity() == null) return false;
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        oldYear = pref.getInt(AppValues.argSnapshotKey, -1);
        int currYear = LocalDate.now().getYear();
        if(oldYear == -1){
            editor = pref.edit();
            editor.putInt(AppValues.argSnapshotKey, currYear);
            editor.apply();
        }else if(oldYear != currYear){
            editor = pref.edit();
            editor.putInt(AppValues.argSnapshotKey, currYear);
            editor.apply();
            return appVM.getLands(oldYear).size() > 0;
        }
        return false;
    }

    private void showDialogNewYearImport(Activity activity) {
        if(activity == null) return;
        activity.runOnUiThread(()->{
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialog);
            builder.setCancelable(false);
            builder.setTitle(R.string.new_year_import_title);
            builder.setMessage(R.string.new_year_import_msg);
            builder.setNeutralButton(
                    R.string.cancel,
                    (d,i)-> toMenu(activity)
            );
            builder.setPositiveButton(
                    R.string.import_label,
                    (d,i)-> AsyncTask.execute(this::importToNewYear)
            );
            builder.create().show();
        });
    }

    private void importToNewYear() {
        if(appVM != null)
            appVM.importFromSnapshotToAnotherSnapshot(oldYear, LocalDate.now().getYear());
    }

    private void handleFile(Intent intent) {
        this.intent = intent;
        permissionReadChecker.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void handleFileAction(boolean permissionResult) {
        if(permissionResult){
            Activity activity = getActivity();
            new Thread(()->{
                ArrayList<LandData> data = FileUtil.handleFile(activity,intent);
                if(data.size()>0){
                    toMapPreview(activity,data);
                }else{
                    if(getActivity() != null){
                        getActivity().runOnUiThread(()->{
                            Toast.makeText(
                                    getActivity(),
                                    getText(R.string.file_input_error),
                                    Toast.LENGTH_SHORT
                            ).show();
                            getActivity().finish();
                        });
                    }
                }
            }).start();
        }
    }

    private void toMenu(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuMain);
            });
    }

    private void toMapPreview(Activity activity, ArrayList<LandData> data) {
        if(activity == null) return;
        Bundle args = new Bundle();
        args.putParcelableArrayList(AppValues.argLands,data);
        activity.runOnUiThread(()-> {
            NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
            if(nav != null)
                nav.navigate(R.id.toMapFile,args);
        });
    }

}