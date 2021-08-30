package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuLandBinding;
import com.mosc.simo.ptuxiaki3741.enums.FileType;
import com.mosc.simo.ptuxiaki3741.enums.LandListActionState;
import com.mosc.simo.ptuxiaki3741.enums.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.adapters.LandListAdapter;
import com.mosc.simo.ptuxiaki3741.enums.ViewModelStatus;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LandMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG ="LandListFragment";

    private final List<Land> data = new ArrayList<>();
    private LandListAdapter adapter;
    private int dialogChecked;
    private LandViewModel vmLands;
    private User currUser;
    private LandListMenuState state = LandListMenuState.NormalState;

    private FragmentMenuLandBinding binding;
    private AlertDialog dialog;
    private Menu menu;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuLandBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initFragment();
        initViewModel();
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        initMenu(menu);
        updateMenu(state);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return menuItemClick(item);
    }
    @Override public boolean onBackPressed() {
        if(state != LandListMenuState.NormalState){
            setState(LandListMenuState.NormalState);
            return false;
        }else{
            return true;
        }
    }

    //init
    private void initActivity() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.land_bar_label));
                actionBar.show();
            }
            activity.setOnBackPressed(this);
        }
    }
    private void initFragment() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvLandList.setLayoutManager(layoutManager);
        binding.rvLandList.setHasFixedSize(true);
        adapter = new LandListAdapter(
                data,
                this::onLandClick,
                this::onLandLongClick
        );
        binding.rvLandList.setAdapter(adapter);
        onVMStatusChange(ViewModelStatus.LOADING);
    }
    private void initViewModel() {
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            onUserChange(vmUsers.getCurrUser().getValue());
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onUserChange);
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            onDataChange(vmLands.getLands().getValue());
            onVMStatusChange(vmLands.getStatus().getValue());
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onDataChange);
            vmLands.getStatus().observe(getViewLifecycleOwner(),this::onVMStatusChange);
        }
    }
    public void initMenu(Menu menu) {
        this.menu = menu;
        final MenuItem add = this.menu.findItem(R.id.menu_item_add);
        final MenuItem delete = this.menu.findItem(R.id.menu_item_delete);
        final MenuItem export = this.menu.findItem(R.id.menu_item_export);
        final MenuItem deleteAction = this.menu.findItem(R.id.menu_delete_action);
        final MenuItem exportAction = this.menu.findItem(R.id.menu_export_action);
        final MenuItem selectAll = this.menu.findItem(R.id.menu_select_all_action);

        add.getActionView().setOnClickListener(v-> this.menu.performIdentifierAction(add.getItemId(),0));
        delete.getActionView().setOnClickListener(v-> this.menu.performIdentifierAction(delete.getItemId(),0));
        export.getActionView().setOnClickListener(v-> this.menu.performIdentifierAction(export.getItemId(),0));
        deleteAction.getActionView().setOnClickListener(v-> this.menu.performIdentifierAction(deleteAction.getItemId(),0));
        exportAction.getActionView().setOnClickListener(v-> this.menu.performIdentifierAction(exportAction.getItemId(),0));
        selectAll.getActionView().setOnClickListener(v-> this.menu.performIdentifierAction(selectAll.getItemId(),0));
    }

    //menu
    private boolean menuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case (R.id.menu_item_delete):
                setState(LandListMenuState.MultiDeleteState);
                return true;
            case (R.id.menu_item_export):
                setState(LandListMenuState.MultiExportState);
                return true;
            case (R.id.menu_item_add):
                doAction(LandListActionState.ToCreate);
                return true;
            case (R.id.menu_delete_action):
                doAction(LandListActionState.DeleteAction);
                return true;
            case (R.id.menu_export_action):
                doAction(LandListActionState.ExportAction);
                return true;
            case (R.id.menu_select_all_action):
                doAction(LandListActionState.SelectAllAction);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void updateMenu(LandListMenuState state) {
        if(menu != null){
            MenuItem add = menu.findItem(R.id.menu_item_add);
            MenuItem delete = menu.findItem(R.id.menu_item_delete);
            MenuItem export = menu.findItem(R.id.menu_item_export);
            MenuItem deleteAction = menu.findItem(R.id.menu_delete_action);
            MenuItem exportAction = menu.findItem(R.id.menu_export_action);
            MenuItem selectAll = menu.findItem(R.id.menu_select_all_action);

            switch (state){
                case MultiSelectState:
                    add.setVisible(false);
                    delete.setVisible(false);
                    export.setVisible(false);
                    deleteAction.setVisible(true);
                    exportAction.setVisible(true);
                    selectAll.setVisible(true);
                    break;
                case MultiDeleteState:
                    add.setVisible(false);
                    delete.setVisible(false);
                    export.setVisible(false);
                    deleteAction.setVisible(true);
                    exportAction.setVisible(false);
                    selectAll.setVisible(true);
                    break;
                case MultiExportState:
                    add.setVisible(false);
                    delete.setVisible(false);
                    export.setVisible(false);
                    deleteAction.setVisible(false);
                    exportAction.setVisible(true);
                    selectAll.setVisible(true);
                    break;
                case NormalState:
                default:
                    add.setVisible(true);
                    delete.setVisible(true);
                    export.setVisible(true);
                    deleteAction.setVisible(false);
                    exportAction.setVisible(false);
                    selectAll.setVisible(false);
                    break;
            }
        }
    }

    //observers
    private void onUserChange(User user) {
        currUser = user;
        if(user == null){
            finish();
        }
    }
    private void onDataChange(List<Land> data) {
        if(this.data.size()>0){
            int size = this.data.size();
            this.data.clear();
            adapter.notifyItemRangeRemoved(0,size);
        }
        this.data.addAll(data);
        adapter.notifyItemRangeInserted(0,data.size());
    }
    private void onVMStatusChange(ViewModelStatus status){
        if(status == ViewModelStatus.LOADING){
            binding.tvLandListActionLabel.setText(getResources().getString(R.string.loading_list));
            binding.tvLandListActionLabel.setVisibility(View.VISIBLE);
            binding.rvLandList.setVisibility(View.GONE);
        }else{
            binding.tvLandListActionLabel.setText(getResources().getString(R.string.empty_list));
            updateUi();
        }
    }
    private void onLandClick(Land land) {
        if(state != LandListMenuState.NormalState) {
            toggleSelectOnPosition(data.indexOf(land));
        }else{
            toLandEdit(getActivity(),land);
        }
    }
    private void onLandLongClick(Land land) {
        if (state == LandListMenuState.NormalState){
            setState(LandListMenuState.MultiSelectState);
        }
        toggleSelectOnPosition(data.indexOf(land));
    }

    //select methods
    private void toggleSelectOnPosition(int position){
        if(position >= 0 && position < data.size()){
            data.get(position).setSelected(!data.get(position).isSelected());
            adapter.notifyItemChanged(position);
        }
        if(returnSelectedLands().size() == 0 && state == LandListMenuState.MultiSelectState){
            setState(LandListMenuState.NormalState);
        }
    }
    private void toggleSelectAll(){
        if(isAllSelected()){
            deselectAllLands();
        }else{
            selectAllLands();
        }
        if(returnSelectedLands().size() == 0 && state == LandListMenuState.MultiSelectState){
            setState(LandListMenuState.NormalState);
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
        if(deleteLands.size()>0 && currUser != null){
            AsyncTask.execute(()->vmLands.removeLands(deleteLands,currUser));
        }
    }

    //data
    private void deleteSelectedLands(){
        onVMStatusChange(ViewModelStatus.LOADING);
        removeSelectedLands();
    }
    private void exportSelectedLands(FileType action){
        List<Land> lands = returnSelectedLands();
        if(lands.size() > 0){
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
            );
            writeOnFile(lands, path, action);
            deselectAllLands();
        }
    }
    private void writeOnFile(List<Land> lands, File path, FileType action) {
        if(lands.size()>0){
            String fileName = (System.currentTimeMillis()/1000)+"_"+lands.size();
            try{
                boolean isPathCreated = false, pathExist = path.exists();
                if (!pathExist) {
                    isPathCreated = path.mkdirs();
                    pathExist = path.exists();
                }
                if( pathExist || isPathCreated ){
                    String output="";
                    boolean doAction = true;
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
                            //TODO: GML
                        default:
                            doAction = false;
                            break;
                    }
                    if(doAction){
                        File mFile = new File(path, fileName);
                        FileWriter writer = new FileWriter(mFile);
                        writer.append(output);
                        writer.flush();
                        writer.close();
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
    private void setState(LandListMenuState state) {
        this.state = state;
        updateMenu(state);
        if(state == LandListMenuState.NormalState)
            deselectAllLands();
    }
    private void doAction(LandListActionState actionState) {
        switch (actionState){
            case ToCreate:
                setState(LandListMenuState.NormalState);
                toLandAdd(getActivity());
                break;
            case SelectAllAction:
                toggleSelectAll();
                break;
            case DeleteAction:
                if(returnSelectedLands().size() > 0){
                    showDeleteDialog();
                }else{
                    setState(LandListMenuState.NormalState);
                }
                break;
            case ExportAction:
                if(returnSelectedLands().size() > 0){
                    showExportDialog();
                }else{
                    setState(LandListMenuState.NormalState);
                }
                break;
            default:
                setState(LandListMenuState.NormalState);
                break;
        }
    }
    private void updateUi() {
        if(data.size()>0){
            binding.rvLandList.setVisibility(View.VISIBLE);
            binding.tvLandListActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvLandListActionLabel.setVisibility(View.VISIBLE);
            binding.rvLandList.setVisibility(View.GONE);
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
            String[] dataTypes = {"KML","GeoJson","GML"};
            dialog = new MaterialAlertDialogBuilder(getContext())
                    .setTitle(getString(R.string.file_type_select_title))
                    .setSingleChoiceItems(dataTypes, dialogChecked, (d, w) -> dialogChecked = w)
                    .setOnDismissListener(dialog -> setState(LandListMenuState.NormalState))
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
            dialog = new MaterialAlertDialogBuilder(getContext())
                    .setTitle(getString(R.string.delete_lands_title))
                    .setMessage(getString(R.string.delete_lands_text))
                    .setOnDismissListener(dialog -> setState(LandListMenuState.NormalState))
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> deleteSelectedLands())
                    .create();
            dialog.show();
        }
    }

    //navigate
    public void toLandEdit(@Nullable Activity activity, Land land) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandCollectionFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(LandMapFragment.argLand,land);
                if(nav != null)
                    nav.navigate(R.id.landCollectionToLandMap,bundle);
            });
    }
    public void toLandAdd(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LandCollectionFragment);
                Land land = new Land();
                Bundle bundle = new Bundle();
                bundle.putParcelable(LandInfoFragment.argLand,land);
                if(nav != null)
                    nav.navigate(R.id.landCollectionToLandInfo,bundle);
            });
    }
    private void finish() {
        if(getActivity() != null)
            getActivity().onBackPressed();
    }
}