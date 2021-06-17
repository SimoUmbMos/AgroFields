package com.mosc.simo.ptuxiaki3741.fragments.landinfo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.User;
import com.mosc.simo.ptuxiaki3741.fragments.landinfo.holders.LandInfoHolder;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;

public class LandInfoFragment extends Fragment implements FragmentBackPress, LandInfoHolder.LandInfoHolderActions {
    private static final String TAG = "LandInfoFragment";
    private LandInfoHolder landInfoHolder;
    private Land land;
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_land_info, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view,getArguments());
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

    private void init(View view, Bundle arguments) {
        user = LandInfoFragmentArgs.fromBundle(arguments).getUser();
        land = LandInfoFragmentArgs.fromBundle(arguments).getLand();
        if(land.getId() == -1 && land.getCreator_id() == -1 && land.getTitle().equals("")){
            land = null;
        }
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if (activity != null) {
            activity.setOnBackPressed(this);
            actionBar = activity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
        }
        initHolders(view);
    }

    private void initHolders(View view) {
        landInfoHolder = new LandInfoHolder(view, getResources(), land, this);
    }

    @Override
    public void onSubmit(String landName) {
        landName = landName.replaceAll(
                "[^a-zA-Z0-9]", " ");
        landName = landName.trim().replaceAll(" +", " ");
        if(!landName.isEmpty()){
            submit(landName.trim());
        }
    }

    @Override
    public void onCancel() {
        finish();
    }

    private void submit(String landName) {
        if(land != null){
            land.setTitle(landName);
        }else{
            land = new Land(user.getId(),landName);
        }
        debugData(land);
        finish();
    }

    private void debugData(Land land) {
        Log.d(TAG, "debug land data: creator_id = " + land.getCreator_id());
        Log.d(TAG, "debug land data: land_name = " + land.getTitle());
    }

    private void finish() {
        if(getActivity() != null)
            getActivity().onBackPressed();
    }
}