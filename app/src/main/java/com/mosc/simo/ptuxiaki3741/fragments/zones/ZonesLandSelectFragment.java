package com.mosc.simo.ptuxiaki3741.fragments.zones;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.LandListAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentZonesLandSelectBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class ZonesLandSelectFragment extends Fragment implements FragmentBackPress {
    //fixme: tags search
    private FragmentZonesLandSelectBinding binding;
    private LandListAdapter adapter;

    private List<Land> data;

    private void initData() {
        data = new ArrayList<>();
    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
                activity.setToolbarTitle(getString(R.string.zones_land_list_bar_label), Gravity.CENTER);
                ActionBar actionBar = activity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.show();
                }
            }
        }
    }
    private void initFragment() {
        binding.tvZoneLandListActionLabel.setText(getResources().getString(R.string.empty_list));
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvZoneLandList.setLayoutManager(layoutManager);
        binding.rvZoneLandList.setHasFixedSize(true);
        adapter = new LandListAdapter(data,this::onLandClick);
    }
    private void initViewModel() {
        if(getActivity() != null){
            AppViewModel vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onDataUpdate);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onDataUpdate(List<Land> lands) {
        data.clear();
        if(lands != null){
            data.addAll(lands);
        }
        adapter.notifyDataSetChanged();
        updateUI();
    }
    private void onLandClick(Land land) {
        toZoneLandSelected(getActivity(),land);
    }

    private void updateUI() {
        if(data.size()>0){
            binding.rvZoneLandList.setVisibility(View.VISIBLE);
            binding.tvZoneLandListActionLabel.setVisibility(View.GONE);
        }else{
            binding.rvZoneLandList.setVisibility(View.GONE);
            binding.tvZoneLandListActionLabel.setVisibility(View.VISIBLE);
        }
    }

    private void toZoneLandSelected(@Nullable Activity activity, Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ZonesLandSelectFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,land);
                if(nav != null){
                    nav.navigate(R.id.toZonesLandSelected,bundle);
                }
            });
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentZonesLandSelectBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
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
    @Override public void onLowMemory() {
        super.onLowMemory();
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onLowMemory();
            }
        }
    }
    @Override public void onPause() {
        super.onPause();
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onPause();
            }
        }
    }
    @Override public void onResume() {
        super.onResume();
        binding.rvZoneLandList.setAdapter(adapter);
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onResume();
            }
        }
    }
    @Override public void onDestroy() {
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onDestroy();
            }
        }
        super.onDestroy();
    }
}