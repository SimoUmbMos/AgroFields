package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.LandHistoryList;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class LandHistoryListAdapter extends RecyclerView.Adapter<LandHistoryListAdapter.ItemViewHolder>{
    private static final int TextViewMargin = 8;
    private final List<LandHistoryList> data;
    private final OnRecordClick onRecordClick;

    public LandHistoryListAdapter(List<LandHistoryList> list, OnRecordClick onRecordClick){
        this.data = list;
        this.onRecordClick = onRecordClick;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.view_land_history_item,
                parent,
                false
        );
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if(position < data.size()){
            holder.setupViewHolder(data.get(position),onRecordClick);
        }
    }

    @Override
    public int getItemCount() {
        if(data != null)
            return data.size();
        return 0;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final View v;
        public final TextView tvLandTitle;
        public final LinearLayout llHistoryRoot;
        public ItemViewHolder(@NonNull View view) {
            super(view);
            this.v = view;
            tvLandTitle = view.findViewById(R.id.tvLandTitle);
            llHistoryRoot = view.findViewById(R.id.llHistoryRoot);
            llHistoryRoot.setVisibility(View.GONE);
        }
        public void setupViewHolder(LandHistoryList item, OnRecordClick onRecordClick){
            if(item!=null){
                if(item.getLand()!=null){
                    setupItemData(item,onRecordClick);
                }else{
                    v.setVisibility(View.GONE);
                }
            }else{
                v.setVisibility(View.GONE);
            }
        }
        public void setupItemData(LandHistoryList item, OnRecordClick onRecordClick) {
            String title = item.getLand().getData().getTitle();
            if(item.getLandRecords().size()>0){
                if(
                        item.getLandRecords().get(item.getLandRecords().size()-1)
                            .getLandData().getActionID() == LandDBAction.DELETE
                ){
                    title = title + " - Deleted";
                }
            }
            tvLandTitle.setText(title);
            tvLandTitle.setOnClickListener(v->toggleList());
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault());
            for(LandRecord record : item.getLandRecords()){
                TextView textView = new TextView(v.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0,TextViewMargin,0,TextViewMargin);
                textView.setLayoutParams(params);
                String action;
                switch (record.getLandData().getActionID()){
                    case CREATE:
                        action = "Created";
                        break;
                    case UPDATE:
                        action = "Updated";
                        break;
                    case RESTORE:
                        action = "Restore";
                        break;
                    case DELETE:
                        action = "Deleted";
                        break;
                    default:
                        action = "";
                        break;
                }
                String display =
                        dateFormat.format(record.getLandData().getDate()) + ": " +
                                action + " " +
                                record.getLandData().getLandTitle();
                textView.setText(display);
                textView.setOnClickListener(v->onRecordClick.onRecordClick(record));
                llHistoryRoot.addView(textView);
            }
        }
        public void toggleList() {
            if(llHistoryRoot.getVisibility() == View.VISIBLE){
                llHistoryRoot.setVisibility(View.GONE);
            }else{
                llHistoryRoot.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnRecordClick{
        void onRecordClick(LandRecord record);
    }
}
