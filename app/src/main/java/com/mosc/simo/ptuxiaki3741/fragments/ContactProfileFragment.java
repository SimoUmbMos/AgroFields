package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
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
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentContactProfileBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

public class ContactProfileFragment extends Fragment implements FragmentBackPress {
    private FragmentContactProfileBinding binding;
    private AlertDialog dialog;

    private UserViewModel vmUsers;
    private LandViewModel vmLands;
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

        binding.btnContactShareLand.setOnClickListener(v->toShareLandList());
    }

    private void toShareLandList() {
        toSharedLands(getActivity());
    }

    private void goBack(){
        if(getActivity() != null)
            getActivity().onBackPressed();
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
                    vmLands.removeAllSharedLandsWithUser(contact);
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

    public void toSharedLands(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ContactProfileFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.SHARE_LAND_ARG,contact);
                if(nav != null)
                    nav.navigate(R.id.contactProfileToShareLand,bundle);
            });
    }
}