package edu.fandm.engagenow;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    final String TAG = "Main activity";

    // notification variables
    private String CHANNEL_ID_1 = "Channel1";
    private final static String PERM = android.Manifest.permission.POST_NOTIFICATIONS;
    private Context CTX;
    private boolean canPostNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // notification code for future use if desired
//        this.CTX = getApplicationContext();
//        int permStatus = ContextCompat.checkSelfPermission(this.CTX, this.PERM);
//        Log.d(TAG, Integer.toString(permStatus));
//        if (permStatus != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[] {this.PERM}, 0);
//            }
//        }

        startUp();
    }

    private void startUp() {
        TextView tv = findViewById(R.id.internet_tv);
        tv.setVisibility(View.VISIBLE);
        if (hasInternetConnection() && hasNetworkConnection()) {
            tv.setVisibility(View.INVISIBLE);
            logIn();
        }
    }

    private void logIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "SUCCESS");
            // User is signed in
//                          access data from database https://www.youtube.com/watch?v=E9drbKeVG7Y
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
            dbr.child(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String accountType = String.valueOf(task.getResult().getValue());
                        Log.d(TAG, accountType);

                        Intent i;
                        if (accountType.equals("volunteer_account")) {
                            i = new Intent(getApplicationContext(), VolunteerSwiping.class);
                        } else {
                            i = new Intent(getApplicationContext(), EventDashboard.class);
                        }

                        Toast.makeText(MainActivity.this, "Logged in as, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        startActivity(i);
                        finish();
                    }
                }
            });
        }
        else {
            // User is signed out
            Intent i = new Intent(getApplicationContext(), SignIn.class);
            startActivity(i);
            Log.d(TAG, "FAILURE");
            Toast.makeText(MainActivity.this, "Please sign in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        assert requestCode == 0;

        if (permissions[0] == android.Manifest.permission.POST_NOTIFICATIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.canPostNotifications = true;
            }
        }
    }

    private boolean hasInternetConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}