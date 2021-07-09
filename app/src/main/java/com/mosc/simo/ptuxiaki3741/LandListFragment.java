package com.mosc.simo.ptuxiaki3741;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.enums.LandListActionState;
import com.mosc.simo.ptuxiaki3741.enums.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.enums.LandListNavigateStates;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.navigators.LandListNavigator;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.holders.LandListMenuHolder;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.holders.LandListRecycleViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;

import java.util.List;

public class LandListFragment  extends Fragment implements FragmentBackPress {
    public static final String TAG ="LandListFragment";

    private LandViewModel vmLands;
    private UserViewModel vmUsers;
    private User currUser;

    private LandListRecycleViewHolder viewHolder;
    private LandListMenuHolder menuHolder;
    private LandListNavigator nav;
    private ActionBar actionBar;

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
        actionBar = null;
        if (activity != null) {
            actionBar = activity.getSupportActionBar();
            activity.setOnBackPressed(this);
            changeActionBarTitle("");
            getViewModels(activity);
            initObservers();
            initHolders(view);
        }
    }
    private void changeActionBarTitle(String title) {
        if( actionBar != null ){
            actionBar.setTitle(title);
            actionBar.show();
        }
    }
    private void getViewModels(MainActivity activity) {
        vmLands = new ViewModelProvider(activity).get(LandViewModel.class);
        vmUsers = new ViewModelProvider(activity).get(UserViewModel.class);
    }
    private void initObservers() {
        if(vmLands != null){
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandListUpdate);
            vmLands.getSelectedLands().observe(getViewLifecycleOwner(),this::onSelectedLandUpdate);
        }
        if(vmUsers != null){
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            if(vmUsers.getCurrUser().getValue() != null){
                currUser = vmUsers.getCurrUser().getValue();
                vmLands.init(currUser);
            }
        }
    }
    private void initHolders(View view) {
        nav = new LandListNavigator(NavHostFragment.findNavController(this));
        viewHolder = new LandListRecycleViewHolder(view, vmLands, this::landClick, this::landLongClick);
        menuHolder = new LandListMenuHolder(this::OnNavigate,this::OnUpdateState,this::onAction);
    }

    private void onCurrUserUpdate(User newLoginUser) {
        this.currUser = newLoginUser;
        if( currUser != null) {
            changeActionBarTitle(currUser.getUsername()+"'s Land's");
            vmLands.init(currUser);
        }
    }
    private void onSelectedLandUpdate(List<Integer> integers) {
        viewHolder.notifyItemsChanged();
    }
    private void onLandListUpdate(List<Land> lands) {
        if(lands.size() == 0){
            
        }else{

        }
        viewHolder.notifyItemsChanged();
    }

    private void OnNavigate(LandListNavigateStates state){
        if(state == LandListNavigateStates.ToCreate && getActivity() != null){
            nav.toCreateLand(getActivity());
        }
    }

    private void OnUpdateState(LandListMenuState state) {
        menuHolder.setupMenu(state);
        if(menuHolder.getState() == LandListMenuState.NormalState)
            vmLands.deselectAllLands();
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
        if(menuHolder.getState() != LandListMenuState.NormalState) {
            vmLands.toggleSelectOnPosition(position);
        }else{
            if(getActivity() != null){
                Land land = vmLands.getLand(position);
                if(land != null){
                    nav.toEditLand(getActivity(),land);
                }
            }
        }
    }
    private boolean landLongClick(int position) {
        if (menuHolder.getState() == LandListMenuState.NormalState){
            menuHolder.setState(LandListMenuState.MultiSelectState);
            vmLands.toggleSelectOnPosition(position);
        }else{
            landClick(position);
        }
        return true;
    }

    private void deleteAction() {
        vmLands.removeSelectedLands(currUser);
        vmLands.deselectAllLands();
        viewHolder.notifyItemsChanged();
    }
    private void exportAction() {
        List<Integer> selectedLands = vmLands.getSelectedLands().getValue();
        if(selectedLands != null){
            List<Land> lands = vmLands.returnSelectedLands();
            //TODO: export action
            vmLands.deselectAllLands();
        }
    }
    private void selectAllAction(){
        if(vmLands.isAllSelected()){
            vmLands.deselectAllLands();
        }else{
            vmLands.selectAllLands();
        }
    }
}
