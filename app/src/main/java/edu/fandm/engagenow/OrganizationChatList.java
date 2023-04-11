package edu.fandm.engagenow;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OrganizationChatList extends OrganizationBaseClass {
    ListView matchesListView;
    ArrayList<String> listOfMatches = new ArrayList<String>();
    HashMap<String, String> volIdMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    String userName;
    String TAG = "OrgChatList";
    static String uid;
    // represents a particular location in database and can be used for reading or writing data to that database location
    private DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id");

    private String CHANNEL_ID_1 = "Channel1";
    private final static String PERM = android.Manifest.permission.POST_NOTIFICATIONS;
    private Context CTX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_match_list);
        uid = FirebaseAuth.getInstance().getUid();
        dbr = dbr.child(uid);
        matchesListView = (ListView) findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfMatches);

        matchesListView.setAdapter(arrayAdapter);
        this.CTX = getApplicationContext();
        Log.d(TAG, dbr.toString());
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<String>();
                // contains data from a firebase location.
                Iterator i = snapshot.getChildren().iterator();
                while(i.hasNext()) {
                    String volunteerKey = ( (DataSnapshot) i.next()).getKey();

                    DatabaseReference volunteerAccDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(volunteerKey).child("email");
                    volunteerAccDbr.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String volEmail = snapshot.getValue().toString();
                            set.add(volEmail);
                            volIdMap.put(volEmail, volunteerKey);
                            arrayAdapter.clear();
                            arrayAdapter.addAll(set);
                            setReadNotifications();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }


                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "FAIL");
            }
        });


        matchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent i = new Intent(getApplicationContext(), OrganizationChat.class);
                userName = ((TextView)view).getText().toString();
                i.putExtra("selected_volunteer", userName);
                i.putExtra("user_name", userName);
                Log.d("HERE", userName);
                i.putExtra("volunteer_id", volIdMap.get(userName));
                startActivity(i);
            }
        });

        Button potMatchButton = (Button) findViewById(R.id.pot_match_button);
        potMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), OrganizationPotentialMatches.class);
                startActivity(i);
            }
        });

    }

    private void setReadNotifications() {
        for (int i = 0; i < matchesListView.getCount(); i++) {
            String email = arrayAdapter.getItem(i).toString();
            String volunteerId = volIdMap.get(email);
            Log.d(email, volunteerId);
            final int idx = i;
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("organization_read");
            Task<DataSnapshot> ds = dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    boolean read = (boolean) task.getResult().getValue();
                    View v = (View) matchesListView.getChildAt(idx);
                    if (!read) {
                        v.setBackgroundColor(Color.RED);
                    }
                    else {
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }
                    arrayAdapter.notifyDataSetChanged();

                }
            });


            Log.d(TAG, Integer.toString(arrayAdapter.getCount()));

        }
    }

    private void getUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText userNameET = new EditText(this);

        builder.setView(userNameET);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userName = userNameET.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getUserName();
            }
        });
        builder.show();
    }

    private void makeNotification() {
        //create the notification and fill with content
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, this.CHANNEL_ID_1);
        nb.setSmallIcon(R.drawable.chat_foreground);
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

            NotificationManager nm = this.CTX.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        // check permission
        if (ContextCompat.checkSelfPermission(this.CTX, this.PERM) == PackageManager.PERMISSION_GRANTED) {
            // display notification
            NotificationManagerCompat nmc = NotificationManagerCompat.from(this.CTX);
            nmc.notify(0, nb.build());
        }
        else {
            Log.d(this.TAG, "Could not send notification, permissions not granted");
        }
    }

}