package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLoadingBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

public class LoadingFragment extends Fragment implements FragmentBackPress {
    private FragmentLoadingBinding binding;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLoadingBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        initViewModel();
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
        MainActivity mainActivity = (MainActivity) getActivity();
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.app_name));
                actionBar.hide();
            }
        }
    }
    private void initViewModel(){
        if(getActivity() != null){
            LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
            AsyncTask.execute(()->{
                UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
                getActivity().runOnUiThread(()->
                        vmUsers.getCurrUser().observe(lifecycleOwner,this::onUserUpdate)
                );
            });
        }
    }

    private void onUserUpdate(User user) {
        if(user != null){
            toMenu(getActivity());
        }else{
            toLogin(getActivity());
        }
    }

    private void toMenu(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
                if(nav != null)
                    nav.navigate(R.id.loadingToMenu);
            });
    }
    private void toLogin(Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoadingFragment);
                if(nav != null)
                    nav.navigate(R.id.loadingToLogin);
            });
    }
}