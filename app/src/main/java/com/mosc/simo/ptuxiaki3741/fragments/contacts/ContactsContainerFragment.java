package com.mosc.simo.ptuxiaki3741.fragments.contacts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentContactsContainerBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.List;

public class ContactsContainerFragment extends Fragment implements FragmentBackPress {
    //fixme: recreate Contact list
    public static final String TAG = "ContactsContainerFragment";
    private FragmentContactsContainerBinding binding;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactsContainerBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewHolder();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initData() {

    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
                ActionBar actionBar = activity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.setTitle("");
                    actionBar.show();
                }
            }
        }
    }
    private void initFragment() {

    }
    private void initViewHolder(){
        if(getActivity() != null){
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getContacts().observe(getViewLifecycleOwner(),this::onDataRefresh);
        }
    }

    private void onDataRefresh(List<Contact> contacts) {

    }

}