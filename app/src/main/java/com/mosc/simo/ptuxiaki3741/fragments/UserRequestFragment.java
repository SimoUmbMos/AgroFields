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
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.UserRequestAdapter;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserRequestBinding;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.UserRequest;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserRequestFragment
        extends Fragment
        implements SearchView.OnQueryTextListener, FragmentBackPress {
    public static final String TAG = "UserRequestFragment";

    private FragmentUserRequestBinding binding;
    private SearchView searchView;
    private ActionBar actionBar;

    private UserViewModel vmUsers;
    private UserRequestAdapter adapter;
    private List<User> requests;
    private List<UserRequest> displayData;

    private boolean isSearching,isSearchOpen;
    private String lastSearchText;

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
        return true;
    }
    @Override public boolean onQueryTextSubmit(String query) {
        search(query.trim());
        return true;
    }
    @Override public boolean onBackPressed() {
        if(isSearchOpen){
            clearSearch(true);
            return false;
        }
        return true;
    }

    private void initData(){
        requests = new ArrayList<>();
        displayData = new ArrayList<>();

        adapter = new UserRequestAdapter(displayData,
                this::onUserSendRequest,
                this::onUserAcceptRequest,
                this::onUserDeclineRequest
        );
        isSearching = false;
        isSearchOpen= false;
        lastSearchText = "";
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
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }
    private void initFragment(){
        binding.tvRequestCount.setText(getString(R.string.loading_list));
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvRequestList.setLayoutManager(layoutManager);
        binding.rvRequestList.setHasFixedSize(true);
        binding.rvRequestList.setAdapter(adapter);
    }
    private void initObservers(){
        if(vmUsers != null){
            if(vmUsers.getFriendRequestList().getValue() != null){
                requests.addAll(vmUsers.getFriendRequestList().getValue());
            }
            updateUi();
            vmUsers.getFriendRequestList().observe(getViewLifecycleOwner(),this::updateRequests);
        }
    }

    private void refresh(){
        if(vmUsers != null)
            AsyncTask.execute(()->vmUsers.refreshLists());
    }

    private void search(String query){
        if(query.isEmpty()){
            isSearching = false;
            isSearchOpen = false;
        }else{
            isSearchOpen = true;
            isSearching = query.length() > 3;
        }

        displayData.clear();
        if(isSearching){
            lastSearchText = query;
            AsyncTask.execute(()->{
                if(vmUsers != null){
                    List<User> temps  = new ArrayList<>(vmUsers.searchUser(lastSearchText));
                    for(User temp : temps){
                        displayData.add(new UserRequest(temp,false));
                    }
                }
                if(getActivity() != null)
                    getActivity().runOnUiThread(this::updateUi);
            });
        }else{
            clearSearch(false);
        }
    }
    private void clearSearch(boolean clearView){
        if(searchView != null && clearView){
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
        }
        isSearching = false;
        isSearchOpen = false;
        lastSearchText = "";
        displayData.clear();
        for(User request : requests){
            displayData.add(new UserRequest(request,true));
        }
        updateUi();
    }

    private void onUserSendRequest(User user){
        AsyncTask.execute(()->{
            if(user != null && vmUsers != null){
                UserFriendRequestStatus result = vmUsers.sendFriendRequest(user);
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
                    getActivity().runOnUiThread(()->{
                        Toast.makeText(
                                getContext(),
                                display,
                                Toast.LENGTH_SHORT
                        ).show();
                        search(lastSearchText);
                    });
            }
        });
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

    private void updateRequests(List<User> r){
        requests.clear();
        if(r != null){
            requests.addAll(r);
        }
        if(!isSearching){
            displayData.clear();
            for(User request : requests){
                displayData.add(new UserRequest(request,true));
            }
            updateUi();
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void updateUi(){
        String display;
        boolean disableRv = false;
        if(isSearching){
            if(actionBar != null){
                actionBar.setTitle(lastSearchText);
            }
            if(displayData.size() == 0){
                display = getString(R.string.no_result);
                disableRv = true;
            }else if(displayData.size() == 1){
                display = displayData.size()+getString(R.string.result);
            }else{
                display = displayData.size()+getString(R.string.results);
            }

        }else{
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.request_title));
            }

            if(requests.size() == 0){
                display = getString(R.string.no_requests);
                disableRv = true;
            }else if(requests.size() == 1){
                display = requests.size()+" "+getString(R.string.request);
            }else{
                display = requests.size()+" "+getString(R.string.requests);
            }

        }
        binding.tvRequestCount.setText(display);
        adapter.notifyDataSetChanged();
        if(disableRv)
            binding.rvRequestList.setVisibility(View.GONE);
        else
            binding.rvRequestList.setVisibility(View.VISIBLE);
    }
}