package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.DialogUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarCategoryEditorBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;

public class CalendarCategoryEditorFragment extends Fragment {
    private FragmentCalendarCategoryEditorBinding binding;
    private CalendarCategory category;

    private AlertDialog dialog;
    private ColorData tempColor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarCategoryEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initData() {
        category = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argCalendarCategory)){
                category = getArguments().getParcelable(AppValues.argCalendarCategory);
            }
        }
        if(category == null){
            category = new CalendarCategory("",new ColorData(AppValues.defaultCalendarCategoryColor.toString()));
        }
    }

    private void initActivity() {
        if(getActivity() == null) return;
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressed(()->true);
    }

    private void initFragment() {
        if(category.getId() > 0){
            binding.tvTitle.setText(R.string.calendar_category_editor_title_edit);
            binding.ibDelete.setEnabled(true);
            binding.ibDelete.setVisibility(View.VISIBLE);
            binding.ibDelete.setOnClickListener(v->clickCategoryDelete());
        }
        binding.ibClose.setOnClickListener(v->clickBack());
        binding.btnCancel.setOnClickListener(v->clickCancel());
        binding.btnSave.setOnClickListener(v->clickSave());
        binding.etEditTitle.setText(category.getName());
        binding.tvEditColorPreview.setOnClickListener(v->clickCategoryColor());
        binding.etEditColor.setOnClickListener(v->clickCategoryColor());
        setColorUi();

    }

    private void setColorUi() {
        binding.etEditColor.setText(category.getColorData().toString());
        binding.tvEditColorPreview.setBackgroundColor(category.getColorData().getColor());
    }

    private void showColorDialog(){
        if(dialog != null){
            if(dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }
        tempColor = new ColorData(category.getColorData().toString());
        dialog = DialogUtil.getColorPickerDialog(getContext())
                .setPositiveButton(getString(R.string.submit),(d, w) -> {
                    category.setColorData(new ColorData(tempColor.toString()));
                    setColorUi();
                })
                .setNegativeButton(getString(R.string.cancel),(d, w) -> d.cancel())
                .show();
        DialogUtil.setupColorDialog(dialog, tempColor);
    }

    private void clickCategoryColor() {
        showColorDialog();
    }

    private void clickCategoryDelete() {
        if(getActivity() == null) return;
        AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        AsyncTask.execute(()->{
            if(viewModel.removeCalendarCategory(category)){
                goBack();
            }
        });
    }

    private void clickBack() {
        goBack();
    }

    private void clickCancel() {
        goBack();
    }

    private void clickSave() {
        if(getActivity() == null) return;
        String title = getTitle();
        if(title != null){
            category.setName(title);
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            AsyncTask.execute(()->{
                viewModel.saveCalendarCategory(category);
                goBack();
            });
        }
    }

    private String getTitle(){
        String temp = "";
        if(binding.etEditTitle.getText() != null) temp = binding.etEditTitle.getText().toString();
        temp = DataUtil.removeSpecialCharactersWithoutSpaces(temp);
        return temp.isEmpty()?null:temp;
    }

    private void goBack() {
        if(getActivity() == null) return;
        getActivity().runOnUiThread(getActivity()::onBackPressed);
    }
}