package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderUserContactBinding;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public class UserContactsAdapter extends RecyclerView.Adapter<UserContactsAdapter.ViewHolder> {
    private final List<User> data;
    private final OnContactClick onContactClick;

    public UserContactsAdapter(List<User> data, OnContactClick onContactClick){
        this.onContactClick = onContactClick;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_user_contact,parent,false);
        return new UserContactsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(data != null){
            if(position<data.size()){
                User user = data.get(position);
                holder.binding.tvUserName.setText(user.getUsername());
                holder.binding.tvUserName.setOnClickListener(
                        v->onContactClick.onContactClick(user)
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        if(data != null)
            return data.size();
        return 0;
    }

    public static class ViewHolder  extends RecyclerView.ViewHolder{
        public ViewHolderUserContactBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderUserContactBinding.bind(itemView);
        }
    }

    public interface OnContactClick{
        void onContactClick(User user);
    }
}
