package com.mosc.simo.ptuxiaki3741.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
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
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserContactsBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class MenuContactsFragment
        extends Fragment
        implements FragmentBackPress, SearchView.OnQueryTextListener
{
    //todo: (idea) merge contact menu with send request and received request fragments w/ bottom navigation
    public static final String TAG = "UserContactsFragment";

    private SearchView searchView;
    private MenuItem itemRequest;
    private ActionBar actionBar;

    private List<User> friendRequests;
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
        inflater.inflate(R.menu.menu_contacts_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        itemRequest = menu.findItem(R.id.menu_item_request);
        if(itemRequest != null && getContext() != null){
            if(friendRequests.size() > 0){
                itemRequest.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_add_request_notification));
            }else{
                itemRequest.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_add_request));
            }
        }
        MenuItem menuSearch = menu.findItem( R.id.menu_item_search);
        searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(v -> isSearchOpen = true);
        searchView.setOnCloseListener(() -> {
            isSearchOpen = isSearching;
            return false;
        });
        menuSearch.setOnActionExpandListener(new  MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (itemRequest != null) {
                    itemRequest.setVisible(false);
                    binding.fabOutboxRequests.setVisibility(View.GONE);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (itemRequest != null) {
                    itemRequest.setVisible(true);
                    binding.fabOutboxRequests.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_request) {
            toUserInboxRequests(getActivity());
            return true;
        }
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
        friendRequests = new ArrayList<>();
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
        binding.fabOutboxRequests.setOnClickListener(v->toUserOutboxRequests(getActivity()));
    }
    private void initObservers() {
        if(vmUsers != null){
            vmUsers.getInboxRequestList().observe(getViewLifecycleOwner(),this::onFriendRequestListUpdate);
            vmUsers.getFriendList().observe(getViewLifecycleOwner(),this::onContactListUpdate);
        }
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
    private void onFriendRequestListUpdate(List<User> requests) {
        friendRequests.clear();
        if(requests != null)
            friendRequests.addAll(requests);
        if(getActivity() != null)
            getActivity().invalidateOptionsMenu();
    }
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
    private void onContactClick(User contact){
        toContactProfile(getActivity(),contact);
    }

    //ui
    @SuppressLint("NotifyDataSetChanged")
    private void viewUpdate(){
        boolean disableRv = false;
        if(isSearching){
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

    public void toContactProfile(@Nullable Activity activity, User contact) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuContactsFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.CONTACT_PROFILE_ARG,contact);
                if(nav != null)
                    nav.navigate(R.id.toProfileContact,bundle);
            });
    }
    public void toUserInboxRequests(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuContactsFragment);
                if(nav != null)
                    nav.navigate(R.id.toUserInboxRequests);
            });
    }
    public void toUserOutboxRequests(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.MenuContactsFragment);
                if(nav != null)
                    nav.navigate(R.id.toUserOutboxRequests);
            });
    }
}