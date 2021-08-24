package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuHistoryBinding;
import com.mosc.simo.ptuxiaki3741.adapters.LandHistoryListAdapter;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandHistoryList;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;

import java.util.ArrayList;
import java.util.List;

public class HistoryMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandHistoryMenuFragment";
    private LandHistoryListAdapter adapter;
    private FragmentMenuHistoryBinding binding;
    private LandViewModel vmLands;
    private boolean isLoading;

    private final List<LandHistoryList> data = new ArrayList<>();
    private final List<LandRecord> history = new ArrayList<>();

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        inflater.inflate(R.menu.land_history_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_toggle_land_history_lists) {
            if(areAllListsVisible()){
                hideAllLists();
            }else{
                showAllLists();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    //init
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
    private void initFragment() {
        isLoading = false;
        binding.tvHistoryActionLabel.setText(getResources().getString(R.string.empty_list));
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvHistoryList.setLayoutManager(layoutManager);
        binding.rvHistoryList.setHasFixedSize(true);
        adapter = new LandHistoryListAdapter(data,this::onRecordClick);
        binding.rvHistoryList.setAdapter(adapter);
        checkIfAdapterIsPopulated();
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

    //ui
    private void onLandHistoryUpdate(List<LandRecord> r) {
        history.clear();
        data.clear();

        history.addAll(r);
        List<List<LandRecord>> recordsList = LandUtil.splitLandRecordByLand(history);
        for(List<LandRecord> records : recordsList){
            data.add(new LandHistoryList(records));
        }

        update();
    }
    private void onLoadingStatus(boolean isLoading) {
        this.isLoading = isLoading;
        if(isLoading){
            binding.tvHistoryActionLabel.setText(getResources().getString(R.string.loading_list));
            binding.tvHistoryActionLabel.setVisibility(View.VISIBLE);
            binding.rvHistoryList.setVisibility(View.GONE);
        }else{
            binding.tvHistoryActionLabel.setText(getResources().getString(R.string.empty_list));
            checkIfAdapterIsPopulated();
        }
    }
    private void checkIfAdapterIsPopulated() {
        if(!isLoading){
            if(adapter.getItemCount()>0){
                binding.rvHistoryList.setVisibility(View.VISIBLE);
                binding.tvHistoryActionLabel.setVisibility(View.GONE);
            }else{
                binding.tvHistoryActionLabel.setVisibility(View.VISIBLE);
                binding.rvHistoryList.setVisibility(View.GONE);
            }
        }
    }
    public boolean areAllListsVisible(){
        for(int i = 0; i < adapter.getItemCount(); i++){
            LandHistoryListAdapter.ItemViewHolder vh = (LandHistoryListAdapter.ItemViewHolder)
                    binding.rvHistoryList.findViewHolderForAdapterPosition(i);
            if(vh != null){
                if(vh.llHistoryRoot.getVisibility() == View.GONE){
                    return false;
                }
            }
        }
        return true;
    }
    public void showAllLists(){
        for(int i = 0; i < adapter.getItemCount(); i++){
            LandHistoryListAdapter.ItemViewHolder vh = (LandHistoryListAdapter.ItemViewHolder)
                    binding.rvHistoryList.findViewHolderForAdapterPosition(i);
            if(vh != null){
                if(vh.llHistoryRoot.getVisibility() != View.VISIBLE){
                    vh.toggleList();
                }
            }
        }
    }
    public void hideAllLists(){
        for(int i = 0; i < adapter.getItemCount(); i++){
            LandHistoryListAdapter.ItemViewHolder vh = (LandHistoryListAdapter.ItemViewHolder)
                    binding.rvHistoryList.findViewHolderForAdapterPosition(i);
            if(vh != null){
                if(vh.llHistoryRoot.getVisibility() != View.GONE){
                    vh.toggleList();
                }
            }
        }
    }

    //recycle view
    public void onRecordClick(LandRecord record){
        Land land = LandUtil.getLandFromLandRecord(record);
        if(land != null)
            navigate(toLandMap(land));
    }
    @SuppressLint("NotifyDataSetChanged")
    public void update() {
        adapter.notifyDataSetChanged();
        checkIfAdapterIsPopulated();
    }

    //ui
    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        if(
                navController.getCurrentDestination() == null ||
                        navController.getCurrentDestination().getId() == R.id.LandHistoryFragment
        )
            navController.navigate(action);
    }
    private NavDirections toLandMap(Land land){
        HistoryMenuFragmentDirections.ToLandMap action =
                HistoryMenuFragmentDirections.toLandMap(land);
        action.setDisplayMode(true);
        return action;
    }
}