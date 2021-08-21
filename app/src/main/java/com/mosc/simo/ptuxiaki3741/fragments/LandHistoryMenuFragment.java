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
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LandHistoryListViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.LandHistoryList;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandHistoryMenuFragment";
    private LandHistoryListViewHolder viewHolder;
    private LandViewModel vmLands;

    private final List<LandHistoryList> data = new ArrayList<>();
    private final List<LandRecord> history = new ArrayList<>();

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
        initObservers();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.land_history_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_toggle_land_history_lists) {
            if(viewHolder != null){
                if(viewHolder.areAllListsVisible()){
                    viewHolder.hideAllLists();
                }else{
                    viewHolder.showAllLists();
                }
            }
        }
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
            actionBar.setTitle(getString(R.string.land_history));
            actionBar.show();
        }
    }
    private void initViewModel() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
        }
    }
    private void initFragment(View view) {
        viewHolder = new LandHistoryListViewHolder(
                view,
                data,
                getResources()
        );
    }
    private void initObservers(){
        if(vmLands != null){
            onLoadingStatus(true);
            onLandHistoryUpdate(vmLands.getLandsHistoryList());
            onLoadingStatus(false);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onLandHistoryUpdate);
            vmLands.isLoadingLandRecords().observe(getViewLifecycleOwner(),this::onLoadingStatus);
        }
    }

    private void onLandHistoryUpdate(List<LandRecord> r) {
        history.clear();
        data.clear();

        history.addAll(r);
        List<List<LandRecord>> recordsList = LandUtil.splitLandRecordByLand(history);
        for(List<LandRecord> records : recordsList){
            data.add(new LandHistoryList(records));
        }

        viewHolder.update();
    }
    private void onLoadingStatus(boolean isLoading) {
        viewHolder.isLoading(isLoading);
    }

}