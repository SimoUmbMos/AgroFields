package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.backend.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.ui.views.CalendarEventView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final List<CalendarNotification> notifications;
    private final String[] typesString;
    private final Integer[] typesColor;
    private final ActionResult<CalendarNotification> onClick;

    public NotificationsAdapter(
            String[] typesString,
            Integer[] typesColor,
            ActionResult<CalendarNotification> onClick
    ) {
        this.notifications = new ArrayList<>();
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
            Integer color = typesColor[notification.getType().ordinal()];
            String type = typesString[notification.getType().ordinal()];
            holder.calendarEventView.setEvent(type, color, notification.toString());
            holder.calendarEventView.setOnClick(v->onClick.onActionResult(notification));
        }else{
            holder.calendarEventView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void saveData(List<CalendarNotification> notifications){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NotificationsDiffUtil(this.notifications, notifications));
        this.notifications.clear();
        this.notifications.addAll(notifications);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final CalendarEventView calendarEventView;
        public ViewHolder(@NonNull View view) {
            super(view);
            calendarEventView = new CalendarEventView(view);
        }
    }

    private static class NotificationsDiffUtil extends DiffUtil.Callback {
        private final List<CalendarNotification> oldList;
        private final List<CalendarNotification> newList;
        public NotificationsDiffUtil(List<CalendarNotification> oldList, List<CalendarNotification> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return DataUtil.checkItemsTheSame(oldList.get(oldItemPosition),newList.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            try{
                CalendarNotification not1 = oldList.get(oldItemPosition);
                CalendarNotification not2 = newList.get(newItemPosition);

                if(not1 == null || not2 == null)
                    return false;
                if(not1.getType() != not2.getType())
                    return false;
                if(!not1.getTitle().equals(not2.getTitle()))
                    return false;
                if(!not1.getMessage().equals(not2.getMessage()))
                    return false;
                if(!not1.getDate().equals(not2.getDate()))
                    return false;

                if(not1.getLid() == null ^ not2.getLid() == null )
                    return false;
                else if( not1.getLid() != null && not2.getLid() != null && (!not1.getLid().equals(not2.getLid())) )
                    return false;

                if(not1.getZid() == null ^ not2.getZid() == null )
                    return false;
                else if( not1.getZid() != null && not2.getZid() != null && (!not1.getZid().equals(not2.getZid())) )
                    return false;
            }catch (Exception e){
                return false;
            }
            return true;
        }
    }
}
