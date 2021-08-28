package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
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
        String[] values = new String[]{
                getString(R.string.land_action_created),
                getString(R.string.land_action_updated),
                getString(R.string.land_action_restored),
                getString(R.string.land_action_deleted)
        };
        adapter = new LandHistoryListAdapter(
                getActivity(),
                data,
                values,
                this::onHeaderClick,
                this::onRecordClick
        );
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
        data.clear();

        List<List<LandRecord>> recordsList = LandUtil.splitLandRecordByLand(r);
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
        for(LandHistoryList item : data){
            if(!item.isVisible())
                return false;
        }
        return true;
    }
    public void showAllLists(){
        for(int i = 0; i < data.size();i++){
            data.get(i).setVisible(true);
            adapter.notifyItemChanged(i);
        }
    }
    public void hideAllLists(){
        for(int i = 0; i < data.size();i++){
            data.get(i).setVisible(false);
            adapter.notifyItemChanged(i);
        }
    }

    //recycle view
    public void onHeaderClick(int pos){
        data.get(pos).setVisible(!data.get(pos).isVisible());
        adapter.notifyItemChanged(pos);
    }
    public void onRecordClick(LandRecord record){
        Land land = LandUtil.getLandFromLandRecord(record);
        if(land != null)
            toLandMap(getActivity(),land);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void update() {
        adapter.notifyDataSetChanged();
        checkIfAdapterIsPopulated();
    }

    //ui
    private NavController getNavController(){
        NavController navController = NavHostFragment.findNavController(this);
        if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.LandHistoryFragment)
            return navController;
        return null;
    }
    public void toLandMap(@Nullable Activity activity, Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = getNavController();
                Bundle bundle = new Bundle();
                bundle.putParcelable(LandMapFragment.argLand,land);
                bundle.putBoolean(LandMapFragment.argDisplayMode,true);
                if(nav != null)
                    nav.navigate(R.id.landHistoryToLandMap,bundle);
            });
    }
}