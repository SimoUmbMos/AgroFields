package com.mosc.simo.ptuxiaki3741.fragments.land;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
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

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.LandHistoryAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandHistoryDeletedBinding;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryDeletedFragment extends Fragment {
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
        data = new ArrayList<>();
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
                data,
                values,
                this::onLandHistoryClick,
                this::onRecordClick
        );
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

    //data
    private void populateData(List<LandHistoryRecord> r) {
        data.clear();
        openIndex = -1;

        List<LandHistory> tempData = LandUtil.splitLandRecordByLand(r);
        for(LandHistory tempLandHistory:tempData){
            if(tempLandHistory.getData().size()>0){
                data.add(tempLandHistory);
            }
        }
    }

    //observers
    private void onHistoryChange(List<LandHistoryRecord> r) {
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

    public void onRecordClick(LandHistoryRecord record){
        Land land = new Land(LandUtil.getLandDataFromLandRecord(record));
        ArrayList<LandZone> zones = LandUtil.getLandZonesFromLandRecord(record);
        if(land.getData() != null){
            toLandMap(getActivity(), land, zones);
        }
    }

    //ui
    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        if(data.size()>0){
            binding.tvDeletedHistoryActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvDeletedHistoryActionLabel.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();

        binding.ibCollapse.setEnabled(openIndex!=-1);
        if(openIndex!=-1){
            binding.ibCollapse.setVisibility(View.VISIBLE);
        }else{
            binding.ibCollapse.setVisibility(View.GONE);
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