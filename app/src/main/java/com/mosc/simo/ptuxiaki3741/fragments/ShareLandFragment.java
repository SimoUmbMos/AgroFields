package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.ShareLandAdapter;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandWithShare;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentShareLandBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class ShareLandFragment extends Fragment implements FragmentBackPress {
    private FragmentShareLandBinding binding;

    private UserViewModel vmUsers;
    private LandViewModel vmLands;

    private User contact, currUser;
    private List<Land> lands;
    private List<LandWithShare> sharedLands, data;
    private ShareLandAdapter adapter;

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
        lands = new ArrayList<>();
        sharedLands = new ArrayList<>();
        data = new ArrayList<>();
        if (getArguments() != null) {
            if(getArguments().containsKey(AppValues.SHARE_LAND_ARG)){
                contact = getArguments().getParcelable(AppValues.SHARE_LAND_ARG);
            }
        }
        if(contact == null)
            goBack();
        adapter = new ShareLandAdapter(data,this::onSharedLandItemClick);
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
    private void initFragment(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rcSharedLand.setLayoutManager(layoutManager);
        binding.rcSharedLand.setHasFixedSize(true);
        binding.rcSharedLand.setAdapter(adapter);
    }
    private void initObservers(){
        if(vmUsers != null){
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
        }
        if(vmLands != null){
            vmLands.getSharedLands().observe(getViewLifecycleOwner(),this::onSharedLandsUpdate);
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandsUpdate);
        }
    }

    //onUpdate
    private void onCurrUserUpdate(User currUser) {
        this.currUser = currUser;
    }
    private void onSharedLandsUpdate(List<LandWithShare> sharedLands) {
        this.sharedLands.clear();
        if(sharedLands != null){
            for(LandWithShare sharedLand : sharedLands){
                if(sharedLand.getData() != null && sharedLand.getSharedData() != null){
                    if(sharedLand.getSharedData().getUserID() == contact.getId()){
                        this.sharedLands.add(sharedLand);
                    }
                }
            }
        }
        onDataUpdate();
    }
    private void onLandsUpdate(List<Land> lands) {
        this.lands.clear();
        if(lands != null){
            for(Land land : lands){
                if(land.getData() != null){
                    if(land.getData().getCreator_id() == currUser.getId()){
                        this.lands.add(land);
                    }
                }
            }
        }
        onDataUpdate();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void onDataUpdate() {
        data.clear();
        if(lands.size()>0){
            if(sharedLands.size()>0){
                boolean isInserted;
                for(Land land:lands){
                    isInserted = false;
                    for(LandWithShare sharedLand:sharedLands){
                        if(sharedLand.getData().getId() == land.getData().getId()){
                            isInserted = true;
                            data.add(sharedLand);
                            break;
                        }
                    }
                    if(isInserted)
                        continue;
                    data.add(new LandWithShare(land.getData(),null));
                }
            }else{
                for(Land land:lands){
                    data.add(new LandWithShare(land.getData(),null));
                }
            }
        }
        if(data.size()>0){
            binding.rcSharedLand.setVisibility(View.VISIBLE);
            binding.tvSharedLandDisplay.setVisibility(View.GONE);
        }else{
            binding.rcSharedLand.setVisibility(View.GONE);
            binding.tvSharedLandDisplay.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
    private void onSharedLandItemClick(int pos, boolean wasChecked){
        LandWithShare entity = data.get(pos);
        if(wasChecked){
            vmLands.removeSharedLand(entity.getData(),contact);
        }else{
            vmLands.addSharedLand(entity.getData(),contact);
        }
    }

    //UI
    private void goBack(){
        if(getActivity() != null)
            getActivity().onBackPressed();
    }
}