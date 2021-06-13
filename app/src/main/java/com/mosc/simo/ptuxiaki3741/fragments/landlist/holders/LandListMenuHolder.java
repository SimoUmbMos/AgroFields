package com.mosc.simo.ptuxiaki3741.fragments.landlist.holders;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListActionState;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListNavigateStates;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListNavigator;

public class LandListMenuHolder {
    private final OnNavigate onNavigate;
    private final OnUpdateState onUpdateState;
    private final OnAction onAction;
    private LandListMenuState state = LandListMenuState.NormalState;

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
