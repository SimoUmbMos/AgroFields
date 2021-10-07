package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderShareLandCheckableBinding;
import com.mosc.simo.ptuxiaki3741.models.LandWithShare;

import java.util.List;

public class ShareLandAdapter extends RecyclerView.Adapter<ShareLandAdapter.ShareLandViewHolder>{
    private final List<LandWithShare> data;
    private final OnSharedLandCheckListener listener;

    public ShareLandAdapter(List<LandWithShare> data, OnSharedLandCheckListener listener){
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
    public void onBindViewHolder(@NonNull ShareLandViewHolder h, int pos) {
        if(pos < data.size()){
            LandWithShare entity = data.get(pos);
            h.binding.ctvSharedLandName.setChecked(entity.getSharedData() != null);
            h.binding.ctvSharedLandName.setText(entity.getData().getTitle());
            h.binding.ctvSharedLandName.setOnClickListener(v->
                    listener.onSharedLandCheck(pos, h.binding.ctvSharedLandName.isChecked())
            );
        }
    }

    @Override
    public int getItemCount() {
        if(data != null)
            return data.size();
        return 0;
    }

    protected static class ShareLandViewHolder extends RecyclerView.ViewHolder{
        public final ViewHolderShareLandCheckableBinding binding;
        public ShareLandViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderShareLandCheckableBinding.bind(view);
        }
    }

    public interface OnSharedLandCheckListener{
        void onSharedLandCheck(int pos, boolean wasChecked);
    }
}
