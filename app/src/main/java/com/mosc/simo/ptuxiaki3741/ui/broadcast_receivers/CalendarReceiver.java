package com.mosc.simo.ptuxiaki3741.ui.broadcast_receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mosc.simo.ptuxiaki3741.ui.activities.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

public class CalendarReceiver extends BroadcastReceiver {
    public CalendarReceiver() {}
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra(AppValues.argNotification)) {
            CalendarNotification notification =
                    intent.getParcelableExtra(AppValues.argNotification);

            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.putExtra(AppValues.argNotification, notification);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) notification.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification.Builder notificationBuilder = new Notification
                    .Builder(context, AppValues.CalendarNotificationChannelID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getMessage())
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.notify((int) notification.getId(),notificationBuilder.build());
        }
    }
}
