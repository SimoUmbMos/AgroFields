package com.mosc.simo.ptuxiaki3741.fragments;

import android.os.AsyncTask;
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
        implements FragmentBackPress, SearchView.OnQueryTextListener
{
    public static final String TAG = "UserContactsFragment";

    private final List<User> friendList = new ArrayList<>();
    private final List<User> data = new ArrayList<>();
    private int pagesCount;
    private UserViewModel vmUsers;
    private boolean isSearching;

    private FragmentUserContactsBinding binding;
    private ActionBar actionBar;

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
        if(getActivity() != null){
            String search = query.trim();
            Log.d(TAG, "onQueryTextChange: "+search);
            if(!search.isEmpty()){
                //todo show result
                AsyncTask.execute(()->{
                    int pageNumber = vmUsers.getSearchPageCount(query);
                    getActivity().runOnUiThread(()->searchUpdate(pageNumber,query));
                });
            }else{
                getActivity().runOnUiThread(()->searchUpdate(0,query));
            }
        }
        return false;
    }
    @Override public boolean onQueryTextSubmit(String query) {
        return false;
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initActivity() {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
            activity.setOnBackPressed(this);
            actionBar = activity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.show();
        }
    }
    private void initData() {
        isSearching = false;
        pagesCount = 0;
    }
    private void initFragment() {

    }
    private void initViewModels() {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::userUpdate);
        }
    }

    private void userUpdate(User user) {
        friendList.clear();
        if(user != null){
            AsyncTask.execute(()->{
                friendList.addAll(vmUsers.getFriends());
                viewUpdate();
            });
        }else{
            viewUpdate();
        }
    }
    private void searchUpdate(int pageNumber, String query) {
        data.clear();
        if(query.length() > 3){
            pagesCount = pageNumber;
            isSearching = true;
            if(pagesCount > 0){
                    asyncGetFirstDataFromSearch(query);
            }else{
                viewUpdate();
            }
        }else{
            pagesCount = 0;
            isSearching = false;
            data.addAll(friendList);
            viewUpdate();
        }
    }
    private void viewUpdate(){
        //notifyDataChanged
        if(isSearching){
            binding.tvContactAction.setText(getString(R.string.empty_search));
        }else{
            binding.tvContactAction.setText(getString(R.string.empty_list));
        }
        String display;
        if(data.size()>1){
            display = data.size()+" "+getString(R.string.list_results);
            binding.tvContactAction.setText(display);
        }else if(data.size() == 1){
            display = data.size()+" "+getString(R.string.list_result);
            binding.tvContactAction.setText(display);
        }else{
            binding.tvContactAction.setVisibility(View.VISIBLE);
        }
    }
    private void asyncGetFirstDataFromSearch(String query) {
        AsyncTask.execute(()->{
            data.clear();
            data.addAll(vmUsers.searchUser(query,0));
            viewUpdate();
        });
    }
}