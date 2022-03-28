package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
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
    private boolean doDialogUpdate;

    private final List<Land> data = new ArrayList<>();
    private final List<Land> displayData = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private String selectedTag;
    private List<Land> exportLands;
    private FileType exportAction;
    private LandListAdapter adapter;
    private int dialogChecked;
    private AppViewModel vmLands;
    private ListMenuState state;

    private final ActivityResultLauncher<String> permissionWriteChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onPermissionWriteResult
    );

    private void onPermissionWriteResult(boolean permission) {
        if(permission){
            writeOnFile();
        }else{
            exportAction = FileType.NONE;
            exportLands.clear();
        }
    }

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
        Handler handler = new Handler();
        handler.postDelayed(this::initViewModel,240);
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
        if(binding != null && binding.getRoot().isDrawerOpen(GravityCompat.END)) {
            toggleMenu(false);
            return false;
        }
        if(state != ListMenuState.NormalState){
            setState(ListMenuState.NormalState, true);
            return false;
        }
        return true;
    }

    //init
    private void initData(){
        selectedTag = null;
        exportLands = new ArrayList<>();
        state = ListMenuState.NormalState;
        adapter = new LandListAdapter(
                this::onLandClick,
                this::onLandLongClick
        );
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
        binding.ibMenuButton.setOnClickListener(v -> toggleMenu(true));
        binding.ibSelectAll.setOnClickListener( v -> onSelectAllButtonClick() );
        binding.ibHistory.setOnClickListener( v -> onHistoryButtonClick() );
        binding.ibClose.setOnClickListener( v -> onCloseButtonClick() );
        binding.ibClose1.setOnClickListener( v -> onCloseButtonClick() );

        binding.fabAdd.setOnClickListener( v -> onAddButtonClick() );
        binding.fabExport.setOnClickListener( v -> onExportButtonClick() );
        binding.fabDelete.setOnClickListener( v -> onDeleteButtonClick() );

        binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.navLandFilterMenu.setNavigationItemSelectedListener(this::onFilterClick);

        binding.tvLandListActionLabel.setText(getResources().getString(R.string.loading_label));

        binding.rvLandList.setHasFixedSize(true);

        binding.rvLandList.setAdapter(adapter);

        setupSideMenu();
        updateUi();

        final int maxColumnNumber = getResources().getInteger(R.integer.screenMaxColumnNumber);
        binding.rvLandList.post(()->{
            int maxWidth = getResources().getDimensionPixelSize(R.dimen.max_grid_width);
            int spanCount = 1;
            if(maxWidth != 0) {
                spanCount = Math.floorDiv(binding.rvLandList.getWidth(), maxWidth);
                if (spanCount == 0) spanCount = 1;
            }
            if(spanCount > maxColumnNumber){
                spanCount = maxColumnNumber;
            }
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL);
            binding.rvLandList.setLayoutManager(staggeredGridLayoutManager);
        });
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
        onLandsTagsChange(LandUtil.getLandsTags(data));
        updateList();
    }
    private void onLandsTagsChange(List<String> landTags) {
        String emptyTag = getString(R.string.filter_lands_empty_tag);
        tags.clear();
        tags.add(getString(R.string.filter_lands_all_tag));
        if(landTags != null){
            for(String tag : landTags){
                String tempTag = tag;
                if(tempTag == null) tempTag = emptyTag;
                if(tags.contains(tempTag)) continue;
                if(tempTag.equals(emptyTag)) tags.add(1,tempTag);
                else tags.add(tempTag);
            }
        }
        setupSideMenu();
    }
    private void onLandClick(Land land) {
        if(state != ListMenuState.NormalState) {
            toggleSelectOnPosition(displayData.indexOf(land));
        }else{
            toLandPreview(getActivity(),land);
        }
    }
    private void onLandLongClick(Land land) {
        if (state == ListMenuState.NormalState){
            setState(ListMenuState.MultiSelectState, true);
        }
        toggleSelectOnPosition(displayData.indexOf(land));
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
            setState(ListMenuState.MultiExportState, true);
        }
    }
    private void onDeleteButtonClick(){
        if(state == ListMenuState.MultiDeleteState || state == ListMenuState.MultiSelectState){
            actionDelete();
        }else{
            setState(ListMenuState.MultiDeleteState, true);
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
            setState(ListMenuState.NormalState, true);
        }else{
            goBack();
        }
    }
    private boolean onFilterClick(MenuItem item) {
        int i = item.getItemId();
        if(i < 0 || i >= tags.size()) return false;
        String tag = tags.get(i);
        if(tag.equals(getString(R.string.filter_lands_all_tag))){
            selectedTag = null;
        }else{
            selectedTag = tag;
        }
        toggleMenu(false);
        new Handler().post(this::updateList);
        return true;
    }

    //select methods
    private void toggleSelectOnPosition(int position){
        if(position >= 0 && position < displayData.size()){
            displayData.get(position).setSelected(!displayData.get(position).isSelected());
            adapter.notifyItemChanged(position);
        }
        if(returnSelectedLands().size() == 0 && state == ListMenuState.MultiSelectState){
            setState(ListMenuState.NormalState, true);
        }
    }
    private void toggleSelectAll(){
        if(isAllSelected()){
            deselectAllLands();
        }else{
            selectAllLands();
        }
        if(returnSelectedLands().size() == 0 && state == ListMenuState.MultiSelectState){
            setState(ListMenuState.NormalState, true);
        }
    }
    private boolean isAllSelected() {
        for (Land land:displayData){
            if(!land.isSelected())
                return false;
        }
        return true;
    }
    private void selectAllLands() {
        for (Land land:displayData){
            if(!land.isSelected()){
                land.setSelected(true);
                adapter.notifyItemChanged(displayData.indexOf(land));
            }
        }
    }
    private void deselectAllLands() {
        for (Land land:displayData){
            if(land.isSelected()){
                land.setSelected(false);
                adapter.notifyItemChanged(displayData.indexOf(land));
            }
        }
    }
    private List<Land> returnSelectedLands(){
        List<Land> result = new ArrayList<>();
        for(Land land:displayData){
            if(land.isSelected())
                result.add(land);
        }
        return result;
    }
    private void removeSelectedLands() {
        if(getActivity() == null) {
            setState(ListMenuState.NormalState, true);
            return;
        }
        Activity activity = getActivity();
        List<Land> deleteLands = returnSelectedLands();
        setState(ListMenuState.NormalState, false);
        if(deleteLands.size()>0){
            AsyncTask.execute(()-> {
                String display;
                if(vmLands.removeLands(deleteLands)){
                    display = getString(R.string.land_delete_successful);
                }else{
                    display = getString(R.string.some_lands_have_zones_error);
                }
                activity.runOnUiThread(()-> showSnackBar(display));
            });
        }else{
            showSnackBar(getString(R.string.some_lands_have_zones_error));
        }
    }

    //action methods
    private void actionCreate(){
        setState(ListMenuState.NormalState, true);
        toLandAdd(getActivity());
    }
    private void actionSelectAll(){
        toggleSelectAll();
    }
    private void actionExport(){
        if(returnSelectedLands().size() > 0){
            showExportDialog();
        }else{
            setState(ListMenuState.NormalState, true);
        }
    }
    private void actionDelete(){
        if(returnSelectedLands().size() > 0){
            showDeleteDialog();
        }else{
            setState(ListMenuState.NormalState, true);
        }
    }

    //data
    private void exportSelectedLands(FileType action){
        exportAction = action;
        exportLands.clear();
        exportLands.addAll(returnSelectedLands());
        deselectAllLands();
        exportAction();
    }
    private void exportAction(){
        setState(ListMenuState.NormalState, false);
        if(exportLands.size()>0 && exportAction != FileType.NONE){
            if(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                permissionWriteChecker.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }else{
                onPermissionWriteResult(true);
            }
        }else{
            exportAction = FileType.NONE;
            exportLands.clear();
        }
    }
    private void writeOnFile() {
        if(exportLands.size()>0){
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
            );
            String fileName = (System.currentTimeMillis()/1000)+"_"+exportLands.size();
            String display;
            try{
                boolean isPathCreated = false, pathExist = path.exists();
                if (!pathExist) {
                    isPathCreated = path.mkdirs();
                    pathExist = path.exists();
                }
                if( pathExist || isPathCreated ){
                    String output="";
                    switch(exportAction){
                        case KML:
                            output = FileUtil.landsToKmlString(exportLands,fileName);
                            fileName = fileName+".kml";
                            break;
                        case GEOJSON:
                            output = FileUtil.landsToGeoJsonString(exportLands);
                            fileName = fileName+".json";
                            break;
                        case GML:
                            output = FileUtil.landsToGmlString(exportLands);
                            fileName = fileName+".gml";
                            break;
                        case WKT:
                            output = FileUtil.landsToWKTString(exportLands);
                            fileName = fileName+".txt";
                            break;
                    }
                    if(FileUtil.createFile(output, fileName, path)){
                        display = getString(R.string.land_export);
                    }else{
                        display = getString(R.string.file_not_created);
                    }
                }else{
                    display = getString(R.string.file_path_not_created);
                }
            } catch (IOException e) {
                Log.e(TAG, "writeOnFile: ", e);
                display = getString(R.string.file_something_did_not_run);
            }
            showSnackBar(display);
        }
        exportAction = FileType.NONE;
        exportLands.clear();
    }

    //ui
    private void setCheckableRecycleView(boolean showCheckBox){
        binding.ibHistory.setEnabled(!showCheckBox);
        binding.ibMenuButton.setEnabled(!showCheckBox);
        binding.ibSelectAll.setEnabled(showCheckBox);
        if(showCheckBox){
            binding.ibHistory.setVisibility(View.GONE);
            binding.ibMenuButton.setVisibility(View.GONE);
            binding.ibSelectAll.setVisibility(View.VISIBLE);
        }else{
            binding.ibHistory.setVisibility(View.VISIBLE);
            binding.ibMenuButton.setVisibility(View.VISIBLE);
            binding.ibSelectAll.setVisibility(View.GONE);
        }
        updateList(showCheckBox);
    }
    private void setState(ListMenuState state, boolean doUpdate) {
        if(this.state == state) return;
        toggleMenu(false);
        this.state = state;
        if(state == ListMenuState.NormalState) {
            deselectAllLands();
            if(doUpdate) binding.mlRoot.transitionToStart();
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
    private void updateList() {
        displayData.clear();
        if(selectedTag == null){
            displayData.addAll(data);
        }else{
            boolean isEmpty = selectedTag.equals(getString(R.string.filter_lands_empty_tag));
            for(Land land : data){
                if(land == null || land.getData() == null) continue;
                List<String> landTags = LandUtil.getLandTags(land.getData());
                if(isEmpty){
                    if(landTags.contains(null)) displayData.add(land);
                }else{
                    if(landTags.contains(selectedTag)) displayData.add(land);
                }
            }
        }
        if(displayData.size()>0){
            binding.tvLandListActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvLandListActionLabel.setVisibility(View.VISIBLE);
        }
        adapter.saveData(displayData);
        binding.rvLandList.smoothScrollBy(0, 1);
    }
    private void updateList(boolean showCheckBox) {
        displayData.clear();
        if(selectedTag == null){
            displayData.addAll(data);
        }else{
            boolean isEmpty = selectedTag.equals(getString(R.string.filter_lands_empty_tag));
            for(Land land : data){
                if(land == null || land.getData() == null) continue;
                List<String> landTags = LandUtil.getLandTags(land.getData());
                if(isEmpty){
                    if(landTags.contains(null)) displayData.add(land);
                }else{
                    if(landTags.contains(selectedTag)) displayData.add(land);
                }
            }
        }
        if(displayData.size()>0){
            binding.tvLandListActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvLandListActionLabel.setVisibility(View.VISIBLE);
        }
        adapter.saveData(displayData,showCheckBox);
        binding.rvLandList.smoothScrollBy(0, 1);
    }
    private void setupSideMenu(){
        Menu menu = binding.navLandFilterMenu.getMenu();
        menu.clear();
        SubMenu subMenu = menu.addSubMenu(Menu.NONE,-1,Menu.NONE,getString(R.string.land_filter_title));
        for(int i = 0; i < tags.size(); i++){
            subMenu.add(Menu.NONE,i,Menu.NONE,tags.get(i));
        }
    }
    private void showExportDialog(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            doDialogUpdate = true;
            dialogChecked = 0;
            String[] dataTypes = {"KML","GeoJson","GML","Well Known Text"};
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog)
                    .setIcon(R.drawable.ic_menu_export)
                    .setTitle(getString(R.string.file_type_select_title))
                    .setSingleChoiceItems(dataTypes, dialogChecked, (d, w) -> dialogChecked = w)
                    .setOnDismissListener(dialog -> {
                        if(doDialogUpdate) setState(ListMenuState.NormalState, true);
                    })
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> {
                        doDialogUpdate = true;
                        d.cancel();
                    })
                    .setPositiveButton(getString(R.string.accept), (d, w) -> {
                        doDialogUpdate = false;
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
            doDialogUpdate = true;
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.ErrorMaterialAlertDialog)
                    .setIcon(R.drawable.ic_menu_delete)
                    .setTitle(getString(R.string.delete_lands_title))
                    .setMessage(getString(R.string.delete_lands_text))
                    .setOnDismissListener(dialog -> {
                        if(doDialogUpdate){
                            setState(ListMenuState.NormalState, true);
                        }
                    })
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> {
                        doDialogUpdate = true;
                        d.cancel();
                    })
                    .setPositiveButton(getString(R.string.accept), (d, w) -> {
                        doDialogUpdate = false;
                        removeSelectedLands();
                    })
                    .create();
            dialog.show();
        }
    }
    private void showSnackBar(String string) {
        Log.d(TAG, "showSnackBar: "+string);
        Snackbar snackbar = Snackbar.make(
                binding.clSnackBarContainer,
                string,
                Snackbar.LENGTH_LONG
        );
        snackbar.setAction(getString(R.string.okey),v->{});
        snackbar.show();
    }
    private void toggleMenu(boolean open){
        if(binding == null) return;
        if(open){
            binding.getRoot().openDrawer(GravityCompat.END, true);
        }else{
            binding.getRoot().closeDrawer(GravityCompat.END, false);
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
                bundle.putParcelable(AppValues.argLand,new Land(land));
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