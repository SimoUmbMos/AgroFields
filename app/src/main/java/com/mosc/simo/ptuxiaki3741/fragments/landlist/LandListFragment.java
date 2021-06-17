package com.mosc.simo.ptuxiaki3741.fragments.landlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.User;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListActionState;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListNavigateStates;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListNavigator;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.holders.LandListMenuHolder;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.holders.LandListViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;

import java.util.ArrayList;
import java.util.List;

public class LandListFragment  extends Fragment implements FragmentBackPress {
    private User user;
    private List<Land> lands;
    private List<Integer> selectedLands;
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
        initMenu();
        setupMenu(LandListMenuState.NormalState);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("debug", "onOptionsItemSelected: "+item.getItemId());
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

    private void initMenu() {
        final MenuItem add = menu.findItem(R.id.menu_item_add);
        final MenuItem delete = menu.findItem(R.id.menu_item_delete);
        final MenuItem export = menu.findItem(R.id.menu_item_export);
        final MenuItem deleteAction = menu.findItem(R.id.menu_delete_action);
        final MenuItem exportAction = menu.findItem(R.id.menu_export_action);
        final MenuItem selectAll = menu.findItem(R.id.menu_select_all_action);

        add.getActionView().setOnClickListener(v-> menu.performIdentifierAction(add.getItemId(),0));
        delete.getActionView().setOnClickListener(v-> menu.performIdentifierAction(delete.getItemId(),0));
        export.getActionView().setOnClickListener(v-> menu.performIdentifierAction(export.getItemId(),0));
        deleteAction.getActionView().setOnClickListener(v-> menu.performIdentifierAction(deleteAction.getItemId(),0));
        exportAction.getActionView().setOnClickListener(v-> menu.performIdentifierAction(exportAction.getItemId(),0));
        selectAll.getActionView().setOnClickListener(v-> menu.performIdentifierAction(selectAll.getItemId(),0));
    }
    private void setupMenu(LandListMenuState state) {
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

    private void init(View view) {
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if (activity != null) {
            activity.setOnBackPressed(this);
            //todo get real user
            getMockUser();
            actionBar = activity.getSupportActionBar();
        }
        if(user != null){
            if(actionBar != null){
                actionBar.setTitle("");
            }
            initLists();
            initHolders(view);
        }else{
            finish();
        }
    }
    private void initLists() {
        if(lands == null){
            lands = new ArrayList<>();
        }else{
            lands.clear();
        }
        //todo read db
        for(int i = 1; i < 6 ;i++){
            lands.add(new Land(user.getId(),"test "+i));
        }
        if(selectedLands == null){
            selectedLands = new ArrayList<>();
        }else{
            selectedLands.clear();
        }
    }
    private void initHolders(View view) {
        nav = new LandListNavigator(NavHostFragment.findNavController(this),user);
        viewHolder = new LandListViewHolder(view, lands, selectedLands, this::landClick, this::landLongClick);
        menuHolder = new LandListMenuHolder(this::OnNavigate,this::OnUpdateState,this::onAction);
    }

    private void landClick(int position) {
        if(menuHolder.getState() == LandListMenuState.NormalState)
            nav.toEditLand(lands.get(position));
        else
            toggleSelected(position);
    }
    private boolean landLongClick(int position) {
        if (menuHolder.getState() == LandListMenuState.NormalState){
            menuHolder.setState(LandListMenuState.MultiSelectState);
            toggleSelected(position);
        }else
            landClick(position);
        return true;
    }

    private void OnNavigate(LandListNavigateStates state){
        if(state == LandListNavigateStates.ToCreate){
            nav.toCreateLand();
        }
    }
    private void OnUpdateState(LandListMenuState state) {
        setupMenu(state);
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

    private void deleteAction() {
        Log.d("debug", "deleteAction: "+selectedLands.size()+" lands");
        List<Land> tempLands = new ArrayList<>();
        for(int index:selectedLands){
            Log.d("debug", "deleteAction: delete "+index);
            tempLands.add(lands.get(index));
            //todo remove from db
            viewHolder.notifyItemRemoved(index);
        }
        lands.removeAll(tempLands);
        selectedLands.clear();
    }
    private void exportAction() {
        Log.d("debug", "exportAction: "+selectedLands.size()+" lands");
        for(int index:selectedLands){
            Log.d("debug", "exportAction: export "+index);
            //todo export action
        }
        selectedLands.clear();
        viewHolder.notifyItemsChanged();
    }
    private void selectAllAction() {
        if(selectedLands.size() == lands.size()){
            selectedLands.clear();
        }else{
            selectedLands.clear();
            for(int i = 0; i <lands.size();i++)
                selectedLands.add(i);
        }
        viewHolder.notifyItemsChanged();
    }
    private void deselectAll() {
        selectedLands.clear();
        viewHolder.notifyItemsChanged();
    }
    private void toggleSelected(int position) {
        if(selectedLands.contains(position)){
            selectedLands.remove(position);
        }else{
            selectedLands.add(position);
        }
        viewHolder.notifyItemChanged(position);
    }

    private void finish() {
        if(getActivity() != null)
            getActivity().onBackPressed();
    }

    private void getMockUser() {
        user = new User(420,423,"makos");
    }
}
