package edu.fandm.engagenow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private String CHANNEL_ID_1 = "Channel1";
    private final static String PERM = android.Manifest.permission.POST_NOTIFICATIONS;

    String TAG = "NotificationService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        startNotificationCheck();

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");


    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();


    }

    private void startNotificationCheck() {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(FirebaseAuth.getInstance().getUid());
        dbr.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeNotification();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }




    private void makeNotification() {
        //create the notification and fill with content
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, this.CHANNEL_ID_1);
        nb.setSmallIcon(R.drawable.ic_launcher_background);
        nb.setContentTitle("ENGAGE NOW");
        nb.setContentText("Test notification");
        nb.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // create the channel, (necessary and only possible on newer versions of android)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel1";
            String desc = "Main channel for this app.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(this.CHANNEL_ID_1, name, importance);
            channel.setDescription(desc);

            NotificationManager nm = this.getApplication().getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        // check permission
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), this.PERM) == PackageManager.PERMISSION_GRANTED) {
            // display notification
            NotificationManagerCompat nmc = NotificationManagerCompat.from(this.getApplicationContext());
            nmc.notify(0, nb.build());
        }
        else {
            Log.d(this.TAG, "Could not send notification, permissions not granted");
        }
    }
}
