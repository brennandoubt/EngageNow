package edu.fandm.engagenow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    /*
    This is the implementation of the firebase messaging service which you can using the firebase console to send a message to a
    target android device using the registration token stored inside real time database under each account.

    To make the automatic notification work, google cloud function would be needed to listen to changes to the database and triggers the
    notification request to the firebase cloud messaging.

    You will probably needs to put this token code somewhere in the app on the sign in to generate a new taken if the user
    signs into a different android device or deleted the app's data and reinstalled. You can look this up on the firebase cloud
    messaging documentation.

    //    @Override
//    public void onNewToken(@NonNull String token) {
//        Log.d(TAG, "Refreshed token: " + token);
//
//        FirebaseUser user = fbAuth.getCurrentUser();
//        userId = user.getUid();
//        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId);
//        Map<String, Object> orgDBHashmap = new HashMap<>();
//
//        // If you want to send messages to this application instance or
//        // manage this apps subscriptions on the server side, send the
//        // FCM registration token to your app server.
//        sendRegistrationToServer(token);
//    }
     */
    private String TAG = "FirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        sendNotification(remoteMessage.getNotification().getBody());
        sendNotificationToast(remoteMessage.getFrom(), remoteMessage.getNotification().getBody());
    }

    private void sendNotificationToast(String from, String body){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(FirebaseMessagingService.this.getApplicationContext(), from  + "->" + body, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.place_holder_fore_ground)
                        .setContentTitle("Engage Now")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }




}
