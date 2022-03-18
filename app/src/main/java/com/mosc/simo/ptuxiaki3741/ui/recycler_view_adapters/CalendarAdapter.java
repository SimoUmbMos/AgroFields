package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderCalendarDateBinding;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.ui.views.CalendarEventView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private final LinkedHashMap<LocalDate, List<CalendarEntity>> data;
    private final ActionResult<LocalDate> onDateClick;
    private final ActionResult<CalendarNotification> onEventClick;

    public CalendarAdapter(
            ActionResult<LocalDate> onDateClick,
            ActionResult<CalendarNotification> onEventClick
    ){
        this.data = new LinkedHashMap<>();
        this.onDateClick = onDateClick;
        this.onEventClick = onEventClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_holder_calendar_date,
                parent,
                false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position > -1 && position < data.size()){
            LocalDate date = (LocalDate) data.keySet().toArray()[position];
            List<CalendarEntity> events = data.getOrDefault(date,null);
            if(events != null){
                holder.show(date, events, onDateClick, onEventClick);
            }else{
                holder.hide();
            }
        }else{
            holder.hide();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void saveData(LinkedHashMap<LocalDate, List<CalendarEntity>> data){

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CalendarDiffUtil(this.data, data));
        this.data.clear();
        this.data.putAll(data);
        diffResult.dispatchUpdatesTo(this);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewHolderCalendarDateBinding binding;
        public ViewHolder(@NonNull View view) {
            super(view);
            binding = ViewHolderCalendarDateBinding.bind(view);
        }

        public void hide() {
            binding.getRoot().setVisibility(View.GONE);
            binding.tvDayName.setText("");
            binding.tvDayNumber.setText("");
            binding.llEventsContainer.removeAllViews();
            binding.getRoot().setOnClickListener(null);
        }
        public void show(
                LocalDate d,
                List<CalendarEntity> e,
                ActionResult<LocalDate> dc,
                ActionResult<CalendarNotification> ec
        ) {
            binding.getRoot().setVisibility(View.VISIBLE);
            binding.getRoot().setOnClickListener(v->dc.onActionResult(d));
            binding.tvDayName.setText(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            String dateString = d.getDayOfMonth()+"/"+d.getMonthValue();
            binding.tvDayNumber.setText(dateString);
            binding.llEventsContainer.removeAllViews();
            for(CalendarEntity event : e){
                CalendarEventView eventView = new CalendarEventView(binding.getRoot().getContext());
                eventView.setEvent(
                        event.getCategory().getName(),
                        event.getCategory().getColorData(),
                        event.getNotification().toString()
                );
                eventView.setOnClick(v->ec.onActionResult(event.getNotification()));
                binding.llEventsContainer.addView(eventView);
            }
        }
    }

    private static class CalendarDiffUtil extends DiffUtil.Callback {
        private final LinkedHashMap<LocalDate, List<CalendarEntity>> oldData;
        private final LinkedHashMap<LocalDate, List<CalendarEntity>> newData;

        public CalendarDiffUtil(
                LinkedHashMap<LocalDate, List<CalendarEntity>> oldData,
                LinkedHashMap<LocalDate, List<CalendarEntity>> newData
        ){
            this.oldData = oldData;
            this.newData = newData;
        }

        @Override
        public int getOldListSize() {
            return oldData.keySet().size();
        }

        @Override
        public int getNewListSize() {
            return newData.keySet().size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            LocalDate oldDataKey = new ArrayList<>(oldData.keySet()).get(oldItemPosition);
            LocalDate newDataKey = new ArrayList<>(newData.keySet()).get(newItemPosition);
            return oldDataKey.equals(newDataKey);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            try{
                LocalDate oldDataKey = new ArrayList<>(oldData.keySet()).get(oldItemPosition);
                LocalDate newDataKey = new ArrayList<>(newData.keySet()).get(newItemPosition);
                List<CalendarEntity> oldDataValues = oldData.get(oldDataKey);
                List<CalendarEntity> newDataValues = newData.get(newDataKey);
                if(oldDataValues == null || newDataValues == null)
                    return false;
                if(oldDataValues.size() != newDataValues.size())
                    return false;
                if(!ListUtils.arraysMatch(oldDataValues,newDataValues))
                    return false;
            }catch (Exception e){
                return false;
            }
            return true;
        }
    }
}
