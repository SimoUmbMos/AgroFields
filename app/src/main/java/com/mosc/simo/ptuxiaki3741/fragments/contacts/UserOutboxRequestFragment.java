package com.mosc.simo.ptuxiaki3741.fragments.contacts;

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
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.UserSendRequestAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserOutboxRequestBinding;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.UserRequest;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class UserOutboxRequestFragment extends Fragment implements SearchView.OnQueryTextListener {
    private FragmentUserOutboxRequestBinding binding;
    private ActionBar actionBar;
    private UserSendRequestAdapter adapter;
    private LinearLayoutManager layoutManager;

    private final List<User> requests = new ArrayList<>();
    private final List<UserRequest> displayData = new ArrayList<>();
    private UserViewModel vmUsers;

    private String lastQuery;
    private int searchPage,maxSearchPage;

    private void initData(){
        lastQuery = "";
        adapter = new UserSendRequestAdapter(
                displayData,
                this::onUserSendRequest,
                this::onDeleteUserRequest
        );
    }
    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                if(activity != null){
                    actionBar = activity.getSupportActionBar();
                    if(actionBar != null){
                        actionBar.setTitle(getString(R.string.outbox_request_title));
                        actionBar.show();
                    }
                }
            }
        }
    }
    private void initViewModel(){
        if(getActivity() != null)
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
    }
    private void initFragment(){
        layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvRequestList.setLayoutManager(layoutManager);
        binding.rvRequestList.setHasFixedSize(true);
        binding.rvRequestList.setAdapter(adapter);
        binding.rvRequestList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(searchPage < maxSearchPage){
                    if(layoutManager.findLastVisibleItemPosition() == displayData.size() - 1){
                        loadMoreData();
                    }
                }
            }
        });
        updateUi();
    }
    private void initObservers(){
        if(vmUsers != null)
            vmUsers.getSendedRequestList().observe(getViewLifecycleOwner(),this::onUpdateRequests);
    }

    private void onSearchUpdate(String query) {
        lastQuery = query;
        loadInitData();
    }
    private void onUpdateRequests(List<User> data){
        requests.clear();
        requests.addAll(data);
        refresh();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void setDataFromRequest() {
        displayData.clear();
        for(User temp : requests){
            displayData.add(new UserRequest(temp,true));
        }
        adapter.notifyDataSetChanged();
        updateUi();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void setData(List<User> data, boolean clearList) {
        if(clearList)
            displayData.clear();
        for(User temp : data){
            if(requests.contains(temp))
                displayData.add(new UserRequest(temp,true));
            else
                displayData.add(new UserRequest(temp,false));
        }
        adapter.notifyDataSetChanged();
        updateUi();
    }

    private void onUserSendRequest(User user){
        AsyncTask.execute(()->{
            if(user != null && vmUsers != null){
                UserFriendRequestStatus result = vmUsers.sendRequest(user);
                String display;
                switch (result){
                    case ACCEPTED:
                        display = getString(R.string.accept_request);
                        break;
                    case REQUESTED:
                        display = getString(R.string.request_sent);
                        break;
                    case REQUEST_FAILED:
                    default:
                        display = getString(R.string.request_send_error);
                        break;
                }
                if(getActivity() != null)
                    getActivity().runOnUiThread(()-> Toast.makeText(
                            getContext(),
                            display,
                            Toast.LENGTH_SHORT
                    ).show());
            }
        });
    }
    private void onDeleteUserRequest(User user){
        AsyncTask.execute(()->{
            if(vmUsers != null){
                final boolean r = vmUsers.deleteRequest(user);
                if(getActivity() != null)
                    getActivity().runOnUiThread(()-> {
                        String display;
                        if(r){
                            display = getString(R.string.request_deleted);
                        }else{
                            display = getString(R.string.request_send_error);
                        }
                        Toast.makeText(
                                getContext(),
                                display,
                                Toast.LENGTH_SHORT
                        ).show();
                    });
            }
        });
    }

    private void refresh(){
        loadInitData();
    }
    private void loadInitData() {
        searchPage = 1;
        maxSearchPage = 1;
        if(isValidSearch(lastQuery)){
            actionBar.setTitle(lastQuery);
            AsyncTask.execute(()->{
                List<User> data = new ArrayList<>();
                if (vmUsers != null) {
                    maxSearchPage = vmUsers.searchUserMaxPage(lastQuery);
                    data.addAll(vmUsers.searchUser(lastQuery, searchPage));
                }
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> setData(data, true));
            });
        }else{
            lastQuery = "";
            actionBar.setTitle(getString(R.string.outbox_request_title));
            setDataFromRequest();
        }
    }
    private void loadMoreData(){
        if(searchPage < maxSearchPage){
            searchPage++;
            AsyncTask.execute(()->{
                List<User> data = new ArrayList<>();
                if (vmUsers != null) {
                    if (searchPage <= maxSearchPage) {
                        data.addAll(vmUsers.searchUser(lastQuery, searchPage));
                    }
                }
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> setData(data, false));
            });
        }
    }

    private boolean isValidSearch(String query){
        if(query.trim().isEmpty())
            return false;
        return query.length()>3;
    }

    private void updateUi() {
        if(binding != null){
            if(displayData.size()>0){
                binding.rvRequestList.setVisibility(View.VISIBLE);
                binding.tvRequestDisplay.setVisibility(View.GONE);
            }else{
                binding.tvRequestDisplay.setVisibility(View.VISIBLE);
                binding.rvRequestList.setVisibility(View.GONE);
                if(lastQuery.isEmpty()){
                    binding.tvRequestDisplay.setText(getString(R.string.empty_user_outbox_request_list));
                }else{
                    binding.tvRequestDisplay.setText(getString(R.string.empty_user_outbox_request_search_list));
                }
            }
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentUserOutboxRequestBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewModel();
        initFragment();
        initObservers();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_contact_menu, menu);
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
    @Override public boolean onQueryTextSubmit(String query) {
        onSearchUpdate(query.trim());
        return true;
    }
    @Override public boolean onQueryTextChange(String query) {
        onSearchUpdate(query.trim());
        return true;
    }
}