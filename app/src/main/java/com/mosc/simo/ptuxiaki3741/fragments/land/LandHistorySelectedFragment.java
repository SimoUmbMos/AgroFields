package com.mosc.simo.ptuxiaki3741.fragments.land;

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
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.LandHistorySelectedAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandHistorySelectedBinding;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class LandHistorySelectedFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandHistorySelectedFragment";
    private LandHistorySelectedAdapter adapter;
    private FragmentLandHistorySelectedBinding binding;

    private Land land;
    private List<LandDataRecord> data2;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLandHistorySelectedBinding.inflate(inflater, container, false);
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
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    //init
    private void initData(){
        data2 = new ArrayList<>();
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
                mainActivity.setOnBackPressed(this);
                mainActivity.setToolbarTitle(getString(R.string.land_history));
                ActionBar actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.show();
                }
            }
        }
    }
    private void initViewModel() {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onHistoryChange);
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
                getString(R.string.land_action_deleted)
        };
        adapter = new LandHistorySelectedAdapter(
                data2,
                values,
                this::onRecordClick
        );
        binding.rvHistoryList.setAdapter(adapter);
    }

    //data
    private void populateData(List<LandDataRecord> r) {
        List<LandHistory> temp1 = LandUtil.splitLandRecordByLand(r);
        if(land != null){
            data2.clear();
            for(LandHistory temp2:temp1){
                if(temp2.getLandData().getId() == land.getData().getId()){
                    data2.addAll(temp2.getData());
                    break;
                }
            }
        }
    }

    //ui
    @SuppressLint("NotifyDataSetChanged")
    private void onHistoryChange(List<LandDataRecord> r) {
        AsyncTask.execute(()->{
            if(getActivity() != null) {
                populateData(r);
                getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    updateUI();
                });
            }
        });
    }
    private void updateUI() {
        if(getActivity() != null){
            getActivity().runOnUiThread(()->{
                if(data2.size()>0 ){
                    binding.rvHistoryList.setVisibility(View.VISIBLE);
                    binding.tvHistoryActionLabel.setVisibility(View.GONE);
                }else{
                    binding.tvHistoryActionLabel.setVisibility(View.VISIBLE);
                    binding.rvHistoryList.setVisibility(View.GONE);
                }
            });
        }
    }

    //recycle view
    public void onRecordClick(LandDataRecord record){
        Land land = new Land(LandUtil.getLandDataFromLandRecord(record));
        if(land.getData() != null){
            toLandMap(getActivity(),land);
        }
    }

    //ui
    public void toLandMap(@Nullable Activity activity, Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandHistorySelectedFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,land);
                bundle.putBoolean(AppValues.argIsHistory,true);
                if(nav != null)
                    nav.navigate(R.id.toMapLandPreview,bundle);
            });
    }
}