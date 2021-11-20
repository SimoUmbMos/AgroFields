package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
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
import com.mosc.simo.ptuxiaki3741.adapters.LandHistoryDeletedAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandHistoryDeletedBinding;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryDeletedFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandHistoryDeletedFragment";
    private LandHistoryDeletedAdapter adapter;
    private FragmentLandHistoryDeletedBinding binding;

    private UserViewModel vmUsers;

    private int openIndex;
    private boolean notInit;
    private List<LandHistory> data;
    private List<User> users;

    //init
    private void initData() {
        notInit = true;
        openIndex = -1;
        data = new ArrayList<>();
        users = new ArrayList<>();
    }
    private void initActivity() {
        ActionBar actionBar = null;
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                if( mainActivity != null){
                    mainActivity.setOnBackPressed(this);
                    actionBar = mainActivity.getSupportActionBar();
                }
            }
        }
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.land_history));
            actionBar.show();
        }
    }
    private void initFragment() {
        String[] values = new String[]{
                getString(R.string.land_action_created),
                getString(R.string.land_action_updated),
                getString(R.string.land_action_restored),
                getString(R.string.land_action_deleted)
        };
        adapter = new LandHistoryDeletedAdapter(
                data,
                users,
                values,
                this::onLandHistoryClick,
                this::onRecordClick
        );
        binding.tvDeletedHistoryActionLabel.setText(getString(R.string.loading_list));
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvDeletedHistoryList.setLayoutManager(layoutManager);
        binding.rvDeletedHistoryList.setHasFixedSize(true);
        binding.rvDeletedHistoryList.setAdapter(adapter);
    }
    private void initViewModels() {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onHistoryChange);
        }
    }
    //data
    private void populateData(List<LandDataRecord> r) {
        List<Long> uid = new ArrayList<>();
        users.clear();
        data.clear();
        openIndex = -1;

        List<LandHistory> tempData = LandUtil.splitLandRecordByLand(r);
        for(LandHistory tempLandHistory:tempData){
            if(tempLandHistory.getData().size()>0){
                data.add(tempLandHistory);
                for(LandDataRecord tempRecord : tempLandHistory.getData()){
                    if(!uid.contains(tempRecord.getUserID())){
                        uid.add(tempRecord.getUserID());
                        users.add(vmUsers.getUserByID(tempRecord.getUserID()));
                    }
                }
            }
        }
    }
    private void onHistoryChange(List<LandDataRecord> r) {
        AsyncTask.execute(()->{
            if(getActivity() != null) {
                populateData(r);
                getActivity().runOnUiThread(this::updateUI);
            }
        });
        if(notInit){
            binding.tvDeletedHistoryActionLabel.setText(getString(R.string.empty_list));
            notInit = false;
        }
    }
    private void onLandHistoryClick(LandHistory landHistory){
        int tempIndex = data.indexOf(landHistory);
        if(openIndex != -1){
            if(tempIndex == openIndex){
                data.get(tempIndex).setVisible(false);
                tempIndex = -1;
            }else{
                data.get(openIndex).setVisible(false);
                data.get(tempIndex).setVisible(true);
            }
        }else{
            data.get(tempIndex).setVisible(true);
        }
        openIndex = tempIndex;
        updateUI();
    }
    public void onRecordClick(LandDataRecord record){
        Land land = new Land(LandUtil.getLandDataFromLandRecord(record));
        if(land.getData() != null){
            toLandMap(getActivity(),land);
        }
    }

    //ui
    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        if(data.size()>0){
            binding.rvDeletedHistoryList.setVisibility(View.VISIBLE);
            binding.tvDeletedHistoryActionLabel.setVisibility(View.GONE);
        }else{
            binding.rvDeletedHistoryList.setVisibility(View.GONE);
            binding.tvDeletedHistoryActionLabel.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
        if(getActivity() != null){
            getActivity().invalidateOptionsMenu();
        }
    }
    private void closeAllTabs() {
        if(openIndex != -1){
            data.get(openIndex).setVisible(false);
            openIndex = -1;
            updateUI();
        }
    }

    //nav
    public void toLandMap(@Nullable Activity activity, Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandHistoryDeletedFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandLandMapPreviewFragment,land);
                bundle.putBoolean(AppValues.argIsHistoryLandMapPreviewFragment,true);
                if(nav != null)
                    nav.navigate(R.id.toMapLandPreview,bundle);
            });
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLandHistoryDeletedBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModels();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.land_history_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_item_toggle_land_history_lists);
        if(item != null){
            item.setVisible(openIndex!=-1);
        }
        super.onPrepareOptionsMenu(menu);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_toggle_land_history_lists){
            closeAllTabs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }
}