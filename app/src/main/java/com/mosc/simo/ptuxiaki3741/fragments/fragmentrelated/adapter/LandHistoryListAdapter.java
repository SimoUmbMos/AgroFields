package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;

import java.util.List;


public class LandHistoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int
            TYPE_HEADER = 0,
            TYPE_ITEM = 1,
            TYPE_TEXT = 2;
    private final List<Object> data;
    private final OnItemClick onItemClick;
    private final OnItemLongClick onItemLongClick;

    public LandHistoryListAdapter(
            List<Object> list,
            OnItemClick onItemClick,
            OnItemLongClick onItemLongClick
    ){
        this.data = list;
        this.onItemClick = onItemClick;
        this.onItemLongClick = onItemLongClick;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view;
        switch (viewType){
            case TYPE_HEADER:
                view = LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.view_land_history_header,
                        parent,
                        false
                );
                return new HeaderViewHolder(view);
            case TYPE_ITEM:
                view = LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.view_land_history_item,
                        parent,
                        false
                );
                return new ItemViewHolder(view);
            case TYPE_TEXT:
            default:
                view = LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.view_land_history_text,
                        parent,
                        false
                );
                return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        if(position < data.size()){
            Object obj = data.get(position);
            if(obj instanceof Land){
                HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
                headerHolder.setupViewHolder((Land) obj);
                headerHolder.v.setOnClickListener(v->onItemClick.onItemClick(position));
                headerHolder.v.setOnLongClickListener(v->{
                    onItemLongClick.onItemLongClick(position);
                    return true;
                });
            }else if(obj instanceof LandRecord) {
                ItemViewHolder itemHolder = (ItemViewHolder) holder;
                itemHolder.setupViewHolder((LandRecord) obj);
                itemHolder.v.setOnClickListener(v->onItemClick.onItemClick(position));
                itemHolder.v.setOnLongClickListener(v->{
                    onItemLongClick.onItemLongClick(position);
                    return true;
                });
            }else{
                TextViewHolder textHolder = (TextViewHolder) holder;
                if(obj instanceof String){
                    textHolder.setupViewHolder((String) obj);
                }else{
                    textHolder.setupViewHolder(null);
                }
                textHolder.v.setOnClickListener(v->onItemClick.onItemClick(position));
                textHolder.v.setOnLongClickListener(v->{
                    onItemLongClick.onItemLongClick(position);
                    return true;
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if(data != null)
            return data.size();
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(data != null){
            if (position < data.size()) {
                Object object = data.get(position);
                if(object instanceof Land){
                    return TYPE_HEADER;
                }else if(object instanceof LandRecord){
                    return TYPE_ITEM;
                }
            }
        }
        return TYPE_TEXT;
    }

    protected static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final View v;
        public HeaderViewHolder(@NonNull View view) {
            super(view);
            this.v = view;
            //todo create views for HeaderViewHolder
        }
        public void setupViewHolder(Land header){
            if(header!=null){
                if(header.getData()!=null){
                    //todo setup views of HeaderViewHolder
                }else{
                    v.setVisibility(View.GONE);
                }
            }else{
                v.setVisibility(View.GONE);
            }
        }
    }
    protected static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final View v;
        public ItemViewHolder(@NonNull View view) {
            super(view);
            this.v = view;
            //todo create views for ItemViewHolder
        }
        public void setupViewHolder(LandRecord item){
            if(item!=null){
                if(item.getLandData()!=null){
                    //todo setup views of ItemViewHolder
                }else{
                    v.setVisibility(View.GONE);
                }
            }else{
                v.setVisibility(View.GONE);
            }
        }
    }
    protected static class TextViewHolder extends RecyclerView.ViewHolder {
        public final View v;
        public TextViewHolder(@NonNull View view) {
            super(view);
            this.v = view;
            //todo create views for TextViewHolder
        }
        public void setupViewHolder(String text){
            if(text != null){
                //todo setup views of TextViewHolder
            }else{
                v.setVisibility(View.GONE);
            }
        }
    }

    public interface OnItemClick{
        void onItemClick(int position);
    }
    public interface OnItemLongClick{
        void onItemLongClick(int position);
    }
}
