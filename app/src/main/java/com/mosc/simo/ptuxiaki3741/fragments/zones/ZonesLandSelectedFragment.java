package com.mosc.simo.ptuxiaki3741.fragments.zones;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.LandZonesListAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentZonesLandSelectedBinding;
import com.mosc.simo.ptuxiaki3741.enums.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class ZonesLandSelectedFragment extends Fragment implements FragmentBackPress {
    private FragmentZonesLandSelectedBinding binding;
    private LandZonesListAdapter adapter;
    private Land selectedLand;
    private List<LandZone> data;

    private LandListMenuState state;
    private ActionMode actionMenu;

    private AppViewModel vmLands;

    //init relative
    private void initData() {
        data = new ArrayList<>();
        selectedLand = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLand)){
                selectedLand = getArguments().getParcelable(AppValues.argLand);
            }
        }
    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
                ActionBar actionBar = activity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.setTitle(getString(R.string.zones_list_bar_label));
                    actionBar.show();
                }
            }
        }
    }
    private void initFragment() {
        binding.tvZonesListActionLabel.setText(getResources().getString(R.string.empty_list));

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        adapter = new LandZonesListAdapter(
                selectedLand,
                data,
                this::onZoneClick,
                this::onZoneLongClick
        );

        binding.rvZoneList.setHasFixedSize(true);
        binding.rvZoneList.setLayoutManager(layoutManager);
        binding.rvZoneList.setAdapter(adapter);
        updateMenu(LandListMenuState.NormalState);
    }
    private void initViewModel() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            vmLands.getLandZones().observe(getViewLifecycleOwner(),this::onDataUpdate);
        }
    }
    private void initContextualMenu(Menu menu, LandListMenuState state) {
        MenuItem item;
        switch (state){
            case MultiDeleteState:
                item = menu.findItem(R.id.menu_export_action);
                if(item != null){
                    item.setEnabled(false);
                    item.setVisible(false);
                }
                break;
            case MultiExportState:
                item = menu.findItem(R.id.menu_delete_action);
                if(item != null){
                    item.setEnabled(false);
                    item.setVisible(false);
                }
                break;
        }
    }

    //listener relative
    private boolean onMenuItemClick(MenuItem item){
        switch (item.getItemId()){
            case (R.id.menu_item_add):
                toZoneSelected(getActivity(),null);
                return true;
            case (R.id.menu_item_delete):
                updateMenu(LandListMenuState.MultiDeleteState);
                return true;
            case (R.id.menu_item_export):
                updateMenu(LandListMenuState.MultiExportState);
                return true;
            case (R.id.menu_delete_action):
                deleteAction();
                return true;
            case (R.id.menu_export_action):
                exportAction();
                return true;
            case (R.id.menu_select_all_action):
                toggleAllZone();
                return true;
            default:
                return false;
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void onDataUpdate(List<LandZone> zones) {
        data.clear();
        if(selectedLand != null && zones != null){
            for(LandZone zone : zones){
                if(zone.getData().getLid() == selectedLand.getData().getId()){
                    data.add(zone);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateUI();
    }
    private void onZoneClick(LandZone zone) {
        if(actionMenu != null){
            toggleZone(zone);
        }else{
            toZoneSelected(getActivity(),zone);
        }
    }
    private void onZoneLongClick(LandZone zone) {
        if(actionMenu != null){
            toggleZone(zone);
        }else{
            toggleZone(zone);
            updateMenu(LandListMenuState.MultiSelectState);
        }
    }

    //zone selector relative
    private void toggleZone(LandZone zone) {
        zone.setSelected(!zone.isSelected());
        adapter.notifyItemChanged(data.indexOf(zone));
        if(state == LandListMenuState.MultiSelectState && areNonZoneSelected()){
            updateMenu(LandListMenuState.NormalState);
        }
    }
    private void toggleAllZone() {
        if(areAllZoneSelected()){
            if(state == LandListMenuState.MultiSelectState){
                updateMenu(LandListMenuState.NormalState);
            }else{
                deselectAllZones();
            }
        }else{
            selectAllZones();
        }
    }
    private void selectAllZones() {
        for(LandZone zone:data){
            zone.setSelected(true);
        }
    }
    private void deselectAllZones() {
        for(LandZone zone:data){
            zone.setSelected(false);
        }
    }
    private boolean areAllZoneSelected() {
        for(LandZone zone:data){
            if(!zone.isSelected()){
                return false;
            }
        }
        return true;
    }
    private boolean areNonZoneSelected() {
        for(LandZone zone:data){
            if(zone.isSelected()){
                return false;
            }
        }
        return true;
    }
    private List<LandZone> getSelectedZones() {
        List<LandZone> ans = new ArrayList<>();
        for(LandZone zone:data){
            if(zone.isSelected()){
                ans.add(zone);
            }
        }
        return ans;
    }

    //actions relative
    private void deleteAction(){
        List<LandZone> selectedZones = getSelectedZones();
        if(selectedZones.size()>0){
            if(vmLands != null){
                Snackbar.make(binding.getRoot(), R.string.zone_delete, Snackbar.LENGTH_SHORT).show();
                vmLands.removeZones(selectedZones);
            }
            updateMenu(LandListMenuState.NormalState);
        }
    }
    private void exportAction(){
        //fixme: create zone export
        List<LandZone> selectedZones = getSelectedZones();
        if(selectedZones.size()>0){
            Snackbar.make(binding.getRoot(), R.string.zone_export, Snackbar.LENGTH_SHORT).show();
            updateMenu(LandListMenuState.NormalState);
        }
    }

    //ui relative
    @SuppressLint("NotifyDataSetChanged")
    private void updateCheckBoxes(boolean show) {
        adapter.setShowCheckMark(show);
        adapter.notifyDataSetChanged();
    }
    private void updateMenu(LandListMenuState state){
        this.state = state;
        if(actionMenu != null){
            actionMenu.finish();
            actionMenu = null;
        }
        ActionMode.Callback callback = null;
        switch (state){
            case MultiDeleteState:
                callback = new ActionMode.Callback() {
                    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        mode.getMenuInflater().inflate(R.menu.zone_list_contextual_menu,menu);
                        mode.setTitle(R.string.contextual_menu_zone_label_multi_delete);
                        return data.size()>0;
                    }
                    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        initContextualMenu(menu,state);
                        updateCheckBoxes(true);
                        return true;
                    }
                    @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item){
                        return onMenuItemClick(item);
                    }
                    @Override public void onDestroyActionMode(ActionMode mode) {
                        updateCheckBoxes(false);
                        updateMenu(LandListMenuState.NormalState);
                    }
                };
                break;
            case MultiExportState:
                callback = new ActionMode.Callback() {
                    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        mode.getMenuInflater().inflate(R.menu.zone_list_contextual_menu,menu);
                        mode.setTitle(R.string.contextual_menu_zone_label_multi_export);
                        return data.size()>0;
                    }
                    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        initContextualMenu(menu,state);
                        updateCheckBoxes(true);
                        return true;
                    }
                    @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item){
                        return onMenuItemClick(item);
                    }
                    @Override public void onDestroyActionMode(ActionMode mode) {
                        updateCheckBoxes(false);
                        updateMenu(LandListMenuState.NormalState);
                    }
                };
                break;
            case MultiSelectState:
                callback = new ActionMode.Callback() {
                    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        mode.getMenuInflater().inflate(R.menu.zone_list_contextual_menu,menu);
                        mode.setTitle(R.string.contextual_menu_zone_label_multi_select);
                        return data.size()>0;
                    }
                    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        initContextualMenu(menu,state);
                        updateCheckBoxes(true);
                        return true;
                    }
                    @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item){
                        return onMenuItemClick(item);
                    }
                    @Override public void onDestroyActionMode(ActionMode mode) {
                        updateCheckBoxes(false);
                        updateMenu(LandListMenuState.NormalState);
                    }
                };
                break;
        }
        if(callback != null && getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                actionMenu = activity.startSupportActionMode(callback);
            }
        }else{
            deselectAllZones();
        }
    }
    private void updateUI() {
        if(data.size()>0){
            binding.rvZoneList.setVisibility(View.VISIBLE);
            binding.tvZonesListActionLabel.setVisibility(View.GONE);
        }else{
            binding.rvZoneList.setVisibility(View.GONE);
            binding.tvZonesListActionLabel.setVisibility(View.VISIBLE);
        }
    }

    //navigator relative
    private void toZoneSelected(@Nullable Activity activity, LandZone z) {
        Log.d("debug", "toZoneSelected: ");
        if(activity != null){
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ZonesLandSelectedFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,selectedLand);
                if(z != null){
                    bundle.putParcelable(AppValues.argZone,z);
                }
                if(nav != null){
                    nav.navigate(R.id.toLandPreview,bundle);
                }
            });
        }
    }

    //Override relative
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentZonesLandSelectedBinding.inflate(inflater,container,false);
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
        inflater.inflate(R.menu.zone_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        final MenuItem item1 = menu.findItem(R.id.menu_item_add);
        if(item1 != null){
            item1.getActionView().setOnClickListener(v->
                    menu.performIdentifierAction(item1.getItemId(),0)
            );
        }
        final MenuItem item2 = menu.findItem(R.id.menu_item_delete);
        if(item2 != null){
            item2.getActionView().setOnClickListener(v->
                    menu.performIdentifierAction(item2.getItemId(),0)
            );
        }
        final MenuItem item3 = menu.findItem(R.id.menu_item_add);
        if(item3 != null){
            item3.getActionView().setOnClickListener(v->
                    menu.performIdentifierAction(item3.getItemId(),0)
            );
        }
        super.onPrepareOptionsMenu(menu);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(onMenuItemClick(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        if(state != LandListMenuState.NormalState){
            updateMenu(LandListMenuState.NormalState);
            return false;
        }
        return true;
    }
}