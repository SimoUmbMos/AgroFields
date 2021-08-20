package com.mosc.simo.ptuxiaki3741.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LandHistoryListViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LandHistoryMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LandHistoryMenuFragment";
    private LandHistoryListViewHolder viewHolder;
    private LandViewModel vmLands;

    private final List<Object> data = new ArrayList<>();
    private final List<Land> lands = new ArrayList<>();
    private final List<LandRecord> history = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_land_history_menu, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initViewModel();
        initFragment(view);
        initObservers();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initActivity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.hide();
        }
    }
    private void initViewModel() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
        }
    }
    private void initObservers(){
        if(vmLands != null){
            onLoadingStatus(true);
            onLandUpdate(vmLands.getLandsList());
            onLandHistoryUpdate(vmLands.getLandsHistoryList());
            onLoadingStatus(false);
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
            vmLands.getLandsHistory().observe(getViewLifecycleOwner(),this::onLandHistoryUpdate);
            vmLands.isLoadingLands().observe(getViewLifecycleOwner(),this::onLoadingStatus);
        }
    }
    private void initFragment(View view) {
        viewHolder = new LandHistoryListViewHolder(
                view,
                data,
                this::onItemClick,
                this::onItemLongClick,
                getResources()
        );
    }

    private void onItemClick(int position) {
        //todo onItemClick
    }
    private void onItemLongClick(int position) {
        onItemClick(position);
    }

    private void onLandUpdate(List<Land> lands) {
        this.lands.clear();
        this.lands.addAll(lands);
        getLandsWithHistory();
        viewHolder.update();
    }
    private void onLandHistoryUpdate(List<LandRecord> history) {
        this.history.clear();
        this.history.addAll(history);
        getLandsWithHistory();
        viewHolder.update();
    }
    private void onLoadingStatus(boolean isLoading) {
        viewHolder.isLoading(isLoading);
    }

    public void getLandsWithHistory() {
        List<Land> landList = new ArrayList<>(lands);
        List<LandRecord> historyList = new ArrayList<>(history);

        data.clear();

        for (Iterator<Land> l = landList.iterator(); l.hasNext();) {
            Land land = l.next();
            if(land.getData() != null){
                data.add(land);
                for (Iterator<LandRecord> h = historyList.iterator(); h.hasNext();) {
                    LandRecord landRecord = h.next();
                    if (landRecord.getLandData() != null) {
                        if(landRecord.getLandData().getLandID() == land.getData().getId()){
                            data.add(landRecord);
                            h.remove();
                        }
                    }else{
                        h.remove();
                    }
                }
            }
            l.remove();
        }

        if(historyList.size() > 0){
            data.add(getString(R.string.delete_lands_title));
            for (Iterator<LandRecord> j = historyList.iterator(); j.hasNext();) {
                LandRecord landRecord = j.next();
                data.add(landRecord);
                j.remove();
            }
        }

        debugList();
    }

    private void debugList() {
        Log.d(TAG, "debugList:");
        for(Object obj:data){
            if(obj instanceof Land){
                LandData data = ((Land)obj).getData();
                Log.d(TAG, "Land:");
                Log.d(TAG, data.getId()+" "+data.getCreator_id()+" "+data.getTitle()
                );
            }else if(obj instanceof LandRecord){
                LandDataRecord data = ((LandRecord)obj).getLandData();
                String action;
                switch (data.getActionID()){
                    case CREATE:
                        action = "CREATE";
                        break;
                    case UPDATE:
                        action = "UPDATE";
                        break;
                    case RESTORE:
                        action = "RESTORE";
                        break;
                    case DELETE:
                        action = "DELETE";
                        break;
                    default:
                        action = "UNKNOWN";
                        break;
                }
                Log.d(TAG,"LandRecord:");
                Log.d(TAG,data.getId()+" "+data.getUserID()+" "+action);
                Log.d(TAG,data.getLandID()+" "+data.getLandCreatorID()+" "+data.getLandTitle());
            }else if(obj instanceof String){
                String string = (String)obj;
                Log.d(TAG, "String:");
                Log.d(TAG, string);
            }else{
                Log.d(TAG, "unknown object on data list");
            }
        }
    }
}