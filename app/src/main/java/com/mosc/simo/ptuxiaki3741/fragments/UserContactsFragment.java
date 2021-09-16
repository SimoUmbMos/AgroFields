package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.UserContactsAdapter;
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

    private UserViewModel vmUsers;
    private SearchView searchView;
    private ActionBar actionBar;

    private final List<User> contactList = new ArrayList<>();
    private final List<User> displayData = new ArrayList<>();
    private UserContactsAdapter adapter;
    private String lastQuery;
    private boolean isSearching,isSearchOpen;

    private FragmentUserContactsBinding binding;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentUserContactsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModels();
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
        searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_refresh)
            refresh();
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
        if(isSearchOpen){
            clearSearch();
            return false;
        }
        return true;
    }

    //init
    private void initData() {
        isSearching = false;
        isSearchOpen = false;
        lastQuery="";
        adapter=new UserContactsAdapter(displayData,this::onContactClick);
    }
    private void initActivity() {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
            activity.setOnBackPressed(this);
            actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.contacts_title));
                actionBar.show();
            }
        }
    }
    private void initViewModels() {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }
    private void initFragment() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvContactList.setLayoutManager(layoutManager);
        binding.rvContactList.setHasFixedSize(true);
        binding.rvContactList.setAdapter(adapter);
    }
    private void initObservers() {
        if(vmUsers != null){
            vmUsers.getFriendList().observe(getViewLifecycleOwner(),this::onContactListUpdate);
        }
    }

    private void refresh(){
        if(vmUsers != null)
            AsyncTask.execute(()->vmUsers.refreshLists());
    }

    //search methods
    private void search(String query){
        lastQuery=query.trim();
        displayData.clear();
        if(lastQuery.isEmpty()){
            isSearchOpen = false;
            isSearching = false;
            displayData.addAll(contactList);
        }else{
            isSearchOpen = true;
            isSearching = true;
            displayData.addAll(searchFriends());
        }
        viewUpdate();
    }
    private void clearSearch(){
        if(searchView != null){
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
        }
        isSearching = false;
        isSearchOpen = false;
        lastQuery = "";
        displayData.clear();
        displayData.addAll(contactList);
        viewUpdate();
    }
    private List<User> searchFriends(){
        List<User> result = new ArrayList<>();
        for(User friend : contactList){
            if(friend.getUsername().contains(lastQuery))
                result.add(friend);
        }
        return result;
    }

    //observers
    private void onContactListUpdate(List<User> users) {
        contactList.clear();
        if(users != null){
            contactList.addAll(users);
        }
        displayData.clear();
        if(isSearching){
            displayData.addAll(searchFriends());
        }else{
            displayData.addAll(contactList);
        }
        viewUpdate();
    }
    private void onContactClick(User user){

    }

    //ui
    @SuppressLint("NotifyDataSetChanged")
    private void viewUpdate(){
        if(actionBar != null){
            if(isSearching){
                actionBar.setTitle(lastQuery);
            }else{
                actionBar.setTitle(getString(R.string.contacts_title));
            }
        }
        boolean disableRv = false;
        if(displayData.size() > 0){
            if(isSearching){
                binding.tvContactAction.setVisibility(View.VISIBLE);
                String display;
                if(displayData.size()>1){
                    display = displayData.size()+" "+getString(R.string.list_results);
                }else{
                    display = displayData.size()+" "+getString(R.string.list_result);
                }
                binding.tvContactAction.setText(display);
            }else{
                binding.tvContactAction.setVisibility(View.GONE);
            }
        }else{
            disableRv = true;
            binding.tvContactAction.setVisibility(View.VISIBLE);
            if(isSearching){
                binding.tvContactAction.setText(getString(R.string.empty_search));
            }else{
                binding.tvContactAction.setText(getString(R.string.empty_list));
            }
        }
        adapter.notifyDataSetChanged();
        if(disableRv)
            binding.rvContactList.setVisibility(View.GONE);
        else
            binding.rvContactList.setVisibility(View.VISIBLE);
    }
}