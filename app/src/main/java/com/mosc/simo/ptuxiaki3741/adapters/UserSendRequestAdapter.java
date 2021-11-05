package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderUserRequestBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.UserRequest;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public class UserSendRequestAdapter extends RecyclerView.Adapter<UserSendRequestAdapter.RequestViewHolder> {
    private final List<UserRequest> data;
    private final ActionResult<User> onAcceptClick;
    private final ActionResult<User> onDeclineClick;

    public UserSendRequestAdapter(List<UserRequest> data,
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
                .inflate(R.layout.view_holder_user_request,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        if(data != null){
            if(position<data.size()){
                UserRequest userRequest = data.get(position);
                holder.binding.tvUserName.setText(userRequest.getUser().getUsername());
                holder.binding.fabAccept.setOnClickListener(
                        v->onAcceptClick.onActionResult(userRequest.getUser())
                );
                holder.binding.fabDecline.setOnClickListener(
                        v->onDeclineClick.onActionResult(userRequest.getUser())
                );
                if(userRequest.isRequest()){
                    holder.binding.fabAccept.setVisibility(View.GONE);
                    holder.binding.fabDecline.setVisibility(View.VISIBLE);
                }else{
                    holder.binding.fabAccept.setVisibility(View.VISIBLE);
                    holder.binding.fabDecline.setVisibility(View.GONE);
                }
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
        public final ViewHolderUserRequestBinding binding;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderUserRequestBinding.bind(itemView);
        }
    }
}
