package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainMenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG ="MenuFragment";

    private UserViewModel vmUsers;
    private LandViewModel vmLands;
    private List<User> friendRequests;
    private User currUser;

    private FragmentMenuMainBinding binding;
    private ActionBar actionBar;
    private Menu menu;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModels();
        initObservers();
        initFragment();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.request_menu, menu);
        this.menu = menu;
        updateMenu();
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_request) {
            toRequestMenu(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initData(){
        friendRequests = new ArrayList<>();
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
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }
    private void initFragment() {
        binding.btnLands.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnHistory.setOnClickListener(v -> toLandHistory(getActivity()));
        binding.btnContacts.setOnClickListener(v -> toUserContacts(getActivity()));
        binding.btnProfile.setOnClickListener(v -> toProfile(getActivity()));
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
            vmUsers.getFriendRequestList().observe(getViewLifecycleOwner(),this::onFriendRequestListUpdate);
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandUpdate);
        }
    }

    private void onCurrUserUpdate(User user) {
        currUser = user;
        if(currUser != null){
            if(actionBar != null){
                actionBar.setTitle(currUser.getUsername());
            }
        }else{
            toLogin(getActivity());
        }
    }
    private void onFriendRequestListUpdate(List<User> requests) {
        friendRequests.clear();
        if(requests != null)
            friendRequests.addAll(requests);

        updateMenu();
    }

    private void updateMenu() {
        if(menu != null && getContext() != null){
            MenuItem item = menu.findItem(R.id.menu_item_request);
            if(item != null){
                if(friendRequests.size() > 0){
                    item.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_add_request_notification));
                }else{
                    item.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_add_request));
                }
            }
        }
    }

    private void onLandUpdate(List<Land> lands) {
        //todo: change with google maps
        if(binding != null){
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
                        if(land.getData() != null){
                            if(land.getData().getBorder() != null){
                                areaSum = areaSum + MapUtil.area(land.getData().getBorder());
                            }
                        }
                    }
                }else{
                    for(Land land : lands){
                        if(land.getData() != null){
                            if(land.getData().getBorder() != null){
                                areaSum = areaSum + MapUtil.area(land.getData().getBorder());
                            }
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
    }

    public void toLandHistory(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToLandHistory);
            });
    }
    public void toListMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToListMenu);
            });
    }
    public void toProfile(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToUserProfile);
            });
    }
    public void toLogin(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToLogin);
            });
    }
    public void toUserContacts(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null)
                    nav.navigate(R.id.mainMenuToUserContacts);
            });
    }
    public void toRequestMenu(@Nullable Activity activity) {
        if(activity != null){
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MainMenuFragment);
                if(nav != null){
                    //todo: nav.navigate(R.id.mainMenuToUserRequests);
                }
            });
        }
    }
}