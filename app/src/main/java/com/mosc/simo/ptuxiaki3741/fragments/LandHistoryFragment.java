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
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.LandHistoryListAdapter;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuHistoryBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryFragment extends Fragment implements FragmentBackPress {
    //fixme: place only on land preview
    public static final String TAG = "LandHistoryMenuFragment";
    private LandHistoryListAdapter adapter;
    private FragmentMenuHistoryBinding binding;

    private UserViewModel vmUsers;

    private Land land;
    private List<LandDataRecord> data2;
    private List<User> users;

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
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    //init
    private void initData(){
        data2 = new ArrayList<>();
        users = new ArrayList<>();
        land = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLandHistoryFragment)){
                Object o = getArguments().getParcelable(AppValues.argLandHistoryFragment);
                if(o.getClass() == Land.class){
                    land = (Land) o;
                }
            }
        }
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
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            LandViewModel vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
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
                getString(R.string.land_action_deleted)
        };
        adapter = new LandHistoryListAdapter(
                data2,
                users,
                values,
                this::onRecordClick
        );
        binding.rvHistoryList.setAdapter(adapter);
    }

    //data
    private void populateData(List<LandDataRecord> r) {
        List<LandHistory> temp1 = LandUtil.splitLandRecordByLand(r);
        List<Long> uid = new ArrayList<>();
        users.clear();
        if(land != null){
            data2.clear();
            for(LandHistory temp2:temp1){
                if(temp2.getLandData().getId() == land.getData().getId()){
                    for(LandDataRecord temp3:temp2.getData()){
                        data2.add(temp3);
                        if(!uid.contains(temp3.getUserID())){
                            uid.add(temp3.getUserID());
                            users.add(vmUsers.getUserByID(temp3.getUserID()));
                        }
                    }
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
            land.setPerm(this.land.getPerm());
            toLandMap(getActivity(),land);
        }
    }

    //ui
    public void toLandMap(@Nullable Activity activity, Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandHistoryFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLandLandMapPreviewFragment,land);
                bundle.putBoolean(AppValues.argIsHistoryLandMapPreviewFragment,true);
                if(nav != null)
                    nav.navigate(R.id.toMapLandPreview,bundle);
            });
    }
}