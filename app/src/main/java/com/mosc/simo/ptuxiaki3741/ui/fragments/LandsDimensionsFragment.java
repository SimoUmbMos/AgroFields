package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLandsDimensionsBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.LandDimenAdapter;

import java.util.ArrayList;
import java.util.List;

public class LandsDimensionsFragment extends Fragment {
    private FragmentLandsDimensionsBinding binding;
    private LandDimenAdapter adapter;

    private final List<Land> data = new ArrayList<>();
    private final List<LandData> selectedData = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLandsDimensionsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initFragment();
        initViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initActivity() {
        if(getActivity() == null) return;
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressed(()->true);
    }

    private void initFragment() {
        binding.ibBack.setOnClickListener(v->goBack());
        binding.ibSelectAll.setOnClickListener(v->selectAllData());

        binding.rvLandsDimensions.setHasFixedSize(true);
        binding.rvLandsDimensions.setLayoutManager(
                new LinearLayoutManager(
                        binding.rvLandsDimensions.getContext(),
                        RecyclerView.VERTICAL,
                        false
                )
        );
        adapter = new LandDimenAdapter();
        adapter.setOnClick(this::onLandsSelect);
        binding.rvLandsDimensions.setAdapter(adapter);
        binding.tvListLabel.setText(R.string.loading_list);
        updateAreaUI();
    }

    private void initViewModel() {
        FragmentActivity activity = getActivity();
        if(activity == null) return;
        AppViewModel appVM = new ViewModelProvider(activity).get(AppViewModel.class);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> appVM.getLands().observe(
                getViewLifecycleOwner(),
                this::onLandsUpdate
        ));
    }

    private void onLandsUpdate(List<Land> lands) {
        binding.tvListLabel.setText(R.string.empty_list);
        data.clear();
        selectedData.clear();
        if(lands != null){
            for(Land land : lands){
                if(land == null || land.getData() == null) continue;
                data.add(land);
            }
        }
        updateListUI();
    }

    private void onLandsSelect(Land land){
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).getData().getId() == land.getData().getId()) {
                Land currLand = data.get(i);
                if(currLand.isSelected()) {
                    selectedData.remove(currLand.getData());
                    currLand.setSelected(false);
                }else{
                    selectedData.add(currLand.getData());
                    currLand.setSelected(true);
                }
                adapter.notifyItemChanged(i);
                break;
            }
        }
        updateAreaUI();
    }

    private void selectAllData(){
        Log.d("TAG", "selectAllData: ");
        if(selectedData.size() == data.size()){
            selectedData.clear();
            for(Land land:data){
                land.setSelected(false);
                adapter.notifyItemChanged(data.indexOf(land));
            }
        }else{
            for(Land land:data){
                if(!selectedData.contains(land.getData())){
                    land.setSelected(true);
                    selectedData.add(land.getData());
                    adapter.notifyItemChanged(data.indexOf(land));
                }
            }
        }
        updateAreaUI();
    }

    private void updateListUI() {
        adapter.setData(new ArrayList<>(data));
        updateAreaUI();
        if(data.size() > 0)
            binding.tvListLabel.setVisibility(View.GONE);
        else
            binding.tvListLabel.setVisibility(View.VISIBLE);
    }

    private void updateAreaUI(){
        binding.tvSubTitle.setText(
                DataUtil.getAreaString(
                        binding.tvSubTitle.getContext(),
                        calcAreas()
                )
        );
        binding.tvSubTitle.setSelected(true);
    }

    private double calcAreas(){
        double totalArea = 0.0;
        for(LandData data : selectedData){
            totalArea += LandUtil.landArea(data);
        }
        return totalArea;
    }

    private void goBack() {
        FragmentActivity activity = getActivity();
        if(activity == null) return;
        activity.runOnUiThread(activity::onBackPressed);
    }
}