package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.databinding.ViewLandHistoryItemBinding;
import com.mosc.simo.ptuxiaki3741.models.LandHistoryList;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LandHistoryListAdapter extends RecyclerView.Adapter<LandHistoryListAdapter.ItemViewHolder>{
    private static final int TextViewMargin = 8;
    private final DateFormat dateFormat;
    private final List<LandHistoryList> data;
    private final OnRecordClick onRecordClick;
    private final OnHeaderClick onHeaderClick;
    private final String[] values;
    public LandHistoryListAdapter(
            List<LandHistoryList> list,
            String[] values,
            OnHeaderClick onHeaderClick,
            OnRecordClick onRecordClick
    ){
        this.data = list;
        this.values = values;
        this.onRecordClick = onRecordClick;
        this.onHeaderClick = onHeaderClick;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    }

    @NonNull @Override public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_land_history_item,
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
        LandHistoryList item = data.get(position);
        String title = item.getLand().getData().getTitle()+
                " #"+
                EncryptUtil.convert4digit(item.getLand().getData().getId());
        if(item.getLandRecords().get(item.getLandRecords().size()-1).getLandData()
                .getActionID() == LandDBAction.DELETE
        )
            title = title + " - " +values[3];
        holder.binding.tvLandTitle.setText(title);
        holder.binding.tlHistoryRoot.removeAllViews();
        for(LandRecord record : item.getLandRecords()){
            TableRow tr = new TableRow(holder.binding.getRoot().getContext());
            TableLayout.LayoutParams params= new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0,TextViewMargin,0,TextViewMargin);
            tr.setLayoutParams(params);
            tr.setWeightSum(2);

            TextView tv1 = new TextView(holder.binding.getRoot().getContext());
            TextView tv2 = new TextView(holder.binding.getRoot().getContext());
            TableRow.LayoutParams params1 = new TableRow.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            );
            TableRow.LayoutParams params2 = new TableRow.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            );
            params1.weight = 1;
            params2.weight = 1;
            tv1.setLayoutParams(params1);
            tv2.setLayoutParams(params2);

            tv1.setText(dateFormat.format(record.getLandData().getDate()));
            String action;
            switch (record.getLandData().getActionID()){
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
            tv2.setText(action);

            tr.addView(tv1);
            tr.addView(tv2);
            tr.setOnClickListener(v->onRecordClick.onRecordClick(record));
            holder.binding.tlHistoryRoot.addView(tr);
        }
        if(item.isVisible()){
            holder.binding.tlHistoryRoot.setVisibility(View.VISIBLE);
            holder.binding.tvLandTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu_close, 0);
        }else{
            holder.binding.tlHistoryRoot.setVisibility(View.GONE);
            holder.binding.tvLandTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu_open, 0);
        }
        holder.binding.tvLandTitle.setOnClickListener(v->onHeaderClick.onHeaderClick(position));
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final ViewLandHistoryItemBinding binding;
        public ItemViewHolder(@NonNull View view) {
            super(view);
            binding = ViewLandHistoryItemBinding.bind(view);
        }
    }

    public interface OnRecordClick{
        void onRecordClick(LandRecord record);
    }
    public interface OnHeaderClick{
        void onHeaderClick(int pos);
    }
}
