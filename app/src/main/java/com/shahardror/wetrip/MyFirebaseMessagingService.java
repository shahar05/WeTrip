package com.shahardror.wetrip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.shahardror.wetrip.MainActivity.localToken;
import static com.shahardror.wetrip.MainActivity.localUserID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String TAG;

    public MyFirebaseMessagingService()
    {
        TAG = "FCM_Service";
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (!MainActivity.foregrounded())
            {
                Log.i("FCM", "app in BACKground!");
                eranNotify(remoteMessage);
            }
            else
            {
                Log.i("FCM", "app in FOREground!");
                // for FOREGROUND:

                Intent intent = new Intent("message_received");
                intent.putExtra("message",remoteMessage.getData().get("message"))
                        .putExtra("from",remoteMessage.getData().get("senderUID"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        localToken = token;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersTable = database.getReference("users");

        if(localUserID != null)
        usersTable.child(localUserID)
                .child("token")
                .setValue(localToken);
        else
            Log.i(TAG , "localUserID is null when token is refreshed");

        //sendRegistrationToServer(token);
    }

    @Override
    public void onMessageSent(@NonNull String s) {
        //super.onMessageSent(s);
        Log.i(TAG,"onMessageSent sent: "+s);
    }

    @Override
    public void onSendError(@NonNull String s, @NonNull Exception e) {
        //super.onSendError(s, e);
        Log.i(TAG,"onSendError failed to send: "+s+" with error: "+ e);
    }

    public void eranNotify(RemoteMessage remoteMessage) {

        // for BACKGROUND:

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);

        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("id_1", "name_1", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId("id_1");
        }

        builder.setContentTitle(getResources().getString(R.string.new_msg_from) + remoteMessage.getData().get("senderName"))
                .setContentText(remoteMessage.getData().get("message"))
                .setSmallIcon(R.drawable.world);

        Intent intent1 = new Intent(this,MainActivity.class);

        intent1.putExtra("ChatNotif",remoteMessage.getData().get("senderUID"));
        Log.i(TAG,remoteMessage.getData().get("senderUID"));

        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        manager.notify(1,notification);
    }
}
