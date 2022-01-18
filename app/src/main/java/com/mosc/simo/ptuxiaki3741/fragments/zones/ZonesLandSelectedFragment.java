package com.mosc.simo.ptuxiaki3741.fragments.zones;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.LandZonesListAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentZonesLandSelectedBinding;
import com.mosc.simo.ptuxiaki3741.enums.FileType;
import com.mosc.simo.ptuxiaki3741.enums.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZonesLandSelectedFragment extends Fragment implements FragmentBackPress {
    //fixme: tags search
    private static final String TAG = "ZonesLandSelectedFragment";
    private FragmentZonesLandSelectedBinding binding;
    private LandZonesListAdapter adapter;
    private Land selectedLand;
    private List<LandZone> data;

    private AlertDialog dialog;
    private int dialogChecked;

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
        binding.rvZoneList.setLayoutManager(layoutManager);
        binding.rvZoneList.setHasFixedSize(true);
        adapter = new LandZonesListAdapter(
                selectedLand,
                data,
                this::onZoneClick,
                this::onZoneLongClick
        );

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
                if(getSelectedZones().size() > 0){
                    showExportDialog();
                }else{
                    updateMenu(LandListMenuState.NormalState);
                }
                return true;
            case (R.id.menu_select_all_action):
                toggleAllZone();
                return true;
            default:
                return false;
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void onDataUpdate(Map<Long,List<LandZone>> zones) {
        data.clear();
        if(selectedLand != null && zones != null){
            List<LandZone> temp = zones.getOrDefault(selectedLand.getData().getId(),null);
            if(temp != null)
                data.addAll(temp);
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
                vmLands.removeZones(selectedZones);
                Snackbar.make(binding.getRoot(), R.string.zone_delete, Snackbar.LENGTH_SHORT).show();
            }
            updateMenu(LandListMenuState.NormalState);
        }
    }
    private void showExportDialog(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            dialogChecked = 0;
            String[] dataTypes = {"KML","GeoJson","GML","Well Known Text"};
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog)
                    .setTitle(getString(R.string.file_type_select_title))
                    .setSingleChoiceItems(dataTypes, dialogChecked, (d, w) -> dialogChecked = w)
                    .setOnDismissListener(dialog -> updateMenu(LandListMenuState.NormalState))
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> {
                        switch (dialogChecked) {
                            case 0:
                                exportSelectedZones(FileType.KML);
                                break;
                            case 1:
                                exportSelectedZones(FileType.GEOJSON);
                                break;
                            case 2:
                                exportSelectedZones(FileType.GML);
                                break;
                            case 3:
                                exportSelectedZones(FileType.WKT);
                                break;
                            default:
                                exportSelectedZones(FileType.NONE);
                                break;
                        }
                    })
                    .create();
            dialog.show();
        }
    }
    private void exportSelectedZones(FileType action){
        List<LandZone> exportZones = new ArrayList<>(getSelectedZones());
        deselectAllZones();
        exportAction(exportZones,action);
    }
    private void exportAction(List<LandZone> exportZones, FileType exportAction){
        if(exportZones.size()>0 && exportAction != FileType.NONE){
            writeOnFile(exportZones, exportAction);
            Snackbar.make(binding.getRoot(), R.string.zone_export, Snackbar.LENGTH_SHORT).show();
        }
    }
    private void writeOnFile(List<LandZone> zones, FileType action) {
        if(zones.size()>0){
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
            );
            String landTitle = selectedLand.getData().getTitle();
            landTitle = landTitle.replaceAll("\\s{2,}", " ").trim();
            landTitle = landTitle.replaceAll(" ","_");
            String fileName = landTitle+"_"+(System.currentTimeMillis()/1000)+"_"+zones.size();
            try{
                boolean isPathCreated = false, pathExist = path.exists();
                if (!pathExist) {
                    isPathCreated = path.mkdirs();
                    pathExist = path.exists();
                }
                if( pathExist || isPathCreated ){
                    String output="";
                    switch(action){
                        case KML:
                            output = FileUtil.zonesToKmlString(zones,fileName);
                            fileName = fileName+".kml";
                            break;
                        case GEOJSON:
                            output = FileUtil.zonesToGeoJsonString(zones);
                            fileName = fileName+".json";
                            break;
                        case GML:
                            output = FileUtil.zonesToGmlString(zones);
                            fileName = fileName+".gml";
                            break;
                        case WKT:
                            output = FileUtil.zonesToWKTString(zones);
                            fileName = fileName+".txt";
                            break;
                    }
                    if(FileUtil.createFile(output, fileName, path)){
                        Toast.makeText(getContext(), getString(R.string.file_created), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), getString(R.string.file_not_created), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "writeOnFile: ", e);
            }
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
        binding.rvZoneList.setAdapter(adapter);
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