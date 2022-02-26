package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderHistorySelectedBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.LandHistoryRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LandHistorySelectedAdapter extends RecyclerView.Adapter<LandHistorySelectedAdapter.HistoryItemSelectedViewHolder>{
    private final List<LandHistoryRecord> data;
    private final String[] values;
    private final ActionResult<LandHistoryRecord> onClick;
    private final DateFormat dateFormat;
    public LandHistorySelectedAdapter(
            List<LandHistoryRecord> data,
            String[] values,
            ActionResult<LandHistoryRecord> onClick
    ){
        this.data = data;
        this.values = values;
        this.onClick = onClick;
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    }

    @NonNull @Override public HistoryItemSelectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_holder_history_selected,
                parent,
                false
        );
        return new HistoryItemSelectedViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull HistoryItemSelectedViewHolder holder, int position) {
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

    private void setupViewHolder(@NonNull HistoryItemSelectedViewHolder holder, int position) {
        LandHistoryRecord item = data.get(position);
        holder.binding.getRoot().setOnClickListener(v->onClick.onActionResult(item));

        String date = dateFormat.format(item.getLandData().getDate());
        holder.binding.hiData.setDate(date);

        String action;
        switch (item.getLandData().getActionID()){
            case CREATE:
                action = values[0];
                break;
            case UPDATE:
                action = values[1];
                break;
            case RESTORE:
                action = values[2];
                break;
            case IMPORTED:
                action = values[3];
                break;
            case DELETE:
                action = values[4];
                break;
            case ZONE_ADDED:
                action = values[5];
                break;
            case ZONE_UPDATED:
                action = values[6];
                break;
            case ZONE_IMPORTED:
                action = values[7];
                break;
            case ZONE_REMOVED:
                action = values[8];
                break;
            default:
                action = "";
                break;
        }
        holder.binding.hiData.setAction(action);

    }
    public static class HistoryItemSelectedViewHolder extends RecyclerView.ViewHolder {
        public final ViewHolderHistorySelectedBinding binding;
        public HistoryItemSelectedViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderHistorySelectedBinding.bind(view);
        }
    }
}
