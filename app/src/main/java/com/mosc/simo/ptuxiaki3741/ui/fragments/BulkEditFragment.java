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
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentBulkEditBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.BulkEditorAdapter;

import java.util.ArrayList;
import java.util.List;

public class BulkEditFragment extends Fragment {
    //todo: switch for `bulk edit lands` or `bulk edit zones`
    private FragmentBulkEditBinding binding;
    private LoadingDialog loadingDialog;
    private AlertDialog dialog;
    private ColorData tempColor;

    private AppViewModel viewModel;
    private ArrayAdapter<String> tags;
    private BulkEditorAdapter adapter;

    private List<Land> filteredLands;
    private List<Land> lands;
    private String selectedTag;
    private boolean isSaving;

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
        selectedTag = getString(R.string.bulk_edit_all_tag);
        lands = new ArrayList<>();
        filteredLands = new ArrayList<>();
        List<String> tempTags = new ArrayList<>();
        tempTags.add(getString(R.string.bulk_edit_all_tag));
        tags = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                tempTags
        );
    }

    private void initActivity() {
        if(getActivity() == null) return;
        loadingDialog = new LoadingDialog(getActivity());

        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressed(()->!isSaving);
    }

    private void initFragment() {
        binding.ibClose1.setOnClickListener(v-> goBack());
        binding.ibSave.setOnClickListener(v->saveData());

        binding.tvTag.setAdapter(tags);
        binding.tvTag.setOnItemClickListener((parent, view, position, id) -> {
            selectedTag = tags.getItem(position);
            binding.tvTag.setText(selectedTag,false);
            updateLands(this.lands);
        });
        binding.tvTag.setText(selectedTag,false);

        binding.cbAddTag.setOnCheckedChangeListener((button, checked) -> binding.tilAddTag.setEnabled(checked));
        binding.cbRemoveTag.setOnCheckedChangeListener((button, checked) -> binding.tilRemoveTag.setEnabled(checked));
        binding.cbChangeColor.setOnCheckedChangeListener((button, checked) -> binding.tilChangeColor.setEnabled(checked));
        ColorData color = AppValues.defaultLandColor;
        binding.tvColorPreview.setBackgroundColor(color.getColor());
        binding.etChangeColor.setText(color.toString());
        binding.tvColorPreview.setOnClickListener(v->displayColorDialog());
        binding.etChangeColor.setOnClickListener(v->displayColorDialog());

        adapter = new BulkEditorAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvLandResults.setHasFixedSize(true);
        binding.rvLandResults.setLayoutManager(layoutManager);
        binding.rvLandResults.setAdapter(adapter);
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
        viewModel.getLandsTags().observe(getViewLifecycleOwner(),this::onLandTagsUpdate);
        viewModel.getLands().observe(getViewLifecycleOwner(),this::onLandsUpdate);
    }

    private void onLandsUpdate(List<Land> lands) {
        this.lands.clear();
        if(lands != null) this.lands.addAll(lands);
        updateLands(this.lands);
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

        tags.clear();
        tags.addAll(tempTags);
        tags.notifyDataSetChanged();
        updateUi();
    }

    private void updateUi() {
        boolean resetTag = true;
        for(int i = 0; i < tags.getCount(); i ++){
            if(tags.getItem(i).equals(selectedTag)) {
                resetTag = false;
                break;
            }
        }
        if(resetTag){
            selectedTag = getString(R.string.bulk_edit_all_tag);
            binding.tvTag.setText(selectedTag,false);
        }
        if(tags.getCount() > 2){
            binding.tvTag.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.dropDownHeight));
        }else{
            binding.tvTag.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        updateLands(this.lands);
    }

    private void updateLands(List<Land> lands) {
        filteredLands = new ArrayList<>();
        if(selectedTag.equals(getString(R.string.bulk_edit_all_tag))){
            filteredLands.addAll(lands);
        }else if(selectedTag.equals(getString(R.string.bulk_edit_empty_tag))){
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
                if(tagsList.contains(selectedTag)) filteredLands.add(land);
            }
        }
        adapter.saveData(filteredLands);
        if(filteredLands.size() == 0) binding.tvEmptyList.setVisibility(View.VISIBLE);
        else binding.tvEmptyList.setVisibility(View.GONE);
        String display = filteredLands.size() + " ";
        if(filteredLands.size() == 1){
            display += getString(R.string.singular_land_label);
        }else{
            display += getString(R.string.plural_land_label);
        }
        binding.tvFilterResult.setText(display);
    }

    private void saveData(){
        if(filteredLands.size() == 0) return;

        final String addTags = getTagsToAdd();
        final String removeTags = getTagsToRemove();
        final ColorData newColor = getColorData();

        if(newColor == null && addTags == null && removeTags == null) return;

        isSaving = true;
        if(dialog != null){
            if(dialog.isShowing()) dialog.dismiss();
            dialog = null;
        }
        if(loadingDialog != null) loadingDialog.openDialog();
        final List<LandData> saveList = new ArrayList<>();
        for(Land land: filteredLands){
            if(land.getData() == null) continue;
            boolean needSave = false;
            LandData data = new LandData(land.getData());
            if(newColor != null) {
                data.setColor(newColor);
                needSave = true;
            }
            if(needSave){
                saveList.add(data);
            }
        }
        AsyncTask.execute(()->{
            viewModel.bulkEditLandData(saveList);
            if(loadingDialog != null) loadingDialog.closeDialog();
            isSaving = false;
        });
    }

    private String getTagsToAdd(){
        String temp;
        if(binding.cbAddTag.isChecked()){
            if(binding.etAddTag.getText() != null) {
                temp = binding.etAddTag.getText().toString();
                temp = DataUtil.removeSpecialCharactersCSV(temp);
                List<String> removeTagsList = DataUtil.splitTags(temp);
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < removeTagsList.size(); i++){
                    builder.append(removeTagsList.get(i));
                    if(i != (removeTagsList.size() - 1)) builder.append(", ");
                }
                if(removeTagsList.size() > 0) return builder.toString();
            }
        }
        return null;
    }

    private String getTagsToRemove(){
        String temp;
        if(binding.cbRemoveTag.isChecked()){
            if(binding.etRemoveTag.getText() != null) {
                temp = binding.etRemoveTag.getText().toString();
                temp = DataUtil.removeSpecialCharactersCSV(temp);
                List<String> removeTagsList = DataUtil.splitTags(temp);
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < removeTagsList.size(); i++){
                    builder.append(removeTagsList.get(i));
                    if(i != (removeTagsList.size() - 1)) builder.append(", ");
                }
                if(removeTagsList.size() > 0) return builder.toString();
            }
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

    private void goBack(){
        FragmentActivity activity = getActivity();
        if(activity == null) return;
        activity.runOnUiThread(activity::onBackPressed);
    }
}