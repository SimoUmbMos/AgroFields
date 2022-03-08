package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.LandHistoryAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandHistoryDeletedBinding;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class LandHistoriesFragment extends Fragment {
    public static final String TAG = "LandHistoryDeletedFragment";
    private LandHistoryAdapter adapter;
    private FragmentLandHistoryDeletedBinding binding;

    private int openIndex;
    private boolean notInit;
    private List<LandHistory> data;

    //init
    private void initData() {
        notInit = true;
        openIndex = -1;
        data= new ArrayList<>();
    }

    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(()->true);
            }
        }
    }

    private void initFragment() {
        String[] values = new String[]{
                getString(R.string.land_action_created),
                getString(R.string.land_action_updated),
                getString(R.string.land_action_restored),
                getString(R.string.land_action_imported),
                getString(R.string.land_action_deleted),
                getString(R.string.land_action_zone_created),
                getString(R.string.land_action_zone_updated),
                getString(R.string.land_action_zone_imported),
                getString(R.string.land_action_zone_deleted)
        };
        adapter = new LandHistoryAdapter(
                values,
                this::onLandHistoryClick,
                this::onRecordClick
        );
        binding.ibClose.setOnClickListener(v -> goBack());
        binding.ibCollapse.setOnClickListener( v -> closeAllTabs() );
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
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onHistoryChange);
        }
    }

    //observers
    private void onHistoryChange(List<LandHistoryRecord> r) {
        data.clear();
        openIndex = -1;
        List<LandHistory> tempData = LandUtil.splitLandRecordByLand(r);
        for(LandHistory tempLandHistory:tempData){
            if(tempLandHistory.getData().size()>0){
                data.add(tempLandHistory);
            }
        }
        updateUI();
        if(notInit){
            binding.tvDeletedHistoryActionLabel.setText(getString(R.string.empty_list));
            notInit = false;
        }
    }

    private void onLandHistoryClick(int tempIndex){
        if(openIndex != -1){
            if(tempIndex == openIndex){
                data.get(tempIndex).setVisible(false);
                adapter.notifyItemChanged(tempIndex);
                tempIndex = -1;
            }else{
                data.get(openIndex).setVisible(false);
                data.get(tempIndex).setVisible(true);
                adapter.notifyItemChanged(openIndex);
                adapter.notifyItemChanged(tempIndex);
            }
        }else{
            data.get(tempIndex).setVisible(true);
            adapter.notifyItemChanged(tempIndex);
        }
        openIndex = tempIndex;
        updateUI();
    }

    public void onRecordClick(LandHistoryRecord record){
        Land land = new Land(LandUtil.getLandDataFromLandRecord(record));
        ArrayList<LandZone> zones = LandUtil.getLandZonesFromLandRecord(record);
        if(land.getData() != null){
            toLandMap(getActivity(), land, zones);
        }
    }

    //ui
    private void updateUI() {
        if(data.size()>0){
            binding.tvDeletedHistoryActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvDeletedHistoryActionLabel.setVisibility(View.VISIBLE);
        }

        binding.ibCollapse.setEnabled(openIndex!=-1);
        if(openIndex!=-1){
            binding.ibCollapse.setVisibility(View.VISIBLE);
        }else{
            binding.ibCollapse.setVisibility(View.GONE);
        }
        adapter.saveData(data);
    }

    private void closeAllTabs() {
        if(openIndex != -1){
            data.get(openIndex).setVisible(false);
            adapter.notifyItemChanged(openIndex);
            openIndex = -1;
            updateUI();
        }
    }

    //nav
    private void goBack(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(()->getActivity().onBackPressed());
    }
    public void toLandMap(@Nullable Activity activity, Land land, ArrayList<LandZone> zones) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandHistoryDeletedFragment);
                Bundle bundle = new Bundle();
                bundle.putBoolean(AppValues.argIsHistory,true);
                bundle.putParcelable(AppValues.argLand, land);
                if(zones != null) bundle.putParcelableArrayList(AppValues.argZones, zones);
                if(nav != null)
                    nav.navigate(R.id.toMapLandPreview,bundle);
            });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLandHistoryDeletedBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModels();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}