package com.mosc.simo.ptuxiaki3741.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserRequestBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserRequestFragment
        extends Fragment
        implements SearchView.OnQueryTextListener, FragmentBackPress {
    public static final String TAG = "UserRequestFragment";

    private FragmentUserRequestBinding binding;
    private ActionBar actionBar;

    private List<User> requests;
    private boolean isSearching;
    private String searchText;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentUserRequestBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewHolder();
        initFragment();
        initObservers();
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

    private void initData(){
        isSearching = false;
        requests = new ArrayList<>();
    }
    private void initActivity(){
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
            activity.setOnBackPressed(this);
            actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.request_title));
                actionBar.show();
            }
        }
    }
    private void initViewHolder(){

    }
    private void initFragment(){
        binding.tvRequestCount.setText(getString(R.string.loading_list));
    }
    private void initObservers(){

    }

    private void search(String query){
        if(query.isEmpty()){
            isSearching = false;
        }else{
            isSearching = query.length() > 3;
        }
        if(isSearching){
            searchText = query;
        }else{
            searchText = "";
        }
        updateUi();
    }

    private void onRequestUpdate(List<User> r){
        requests.clear();
        if(r != null){
            requests.addAll(r);
        }
    }
    private void updateUi(){
        if(isSearching){
            if(actionBar != null){
                actionBar.setTitle(searchText);
            }
            //todo: search new user
        }else{
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.request_title));
            }
            String display;
            if(requests.size() == 0){
                display = getString(R.string.no_requests);
            }else if(requests.size() == 1){
                display = requests.size()+getString(R.string.request);
            }else{
                display = requests.size()+getString(R.string.requests);
            }
            binding.tvRequestCount.setText(display);
        }
    }
}