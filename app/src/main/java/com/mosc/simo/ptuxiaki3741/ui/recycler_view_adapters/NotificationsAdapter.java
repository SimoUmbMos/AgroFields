package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.ui.views.CalendarEventView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final List<CalendarEntity> notifications;
    private final ActionResult<CalendarNotification> onClick;

    public NotificationsAdapter(ActionResult<CalendarNotification> onClick) {
        this.notifications = new ArrayList<>();
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
            CalendarEntity entity = notifications.get(position);
            holder.calendarEventView.setEvent(
                    entity.getCategory().getName(),
                    entity.getCategory().getColorData(),
                    entity.getNotification().toString()
            );
            holder.calendarEventView.setOnClick(v->onClick.onActionResult(entity.getNotification()));
        }else{
            holder.calendarEventView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void saveData(List<CalendarEntity> notifications){
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
        private final List<CalendarEntity> oldList;
        private final List<CalendarEntity> newList;
        public NotificationsDiffUtil(List<CalendarEntity> oldList, List<CalendarEntity> newList) {
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
                CalendarEntity ent1 = oldList.get(oldItemPosition);
                CalendarEntity ent2 = newList.get(newItemPosition);
                CalendarNotification not1 = ent1.getNotification();
                CalendarNotification not2 = ent2.getNotification();

                if(not1 == null || not2 == null)
                    return false;
                if(not1.getCategoryID() != not2.getCategoryID())
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
