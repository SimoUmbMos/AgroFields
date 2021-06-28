package com.mosc.simo.ptuxiaki3741.fragments.landlist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.repositorys.LandRepository;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListActionState;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListNavigateStates;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListNavigator;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.holders.LandListMenuHolder;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.holders.LandListViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.List;

public class LandListFragment  extends Fragment implements FragmentBackPress {
    public static final String TAG ="LandListFragment";

    private LandViewModel vmLands;
    private UserViewModel vmUsers;

    private LandListViewHolder viewHolder;
    private LandListMenuHolder menuHolder;
    private LandListNavigator nav;
    private Menu menu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_land_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.list_menu, menu);
        if(menuHolder != null){
            Log.d(TAG, "onCreateOptionsMenu: menuHolder.initMenu");
            menuHolder.initMenu(menu);
            menuHolder.setupMenu(LandListMenuState.NormalState);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: "+item.getItemId());
        if(menuHolder.menuItemClick(item.getItemId())){
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onBackPressed() {
        if(menuHolder.getState() != LandListMenuState.NormalState){
            menuHolder.setState(LandListMenuState.NormalState);
            return false;
        }else{
            return true;
        }
    }

    private void init(View view) {
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if (activity != null) {
            getViewModels(activity);
            activity.setOnBackPressed(this);
            actionBar = activity.getSupportActionBar();
        }
        if(vmUsers != null){
            if(vmUsers.isInit()){
                if( actionBar != null ){
                    actionBar.setTitle("");
                    actionBar.show();
                }
                initHolders(view);
                return;
            }
        }
        finish();
    }

    private void getViewModels(MainActivity activity) {
        vmLands = new ViewModelProvider(activity).get(LandViewModel.class);
        vmUsers = new ViewModelProvider(activity).get(UserViewModel.class);
    }

    private void initHolders(View view) {
        nav = new LandListNavigator(NavHostFragment.findNavController(this));
        viewHolder = new LandListViewHolder(view, vmLands, this::landClick, this::landLongClick);
        menuHolder = new LandListMenuHolder(this::OnNavigate,this::OnUpdateState,this::onAction);

        if(menu != null){
            Log.d(TAG, "initHolders: menuHolder.initMenu");
            menuHolder.initMenu(menu);
            menuHolder.setupMenu(LandListMenuState.NormalState);
        }
    }

    private void OnNavigate(LandListNavigateStates state){
        if(state == LandListNavigateStates.ToCreate && getActivity() != null){
            nav.toCreateLand(getActivity());
        }
    }
    private void OnUpdateState(LandListMenuState state) {
        menuHolder.setupMenu(state);
        if(menuHolder.getState() == LandListMenuState.NormalState)
            deselectAll();
    }
    private void onAction(LandListActionState action) {
        switch (action){
            case DeleteAction:
                deleteAction();
                break;
            case ExportAction:
                exportAction();
                break;
            case SelectAllAction:
                selectAllAction();
                break;
        }
        if(action != LandListActionState.SelectAllAction)
            menuHolder.setState(LandListMenuState.NormalState);
    }

    private void landClick(int position) {
//        todo land Click
//        if(menuHolder.getState() == LandListMenuState.NormalState) {
//            if(getActivity() != null){
//                List<Land> lands = vmLands.getLands().getValue();
//                if(lands != null)
//                    nav.toEditLand(getActivity(),lands.get(position))
//            }
//        }else{
//            toggleSelected(position);
//        }
    }
    private boolean landLongClick(int position) {
//        todo land Long Click
//        if (menuHolder.getState() == LandListMenuState.NormalState){
//            menuHolder.setState(LandListMenuState.MultiSelectState);
//            toggleSelected(position);
//        }else
//            landClick(position);
        return true;
    }

    private void deleteAction() {
//        todo delete Action
//        Log.d(TAG, "deleteAction: "+selectedLands.size()+" lands");
//        for(int index:selectedLands){
//            Log.d(TAG, "deleteAction: delete "+index);
//            viewHolder.notifyItemRemoved(index);
//        }
//        selectedLands.clear();
    }
    private void exportAction() {
//        todo export action
//        Log.d(TAG, "exportAction: "+selectedLands.size()+" lands");
//        for(int index:selectedLands){
//            Log.d(TAG, "exportAction: export "+index);
//        }
//        selectedLands.clear();
//        viewHolder.notifyItemsChanged();
    }
    private void selectAllAction() {
//        todo select All Action
//        if(selectedLands.size() == lands.size()){
//            selectedLands.clear();
//        }else{
//            selectedLands.clear();
//            for(int i = 0; i <lands.size();i++)
//                selectedLands.add(i);
//        }
//        viewHolder.notifyItemsChanged();
    }
    private void deselectAll() {
//        todo deselect All Action
//        selectedLands.clear();
//        viewHolder.notifyItemsChanged();
    }
    private void toggleSelected(int position) {
//        todo toggle Select Action
//        if(selectedLands.contains(position)){
//            selectedLands.remove(position);
//        }else{
//            selectedLands.add(position);
//        }
//        viewHolder.notifyItemChanged(position);
    }

    private void finish() {
        if(getActivity() != null)
            getActivity().onBackPressed();
    }
}
