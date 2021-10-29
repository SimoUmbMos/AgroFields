package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuHistoryBinding;
import com.mosc.simo.ptuxiaki3741.adapters.LandHistoryListAdapter;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class MenuHistoryFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandHistoryMenuFragment";
    private LandHistoryListAdapter adapter;
    private FragmentMenuHistoryBinding binding;

    private List<LandHistory> data;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModel();
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
    private void initData(){
        data = new ArrayList<>();
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
    private void initFragment() {
        binding.tvHistoryActionLabel.setText(getString(R.string.empty_list));
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
                data,
                values,
                this::onHeaderClick,
                this::onRecordClick
        );
        binding.rvHistoryList.setAdapter(adapter);
    }
    private void initViewModel() {
        if(getActivity() != null){
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onHistoryChange);
        }
    }

    //ui
    private void onHistoryChange(List<LandDataRecord> r) {
        if(data.size()>0){
            int size = data.size();
            data.clear();
            adapter.notifyItemRangeRemoved(0,size);
        }
        List<LandHistory> recordsList = LandUtil.splitLandRecordByLand(r);
        for(int i = 0;i<recordsList.size();i++){
            data.add(i,recordsList.get(i));
            adapter.notifyItemInserted(i);
        }
        updateUI();
    }
    private void updateUI() {
        if(data.size()>0){
            binding.rvHistoryList.setVisibility(View.VISIBLE);
            binding.tvHistoryActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvHistoryActionLabel.setVisibility(View.VISIBLE);
            binding.rvHistoryList.setVisibility(View.GONE);
        }
    }
    public boolean areAllListsVisible(){
        for(LandHistory item : data){
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
    public void onRecordClick(LandDataRecord record){
        Land land = new Land(LandUtil.getLandDataFromLandRecord(record));
        if(land.getData() != null)
            toLandMap(getActivity(),land);
    }

    //ui
    public void toLandMap(@Nullable Activity activity, Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuHistoryFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandLandMapPreviewFragment,land);
                bundle.putBoolean(AppValues.argIsHistoryLandMapPreviewFragment,true);
                if(nav != null)
                    nav.navigate(R.id.toMapLandPreview,bundle);
            });
    }
}