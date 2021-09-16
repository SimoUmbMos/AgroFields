package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

    private SearchView searchView;
    private MenuItem menuRefresh;
    private ActionBar actionBar;

    private List<User> contactList;
    private List<User> displayData;
    private UserViewModel vmUsers;
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
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuSearch = menu.findItem( R.id.menu_item_search);
        menuRefresh = menu.findItem( R.id.menu_item_refresh);
        searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(v -> {
            isSearchOpen = true;
            menu.findItem(R.id.menu_item_refresh).setVisible(false);
        });
        searchView.setOnCloseListener(() -> {
            isSearchOpen = isSearching;
            menu.findItem(R.id.menu_item_refresh).setVisible(!isSearching);
            return false;
        });
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
        contactList = new ArrayList<>();
        displayData = new ArrayList<>();
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
        //todo: navigate to contact profile
    }

    //ui
    @SuppressLint("NotifyDataSetChanged")
    private void viewUpdate(){
        boolean disableRv = false;
        if(isSearching){
            if(menuRefresh != null)
                menuRefresh.setVisible(false);
            if(actionBar != null){
                actionBar.setTitle(lastQuery);
            }
            binding.tvContactAction.setVisibility(View.VISIBLE);
            if(displayData.size() > 0){
                String display;
                if(displayData.size()>1){
                    display = displayData.size()+" "+getString(R.string.list_results);
                }else{
                    display = displayData.size()+" "+getString(R.string.list_result);
                }
                binding.tvContactAction.setText(display);
            }else{
                binding.tvContactAction.setText(getString(R.string.empty_search));
                disableRv = true;
            }
        }else{
            if(menuRefresh != null)
                menuRefresh.setVisible(true);
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.contacts_title));
            }
            if(displayData.size() > 0){
                binding.tvContactAction.setVisibility(View.GONE);
            }else{
                binding.tvContactAction.setVisibility(View.VISIBLE);
                binding.tvContactAction.setText(getString(R.string.empty_list));
                disableRv = true;
            }
        }
        adapter.notifyDataSetChanged();
        if(disableRv)
            binding.rvContactList.setVisibility(View.GONE);
        else
            binding.rvContactList.setVisibility(View.VISIBLE);
    }
}