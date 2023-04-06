package edu.fandm.engagenow;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    final String TAG = "Main activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "SUCCESS");
            // User is signed in
//                          access data from database https://www.youtube.com/watch?v=E9drbKeVG7Y
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
            dbr.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String accountType = String.valueOf(task.getResult().getValue());
                            Log.d(TAG, accountType);

                            Intent i;
                            if (accountType.equals("volunteer_account")) {
                                i = new Intent(getApplicationContext(), VolunteerSwiping.class);
                            } else {
                                i = new Intent(getApplicationContext(), OrganizationChatList.class);
                            }

                            Toast.makeText(MainActivity.this, "Logged in as, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(i);
                            finish();
                        }
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
}