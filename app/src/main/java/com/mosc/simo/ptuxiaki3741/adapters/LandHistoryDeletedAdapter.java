package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderHistoryDeletedBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.views.HistoryItemView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LandHistoryDeletedAdapter extends RecyclerView.Adapter<LandHistoryDeletedAdapter.HistoryItemDeletedViewHolder>{
    private final List<LandHistory> data;
    private final String[] values;
    private final ActionResult<LandHistory> onHistoryClick;
    private final ActionResult<LandDataRecord> onRecordClick;
    private final DateFormat dateFormat;
    public LandHistoryDeletedAdapter(
            List<LandHistory> data,
            String[] values,
            ActionResult<LandHistory> onHistoryClick,
            ActionResult<LandDataRecord> onRecordClick
    ){
        this.data = data;
        this.values = values;

        this.onHistoryClick = onHistoryClick;
        this.onRecordClick = onRecordClick;

        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    }

    @NonNull @Override public HistoryItemDeletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_holder_history_deleted,
                parent,
                false
        );
        return new HistoryItemDeletedViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull HistoryItemDeletedViewHolder holder, int position) {
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

    private void setupViewHolder(@NonNull HistoryItemDeletedViewHolder holder, int position) {
        LandHistory item = data.get(position);

        holder.binding.getRoot().setOnClickListener(v->onHistoryClick.onActionResult(item));

        String title = item.getLandData().getTitle() +
                " #"+ item.getLandData().getId();
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
            for(LandDataRecord entry:item.getData()){
                date = dateFormat.format(entry.getDate());
                switch (entry.getActionID()){
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
    public static class HistoryItemDeletedViewHolder extends RecyclerView.ViewHolder {
        public final ViewHolderHistoryDeletedBinding binding;
        public HistoryItemDeletedViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderHistoryDeletedBinding.bind(view);
        }
    }
}
