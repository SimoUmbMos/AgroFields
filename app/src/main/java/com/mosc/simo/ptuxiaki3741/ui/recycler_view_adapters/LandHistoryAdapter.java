package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderHistoryBinding;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.ui.views.HistoryItemView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LandHistoryAdapter extends RecyclerView.Adapter<LandHistoryAdapter.HistoryItemViewHolder>{
    private static final String TAG = "LandHistoryAdapter";
    private final List<LandHistory> data;
    private final String[] values;
    private final ActionResult<Integer> onHistoryClick;
    private final ActionResult<LandHistoryRecord> onRecordClick;
    private final DateFormat dateFormat;
    public LandHistoryAdapter(
            String[] values,
            ActionResult<Integer> onHistoryClick,
            ActionResult<LandHistoryRecord> onRecordClick
    ){
        this.data = new ArrayList<>();
        this.values = values;

        this.onHistoryClick = onHistoryClick;
        this.onRecordClick = onRecordClick;

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
        return data.size();
    }

    public void saveData(List<LandHistory> data){
        Log.d(TAG, "saveData: called");
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new HistoriesDiffUtil(this.data, data));
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }

    private void setupViewHolder(@NonNull HistoryItemViewHolder holder, int position) {
        LandHistory item = data.get(position);

        holder.binding.getRoot().setOnClickListener(v->onHistoryClick.onActionResult(position));

        String title = item.getTitle();
        holder.binding.tvHistoryTitle.setText(title);

        holder.binding.clHistoryData.removeAllViews();
        if(item.isVisible()){
            holder.binding.tvHistoryTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_item_close,
                    0
            );
            holder.binding.clHistoryData.setVisibility(View.VISIBLE);
            HistoryItemView view;
            String action, date;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 2, 0, 2);
            for(LandHistoryRecord entry:item.getData()){
                date = dateFormat.format(entry.getLandData().getDate());
                switch (entry.getLandData().getActionID()){
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
                view = new HistoryItemView(
                        holder.binding.getRoot().getContext(),
                        date,
                        action
                );
                view.setOnClickListener(v->onRecordClick.onActionResult(entry));
                holder.binding.clHistoryData.addView(view,layoutParams);
            }
        }else{
            holder.binding.tvHistoryTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_item_open,
                    0
            );
            holder.binding.clHistoryData.setVisibility(View.GONE);
        }
    }
    public static class HistoryItemViewHolder extends RecyclerView.ViewHolder {
        public final ViewHolderHistoryBinding binding;
        public HistoryItemViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderHistoryBinding.bind(view);
        }
    }
    private static class HistoriesDiffUtil extends DiffUtil.Callback {
        private final List<LandHistory> oldData;
        private final List<LandHistory> newData;

        public HistoriesDiffUtil(List<LandHistory> oldData, List<LandHistory> newData) {
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
                LandHistory oldHistory = oldData.get(oldItemPosition);
                LandHistory newHistory = newData.get(newItemPosition);
                if(oldHistory == null || newHistory == null) return false;
                if(oldHistory.isVisible() != newHistory.isVisible()) return false;
                if(oldHistory.getData().size() != newHistory.getData().size()) return false;
                for(int i = 0 ; i < oldHistory.getData().size(); i++){
                    LandHistoryRecord oldRecord = oldHistory.getData().get(i);
                    LandHistoryRecord newRecord = newHistory.getData().get(i);
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
                }
            }catch (Exception e){
                return false;
            }
            return true;
        }
    }
}
