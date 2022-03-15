package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderHistorySelectedBinding;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LandHistorySelectedAdapter extends RecyclerView.Adapter<LandHistorySelectedAdapter.HistoryItemSelectedViewHolder>{
    private final List<LandHistoryRecord> data;
    private final String[] values;
    private final ActionResult<LandHistoryRecord> onClick;
    private final DateFormat dateFormat;
    public LandHistorySelectedAdapter(
            String[] values,
            ActionResult<LandHistoryRecord> onClick
    ){
        this.data = new ArrayList<>();
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
        return data.size();
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
            case BULK_EDITED:
                action = values[4];
                break;
            case DELETE:
                action = values[5];
                break;
            case ZONE_ADDED:
                action = values[6];
                break;
            case ZONE_UPDATED:
                action = values[7];
                break;
            case ZONE_IMPORTED:
                action = values[8];
                break;
            case ZONE_REMOVED:
                action = values[9];
                break;
            default:
                action = "";
                break;
        }
        holder.binding.hiData.setAction(action);

    }
    public void saveData(List<LandHistoryRecord> data){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new HistoryRecordDiffUtil(this.data, data));
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }
    public static class HistoryItemSelectedViewHolder extends RecyclerView.ViewHolder {
        public final ViewHolderHistorySelectedBinding binding;
        public HistoryItemSelectedViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderHistorySelectedBinding.bind(view);
        }
    }

    private static class HistoryRecordDiffUtil extends DiffUtil.Callback {
        private final List<LandHistoryRecord> oldData;
        private final List<LandHistoryRecord> newData;

        public HistoryRecordDiffUtil(List<LandHistoryRecord> oldData, List<LandHistoryRecord> newData) {
            this.oldData = oldData;
            this.newData = newData;
        }

        @Override
        public int getOldListSize() {
            return oldData.size();
        }

        @Override
        public int getNewListSize() {
            return newData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            try{
                return oldData.get(oldItemPosition).getLandData().getId() == newData.get(newItemPosition).getLandData().getId() &&
                        oldData.get(oldItemPosition).getLandData().getSnapshot() == newData.get(newItemPosition).getLandData().getSnapshot();
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            try{
                LandHistoryRecord oldRecord = oldData.get(oldItemPosition);
                LandHistoryRecord newRecord = newData.get(newItemPosition);
                if(oldRecord.getLandData().getId() != newRecord.getLandData().getId())
                    return false;
                if(oldRecord.getLandData().getSnapshot() != newRecord.getLandData().getSnapshot())
                    return false;
                if(oldRecord.getLandData().getLandID() != newRecord.getLandData().getLandID())
                    return false;
                if(!oldRecord.getLandData().getLandTitle().equals(newRecord.getLandData().getLandTitle()))
                    return false;
                if(!oldRecord.getLandData().getLandColor().toString().equals(newRecord.getLandData().getLandColor().toString()))
                    return false;
                if(oldRecord.getLandData().getActionID() != newRecord.getLandData().getActionID())
                    return false;
                if(!oldRecord.getLandData().getDate().equals(newRecord.getLandData().getDate()))
                    return false;
                if(!ListUtils.arraysMatch(oldRecord.getLandData().getBorder(), newRecord.getLandData().getBorder()))
                    return false;
                if(!ListUtils.arraysMatch(oldRecord.getLandData().getHoles(), newRecord.getLandData().getHoles()))
                    return false;
                if(!ListUtils.arraysMatch(oldRecord.getLandZonesData(), newRecord.getLandZonesData()))
                    return false;
            }catch (Exception e){
                return false;
            }
            return true;
        }
    }
}
