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
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LandHistoryMenuViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public class LandHistoryMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandHistoryMenuFragment";
    private LandViewModel vmLands;
    private LandHistoryMenuViewHolder viewHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_land_history_menu, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initViewModel();
        initFragment(view);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initActivity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.hide();
        }
    }
    private void initViewModel() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onLandUpdate);
        }
    }
    private void initFragment(View view) {
        viewHolder = new LandHistoryMenuViewHolder(
                view,
                vmLands,
                this::onLandHistoryClick,
                this::onLandHistoryLongClick
        );
    }

    private void onLandHistoryClick(int pos) {

    }

    private boolean onLandHistoryLongClick(int pos) {
        return true;
    }

    private void onLandUpdate(List<LandRecord> landRecords) {
        viewHolder.update();
    }
}