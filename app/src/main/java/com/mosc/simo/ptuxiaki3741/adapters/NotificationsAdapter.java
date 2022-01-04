package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewNotificationItemBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final List<CalendarNotification> notifications;
    private final ActionResult<CalendarNotification> onClick;

    public NotificationsAdapter(
            List<CalendarNotification> notifications,
            ActionResult<CalendarNotification> onClick
    ) {
        this.notifications = notifications;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_notification_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position < getItemCount()){
            CalendarNotification notification = notifications.get(position);
            holder.binding.getRoot().setText(notification.toString());
            holder.binding.getRoot().setOnClickListener(v->onClick.onActionResult(notification));
        }else{
            holder.binding.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(notifications != null) return notifications.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewNotificationItemBinding binding;
        public ViewHolder(@NonNull View view) {
            super(view);
            binding = ViewNotificationItemBinding.bind(view);
        }
    }
}
