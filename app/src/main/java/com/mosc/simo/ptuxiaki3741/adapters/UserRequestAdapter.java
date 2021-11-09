package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderUserReceivedRequestBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.RequestViewHolder> {
    private final List<User> data;
    private final ActionResult<User> onAcceptClick;
    private final ActionResult<User> onDeclineClick;

    public UserRequestAdapter(List<User> data,
                              ActionResult<User> onAcceptClick,
                              ActionResult<User> onDeclineClick){
        this.data = data;
        this.onAcceptClick = onAcceptClick;
        this.onDeclineClick = onDeclineClick;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_user_received_request,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        if(data != null){
            if(position<data.size()){
                User userRequest = data.get(position);
                holder.binding.tvUserName.setText(userRequest.getUsername());
                holder.binding.fabAccept.setOnClickListener(
                        v->onAcceptClick.onActionResult(userRequest)
                );
                holder.binding.fabDecline.setOnClickListener(
                        v->onDeclineClick.onActionResult(userRequest)
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

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        public final ViewHolderUserReceivedRequestBinding binding;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderUserReceivedRequestBinding.bind(itemView);
        }
    }
}
