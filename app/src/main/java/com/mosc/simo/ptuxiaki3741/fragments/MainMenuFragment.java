package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;

import java.util.List;
import java.util.Locale;

public class MainMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG ="MenuFragment";

    private UserViewModel vmUsers;
    private LandViewModel vmLands;
    private User currUser;

    private FragmentMenuMainBinding binding;
    private ActionBar actionBar;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initViewModels();
        initObservers();
        initFragment();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initActivity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        actionBar = null;
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.show();
        }
    }
    private void initFragment() {
        binding.btnMainMenuList.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnMainMenuHistory.setOnClickListener(v -> toLandHistory(getActivity()));
        binding.btnMainMenuProfile.setOnClickListener(v -> toProfile(getActivity()));
        binding.btnMainMenuContacts.setOnClickListener(v -> toUserContacts(getActivity()));
    }
    private void initViewModels() {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
        }
    }
    private void initObservers() {
        if(vmUsers != null){
            currUser = vmUsers.getCurrUser().getValue();
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
        }
    }

    private void onCurrUserUpdate(User user) {
        currUser = user;
        if(currUser != null){
            actionBar.setTitle(currUser.getUsername());
            AsyncTask.execute(()->{
                String display = String.valueOf(vmUsers.getFriends().size());
                if(getActivity() != null)
                    getActivity().runOnUiThread(()-> binding.
                            tvMainMenuContactsNumber.setText(display)
                    );
            });
        }else{
            toLogin(getActivity());
        }
    }
    private void onLandUpdate(List<Land> lands) {
        if(lands != null){
            double areaSum = 0;
            int ownedLands = 0, sharedLands = 0;
            if(currUser != null){
                for(Land land : lands){
                    if(land.getData()!=null){
                        if(land.getData().getCreator_id() == currUser.getId()){
                            ownedLands++;
                        }else{
                            sharedLands++;
                        }
                    }
                    if(land.getBorder() != null){
                        areaSum = areaSum + MapUtil.area(LandUtil.getLatLngPoints(land));
                    }
                }
            }else{
                for(Land land : lands){
                    if(land.getBorder() != null){
                        areaSum = areaSum + MapUtil.area(LandUtil.getLatLngPoints(land));
                    }
                }
            }
            String areaDisplay;
            if(areaSum >= 1000000){
                areaSum = areaSum / 1000000;
                areaDisplay = String.format(Locale.getDefault(),"%.0f", areaSum)+" km²";
            }else{
                areaDisplay = String.format(Locale.getDefault(),"%.1f", areaSum)+" m²";
            }
            binding.tvMainMenuLandNumber.setText(String.valueOf(lands.size()));
            binding.tvMainMenuLandArea.setText(areaDisplay);
            binding.tvMainMenuOwnedLands.setText(String.valueOf(ownedLands));
            binding.tvMainMenuSharedLands.setText(String.valueOf(sharedLands));
        }else{
            binding.tvMainMenuLandNumber.setText("0 m²");
            binding.tvMainMenuLandArea.setText("0");
            binding.tvMainMenuOwnedLands.setText("0");
            binding.tvMainMenuSharedLands.setText("0");
        }
    }

    private NavController getNavController(){
        NavController navController = NavHostFragment.findNavController(this);
        if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.MainMenuFragment)
            return navController;
        return null;
    }
    public void toLandHistory(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = getNavController();
                if(nav != null)
                    nav.navigate(R.id.mainMenuToLandHistory);
            });
    }
    public void toListMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = getNavController();
                if(nav != null)
                    nav.navigate(R.id.mainMenuToListMenu);
            });
    }
    public void toProfile(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = getNavController();
                if(nav != null)
                    nav.navigate(R.id.mainMenuToUserProfile);
            });
    }
    public void toLogin(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = getNavController();
                if(nav != null)
                    nav.navigate(R.id.mainMenuToLogin);
            });
    }
    public void toUserContacts(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = getNavController();
                if(nav != null)
                    nav.navigate(R.id.mainMenuToUserContacts);
            });
    }
}