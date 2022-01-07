package com.mosc.simo.ptuxiaki3741.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLoadingBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class LoadingFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LoadingFragment";
    private FragmentLoadingBinding binding;
    private Intent intent;

    private final ActivityResultLauncher<String> permissionReadChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::handleFileAction
    );

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLoadingBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        if(intent != null){
            handleFile();
        }else{
            initViewModel();
        }
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                ActionBar actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.setTitle(getString(R.string.app_name));
                    actionBar.hide();
                }
                intent = mainActivity.getIntentIfCalledByFile();
            }
        }
    }
    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getLandsHistory().observe(getViewLifecycleOwner(),this::onUpdate);
            appVM.init();
        }
    }

    private void onUpdate(List<LandDataRecord> records) {
        if(records != null)
            toMenu(getActivity());
    }

    private void handleFile() {
        permissionReadChecker.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    private void handleFileAction(boolean permissionResult) {
        if(permissionResult){
            Activity activity = getActivity();
            new Thread(()->{
                try {
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
                }catch (Exception e){
                    Log.e(TAG, "handleFile: ",e);
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