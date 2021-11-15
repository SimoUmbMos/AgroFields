package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderHistoryBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LandHistoryListAdapter extends RecyclerView.Adapter<LandHistoryListAdapter.HistoryItemViewHolder>{
    private final List<LandDataRecord> data;
    private final List<User> users;
    private final String[] values;
    private final ActionResult<LandDataRecord> onClick;
    private final DateFormat dateFormat;
    public LandHistoryListAdapter(
            List<LandDataRecord> data,
            List<User> users,
            String[] values,
            ActionResult<LandDataRecord> onClick
    ){
        this.data = data;
        this.users = users;
        this.values = values;
        this.onClick = onClick;
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    }

    @NonNull @Override public HistoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_holder_history,
                parent,
                false
        );
        return new HistoryItemViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull HistoryItemViewHolder holder, int position) {
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

    private void setupViewHolder(@NonNull HistoryItemViewHolder holder, int position) {
        LandDataRecord item = data.get(position);
        holder.binding.getRoot().setOnClickListener(v->onClick.onActionResult(item));

        String username = "";
        for(User user:users){
            if(item.getUserID() == user.getId())
                username = user.getUsername();
        }
        holder.binding.tvHistoryEntryUser.setText(username);

        String date = dateFormat.format(item.getDate());
        holder.binding.tvHistoryEntryDate.setText(date);

        String action = "";
        switch (item.getActionID()){
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
        holder.binding.tvHistoryEntryAction.setText(action);

    }
    public static class HistoryItemViewHolder extends RecyclerView.ViewHolder {
        public final ViewHolderHistoryBinding binding;
        public HistoryItemViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderHistoryBinding.bind(view);
        }
    }
}
