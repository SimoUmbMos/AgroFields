package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.annotation.SuppressLint;
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
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.LandHistorySelectedAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandHistorySelectedBinding;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class LandSelectedHistoryFragment extends Fragment {
    public static final String TAG = "LandHistorySelectedFragment";
    private FragmentLandHistorySelectedBinding binding;
    private LandHistorySelectedAdapter adapter;

    private Land land;
    private List<LandHistoryRecord> data;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        binding = FragmentLandHistorySelectedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //init
    private void initData(){
        data = new ArrayList<>();
        land = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLand)){
                Object o = getArguments().getParcelable(AppValues.argLand);
                if(o.getClass() == Land.class){
                    land = (Land) o;
                }
            }
        }
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
                getString(R.string.land_action_imported),
                getString(R.string.land_action_deleted),
                getString(R.string.land_action_zone_created),
                getString(R.string.land_action_zone_updated),
                getString(R.string.land_action_zone_imported),
                getString(R.string.land_action_zone_deleted)
        };
        adapter = new LandHistorySelectedAdapter(
                values,
                this::onRecordClick
        );
        binding.rvHistoryList.setAdapter(adapter);
        updateUI(false);
    }

    private void initViewModel() {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onHistoryChange);
        }
    }

    //observers
    @SuppressLint("NotifyDataSetChanged")
    private void onHistoryChange(List<LandHistoryRecord> r) {
        data.clear();
        if (land != null) {
            List<LandHistory> temp1 = LandUtil.splitLandRecordByLand(r);
            for(LandHistory temp2 : temp1){
                if(temp2.getLandData().getId() == land.getData().getId()){
                    data.addAll(temp2.getData());
                    break;
                }
            }
        }
        updateUI(true);
    }

    public void onRecordClick(LandHistoryRecord record){
        Land land = new Land(LandUtil.getLandDataFromLandRecord(record));
        ArrayList<LandZone> zones = LandUtil.getLandZonesFromLandRecord(record);
        if(land.getData() != null){
            toLandMap(getActivity(), land, zones);
        }
    }

    //ui
    private void updateUI(boolean save) {
        if(data.size()>0 ){
            binding.tvHistoryActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvHistoryActionLabel.setVisibility(View.VISIBLE);
        }
        if(save) adapter.saveData(data);
    }

    //ui
    public void toLandMap(@Nullable Activity activity, Land land, ArrayList<LandZone> zones) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandHistorySelectedFragment);
                Bundle bundle = new Bundle();
                bundle.putBoolean(AppValues.argIsHistory,true);
                bundle.putParcelable(AppValues.argLand,land);
                if(zones != null) bundle.putParcelableArrayList(AppValues.argZones, zones);
                if(nav != null)
                    nav.navigate(R.id.toMapLandPreview,bundle);
            });
    }
}