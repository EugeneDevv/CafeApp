package com.example.admin.cafeapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Helper.NotificationHelper;
import com.example.admin.cafeapp.MainActivity;
import com.example.admin.cafeapp.Model.Token;
import com.example.admin.cafeapp.OrderStatus;
import com.example.admin.cafeapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() !=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationAPI26(remoteMessage);
            else
                sendNotification(remoteMessage);
        }
    }

    @Override
    public void onNewToken(String tokenRefreshed) {
        super.onNewToken(tokenRefreshed);
        if (Common.currentUser!=null)
            updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(tokenRefreshed,false); //false becausenthis token is sent from client app
        tokens.child(Common.currentUser.getPhone()).setValue(token);

    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        int requestId=(int)System.currentTimeMillis();
        Map<String,String> data=remoteMessage.getData();
        String title=data.get("title");
        String message=data.get("message");

        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if (Common.currentUser !=null) {
            Intent intent = new Intent(this, OrderStatus.class);
            intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            pendingIntent = PendingIntent.getActivity(this, requestId, intent, PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getNotificationChannel(title, message, pendingIntent, defaultSoundUri);

            //Get random id for notification to show all notification
            helper.getManager().notify(new Random().nextInt(), builder.build());
        }
        else
        {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            helper = new NotificationHelper(this);
            builder = helper.getNotificationChannel(title, message, defaultSoundUri);
            helper.getManager().notify(new Random().nextInt(), builder.build());
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        int requestId=(int)System.currentTimeMillis();
        Map<String,String> data=remoteMessage.getData();
        String title=data.get("title");
        String message=data.get("message");
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,requestId,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.unnamed)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.unnamed))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager noti=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0,builder.build());
    }
}
