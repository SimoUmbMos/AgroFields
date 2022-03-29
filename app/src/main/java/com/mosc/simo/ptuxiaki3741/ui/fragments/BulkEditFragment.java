package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentBulkEditBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.LandsBulkEditorAdapter;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.ZonesBulkEditorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BulkEditFragment extends Fragment {
    private FragmentBulkEditBinding binding;
    private LoadingDialog loadingDialog;
    private AlertDialog dialog;
    private ColorData tempColor;

    private AppViewModel viewModel;

    private ArrayAdapter<String> landTags;
    private LandsBulkEditorAdapter landAdapter;
    private final List<Land> lands = new ArrayList<>();
    private List<Land> filteredLands;
    private String selectedLandsTag;

    private ArrayAdapter<String> zoneTags;
    private ZonesBulkEditorAdapter zoneAdapter;
    private final List<LandZone> zones = new ArrayList<>();
    private List<LandZone> filteredZones;
    private String selectedZonesTag;

    private boolean isSaving, isShowingLands;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBulkEditBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModel();
    }

    private void initData() {
        isSaving = false;
        isShowingLands = true;
        filteredLands = new ArrayList<>();
        selectedLandsTag = getString(R.string.bulk_edit_all_tag);
        filteredZones = new ArrayList<>();
        selectedZonesTag = getString(R.string.bulk_edit_all_tag);
        List<String> landTagsList = new ArrayList<>();
        landTagsList.add(getString(R.string.bulk_edit_all_tag));
        landTags = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                landTagsList
        );
        List<String> zoneTagsList = new ArrayList<>();
        zoneTagsList.add(getString(R.string.bulk_edit_all_tag));
        zoneTags = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                zoneTagsList
        );
    }

    private void initActivity() {
        if(getActivity() == null) return;
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setOnBackPressed(()->!isSaving);
        loadingDialog = mainActivity.getLoadingDialog();
    }

    private void initFragment() {
        binding.ibClose1.setOnClickListener(v-> goBack());
        binding.ibSave.setOnClickListener(v->onSaveClick());

        binding.tvLandsTag.setAdapter(landTags);
        binding.tvLandsTag.setOnItemClickListener((parent, view, position, id) -> {
            selectedLandsTag = landTags.getItem(position);
            binding.tvLandsTag.setText(selectedLandsTag,false);
            updateLands(this.lands);
        });
        binding.tvLandsTag.setText(selectedLandsTag,false);

        binding.tvZonesTag.setAdapter(zoneTags);
        binding.tvZonesTag.setOnItemClickListener((parent, view, position, id) -> {
            selectedZonesTag = zoneTags.getItem(position);
            binding.tvZonesTag.setText(selectedZonesTag,false);
            updateZones(this.zones);
        });
        binding.tvZonesTag.setText(selectedZonesTag,false);

        binding.cbChangeNote.setOnCheckedChangeListener((button, checked) -> binding.tilChangeNote.setEnabled(checked));
        binding.cbChangeColor.setOnCheckedChangeListener((button, checked) -> binding.tilChangeColor.setEnabled(checked));
        binding.cbAddTag.setOnCheckedChangeListener((button, checked) -> binding.tilAddTag.setEnabled(checked));
        binding.cbRemoveTag.setOnCheckedChangeListener((button, checked) -> binding.tilRemoveTag.setEnabled(checked));
        binding.tvColorPreview.setOnClickListener(v->displayColorDialog());
        binding.etChangeColor.setOnClickListener(v->displayColorDialog());

        binding.rgDataOption.setOnCheckedChangeListener((radioGroup,id) -> showLands(id != R.id.rbZones));

        landAdapter = new LandsBulkEditorAdapter();
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvLandsResults.setHasFixedSize(true);
        binding.rvLandsResults.setLayoutManager(layoutManager1);
        binding.rvLandsResults.setAdapter(landAdapter);

        zoneAdapter = new ZonesBulkEditorAdapter();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvZonesResults.setHasFixedSize(true);
        binding.rvZonesResults.setLayoutManager(layoutManager2);
        binding.rvZonesResults.setAdapter(zoneAdapter);
        initValues();
        showLands(true);
    }

    private void initValues(){
        binding.cbChangeNote.setChecked(false);
        binding.cbChangeColor.setChecked(false);
        binding.cbAddTag.setChecked(false);
        binding.cbRemoveTag.setChecked(false);
        binding.etAddTag.setText("");
        binding.etRemoveTag.setText("");
        binding.etChangeNote.setText("");
        ColorData color = AppValues.defaultLandColor;
        binding.tvColorPreview.setBackgroundColor(color.getColor());
        binding.etChangeColor.setText(color.toString());
    }

    private void displayColorDialog() {
        if(!binding.tilChangeColor.isEnabled()) return;
        if(dialog != null){
            if(dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }

        if(binding.etChangeColor.getText() != null) tempColor = new ColorData(binding.etChangeColor.getText().toString());
        else tempColor = new ColorData(AppValues.defaultLandColor.toString());

        dialog = DialogUtil.getColorPickerDialog(getContext())
                .setPositiveButton(getString(R.string.submit),(d, w) -> {
                    binding.etChangeColor.setText(tempColor.toString());
                    binding.tvColorPreview.setBackgroundColor(tempColor.getColor());
                    d.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel),(d, w) -> d.cancel())
                .show();
        DialogUtil.setupColorDialog(dialog, tempColor);
    }

    private void initViewModel() {
        if(getActivity() == null) return;
        viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        viewModel.getLands().observe(getViewLifecycleOwner(),this::onLandsUpdate);
        viewModel.getLandZones().observe(getViewLifecycleOwner(),this::onZonesUpdate);
    }

    private void onSaveClick() {
        String newNote = getNoteData();
        ColorData newColor = getColorData();
        List<String> addTags = getTagsToAdd();
        List<String> removeTags = getTagsToRemove();
        initValues();
        if(isShowingLands){
            saveLandsData(newColor,addTags,removeTags);
        }else{
            saveZonesData(newNote,newColor,addTags,removeTags);
        }
    }

    private void showLands(boolean enable){
        if(enable){
            binding.trChangeNote.setVisibility(View.GONE);
            binding.clZonesBulkEdit.setVisibility(View.GONE);
            binding.clLandsBulkEdit.setVisibility(View.VISIBLE);
        }else{
            binding.trChangeNote.setVisibility(View.VISIBLE);
            binding.clZonesBulkEdit.setVisibility(View.VISIBLE);
            binding.clLandsBulkEdit.setVisibility(View.GONE);
        }
        isShowingLands = enable;
    }

    private String getNoteData(){
        if(binding.cbChangeNote.isChecked() && binding.etChangeNote.getText() != null){
            return binding.etChangeNote.getText().toString()
                    .replaceAll("\n+", " ")
                    .replaceAll(" +", " ")
                    .trim();
        }
        return null;
    }

    private ColorData getColorData(){
        if(binding.cbChangeColor.isChecked()){
            if(binding.etChangeColor.getText() != null) {
                return new ColorData(binding.etChangeColor.getText().toString());
            }
        }
        return null;
    }

    private List<String> getTagsToAdd(){
        if(binding.cbAddTag.isChecked() && binding.etAddTag.getText() != null){
            List<String> ans = new ArrayList<>();
            String temp = binding.etAddTag.getText().toString();
            temp = DataUtil.removeSpecialCharactersCSV(temp);
            List<String> tags = DataUtil.splitTags(temp);
            for(String tag : tags){
                if(tag == null || tag.isEmpty()) continue;
                if(!ans.contains(tag)) ans.add(tag);
            }
            return ans;
        }
        return null;
    }

    private List<String> getTagsToRemove(){
        if(binding.cbRemoveTag.isChecked() && binding.etRemoveTag.getText() != null){
            List<String> ans = new ArrayList<>();
            String temp = binding.etRemoveTag.getText().toString();
            temp = DataUtil.removeSpecialCharactersCSV(temp);
            List<String> tags = DataUtil.splitTags(temp);
            for(String tag : tags){
                if(tag == null || tag.isEmpty()) continue;
                if(!ans.contains(tag)) ans.add(tag);
            }
            return ans;
        }
        return null;
    }

    private void onLandsUpdate(List<Land> lands) {
        this.lands.clear();
        if(lands != null) {
            for(Land land : lands){
                if(land == null || land.getData() == null) continue;
                this.lands.add(land);
            }
        }
        onLandTagsUpdate(LandUtil.getLandsTags(this.lands));
    }

    private void onLandTagsUpdate(List<String> landTags) {
        String emptyTag = getString(R.string.bulk_edit_empty_tag);
        List<String> tempTags = new ArrayList<>();
        tempTags.add(getString(R.string.bulk_edit_all_tag));

        if(landTags != null) {
            for(String tag : landTags){
                String tempTag = tag;
                if(tempTag == null) tempTag = emptyTag;
                if(tempTags.contains(tempTag)) continue;
                if(tempTag.equals(emptyTag)) tempTags.add(1,tempTag);
                else tempTags.add(tempTag);
            }
        }

        this.landTags.clear();
        this.landTags.addAll(tempTags);
        this.landTags.notifyDataSetChanged();
        updateLandsUi();
    }

    private void updateLandsUi() {
        boolean resetTag = true;
        for(int i = 0; i < landTags.getCount(); i ++){
            if(landTags.getItem(i).equals(selectedLandsTag)) {
                resetTag = false;
                break;
            }
        }
        if(resetTag){
            selectedLandsTag = getString(R.string.bulk_edit_all_tag);
            binding.tvLandsTag.setText(selectedLandsTag,false);
        }
        if(landTags.getCount() > 2){
            binding.tvLandsTag.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.dropDownHeight));
        }else{
            binding.tvLandsTag.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        updateLands(this.lands);
    }

    private void updateLands(List<Land> lands) {
        filteredLands = new ArrayList<>();
        if(selectedLandsTag.equals(getString(R.string.bulk_edit_all_tag))){
            filteredLands.addAll(lands);
        }else if(selectedLandsTag.equals(getString(R.string.bulk_edit_empty_tag))){
            for(Land land : lands){
                if(land.getData() == null) continue;
                String tags = land.getData().getTags();
                if(tags == null || tags.isEmpty()){
                    filteredLands.add(land);
                }
            }
        }else{
            for(Land land : lands){
                if(land.getData() == null) continue;
                String tags = land.getData().getTags();
                if(tags == null || tags.isEmpty()) continue;
                List<String> tagsList = LandUtil.getLandTags(land.getData());
                if(tagsList.contains(selectedLandsTag)) filteredLands.add(land);
            }
        }
        landAdapter.saveData(filteredLands);
        if(filteredLands.size() == 0) binding.tvLandsEmptyList.setVisibility(View.VISIBLE);
        else binding.tvLandsEmptyList.setVisibility(View.GONE);
        String display = filteredLands.size() + " ";
        if(filteredLands.size() == 1){
            display += getString(R.string.singular_land_label);
        }else{
            display += getString(R.string.plural_land_label);
        }
        binding.tvLandsFilterResult.setText(display);
    }

    private void saveLandsData(ColorData newColor, List<String> addTags, List<String> removeTags){
        if(viewModel == null) return;
        if(filteredLands.size() == 0) return;

        isSaving = true;
        if(dialog != null){
            if(dialog.isShowing()) dialog.dismiss();
            dialog = null;
        }
        if(loadingDialog != null) loadingDialog.openDialog();
        List<Land> landsResult = new ArrayList<>(filteredLands);
        AsyncTask.execute(()->{
            List<LandData> saveList = new ArrayList<>();
            for(Land land: landsResult){
                if(land.getData() == null) continue;
                boolean needSave = false;
                LandData data = new LandData(land.getData());
                if(addTags != null){
                    List<String> landTags = LandUtil.getLandTags(data);
                    landTags.remove(null);
                    for(String addTag : addTags){
                        if(addTag == null || addTag.isEmpty()) continue;
                        if(!landTags.contains(addTag)) landTags.add(addTag);
                    }
                    data.setTags(DataUtil.mergeTags(landTags));
                    needSave = true;
                }
                if(removeTags != null){
                    List<String> landTags = LandUtil.getLandTags(data);
                    landTags.remove(null);
                    for(String removeTag : removeTags){
                        if(removeTag == null || removeTag.isEmpty()) continue;
                        landTags.remove(removeTag);
                    }
                    data.setTags(DataUtil.mergeTags(landTags));
                    needSave = true;
                }
                if(newColor != null) {
                    data.setColor(newColor);
                    needSave = true;
                }
                if(needSave){
                    saveList.add(data);
                }
            }
            viewModel.bulkEditLandData(saveList);
            if(loadingDialog != null) loadingDialog.closeDialog();
            isSaving = false;
        });
    }

    private void onZonesUpdate(Map<Long, List<LandZone>> zonesList) {
        this.zones.clear();
        if(zonesList != null) {
            zonesList.forEach((lid,zones)->{
                for(LandZone zone : zones){
                    if(zone == null || zone.getData() == null) continue;
                    this.zones.add(zone);
                }
            });
        }
        onZoneTagsUpdate(LandUtil.getLandZonesTags(this.zones));
    }

    private void onZoneTagsUpdate(List<String> zoneTags) {
        String emptyTag = getString(R.string.bulk_edit_empty_tag);
        List<String> tempTags = new ArrayList<>();
        tempTags.add(getString(R.string.bulk_edit_all_tag));

        if(zoneTags != null) {
            for(String tag : zoneTags){
                String tempTag = tag;
                if(tempTag == null) tempTag = emptyTag;
                if(tempTags.contains(tempTag)) continue;
                if(tempTag.equals(emptyTag)) tempTags.add(1,tempTag);
                else tempTags.add(tempTag);
            }
        }

        this.zoneTags.clear();
        this.zoneTags.addAll(tempTags);
        this.zoneTags.notifyDataSetChanged();
        updateZonesUi();
    }

    private void updateZonesUi() {
        boolean resetTag = true;
        for(int i = 0; i < zoneTags.getCount(); i ++){
            if(zoneTags.getItem(i).equals(selectedZonesTag)) {
                resetTag = false;
                break;
            }
        }
        if(resetTag){
            selectedZonesTag = getString(R.string.bulk_edit_all_tag);
            binding.tvZonesTag.setText(selectedZonesTag,false);
        }
        if(zoneTags.getCount() > 2){
            binding.tvZonesTag.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.dropDownHeight));
        }else{
            binding.tvZonesTag.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        updateZones(this.zones);
    }

    private void updateZones(List<LandZone> zones) {
        filteredZones = new ArrayList<>();
        if(selectedZonesTag.equals(getString(R.string.bulk_edit_all_tag))){
            filteredZones.addAll(zones);
        }else{
            boolean emptyTag = selectedZonesTag.equals(getString(R.string.bulk_edit_empty_tag));
            for(LandZone zone : zones){
                List<String> tagsList = LandUtil.getLandZoneTags(zone.getData());
                if(emptyTag){
                    if(tagsList.contains(null)) filteredZones.add(zone);
                }else{
                    if(tagsList.contains(selectedZonesTag)) filteredZones.add(zone);
                }
            }
        }
        zoneAdapter.saveData(filteredZones);
        if(filteredZones.size() == 0) binding.tvZonesEmptyList.setVisibility(View.VISIBLE);
        else binding.tvZonesEmptyList.setVisibility(View.GONE);
        String display = filteredZones.size() + " ";
        if(filteredZones.size() == 1){
            display += getString(R.string.singular_zone_label);
        }else{
            display += getString(R.string.plural_zone_label);
        }
        binding.tvZonesFilterResult.setText(display);
    }

    private void saveZonesData(String newNote, ColorData newColor, List<String> addTags, List<String> removeTags) {
        if(viewModel == null) return;
        if(filteredZones.size() == 0) return;

        isSaving = true;
        if(dialog != null){
            if(dialog.isShowing()) dialog.dismiss();
            dialog = null;
        }
        if(loadingDialog != null) loadingDialog.openDialog();
        List<LandZone> zonesResult = new ArrayList<>(filteredZones);
        AsyncTask.execute(()->{
            List<LandZoneData> saveList = new ArrayList<>();
            for(LandZone zone: zonesResult){
                if(zone.getData() == null) continue;
                boolean needSave = false;
                LandZoneData data = new LandZoneData(zone.getData());
                if(addTags != null){
                    List<String> zoneTags = LandUtil.getLandZoneTags(data);
                    zoneTags.remove(null);
                    for(String addTag : addTags){
                        if(addTag == null || addTag.isEmpty()) continue;
                        if(!zoneTags.contains(addTag)) zoneTags.add(addTag);
                    }
                    data.setTags(DataUtil.mergeTags(zoneTags));
                    needSave = true;
                }
                if(removeTags != null){
                    List<String> zoneTags = LandUtil.getLandZoneTags(data);
                    zoneTags.remove(null);
                    for(String removeTag : removeTags){
                        if(removeTag == null || removeTag.isEmpty()) continue;
                        zoneTags.remove(removeTag);
                    }
                    data.setTags(DataUtil.mergeTags(zoneTags));
                    needSave = true;
                }
                if(newNote != null) {
                    data.setNote(newNote);
                    needSave = true;
                }
                if(newColor != null) {
                    data.setColor(newColor);
                    needSave = true;
                }
                if(needSave){
                    saveList.add(data);
                }
            }
            viewModel.bulkEditZoneData(saveList);
            if(loadingDialog != null) loadingDialog.closeDialog();
            isSaving = false;
        });
    }

    private void goBack(){
        FragmentActivity activity = getActivity();
        if(activity == null) return;
        activity.runOnUiThread(activity::onBackPressed);
    }
}