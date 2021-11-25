package com.mosc.simo.ptuxiaki3741.fragments.contacts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
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
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public class MenuContactsFragment extends Fragment implements SearchView.OnQueryTextListener {
    public static final String TAG = "UserContactsFragment";

    private ActionBar actionBar;

    private List<User> contactList;
    private List<User> displayData;
    private UserViewModel vmUsers;
    private UserContactsAdapter adapter;

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
        MenuItem menuSearch = menu.findItem( R.id.menu_item_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(v -> {
        });
        searchView.setOnCloseListener(() -> false);
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

    //init
    private void initData() {
        contactList = new ArrayList<>();
        displayData = new ArrayList<>();
        isSearching = false;
        lastQuery="";
        adapter=new UserContactsAdapter(displayData,this::onContactClick);
    }
    private void initActivity() {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
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

    //search methods
    private void search(String query){
        lastQuery=query.trim();
        displayData.clear();
        if(lastQuery.isEmpty()){
            isSearching = false;
            displayData.addAll(contactList);
        }else{
            isSearching = true;
            displayData.addAll(searchFriends());
        }
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
        Fragment fragment = getParentFragment();
        while(fragment != null){
            if(fragment.getClass() == ContactsContainerFragment.class){
                break;
            }else{
                fragment=fragment.getParentFragment();
            }
        }
        if(fragment == null) return;
        if(contact == null) return;
        if(activity == null) return;
        activity.runOnUiThread(()-> {
            if(getParentFragment() != null){
                NavController nav = UIUtil.getNavController(
                        getParentFragment().getParentFragment(),
                        R.id.ContactsContainerFragment
                );
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.CONTACT_PROFILE_ARG,contact);
                if(nav != null){
                    nav.navigate(R.id.toProfileContact,bundle);
                }
            }
        });
    }
}