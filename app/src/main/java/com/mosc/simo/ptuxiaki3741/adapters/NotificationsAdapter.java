package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.views.CalendarEventView;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final List<CalendarNotification> notifications;
    private final String[] typesString;
    private final int[] typesColor;
    private final ActionResult<CalendarNotification> onClick;

    public NotificationsAdapter(
            List<CalendarNotification> notifications,
            String[] typesString,
            int[] typesColor,
            ActionResult<CalendarNotification> onClick
    ) {
        this.notifications = notifications;
        this.typesString = typesString;
        this.typesColor = typesColor;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_calendar_list_event, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position < getItemCount()){
            CalendarNotification notification = notifications.get(position);
            int color = typesColor[notification.getType().ordinal()];
            String type = typesString[notification.getType().ordinal()];
            holder.calendarEventView.setEvent(type, color, notification.toString());
            holder.calendarEventView.setOnClick(v->onClick.onActionResult(notification));
        }else{
            holder.calendarEventView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(notifications != null) return notifications.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final CalendarEventView calendarEventView;
        public ViewHolder(@NonNull View view) {
            super(view);
            calendarEventView = new CalendarEventView(view);
        }
    }
}
