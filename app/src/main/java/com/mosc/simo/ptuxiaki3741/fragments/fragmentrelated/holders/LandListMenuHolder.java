package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.view.Menu;
import android.view.MenuItem;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.enums.LandListActionState;
import com.mosc.simo.ptuxiaki3741.enums.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.enums.LandListNavigateStates;

public class LandListMenuHolder {
    private final OnNavigate onNavigate;
    private final OnUpdateState onUpdateState;
    private final OnAction onAction;
    private LandListMenuState state = LandListMenuState.NormalState;
    private Menu menu;

    public LandListMenuHolder(OnNavigate onNavigate,OnUpdateState onUpdateState,OnAction onAction){
        this.onNavigate = onNavigate;
        this.onUpdateState = onUpdateState;
        this.onAction = onAction;
    }

    public boolean menuItemClick(int itemId) {
        switch (itemId){
            case(R.id.menu_item_add):
                actionAddLand();
                return true;
            case(R.id.menu_item_delete):
                actionDeleteLand();
                return true;
            case(R.id.menu_item_export):
                actionExportLands();
                return true;
            case(R.id.menu_delete_action):
                actionDelete();
                return true;
            case(R.id.menu_export_action):
                actionExport();
                return true;
            case(R.id.menu_select_all_action):
                actionSelectAll();
                return true;
            default:
                return false;
        }
    }

    private void actionAddLand() {
        onNavigate.onNavigate(LandListNavigateStates.ToCreate);
    }
    private void actionDeleteLand() {
        setState(LandListMenuState.MultiDeleteState);
    }
    private void actionExportLands() {
        setState(LandListMenuState.MultiExportState);
    }
    private void actionDelete(){
        setAction(LandListActionState.DeleteAction);
    }
    private void actionExport(){
        setAction(LandListActionState.ExportAction);
    }
    private void actionSelectAll(){
        setAction(LandListActionState.SelectAllAction);
    }

    public LandListMenuState getState() {
        return state;
    }
    public void setState(LandListMenuState state) {
        this.state = state;
        onUpdateState.onUpdateState(state);
    }

    public void setAction(LandListActionState action) {
        onAction.onAction(action);
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

    public void setupMenu(LandListMenuState state) {
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

    public interface OnUpdateState{
        void onUpdateState(LandListMenuState state);
    }
    public interface OnAction{
        void onAction(LandListActionState state);
    }
    public interface OnNavigate{
        void onNavigate(LandListNavigateStates state);
    }
}
