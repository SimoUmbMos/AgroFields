package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLoadingBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.ArrayList;

public class LoadingFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LoadingFragment";
    private FragmentLoadingBinding binding;
    private Intent intent;
    private User currUser;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLoadingBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        AsyncTask.execute(()->initViewModel(lifecycleOwner));
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

    private void init(){
        currUser = null;
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
    private void initViewModel(LifecycleOwner lifecycleOwner){
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            getActivity().runOnUiThread(()->
                    vmUsers.getCurrUser().observe(lifecycleOwner,this::onUserUpdate)
            );
        }
    }

    private void onUserUpdate(User user) {
        currUser = user;
        if(intent == null){
            if(currUser != null){
                toMenu(getActivity());
            }else{
                toLogin(getActivity());
            }
        }else{
            handleFile();
        }
    }

    private void handleFile() {
        Activity activity = getActivity();
        new Thread(()->{
            try {
                ArrayList<LandData> data = FileUtil.handleFile(activity,intent);
                toMapPreview(activity,data);
            }catch (Exception e){
                Log.e(TAG, "handleFile: ",e);
                toMapPreview(activity,new ArrayList<>());
            }
        }).start();
    }

    private void toMenu(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuMain);
            });
    }
    private void toLogin(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
                if(nav != null)
                    nav.navigate(R.id.toLoginRegister);
            });
    }
    private void toMapPreview(Activity activity, ArrayList<LandData> data) {
        if(activity != null){
            Bundle args = new Bundle();
            args.putParcelableArrayList(AppValues.argImportFragLandDataList,data);
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
                if(nav != null)
                    nav.navigate(R.id.toMapFile,args);
            });
        }
    }
}