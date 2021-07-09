package com.mosc.simo.ptuxiaki3741;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandData;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.holders.LandInfoHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;

public class LandInfoFragment extends Fragment implements FragmentBackPress, LandInfoHolder.LandInfoHolderActions {
    public static final String TAG = "LandInfoFragment";
    private Land land;
    private User currUser;
    private boolean isNew = false;

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
        initData(arguments);
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if (activity != null) {
            activity.setOnBackPressed(this);
            UserViewModel vmUsers = new ViewModelProvider(activity).get(UserViewModel.class);
            currUser = vmUsers.getCurrUser().getValue();
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            actionBar = activity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.hide();
        }
        initHolders(view);
    }
    private void initData(Bundle arguments) {
        land = LandInfoFragmentArgs.fromBundle(arguments).getLand();
        Land tempLand = new Land();
        if(Land.equals(land,tempLand)){
            land = null;
            isNew = true;
        }else{
            isNew = false;
        }
    }
    private void initHolders(View view) {
        LandInfoHolder landInfoHolder = new LandInfoHolder(view, getResources(), this);
        landInfoHolder.init(land);
    }

    @Override
    public void onSubmit(String landName) {
        closeKeyboard();
        landName = landName.replaceAll(
                "[^a-zA-Z0-9]", " ");
        landName = landName.trim().replaceAll(" +", " ");
        if(!landName.isEmpty()){
            if(isNew){
                submitAdd(landName);
            }else{
                submitEdit(landName);
            }
        }
    }
    @Override
    public void onCancel() {
        closeKeyboard();
        finish();
    }
    private void onCurrUserUpdate(User user) {
        currUser = user;
    }

    private void closeKeyboard() {
        if(getActivity() != null){
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private void submitAdd(String landName) {
        if(currUser != null){
            LandData landData = new LandData(currUser.getId(),landName);
            navigate(toMap(new Land(landData)));
        }
    }
    private void submitEdit(String landName) {
        land.getLandData().setTitle(landName);
        navigate(toMap(land));
    }
    private void finish() {
        if(getActivity() != null)
            getActivity().onBackPressed();
    }
    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.landInfoFragment)
            navController.navigate(action);
    }
    private NavDirections toMap(Land land){
        return  LandInfoFragmentDirections.farmEdited(land);
    }
}