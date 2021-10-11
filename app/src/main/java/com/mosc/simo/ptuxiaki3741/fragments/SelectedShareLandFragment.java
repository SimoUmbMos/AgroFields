package com.mosc.simo.ptuxiaki3741.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentSelectedSharedLandBinding;

public class SelectedShareLandFragment extends Fragment {
    //todo: (idea) make toggle edit privilege
    //todo: (idea) make change ownership
    private FragmentSelectedSharedLandBinding binding;

    private void initData() {

    }
    private void initActivity() {

    }
    private void initFragment() {
        binding.mvLand.getMapAsync(this::initMap);
    }
    private void initViewModel() {

    }
    private void initMap(GoogleMap googleMap) {

    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSelectedSharedLandBinding.inflate(inflater,container,false);
        binding.mvLand.onCreate(savedInstanceState);
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
        binding.mvLand.onDestroy();
        binding = null;
    }
    @Override public void onResume() {
        super.onResume();
        binding.mvLand.onResume();
    }
    @Override public void onPause() {
        super.onPause();
        binding.mvLand.onPause();
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        binding.mvLand.onLowMemory();
    }
}