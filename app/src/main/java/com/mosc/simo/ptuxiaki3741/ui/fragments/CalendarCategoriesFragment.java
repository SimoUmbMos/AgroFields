package com.mosc.simo.ptuxiaki3741.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.data.enums.ListMenuState;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarCategoryEntity;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarCategoriesBinding;
import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters.CalendarCategoriesAdapter;

import java.util.ArrayList;
import java.util.List;

public class CalendarCategoriesFragment extends Fragment implements FragmentBackPress {
    private FragmentCalendarCategoriesBinding binding;
    private CalendarCategoriesAdapter adapter;

    private AlertDialog dialog;
    private boolean doDialogUpdate;

    private AppViewModel viewModel;

    private List<CalendarCategoryEntity> data;
    private ListMenuState state;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarCategoriesBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initData(){
        data = new ArrayList<>();
        viewModel = null;
        adapter = new CalendarCategoriesAdapter(this::onItemClick, this::onItemLongClick);
        state = ListMenuState.NormalState;
        doDialogUpdate = true;
    }
    private void initActivity(){
        if(getActivity() == null) return;
        viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        if(getActivity().getClass() != MainActivity.class) return;
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressed(this);
    }
    private void initFragment(){
        binding.ibClose.setOnClickListener(v->goBack());
        binding.ibSelectAll.setOnClickListener(v->toggleAllItems());
        binding.fabAdd.setOnClickListener(v->toCategoryCreator());
        binding.fabDelete.setOnClickListener(v->onDeleteButtonClick());

        binding.rvCalendarCategories.setHasFixedSize(true);

        adapter.saveData(data);
        binding.rvCalendarCategories.setAdapter(adapter);

        updateUi();

        binding.rvCalendarCategories.post(()->{
            final int maxColumnNumber = getResources().getInteger(R.integer.screenMaxColumnNumber);
            int maxWidth = getResources().getDimensionPixelSize(R.dimen.max_grid_width);
            int spanCount = 1;
            if(maxWidth != 0) {
                spanCount = Math.floorDiv(binding.rvCalendarCategories.getWidth(), maxWidth);
                if (spanCount == 0) spanCount = 1;
            }
            spanCount = Math.min(spanCount,maxColumnNumber);
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL);
            binding.rvCalendarCategories.setLayoutManager(staggeredGridLayoutManager);
        });
    }
    private void initViewModel(){
        if(viewModel == null) return;
        viewModel.getCalendarCategories().observe(getViewLifecycleOwner(),this::onCategoriesUpdate);
    }

    private void onCategoriesUpdate(List<CalendarCategory> categories) {
        data.clear();
        if(categories != null){
            for(CalendarCategory category : categories){
                if(category == null || category.getId() == AppValues.defaultCalendarCategoryID) continue;
                data.add(new CalendarCategoryEntity(category));
            }
        }

        binding.tvCategoriesListActionLabel.setText(getResources().getString(R.string.empty_list));
        updateUi();

        adapter.saveData(data);
        binding.rvCalendarCategories.smoothScrollBy(1, 1);
    }
    private void onItemClick(CalendarCategoryEntity item){
        if(state == ListMenuState.NormalState){
            toCategoryEditor(item.getData());
        }else{
            toggleSelected(item);
        }
    }
    private void onItemLongClick(CalendarCategoryEntity item){
        if(state == ListMenuState.NormalState){
            setState(ListMenuState.MultiSelectState);
        }
        toggleSelected(item);
    }

    private boolean allItemsSelected(){
        for(CalendarCategoryEntity entity : data){
            if(!entity.isSelected()) return false;
        }
        return true;
    }
    private void toggleSelected(CalendarCategoryEntity item){
        int index = data.indexOf(item);
        if(index < 0 || index >= data.size()) return;
        item.setSelected(!item.isSelected());
        adapter.notifyItemChanged(index);
        if(state == ListMenuState.MultiSelectState && getSelectedEntities().size() == 0){
            setState(ListMenuState.NormalState);
        }
    }
    private void deselectAllItems(){
        for(int i = 0; i < data.size(); i++){
            CalendarCategoryEntity entity = data.get(i);
            if(entity.isSelected()){
                entity.setSelected(false);
                adapter.notifyItemChanged(i);
            }
        }
    }
    private void selectAllItems(){
        for(int i = 0; i < data.size(); i++){
            CalendarCategoryEntity entity = data.get(i);
            if(!entity.isSelected()){
                entity.setSelected(true);
                adapter.notifyItemChanged(i);
            }
        }
    }
    private void toggleAllItems(){
        if(allItemsSelected()){
            deselectAllItems();
            if(state == ListMenuState.MultiSelectState) setState(ListMenuState.NormalState);
        }else{
            selectAllItems();
        }
    }
    private List<CalendarCategoryEntity> getSelectedEntities(){
        List<CalendarCategoryEntity> ans = new ArrayList<>();
        for(CalendarCategoryEntity entity : data){
            if(entity.isSelected()) ans.add(entity);
        }
        return ans;
    }

    private void updateUi() {
        if(data.size() > 0){
            binding.tvCategoriesListActionLabel.setVisibility(View.GONE);
        }else{
            binding.tvCategoriesListActionLabel.setVisibility(View.VISIBLE);
        }
        switch (state){
            case MultiSelectState:
            case MultiDeleteState:
                setCheckableRecycleView(true);
                binding.fabAdd.setEnabled(false);
                break;
            case NormalState:
            default:
                setCheckableRecycleView(false);
                binding.fabAdd.setEnabled(true);
                break;
        }
    }

    private void setState(ListMenuState state){
        setState(state,true);
    }
    private void setState(ListMenuState state, boolean doUpdate){
        if(this.state == state) return;
        this.state = state;
        if(state == ListMenuState.NormalState){
            deselectAllItems();
            if(doUpdate) binding.getRoot().transitionToStart();
        }
        updateUi();
    }
    private void setCheckableRecycleView(boolean showCheckBox){
        if(showCheckBox){
            binding.ibSelectAll.setEnabled(true);
            binding.ibSelectAll.setVisibility(View.VISIBLE);
        }else{
            binding.ibSelectAll.setEnabled(false);
            binding.ibSelectAll.setVisibility(View.GONE);
        }
        adapter.saveData(data,showCheckBox);
        binding.rvCalendarCategories.smoothScrollBy(1, 1);
    }

    private void onDeleteButtonClick(){
        if(state != ListMenuState.NormalState){
            deleteAction();
        }else{
            setState(ListMenuState.MultiDeleteState);
        }
    }
    private void deleteAction(){
        if(getSelectedEntities().size() > 0){
            showDeleteDialog();
        }else{
            setState(ListMenuState.NormalState);
        }
    }
    private void showDeleteDialog(){
        if(dialog != null){
            if(dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }
        doDialogUpdate = true;
        dialog = new MaterialAlertDialogBuilder(binding.getRoot().getContext(), R.style.ErrorMaterialAlertDialog)
                .setIcon(R.drawable.ic_menu_delete)
                .setTitle(getString(R.string.delete_calendar_categories_title))
                .setMessage(getString(R.string.delete_calendar_categories_text))
                .setOnDismissListener(dialog -> {
                    if(doDialogUpdate){
                        setState(ListMenuState.NormalState);
                    }
                })
                .setNeutralButton(getString(R.string.cancel), (d, w) -> {
                    doDialogUpdate = true;
                    d.cancel();
                })
                .setPositiveButton(getString(R.string.accept), (d, w) -> {
                    doDialogUpdate = false;
                    deleteSelectedEntities();
                })
                .create();
        dialog.show();
    }
    private void deleteSelectedEntities(){
        List<CalendarCategoryEntity> deleteList = getSelectedEntities();
        setState(ListMenuState.NormalState,false);
        if(deleteList.size() == 0 || getActivity() == null || viewModel == null) return;

        FragmentActivity activity = getActivity();
        AsyncTask.execute(()->{
            List<CalendarCategory> finalDeleteList = new ArrayList<>();
            for(CalendarCategoryEntity deleteItem : deleteList){
                if(deleteItem == null || deleteItem.getData() == null) continue;
                if(!viewModel.calendarCategoryHasNotifications(deleteItem.getData())){
                    finalDeleteList.add(deleteItem.getData());
                }
            }
            String result;
            if(deleteList.size() == finalDeleteList.size()){
                result = getString(R.string.removed_all_categories);
            }else if(finalDeleteList.size() > 0){
                result = getString(R.string.removed_some_categories);
            }else{
                result = getString(R.string.removed_none_categories);
            }
            if(finalDeleteList.size() > 0) viewModel.removeCalendarCategories(finalDeleteList);
            activity.runOnUiThread(()->showSnackBar(result));
        });
    }

    private void showSnackBar(String text) {
        Snackbar snackbar = Snackbar.make(
                binding.clSnackBarContainer,
                text,
                Snackbar.LENGTH_LONG
        );
        snackbar.setAction(getString(R.string.okey),v->{});
        snackbar.show();
    }

    private void goBack(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(getActivity()::onBackPressed);
    }
    private void toCategoryEditor(CalendarCategory item) {
        if(item == null || getActivity() == null) return;
        getActivity().runOnUiThread(()->{
            NavController nav = UIUtil.getNavController(this,R.id.CalendarCategoriesMenu);
            Bundle bundle = new Bundle();
            bundle.putParcelable(AppValues.argCalendarCategory, new CalendarCategory(item));
            if(nav != null)
                nav.navigate(R.id.toCalendarCategoryEditor, bundle);
        });
    }
    private void toCategoryCreator(){
        if(getActivity() == null) return;
        getActivity().runOnUiThread(()->{
            NavController nav = UIUtil.getNavController(this,R.id.CalendarCategoriesMenu);
            if(nav != null)
                nav.navigate(R.id.toCalendarCategoryEditor);
        });
    }
}