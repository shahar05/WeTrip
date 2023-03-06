package com.shahardror.wetrip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int reqCode = intent.getIntExtra("reqCode" ,0 );

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = null;
        if(Build.VERSION.SDK_INT>=26) {
            channelId = "id_2";

            NotificationChannel channel = new NotificationChannel(channelId, "Alarm channel", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context , channelId);
        builder.setContentText(context.getResources().getString(R.string.notif_reminder))
                .setContentTitle("We Trip")
                .setSmallIcon(R.drawable.world2);

        builder.setPriority(Notification.PRIORITY_MAX);
        Intent intent1 = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,reqCode,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_VIBRATE;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(reqCode,notification);



    }



}
