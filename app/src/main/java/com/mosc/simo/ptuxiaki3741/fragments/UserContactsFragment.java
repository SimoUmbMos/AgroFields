package com.mosc.simo.ptuxiaki3741.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserContactsBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserContactsFragment
        extends Fragment
        implements FragmentBackPress, SearchView.OnQueryTextListener {
    public static final String TAG = "UserContactsFragment";

    private final List<User> friendList = new ArrayList<>();
    private final List<User> data = new ArrayList<>();
    private String lastQuery;
    private boolean isSearching;

    private FragmentUserContactsBinding binding;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentUserContactsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initData();
        initFragment();
        initViewModels();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem menuSearch = menu.findItem( R.id.menu_item_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onQueryTextChange(String query) {
        search(query.trim());
        return true;
    }
    @Override public boolean onQueryTextSubmit(String query) {
        return true;
    }
    @Override public boolean onBackPressed() {
        if(isSearching){
            search("");
            return false;
        }
        return true;
    }

    //init
    private void initActivity() {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
            activity.setOnBackPressed(this);
            ActionBar actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.contacts_title));
                actionBar.show();
            }
        }
    }
    private void initData() {
        isSearching = false;
        lastQuery="";
    }
    private void initFragment() {

    }
    private void initViewModels() {
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getFriendList().observe(getViewLifecycleOwner(),this::friendListUpdate);
        }
    }

    //search methods
    private void search(String query){
        lastQuery=query;
        data.clear();
        if(lastQuery.isEmpty()){
            isSearching = false;
            data.addAll(friendList);
        }else{
            isSearching = true;
            data.addAll(searchFriends());
        }
        viewUpdate();
    }
    private List<User> searchFriends(){
        List<User> result = new ArrayList<>();
        for(User friend : friendList){
            if(friend.getUsername().contains(lastQuery))
                result.add(friend);
        }
        return result;
    }

    //observers
    private void friendListUpdate(List<User> users) {
        friendList.clear();
        if(users != null){
            friendList.addAll(users);
        }
        data.clear();
        if(isSearching){
            data.addAll(searchFriends());
        }else{
            data.addAll(friendList);
        }
        viewUpdate();
    }

    //ui
    private void viewUpdate(){
        if(data.size() > 0){
            String display;
            if(data.size()>1){
                display = data.size()+" "+getString(R.string.list_results);
            }else{
                display = data.size()+" "+getString(R.string.list_result);
            }
            binding.tvContactAction.setText(display);
        }else{
            if(isSearching){
                binding.tvContactAction.setText(getString(R.string.empty_search));
            }else{
                binding.tvContactAction.setText(getString(R.string.empty_list));
            }
        }
    }
}