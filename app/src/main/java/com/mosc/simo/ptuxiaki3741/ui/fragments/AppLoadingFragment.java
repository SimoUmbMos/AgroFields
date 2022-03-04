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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.backend.entities.Snapshot;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppLoadingFragment extends Fragment {
    public static final String TAG = "LoadingFragment";
    private Intent intent;

    private final ActivityResultLauncher<String> permissionReadChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::handleFileAction
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading,container,false);
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

        AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        appVM.getLandsHistory().observe(getViewLifecycleOwner(), this::onUpdate);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        long snapshotKey = sharedPref.getLong(AppValues.argSnapshotKey, Snapshot.getInstance().getKey());
        Log.d(TAG, "initViewModel: key = "+snapshotKey);
        AsyncTask.execute(()->{
            appVM.setDefaultSnapshot(Snapshot.getInstance(snapshotKey));
            Log.d(TAG, "initViewModel: setDefaultSnapshot = "+snapshotKey);
        });
    }

    private void onUpdate(List<LandHistoryRecord> records) {
        Log.d(TAG, "onUpdate:  called");
        if(records != null) {
            toMenu(getActivity());
        }
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
                        if(getActivity().getClass() == MainActivity.class){
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.runOnUiThread(()->{
                                Toast.makeText(
                                        mainActivity,
                                        getText(R.string.file_input_error),
                                        Toast.LENGTH_SHORT
                                ).show();
                                mainActivity.finish();
                            });
                        }
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
        if(activity != null){
            Bundle args = new Bundle();
            args.putParcelableArrayList(AppValues.argLands,data);
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
                if(nav != null)
                    nav.navigate(R.id.toMapFile,args);
            });
        }
    }

}