package com.mosc.simo.ptuxiaki3741.fragments.zones;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.mosc.simo.ptuxiaki3741.enums.ListMenuState;
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
    private static final String TAG = "ZonesLandSelectedFragment";
    private FragmentZonesLandSelectedBinding binding;

    private ListMenuState state;
    private LandZonesListAdapter adapter;
    private Land selectedLand;
    private List<LandZone> data;
    private AlertDialog dialog;
    private int dialogChecked;

    private AppViewModel vmLands;

    //init relative
    private void initData() {
        data = new ArrayList<>();
        selectedLand = null;
        state = ListMenuState.NormalState;
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
            }
        }
    }
    private void initFragment() {
        if(selectedLand != null){
            binding.tvTitle.setText(selectedLand.toString());
        }
        binding.ibClose.setOnClickListener(view -> onCloseClick());
        binding.ibSelectAll.setOnClickListener(view -> onSelectAllClick());
        binding.fabAdd.setOnClickListener(view -> onAddClick());
        binding.fabDelete.setOnClickListener(view -> onDeleteClick());
        binding.fabExport.setOnClickListener(view -> onExportClick());

        binding.tvZonesListActionLabel.setText(getResources().getString(R.string.loading_label));

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
        binding.rvZoneList.setAdapter(adapter);

        updateUi();
    }
    private void initViewModel() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> vmLands.getLandZones().observe(
                    getViewLifecycleOwner(),
                    this::onDataUpdate
            ));
        }
    }

    //listener relative
    @SuppressLint("NotifyDataSetChanged")
    private void onDataUpdate(Map<Long,List<LandZone>> zones) {
        data.clear();
        if(selectedLand != null && zones != null){
            List<LandZone> temp = zones.getOrDefault(selectedLand.getData().getId(),null);
            if(temp != null)
                data.addAll(temp);
        }
        binding.tvZonesListActionLabel.setText(getResources().getString(R.string.empty_list));
        updateListUI();
        adapter.notifyDataSetChanged();
    }
    private void onZoneClick(LandZone zone) {
        if(state != ListMenuState.NormalState){
            toggleZone(zone);
        }else{
            toZoneSelected(getActivity(),zone);
        }
    }
    private void onZoneLongClick(LandZone zone) {
        if(state == ListMenuState.NormalState){
            setState(ListMenuState.MultiSelectState);
        }
        toggleZone(zone);
    }
    private void onCloseClick() {
        if(state != ListMenuState.NormalState){
            setState(ListMenuState.NormalState);
        }
    }
    private void onSelectAllClick() {
        if(state != ListMenuState.NormalState){
            toggleAllZone();
        }
    }
    private void onAddClick() {
        if(state == ListMenuState.NormalState){
            toZoneSelected(getActivity(),null);
        }
    }
    private void onDeleteClick() {
        if(state == ListMenuState.MultiDeleteState || state == ListMenuState.MultiSelectState){
            showDeleteDialog();
        }else{
            setState(ListMenuState.MultiDeleteState);
        }
    }
    private void onExportClick() {
        if(state == ListMenuState.MultiExportState || state == ListMenuState.MultiSelectState){
            showExportDialog();
        }else{
            setState(ListMenuState.MultiExportState);
        }
    }

    //zone selector relative
    private void toggleZone(LandZone zone) {
        zone.setSelected(!zone.isSelected());
        adapter.notifyItemChanged(data.indexOf(zone));
        if(state == ListMenuState.MultiSelectState && areNonZoneSelected()){
            setState(ListMenuState.NormalState);
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void toggleAllZone() {
        if(areAllZoneSelected()){
            if(state == ListMenuState.MultiSelectState){
                setState(ListMenuState.NormalState);
            }else{
                deselectAllZones();
            }
        }else{
            selectAllZones();
        }
        adapter.notifyDataSetChanged();
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
    private void showDeleteDialog(){
        if(getSelectedZones().size() == 0 ){
            setState(ListMenuState.NormalState);
            return;
        }
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.ErrorMaterialAlertDialog)
                    .setTitle(getString(R.string.delete_lands_title))
                    .setMessage(getString(R.string.delete_lands_text))
                    .setOnDismissListener(dialog -> setState(ListMenuState.NormalState))
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> deleteAction())
                    .create();
            dialog.show();
        }
    }
    private void deleteAction(){
        List<LandZone> selectedZones = getSelectedZones();
        if(selectedZones.size()>0){
            if(vmLands != null){
                vmLands.removeZones(selectedZones);
                Snackbar.make(binding.getRoot(), R.string.zone_delete, Snackbar.LENGTH_SHORT).show();
            }
        }
        setState(ListMenuState.NormalState);
    }
    private void showExportDialog(){
        if(getSelectedZones().size() == 0 ){
            setState(ListMenuState.NormalState);
            return;
        }
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
                    .setOnDismissListener(dialog -> setState(ListMenuState.NormalState))
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
    private void setCheckableRecycleView(boolean show) {
        binding.ibSelectAll.setEnabled(show);
        if(show){
            binding.ibSelectAll.setVisibility(View.VISIBLE);
        }else{
            binding.ibSelectAll.setVisibility(View.GONE);
        }
        adapter.setShowCheckMark(show);
        adapter.notifyDataSetChanged();
    }
    public void setState(ListMenuState state) {
        if(this.state == state) return;

        this.state = state;
        if(state == ListMenuState.NormalState){
            deselectAllZones();
            binding.getRoot().transitionToStart();
            binding.ibClose.setVisibility(View.GONE);
        }else{
            binding.ibClose.setVisibility(View.VISIBLE);
        }
        updateUi();
    }
    private void updateUi(){
        switch (state){
            case MultiExportState:
                setCheckableRecycleView(true);

                binding.fabAdd.setEnabled(false);
                binding.fabDelete.setEnabled(false);
                binding.fabExport.setEnabled(true);
                binding.fabToggleMenu.setEnabled(false);
                break;
            case MultiDeleteState:
                setCheckableRecycleView(true);

                binding.fabAdd.setEnabled(false);
                binding.fabDelete.setEnabled(true);
                binding.fabExport.setEnabled(false);
                binding.fabToggleMenu.setEnabled(false);
                break;
            case MultiSelectState:
                setCheckableRecycleView(true);

                binding.fabAdd.setEnabled(false);
                binding.fabDelete.setEnabled(true);
                binding.fabExport.setEnabled(true);
                binding.fabToggleMenu.setEnabled(true);
                break;
            default:
                setCheckableRecycleView(false);

                binding.fabAdd.setEnabled(true);
                binding.fabDelete.setEnabled(true);
                binding.fabExport.setEnabled(true);
                binding.fabToggleMenu.setEnabled(true);
                break;
        }
    }
    private void updateListUI() {
        if(data.size()>0){
            binding.tvZonesListActionLabel.setVisibility(View.GONE);
        }else{
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
        binding = FragmentZonesLandSelectedBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModel();
    }

    @Override
    public boolean onBackPressed() {
        if(state != ListMenuState.NormalState){
            setState(ListMenuState.NormalState);
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onResume();
            }
        }
    }

    @Override
    public void onStart() {
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onStart();
            }
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onStop();
            }
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onPause();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onDestroy();
            }
        }
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onLowMemory();
            }
        }
        super.onLowMemory();
    }
}