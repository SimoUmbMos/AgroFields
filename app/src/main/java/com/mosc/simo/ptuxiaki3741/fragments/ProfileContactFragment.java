package com.mosc.simo.ptuxiaki3741.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.ShareLandAdapter;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentContactProfileBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class ProfileContactFragment extends Fragment implements FragmentBackPress {
    //todo: (idea) make memo based on user
    private FragmentContactProfileBinding binding;
    private AlertDialog dialog;

    private UserViewModel vmUsers;
    private LandViewModel vmLands;
    private ShareLandAdapter adapter;

    private final List<Land> myLands = new ArrayList<>();
    private final List<Land> mySharedLands = new ArrayList<>();
    private final List<Land> data = new ArrayList<>();
    private User contact;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentContactProfileBinding.inflate(inflater, container, false);
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
        inflater.inflate(R.menu.contact_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_remove_contact){
            removeContact();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initData(){
        if (getArguments() != null) {
            if(getArguments().containsKey(AppValues.CONTACT_PROFILE_ARG)){
                contact = getArguments().getParcelable(AppValues.CONTACT_PROFILE_ARG);
            }
        }
        if(contact == null)
            goBack();
    }
    private void initActivity(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(contact.getUsername());
                actionBar.show();
            }
        }
    }
    private void initViewModel(){
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }
    private void initFragment(){
        binding.etContactProfileEmail.setFocusable(false);
        binding.etContactProfileEmail.setClickable(true);
        binding.etContactProfileEmail.setInputType(InputType.TYPE_NULL);
        binding.etContactProfilePhone.setFocusable(false);
        binding.etContactProfilePhone.setClickable(true);
        binding.etContactProfilePhone.setInputType(InputType.TYPE_NULL);

        String tempEmail = contact.getEmail().trim().replaceAll("\\s+","");
        if(!tempEmail.isEmpty()){
            binding.etContactProfileEmail.setText(tempEmail);
            binding.etContactProfileEmail.setOnLongClickListener(v->{
                if(getContext() != null){
                    UIUtil.setClipboard(getContext(),tempEmail);
                    Toast.makeText(getContext(), getString(R.string.clipboard_message), Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            binding.etContactProfileEmail.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, tempEmail);
                startActivity(intent);
            });
        }
        String tempPhone = contact.getPhone().trim().replaceAll("\\s+","");
        if(UIUtil.isValidMobileNo(tempPhone)){
            binding.etContactProfilePhone.setText(tempPhone);
            binding.etContactProfilePhone.setOnLongClickListener(v->{
                if(getContext() != null){
                    UIUtil.setClipboard(getContext(),tempPhone);
                    Toast.makeText(getContext(), getString(R.string.clipboard_message), Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            binding.etContactProfilePhone.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+tempPhone));
                startActivity(intent);
            });
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        adapter = new ShareLandAdapter(data,this::onLandClick);
        binding.rcSharedLand.setLayoutManager(layoutManager);
        binding.rcSharedLand.setHasFixedSize(true);
        binding.rcSharedLand.setAdapter(adapter);
    }
    private void initObservers() {
        if(vmLands != null){
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onMyLandsUpdate);
            vmLands.getSharedLands().observe(getViewLifecycleOwner(),this::onMySharedLandsUpdate);
        }
    }

    private void onMyLandsUpdate(List<Land> myLands){
        onDataUpdateStart(this.myLands);
        this.myLands.clear();
        this.myLands.addAll(myLands);
        onDataUpdateEnd(this.myLands);
    }
    private void onMySharedLandsUpdate(List<Land> mySharedLands){
        onDataUpdateStart(this.mySharedLands);
        this.mySharedLands.clear();
        this.mySharedLands.addAll(mySharedLands);
        onDataUpdateEnd(this.mySharedLands);
    }
    private void onDataUpdateStart(List<Land> lands){
        for(Land land:lands){
            for(int i = 0;i<data.size();i++){
                if(data.get(i).getData().getId() == land.getData().getId()){
                    data.remove(i);
                    adapter.notifyItemRemoved(i);
                    break;
                }
            }
        }
    }
    private void onDataUpdateEnd(List<Land> lands){
        for(Land land:lands){
            if(land.getPerm().isAdmin()){
                data.add(land);
                adapter.notifyItemInserted(data.indexOf(land));
            }
        }
        updateUi();
    }
    private void onLandClick(Land land){
        toSelectedLand(land);
    }
    private void updateUi() {
        if(data.size()>0){
            binding.tvSharedLandDisplay.setVisibility(View.GONE);
            binding.rcSharedLand.setVisibility(View.VISIBLE);
        }else{
            binding.tvSharedLandDisplay.setVisibility(View.VISIBLE);
            binding.rcSharedLand.setVisibility(View.GONE);
        }
    }

    private void goBack(){
        if(getActivity() != null)
            getActivity().onBackPressed();
    }
    private void toSelectedLand(Land land) {
        if(getActivity() != null)
            getActivity().runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ProfileContactFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.SHARE_LAND_DATA_ARG,land);
                bundle.putParcelable(AppValues.SHARE_LAND_USER_ARG,contact);
                if(nav != null)
                    nav.navigate(R.id.toShareLand,bundle);
            });
    }

    private void removeContact(){
        if(getContext() != null){
            if(dialog != null){
                if(dialog.isShowing())
                    dialog.dismiss();
                dialog = null;
            }
            String message =
                    getString(R.string.contact_delete_message) + ": " + contact.getUsername();
            dialog = new MaterialAlertDialogBuilder(getContext(), R.style.ErrorMaterialAlertDialog)
                    .setTitle(getString(R.string.contact_delete_title))
                    .setMessage(message)
                    .setNeutralButton(getString(R.string.cancel), (d, w) -> d.cancel())
                    .setPositiveButton(getString(R.string.accept), (d, w) -> removeContactAction())
                    .create();
            dialog.show();
        }
    }
    private void removeContactAction(){
        if(vmUsers != null){
            AsyncTask.execute(()->{
                String display;
                if(vmUsers.deleteFriend(contact)){
                    vmLands.removeAllLandPermissions(contact);
                    display = getString(R.string.deleted_contact);
                }else{
                    display = getString(R.string.deleted_contact_error);
                }
                if(getActivity() != null){
                    getActivity().runOnUiThread(()-> {
                        Toast.makeText(
                                getActivity(),
                                display,
                                Toast.LENGTH_SHORT
                        ).show();
                        goBack();
                    });
                }
            });
        }
    }
}