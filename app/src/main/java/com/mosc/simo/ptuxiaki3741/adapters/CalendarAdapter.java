package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderCalendarDateBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.views.CalendarEventView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private final LinkedHashMap<LocalDate, List<CalendarNotification>> data;
    private final String[] typesString;
    private final Integer[] typesColor;
    private final ActionResult<LocalDate> onDateClick;
    private final ActionResult<CalendarNotification> onEventClick;

    public CalendarAdapter(
            LinkedHashMap<LocalDate, List<CalendarNotification>> data,
            String[] typesString,
            Integer[] typesColor,
            ActionResult<LocalDate> onDateClick,
            ActionResult<CalendarNotification> onEventClick
    ){
        this.data = data;
        this.typesString = typesString;
        this.typesColor = typesColor;
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
            List<CalendarNotification> events = data.getOrDefault(date,null);
            if(events != null){
                holder.show(date, typesString, typesColor, events, onDateClick, onEventClick);
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
                String[] typesString,
                Integer[] typesColor,
                List<CalendarNotification> e,
                ActionResult<LocalDate> dc,
                ActionResult<CalendarNotification> ec
        ) {
            binding.getRoot().setVisibility(View.VISIBLE);
            binding.getRoot().setOnClickListener(v->dc.onActionResult(d));
            binding.tvDayName.setText(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            String dateString = d.getDayOfMonth()+"/"+d.getMonthValue();
            binding.tvDayNumber.setText(dateString);
            binding.llEventsContainer.removeAllViews();
            String type;
            int color;
            for(CalendarNotification event : e){
                CalendarEventView eventView = new CalendarEventView(binding.getRoot().getContext());
                type = typesString[event.getType().ordinal()];
                color = typesColor[event.getType().ordinal()];
                eventView.setEvent(type, color, event.toString());
                eventView.setOnClick(v->ec.onActionResult(event));
                binding.llEventsContainer.addView(eventView);
            }
        }
    }
}
