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
    HashMap<String, HashMap<String, String>> volIdMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    String userName;
    String TAG = "OrgChatList";
    static String uid, orgName;
    // represents a particular location in database and can be used for reading or writing data to that database location
    private DatabaseReference dbr;

    private String CHANNEL_ID_1 = "Channel1";
    private final static String PERM = android.Manifest.permission.POST_NOTIFICATIONS;
    private Context CTX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_match_list);
        setTitle("Chats");

        uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference nameDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("name");
        nameDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                orgName = task.getResult().getValue().toString();
            }
        });

        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid);

        matchesListView = (ListView) findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfMatches);

        matchesListView.setAdapter(arrayAdapter);
        this.CTX = getApplicationContext();
        Log.d(TAG, dbr.toString());

        populateChats();
        matchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent i = new Intent(getApplicationContext(), OrganizationChat.class);
                String[] nameEmail = ((TextView)view).getText().toString().split(":");
                String name = nameEmail[0].trim();
                String email = nameEmail[1].trim();
                String volunteerId = volIdMap.get(email).get("id");
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("organization_read");
                dbr.setValue(true);

                i.putExtra("selected_volunteer_name", name);
                i.putExtra("volunteer_id", volunteerId);
                i.putExtra("org_name", orgName);
                startActivity(i);
            }
        });

    }

    private void populateChats() {
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> chatsMap = (HashMap<String, Object>) snapshot.getValue();
                Set<String> set = new HashSet<String>();
                arrayAdapter.clear();
                HashMap<String, Boolean> chatsHashmap = new HashMap<>();
                // contains data from a firebase location.
                for (String key : chatsMap.keySet()) {

                    HashMap<String, Object> volChat = (HashMap<String, Object>) chatsMap.get(key);
                    Log.d(TAG, "YOOO " + volChat);
                    String volId = key;

                    DatabaseReference volunteerAccDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(volId);
                    volunteerAccDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            HashMap<String, String> volInfo = (HashMap<String, String>) task.getResult().getValue();

                            String volFirstName = (String) volInfo.get("first_name");
                            String volLastName = (String) volInfo.get("last_name");
                            String volEmail = (String) volInfo.get("email");
                            Boolean readChat = (Boolean) volChat.get("organization_read");

                            chatsHashmap.put(volFirstName + " " + volLastName + ": " + volEmail, readChat);

                            volInfo.put("id", volId);
                            volInfo.put("id", volId);
                            volInfo.put("last_name", volLastName);
                            volInfo.put("first_name", volFirstName);
                            volIdMap.put(volEmail, volInfo);
                            arrayAdapter.clear();
                            for (String key : chatsHashmap.keySet()) {
                                if (!chatsHashmap.get(key)) {
                                    arrayAdapter.insert(key, 0);
                                }
                                else {
                                    arrayAdapter.add(key);
                                }
                            }
//                            arrayAdapter.addAll(chatsHashmap.keySet());
                            setReadNotifications();
                            Log.d(TAG, "CHATMAP: " + chatsHashmap.toString());

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
    }

    private void setReadNotifications() {
        for (int i = 0; i < matchesListView.getCount(); i++) {
            String email = arrayAdapter.getItem(i).toString().split(":")[1].trim();
            String volunteerId = volIdMap.get(email).get("id");

            final int idx = i;
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("organization_read");
            dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    boolean read = (boolean) task.getResult().getValue();
                    View v = (View) matchesListView.getChildAt(idx);
                    if (v != null) {
                        if (!read) {
                            v.setBackgroundColor(Color.RED);
                            arrayAdapter.remove(v);
                            arrayAdapter.notifyDataSetChanged();


                        } else {
                            v.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                }
            });


            Log.d(TAG, Integer.toString(arrayAdapter.getCount()));

        }

        arrayAdapter.notifyDataSetChanged();

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