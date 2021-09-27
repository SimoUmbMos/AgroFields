package com.mosc.simo.ptuxiaki3741.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentShareLandBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class ShareLandFragment extends Fragment implements FragmentBackPress {
    private FragmentShareLandBinding binding;

    private UserViewModel vmUsers;
    private LandViewModel vmLands;

    private User contact;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentShareLandBinding.inflate(inflater,parent,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModel();
        initFragment();
        initObservers();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    //init
    private void initData(){
        if (getArguments() != null) {
            if(getArguments().containsKey(AppValues.SHARE_LAND_ARG)){
                contact = getArguments().getParcelable(AppValues.SHARE_LAND_ARG);
            }
        }
        if(contact == null)
            goBack();
    }
    private void initActivity(){
        MainActivity mainActivity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.share_land_list_title));
            actionBar.show();
        }
    }
    private void initViewModel(){
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
        }
    }
    //todo: fill initFragment, initObservers methods
    private void initFragment(){

    }
    private void initObservers(){

    }

    //UI
    private void goBack(){
        if(getActivity() != null)
            getActivity().onBackPressed();
    }
}