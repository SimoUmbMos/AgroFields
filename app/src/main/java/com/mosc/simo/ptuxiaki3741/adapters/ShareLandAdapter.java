package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderShareLandCheckableBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.Land;

import java.util.List;

public class ShareLandAdapter extends RecyclerView.Adapter<ShareLandAdapter.ShareLandViewHolder>{
    private final List<Land> data;
    private final ActionResult<Land> listener;

    public ShareLandAdapter(List<Land> data, ActionResult<Land> listener){
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShareLandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_holder_share_land_checkable,
                parent,
                false
        );
        return new ShareLandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareLandViewHolder holder, int pos) {
        if(pos < data.size()){
            Land entity = data.get(pos);
            holder.bind(entity,listener);
        }
    }

    @Override
    public int getItemCount() {
        if(data != null)
            return data.size();
        return 0;
    }

    protected static class ShareLandViewHolder extends RecyclerView.ViewHolder{
        private final ViewHolderShareLandCheckableBinding binding;
        public ShareLandViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderShareLandCheckableBinding.bind(view);
        }
        public void bind(Land land, ActionResult<Land> listener){
            binding.tvSharedLandName.setText(land.getData().getTitle());
            binding.tvSharedLandName.setOnClickListener(v->
                    listener.onActionResult(land)
            );
        }
    }
}
