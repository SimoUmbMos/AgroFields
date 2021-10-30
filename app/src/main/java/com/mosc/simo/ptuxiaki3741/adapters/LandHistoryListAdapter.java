package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderHistoryBinding;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.view.HistoryEntryView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LandHistoryListAdapter extends RecyclerView.Adapter<LandHistoryListAdapter.ItemViewHolder>{
    private final List<LandHistory> data;
    private final List<User> users;
    private final String[] values;
    private final OnRecordClick onRecordClick;
    private final OnHeaderClick onHeaderClick;
    private final DateFormat dateFormat;
    public LandHistoryListAdapter(
            List<LandHistory> data,
            List<User> users,
            String[] values,
            OnHeaderClick onHeaderClick,
            OnRecordClick onRecordClick
    ){
        this.data = data;
        this.users = users;
        this.values = values;
        this.onRecordClick = onRecordClick;
        this.onHeaderClick = onHeaderClick;
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    }

    @NonNull @Override public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_holder_history,
                parent,
                false
        );
        return new ItemViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if(position < data.size()){
            if(data.get(position) != null){
                setupViewHolder(holder, position);
            }else{
                holder.binding.getRoot().setVisibility(View.GONE);
            }
        }
    }
    @Override public int getItemCount() {
        if(data != null)
            return data.size();
        return 0;
    }

    private void setupViewHolder(@NonNull ItemViewHolder holder, int position) {
        LandHistory item = data.get(position);
        holder.binding.tvLandTitle.setText(item.getTitle());
        if(item.isVisible()){
            holder.binding.tvLandTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_item_close,
                    0
            );
        }else{
            holder.binding.tvLandTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_item_open,
                    0
            );
        }
        holder.binding.tvLandTitle.setOnClickListener(v->onHeaderClick.onHeaderClick(position));
        holder.binding.tlHistoryRoot.removeAllViews();
        if(item.isVisible()){
            String username;
            String date;
            String action;
            for(LandDataRecord record : item.getData()){
                username = "";
                for(User user:users){
                    if(record.getUserID() == user.getId())
                        username = user.getUsername();
                }
                date = dateFormat.format(record.getDate());
                action = "";
                switch (record.getActionID()){
                    case CREATE:
                        action = values[0];
                        break;
                    case UPDATE:
                        action = values[1];
                        break;
                    case RESTORE:
                        action = values[2];
                        break;
                    case DELETE:
                        action = values[3];
                        break;
                }
                holder.binding.tlHistoryRoot.addView(new HistoryEntryView(
                        holder.binding.getRoot().getContext(),
                        username,
                        date,
                        action,
                        v->onRecordClick.onRecordClick(record)
                ));
            }
            holder.binding.tlHistoryRoot.setVisibility(View.VISIBLE);
        }else{
            holder.binding.tlHistoryRoot.setVisibility(View.GONE);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final ViewHolderHistoryBinding binding;
        public ItemViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderHistoryBinding.bind(view);
        }
    }

    public interface OnRecordClick{
        void onRecordClick(LandDataRecord record);
    }
    public interface OnHeaderClick{
        void onHeaderClick(int pos);
    }
}
