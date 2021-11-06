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
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.UserRequestAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserInboxRequestBinding;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserInboxRequestFragment extends Fragment implements SearchView.OnQueryTextListener {
    private FragmentUserInboxRequestBinding binding;

    private UserViewModel vmUsers;
    private UserRequestAdapter adapter;

    private List<User> displayData;
    private List<User> requests;
    private String lastQuery;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentUserInboxRequestBinding.inflate(inflater,container,false);
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
        inflater.inflate(R.menu.user_request_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuSearch = menu.findItem( R.id.menu_item_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        menuSearch.setOnActionExpandListener(new  MenuItem.OnActionExpandListener() {
            @Override public boolean onMenuItemActionExpand(MenuItem item) {
                for(int i = 0; i < menu.size();i++){
                    if(menu.getItem(i).getItemId() != R.id.menu_item_search)
                        menu.getItem(i).setVisible(false);
                }
                return true;
            }
            @Override public boolean onMenuItemActionCollapse(MenuItem item) {
                for(int i = 0; i < menu.size();i++){
                    if(menu.getItem(i).getItemId() != R.id.menu_item_search)
                        menu.getItem(i).setVisible(true);
                }
                return true;
            }
        });
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_refresh){
            refresh();
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

    private void initData(){
        lastQuery = "";
        requests = new ArrayList<>();
        displayData = new ArrayList<>();
        adapter = new UserRequestAdapter(displayData,
                this::onUserAcceptRequest,
                this::onUserDeclineRequest
        );
    }
    private void initActivity(){
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
            ActionBar actionBar = activity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.inbox_request_title));
                actionBar.show();
            }
        }
    }
    private void initViewHolder(){
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }
    private void initFragment(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvRequestList.setLayoutManager(layoutManager);
        binding.rvRequestList.setHasFixedSize(true);
        binding.rvRequestList.setAdapter(adapter);
        updateUi();
    }
    private void initObservers(){
        if(vmUsers != null){
            vmUsers.getReceivedRequestList().observe(getViewLifecycleOwner(),this::onUpdateRequests);
        }
    }

    private void search(String query){
        if(!query.isEmpty()){
            lastQuery = query;
            onUpdateData(queryList(query));
        }else{
            lastQuery = "";
            onUpdateData(requests);
        }
    }
    private void refresh() {
        search(lastQuery);
    }

    private List<User> queryList(String query) {
        List<User> res = new ArrayList<>();
        for(User request : requests){
            if(request.getUsername().toUpperCase().contains(query.toUpperCase()))
                res.add(request);
        }
        return res;
    }

    private void onUserAcceptRequest(User user){
        AsyncTask.execute(()->{
            if(user != null && vmUsers != null){
                boolean result = vmUsers.acceptRequest(user);
                String display;
                if(result){
                    display = getString(R.string.accept_request);
                }else{
                    display = getString(R.string.accept_request_error);
                }
                if(getActivity() != null)
                    getActivity().runOnUiThread(()->Toast.makeText(
                            getContext(),
                            display,
                            Toast.LENGTH_SHORT
                    ).show());
            }
        });
    }
    private void onUserDeclineRequest(User user){
        AsyncTask.execute(()->{
            if(user != null && vmUsers != null){
                boolean result = vmUsers.declineRequest(user);
                String display;
                if(result){
                    display = getString(R.string.decline_request);
                }else{
                    display = getString(R.string.decline_request_error);
                }
                if(getActivity() != null)
                    getActivity().runOnUiThread(()->Toast.makeText(
                            getContext(),
                            display,
                            Toast.LENGTH_SHORT
                    ).show());
            }
        });
    }

    private void onUpdateRequests(List<User> requests){
        this.requests.clear();
        if(requests != null){
            this.requests.addAll(requests);
        }
        search(lastQuery);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void onUpdateData(List<User> data){
        displayData.clear();
        displayData.addAll(data);
        adapter.notifyDataSetChanged();
        updateUi();
    }

    private void updateUi() {
        if(displayData.size()>0){
            binding.rvRequestList.setVisibility(View.VISIBLE);
            binding.tvRequestDisplay.setVisibility(View.GONE);
        }else{
            binding.tvRequestDisplay.setVisibility(View.VISIBLE);
            binding.rvRequestList.setVisibility(View.GONE);
        }
    }
}