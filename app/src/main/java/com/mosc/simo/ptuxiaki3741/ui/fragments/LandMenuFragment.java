package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.app.Activity;
import android.os.AsyncTask;
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
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuLandBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.FileType;
import com.mosc.simo.ptuxiaki3741.data.enums.ListMenuState;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.LandListAdapter;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LandMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG ="LandListFragment";

    private FragmentMenuLandBinding binding;
    private AlertDialog dialog;

    private List<Land> data = new ArrayList<>();
    private List<Land> exportLands;
    private FileType exportAction;
    private LandListAdapter adapter;
    private int dialogChecked;
    private AppViewModel vmLands;
    private ListMenuState state;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMenuLandBinding.inflate(inflater,container,false);
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
    public void onResume() {
        super.onResume();
        for (MapView m : adapter.getMapViews()) {
            if(m.getTag() != null) {
                m.onResume();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        for (MapView m : adapter.getMapViews()) {
            if(m.getTag() != null) {
                m.onStart();
            }
        }
    }

    @Override
    public void onStop() {
        for (MapView m : adapter.getMapViews()) {
            if(m.getTag() != null) {
                m.onStop();
            }
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        for (MapView m : adapter.getMapViews()) {
            if(m.getTag() != null) {
                m.onPause();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        for (MapView m : adapter.getMapViews()) {
            if(m.getTag() != null) {
                m.onDestroy();
            }
        }
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        for (MapView m : adapter.getMapViews()) {
            if(m.getTag() != null) {
                m.onLowMemory();
            }
        }
        super.onLowMemory();
    }

    @Override
    public boolean onBackPressed() {
        if(state != ListMenuState.NormalState){
            setState(ListMenuState.NormalState);
            return false;
        }
        return true;
    }

    //init
    private void initData(){
        data = new ArrayList<>();
        exportLands = new ArrayList<>();
        state = ListMenuState.NormalState;
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
        binding.ibSelectAll.setOnClickListener( v -> onSelectAllButtonClick() );
        binding.ibHistory.setOnClickListener( v -> onHistoryButtonClick() );
        binding.ibClose.setOnClickListener( v -> onCloseButtonClick() );
        binding.ibClose1.setOnClickListener( v -> onCloseButtonClick() );

        binding.fabAdd.setOnClickListener( v -> onAddButtonClick() );
        binding.fabExport.setOnClickListener( v -> onExportButtonClick() );
        binding.fabDelete.setOnClickListener( v -> onDeleteButtonClick() );

        binding.tvLandListActionLabel.setText(getResources().getString(R.string.loading_label));

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvLandList.setLayoutManager(layoutManager);
        binding.rvLandList.setHasFixedSize(true);
        adapter = new LandListAdapter(
                this::onLandClick,
                this::onLandLongClick
        );
        binding.rvLandList.setAdapter(adapter);

        updateListUi();
        updateUi();
    }
    private void initViewModel() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> vmLands.getLands().observe(
                    getViewLifecycleOwner(),
                    this::onLandsChange
            ));
        }
    }

    //observers
    private void onLandsChange(List<Land> lands) {
        data.clear();
        if(lands != null){
            for(Land land : lands){
                if(land == null || land.getData() == null) continue;
                data.add(land);
            }
        }
        binding.tvLandListActionLabel.setText(getResources().getString(R.string.empty_list));
        updateListUi();
        adapter.saveData(data);
    }
    private void onLandClick(Land land) {
        if(state != ListMenuState.NormalState) {
            toggleSelectOnPosition(data.indexOf(land));
        }else{
            toLandPreview(getActivity(),land);
        }
    }
    private void onLandLongClick(Land land) {
        if (state == ListMenuState.NormalState){
            setState(ListMenuState.MultiSelectState);
        }
        toggleSelectOnPosition(data.indexOf(land));
    }
    private void onAddButtonClick(){
        if(state == ListMenuState.NormalState){
            actionCreate();
        }
    }
    private void onExportButtonClick(){
        if(state == ListMenuState.MultiExportState || state == ListMenuState.MultiSelectState){
            actionExport();
        }else{
            setState(ListMenuState.MultiExportState);
        }
    }
    private void onDeleteButtonClick(){
        if(state == ListMenuState.MultiDeleteState || state == ListMenuState.MultiSelectState){
            actionDelete();
        }else{
            setState(ListMenuState.MultiDeleteState);
        }
    }
    private void onSelectAllButtonClick(){
        if(state != ListMenuState.NormalState){
            actionSelectAll();
        }
    }
    private void onHistoryButtonClick(){
        if(state == ListMenuState.NormalState){
            toHistory(getActivity());
        }
    }
    private void onCloseButtonClick(){
        if(state != ListMenuState.NormalState){
            setState(ListMenuState.NormalState);
        }else{
            goBack();
        }
    }

    //select methods
    private void toggleSelectOnPosition(int position){
        if(position >= 0 && position < data.size()){
            data.get(position).setSelected(!data.get(position).isSelected());
            adapter.notifyItemChanged(position);
        }
        if(returnSelectedLands().size() == 0 && state == ListMenuState.MultiSelectState){
            setState(ListMenuState.NormalState);
        }
    }
    private void toggleSelectAll(){
        if(isAllSelected()){
            deselectAllLands();
        }else{
            selectAllLands();
        }
        if(returnSelectedLands().size() == 0 && state == ListMenuState.MultiSelectState){
            setState(ListMenuState.NormalState);
        }
    }
    private boolean isAllSelected() {
        for (Land land:data){
            if(!land.isSelected())
                return false;
        }
        return true;
    }
    private void selectAllLands() {
        for (Land land:data){
            if(!land.isSelected()){
                land.setSelected(true);
                adapter.notifyItemChanged(data.indexOf(land));
            }
        }
    }
    private void deselectAllLands() {
        for (Land land:data){
            if(land.isSelected()){
                land.setSelected(false);
                adapter.notifyItemChanged(data.indexOf(land));
            }
        }
    }
    private List<Land> returnSelectedLands(){
        List<Land> result = new ArrayList<>();
        for(Land land:data){
            if(land.isSelected())
                result.add(land);
        }
        return result;
    }
    private void removeSelectedLands() {
        List<Land> deleteLands = new ArrayList<>();
        for (Land land : data) {
            if (land.isSelected()) {
                deleteLands.add(land);
            }
        }
        if(deleteLands.size()>0){
            AsyncTask.execute(()-> {
                if(!vmLands.removeLands(deleteLands)){
                    if(getActivity() != null){
                        getActivity().runOnUiThread(()-> {
                            Snackbar snackbar = Snackbar.make(
                                    binding.getRoot(),
                                    getString(R.string.some_lands_have_zones_error),
                                    Snackbar.LENGTH_SHORT
                            );
                            snackbar.setAnchorView(binding.rvLandList);
                            snackbar.show();
                        });
                    }
                }
            });
        }
    }

    //action methods
    private void actionCreate(){
        setState(ListMenuState.NormalState);
        toLandAdd(getActivity());
    }
    private void actionSelectAll(){
        toggleSelectAll();
    }
    private void actionExport(){
        if(returnSelectedLands().size() > 0){
            showExportDialog();
        }else{
            setState(ListMenuState.NormalState);
        }
    }
    private void actionDelete(){
        if(returnSelectedLands().size() > 0){
            showDeleteDialog();
        }else{
            setState(ListMenuState.NormalState);
        }
    }

    //data
    private void deleteSelectedLands(){
        removeSelectedLands();
    }
    private void exportSelectedLands(FileType action){
        exportAction = action;
        exportLands.clear();
        exportLands.addAll(returnSelectedLands());
        deselectAllLands();
        exportAction();
    }
    private void exportAction(){
        if(exportLands.size()>0 && exportAction != FileType.NONE){
            writeOnFile(exportLands, exportAction);
        }
        exportAction = FileType.NONE;
        exportLands.clear();
    }
    private void writeOnFile(List<Land> lands, FileType action) {
        if(lands.size()>0){
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
            );
            String fileName = (System.currentTimeMillis()/1000)+"_"+lands.size();
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
                            output = FileUtil.landsToKmlString(lands,fileName);
                            fileName = fileName+".kml";
                            break;
                        case GEOJSON:
                            output = FileUtil.landsToGeoJsonString(lands);
                            fileName = fileName+".json";
                            break;
                        case GML:
                            output = FileUtil.landsToGmlString(lands);
                            fileName = fileName+".gml";
                            break;
                        case WKT:
                            output = FileUtil.landsToWKTString(lands);
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

    //ui
    private void setCheckableRecycleView(boolean showCheckBox){
        binding.ibHistory.setEnabled(!showCheckBox);
        binding.ibSelectAll.setEnabled(showCheckBox);
        if(showCheckBox){
            binding.ibHistory.setVisibility(View.GONE);
            binding.ibSelectAll.setVisibility(View.VISIBLE);
        }else{
            binding.ibHistory.setVisibility(View.VISIBLE);
            binding.ibSelectAll.setVisibility(View.GONE);
        }
        adapter.saveData(data,showCheckBox);
    }
    private void setState(ListMenuState state) {
        if(this.state == state) return;

        this.state = state;
        if(state == ListMenuState.NormalState) {
            deselectAllLands();
            binding.getRoot().transitionToStart();
            binding.ibClose.setVisibility(View.GONE);
            binding.ibClose1.setVisibility(View.VISIBLE);
        }else{
            binding.ibClose.setVisibility(View.VISIBLE);
            binding.ibClose1.setVisibility(View.GONE);
        }
        updateUi();
    }
    private void updateUi() {
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
    private void updateListUi() {
        if(data.size()>0){
            binding.tvLandListActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvLandListActionLabel.setVisibility(View.VISIBLE);
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
                    .setOnDismissListener(dialog -> setState(ListMenuState.NormalState))
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> {
                        switch (dialogChecked) {
                            case 0:
                                exportSelectedLands(FileType.KML);
                                break;
                            case 1:
                                exportSelectedLands(FileType.GEOJSON);
                                break;
                            case 2:
                                exportSelectedLands(FileType.GML);
                                break;
                            case 3:
                                exportSelectedLands(FileType.WKT);
                                break;
                            default:
                                exportSelectedLands(FileType.NONE);
                                break;
                        }
                    })
                    .create();
            dialog.show();
        }
    }
    private void showDeleteDialog(){
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
                    .setPositiveButton(getString(R.string.accept), (d, w) -> deleteSelectedLands())
                    .create();
            dialog.show();
        }
    }

    //navigate
    public void goBack(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(()->getActivity().onBackPressed());
    }
    public void toLandPreview(@Nullable Activity activity, Land land) {
        if(land == null || land.getData() == null) return;

        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuLandsFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,new Land(new LandData(land.getData())));
                if(nav != null)
                    nav.navigate(R.id.toMapLandPreview,bundle);
            });
    }
    public void toLandAdd(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuLandsFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,null);
                if(nav != null)
                    nav.navigate(R.id.toProfileLand,bundle);
            });
    }
    public void toHistory(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuLandsFragment);
                if(nav != null)
                    nav.navigate(R.id.toLandsHistory);
            });
    }
}