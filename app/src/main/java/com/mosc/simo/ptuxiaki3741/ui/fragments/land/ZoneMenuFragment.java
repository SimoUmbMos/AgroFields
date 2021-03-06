package com.mosc.simo.ptuxiaki3741.ui.fragments.land;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
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
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.LandZonesListAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentZonesMenuBinding;
import com.mosc.simo.ptuxiaki3741.data.enums.FileType;
import com.mosc.simo.ptuxiaki3741.data.enums.ListMenuState;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.data.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZoneMenuFragment extends Fragment implements FragmentBackPress {
    private static final String TAG = "ZonesLandSelectedFragment";
    private FragmentZonesMenuBinding binding;

    private ListMenuState state;
    private LandZonesListAdapter adapter;
    private Land selectedLand;
    private final List<LandZone> data = new ArrayList<>();
    private final List<LandZone> displayData = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private String selectedTag;
    private AlertDialog dialog;
    private boolean doDialogUpdate;
    private int dialogChecked;

    private AppViewModel vmLands;

    private List<LandZone> exportZones;
    private FileType exportAction;

    private final ActivityResultLauncher<Intent> startActivityIntentFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(binding == null || result.getResultCode() != Activity.RESULT_OK) {
                    exportZones = null;
                    exportAction = null;
                    return;
                }
                Intent data = result.getData();
                if(data == null) {
                    exportZones = null;
                    exportAction = null;
                    return;
                }
                OutputStream fileOutputStream;
                try{
                    fileOutputStream = requireActivity().getContentResolver().openOutputStream(data.getData());
                } catch (FileNotFoundException e) {
                    fileOutputStream = null;
                }
                writeToFile(fileOutputStream);
            });

    private final ActivityResultLauncher<String> permissionWriteChecker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onPermissionWriteResult
    );

    private void onPermissionWriteResult(boolean permission) {
        if(permission){
            exportAction();
        }else{
            exportZones = null;
            exportAction = null;
        }
    }

    //init relative
    private boolean initData() {
        selectedLand = null;
        state = ListMenuState.NormalState;

        tags.clear();
        selectedTag = null;
        tags.add(getString(R.string.all_tags_tag));

        exportZones = null;
        exportAction = null;

        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argLand)){
                selectedLand = getArguments().getParcelable(AppValues.argLand);
            }
        }
        return selectedLand != null;
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
        adapter = new LandZonesListAdapter(
                selectedLand,
                this::onZoneClick,
                this::onZoneLongClick
        );

        binding.tvTitle.setText(selectedLand.toString());
        binding.ibClose.setOnClickListener(view -> onCloseClick());
        binding.ibClose1.setOnClickListener(view -> onCloseClick());
        binding.ibMenuButton.setOnClickListener(v -> toggleMenu(true));
        binding.ibSelectAll.setOnClickListener(view -> onSelectAllClick());
        binding.fabAdd.setOnClickListener(view -> onAddClick());
        binding.fabDelete.setOnClickListener(view -> onDeleteClick());
        binding.fabExport.setOnClickListener(view -> onExportClick());

        binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.navLandZoneFilterMenu.setNavigationItemSelectedListener(this::onFilterClick);

        binding.tvZonesListActionLabel.setText(getResources().getString(R.string.loading_label));
        binding.rvZoneList.setHasFixedSize(true);
        binding.rvZoneList.setAdapter(adapter);

        setupSideMenu();
        updateUi();

        binding.rvZoneList.post(()->{
            final int maxColumnNumber = getResources().getInteger(R.integer.screenMaxColumnNumber);
            int maxWidth = getResources().getDimensionPixelSize(R.dimen.max_grid_width);
            int spanCount = 1;
            if(maxWidth != 0) {
                spanCount = Math.floorDiv(binding.rvZoneList.getWidth(), maxWidth);
                if (spanCount == 0) spanCount = 1;
            }
            spanCount = Math.min(spanCount,maxColumnNumber);
            StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
            binding.rvZoneList.setLayoutManager(gridLayoutManager);
        });
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
    private void onDataUpdate(Map<Long,List<LandZone>> zones) {
        data.clear();
        if(selectedLand != null && zones != null){
            List<LandZone> temp = zones.getOrDefault(selectedLand.getData().getId(),null);
            if(temp != null){
                for(LandZone tempZone : temp){
                    if(tempZone == null || tempZone.getData() == null) continue;
                    data.add(tempZone);
                }
            }
        }
        binding.tvZonesListActionLabel.setText(getResources().getString(R.string.empty_list));
        onLandZonesTagsChange(LandUtil.getLandZonesTags(data));
        updateList();
    }
    private void onLandZonesTagsChange(List<String> landZonesTags) {
        String emptyTag = getString(R.string.empty_tag_tag);
        tags.clear();
        tags.add(getString(R.string.all_tags_tag));
        if(landZonesTags != null){
            for(String tag : landZonesTags){
                String tempTag = tag;
                if(tempTag == null) tempTag = emptyTag;
                if(tags.contains(tempTag)) continue;
                if(tempTag.equals(emptyTag)) tags.add(1,tempTag);
                else tags.add(tempTag);
            }
        }
        setupSideMenu();
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
            setState(ListMenuState.MultiSelectState, true);
            binding.mlRoot.transitionToEnd();
        }
        toggleZone(zone);
    }
    private void onCloseClick() {
        goBack(getActivity());
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
            setState(ListMenuState.MultiDeleteState, true);
        }
    }
    private void onExportClick() {
        if(state == ListMenuState.MultiExportState || state == ListMenuState.MultiSelectState){
            showExportDialog();
        }else{
            setState(ListMenuState.MultiExportState, true);
        }
    }
    private boolean onFilterClick(MenuItem item) {
        int i = item.getItemId();
        if(i < 0 || i >= tags.size()) return false;
        String tag = tags.get(i);
        if(tag.equals(getString(R.string.all_tags_tag))){
            selectedTag = null;
        }else{
            selectedTag = tag;
        }
        toggleMenu(false);
        new Handler().post(this::updateList);
        return true;
    }

    //zone selector relative
    private void toggleZone(LandZone zone) {
        zone.setSelected(!zone.isSelected());
        adapter.notifyItemChanged(displayData.indexOf(zone));
        if(state == ListMenuState.MultiSelectState && areNonZoneSelected()){
            setState(ListMenuState.NormalState, true);
        }
    }
    private void toggleAllZone() {
        if(areAllZoneSelected()){
            deselectAllZones();
        }else{
            selectAllZones();
        }
        if(getSelectedZones().size() == 0 && state == ListMenuState.MultiSelectState){
            setState(ListMenuState.NormalState, true);
        }
    }
    private void selectAllZones() {
        for(LandZone zone:displayData){
            if(!zone.isSelected()){
                zone.setSelected(true);
                adapter.notifyItemChanged(displayData.indexOf(zone));
            }
        }
    }
    private void deselectAllZones() {
        for(LandZone zone:displayData){
            if(zone.isSelected()){
                zone.setSelected(false);
                adapter.notifyItemChanged(displayData.indexOf(zone));
            }
        }
    }
    private boolean areAllZoneSelected() {
        for(LandZone zone:displayData){
            if(!zone.isSelected()){
                return false;
            }
        }
        return true;
    }
    private boolean areNonZoneSelected() {
        for(LandZone zone:displayData){
            if(zone.isSelected()){
                return false;
            }
        }
        return true;
    }
    private List<LandZone> getSelectedZones() {
        List<LandZone> ans = new ArrayList<>();
        for(LandZone zone:displayData){
            if(zone.isSelected()){
                ans.add(zone);
            }
        }
        return ans;
    }

    //actions relative
    private void showDeleteDialog(){
        if(getSelectedZones().size() == 0 ){
            setState(ListMenuState.NormalState, true);
            return;
        }
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            doDialogUpdate = true;
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Error)
                    .setIcon(R.drawable.ic_menu_delete)
                    .setTitle(getString(R.string.delete_zones_dialog_title))
                    .setMessage(getString(R.string.delete_zones_dialog_text))
                    .setOnDismissListener(dialog -> {
                        if(doDialogUpdate) setState(ListMenuState.NormalState, true);
                    })
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> {
                        doDialogUpdate = true;
                        d.cancel();
                    })
                    .setPositiveButton(getString(R.string.accept), (d, w) -> {
                        doDialogUpdate = false;
                        deleteAction();
                    })
                    .create();
            dialog.show();
        }
    }
    private void deleteAction(){
        AsyncTask.execute(()->{
            List<LandZone> selectedZones = getSelectedZones();
            if(selectedZones.size()>0){
                if(vmLands != null){
                    String display;
                    if(vmLands.removeZones(selectedZones)){
                        display = getString(R.string.zone_delete);
                    }else{
                        display = getString(R.string.zone_delete_error);
                    }
                    if(getActivity() != null)
                        getActivity().runOnUiThread(()-> showSnackBar(display));
                }
            }
            if(getActivity() != null)
                getActivity().runOnUiThread(()->
                        setState(ListMenuState.NormalState, false)
                );
        });
    }
    private void showExportDialog(){
        if(getSelectedZones().size() == 0 ){
            setState(ListMenuState.NormalState, true);
            return;
        }
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            doDialogUpdate = true;
            dialogChecked = 0;
            String[] dataTypes = {"KML","GeoJson","GML"};
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
                                exportSelectedZones(FileType.KML);
                                break;
                            case 1:
                                exportSelectedZones(FileType.GEOJSON);
                                break;
                            case 2:
                                exportSelectedZones(FileType.GML);
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
        exportZones = new ArrayList<>(getSelectedZones());
        exportAction = action;
        deselectAllZones();
        setState(ListMenuState.NormalState, false);
        if(exportZones.size()>0 && exportAction != FileType.NONE){
            if(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                permissionWriteChecker.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }else{
                onPermissionWriteResult(true);
            }
        }else{
            onPermissionWriteResult(false);
        }
    }
    private void exportAction(){
        if(exportZones == null) return;
        if(exportAction == null) return;

        if(exportZones.size()>0 && exportAction != FileType.NONE){
            initWriteToFile();
        }else{
            exportZones = null;
            exportAction = null;
        }
    }
    private void initWriteToFile() {
        if(exportZones != null && exportZones.size()>0){
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String fileName = "export-"+now.format(dateTimeFormatter);
            String fileType = null;
            switch(exportAction){
                case KML:
                    fileType = "application/vnd.google-earth.kml+xml";
                    fileName = fileName+".kml";
                    break;
                case GEOJSON:
                    fileType = "application/geo+json";
                    fileName = fileName+".geojson";
                    break;
                case GML:
                    fileType = "application/gml+xml";
                    fileName = fileName+".gml";
                    break;
                default:
                    fileName = null;
            }
            if(fileName != null) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(fileType);
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                startActivityIntentFile.launch(intent);
            }else{
                exportZones = null;
                exportAction = null;
            }
        }else{
            exportZones = null;
            exportAction = null;
        }
    }
    private void writeToFile(OutputStream output){
        if(output != null){
            String content="";
            switch(exportAction){
                case KML:
                    content = FileUtil.zonesToKmlString(exportZones);
                    break;
                case GEOJSON:
                    content = FileUtil.zonesToGeoJsonString(exportZones);
                    break;
                case GML:
                    content = FileUtil.zonesToGmlString(exportZones);
                    break;
            }
            String display;
            if(FileUtil.createFile(content, output)){
                display = getString(R.string.land_export);
            }else{
                display = getString(R.string.file_not_created);
            }
            showSnackBar(display);
        }
        exportZones = null;
        exportAction = null;
    }

    //ui relative
    private void setCheckableRecycleView(boolean show) {
        binding.ibSelectAll.setEnabled(show);
        binding.ibMenuButton.setEnabled(!show);
        if(show){
            binding.ibSelectAll.setVisibility(View.VISIBLE);
            binding.ibMenuButton.setVisibility(View.GONE);
        }else{
            binding.ibSelectAll.setVisibility(View.GONE);
            binding.ibMenuButton.setVisibility(View.VISIBLE);
        }
        updateList(show);
    }
    public void setState(ListMenuState state, boolean doUpdate) {
        if(this.state == state) return;

        this.state = state;
        if(state == ListMenuState.NormalState){
            deselectAllZones();
            if(doUpdate) binding.mlRoot.transitionToStart();
            binding.ibClose.setVisibility(View.GONE);
            binding.ibClose1.setVisibility(View.VISIBLE);
        }else{
            binding.ibClose.setVisibility(View.VISIBLE);
            binding.ibClose1.setVisibility(View.GONE);
        }
        updateUi();
    }
    private void setupSideMenu() {
        Menu menu = binding.navLandZoneFilterMenu.getMenu();
        menu.clear();
        SubMenu subMenu = menu.addSubMenu(Menu.NONE,-1,Menu.NONE,getString(R.string.land_zones_filter_title));
        String emptyTag = getString(R.string.empty_tag_tag);
        boolean checked = false;
        List<String> insertedTags = new ArrayList<>();
        for(int i = 0; i < tags.size(); i++){
            String tag = tags.get(i);
            if(tag == null || tag.isEmpty()) tag = emptyTag;

            if(insertedTags.contains(tag)) continue;
            insertedTags.add(tag);

            MenuItem item = subMenu.add(Menu.NONE,i,Menu.NONE,tag);
            item.setCheckable(true);
            if(selectedTag != null){
                if(binding != null && tag.equals(selectedTag) && !checked) {
                    binding.navLandZoneFilterMenu.setCheckedItem(item);
                    checked = true;
                }
            }else{
                if(binding != null && tag.equals(getString(R.string.all_tags_tag)) && !checked){
                    binding.navLandZoneFilterMenu.setCheckedItem(item);
                    checked = true;
                }
            }
        }
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
    private void updateList() {
        displayData.clear();
        if(selectedTag == null){
            displayData.addAll(data);
        }else{
            boolean isEmpty = selectedTag.equals(getString(R.string.empty_tag_tag));
            for(LandZone zone : data){
                if(zone == null || zone.getData() == null) continue;
                List<String> landZoneTags = LandUtil.getLandZoneTags(zone.getData());
                if(isEmpty){
                    if(landZoneTags.contains(null)) displayData.add(zone);
                }else{
                    if(landZoneTags.contains(selectedTag)) displayData.add(zone);
                }
            }
        }
        if(displayData.size()>0){
            binding.tvZonesListActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvZonesListActionLabel.setVisibility(View.VISIBLE);
        }
        adapter.saveData(displayData);
        binding.rvZoneList.smoothScrollBy(1, 1);
    }
    private void updateList(boolean show) {
        displayData.clear();
        if(selectedTag == null){
            displayData.addAll(data);
        }else{
            boolean isEmpty = selectedTag.equals(getString(R.string.empty_tag_tag));
            for(LandZone zone : data){
                if(zone == null || zone.getData() == null) continue;
                List<String> landZoneTags = LandUtil.getLandZoneTags(zone.getData());
                if(isEmpty){
                    if(landZoneTags.contains(null)) displayData.add(zone);
                }else{
                    if(landZoneTags.contains(selectedTag)) displayData.add(zone);
                }
            }
        }
        if(displayData.size()>0){
            binding.tvZonesListActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvZonesListActionLabel.setVisibility(View.VISIBLE);
        }
        adapter.saveData(displayData, show);
        binding.rvZoneList.smoothScrollBy(1, 1);
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

    //navigator relative
    private void toZoneSelected(@Nullable Activity activity, LandZone z) {
        Log.d("debug", "toZoneSelected: ");
        if(activity != null){
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ZonesLandSelectedFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argLand,new Land(selectedLand));
                if(z != null){
                    bundle.putParcelable(AppValues.argZone,new LandZone(z));
                }
                if(nav != null){
                    nav.navigate(R.id.toLandPreview,bundle);
                }
            });
        }
    }
    private void goBack(Activity activity){
        if(activity == null) return;
        activity.runOnUiThread(activity::onBackPressed);
    }

    //Override relative
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentZonesMenuBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        if(initData()){
            initFragment();
            initViewModel();
        }else{
            goBack(getActivity());
        }
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