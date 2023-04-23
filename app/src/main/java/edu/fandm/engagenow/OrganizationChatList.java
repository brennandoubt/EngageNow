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
                finish();
            }
        });

        matchesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteConversation(view, i);
                return true;
            }
        });

    }

    private void deleteConversation(View view, int idx) {
        String[] nameEmail = arrayAdapter.getItem(idx).toString().split(":");
        String volName = nameEmail[0].trim();
        String volEmail = nameEmail[1].trim();

        String volId = volIdMap.get(volEmail).get("id");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DANGER: Remove Match");
        TextView description = new TextView(this);
        description.setText("Are you sure you would like to disconnect with " + volName + "? This will PERMANENTLY remove your conversation with the volunteer. This volunteer can match with you again in the future if they wish.");
        description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        description.setTextSize(15);
        description.setPadding(20, 10, 20, 10);
        builder.setView(description);

        builder.setPositiveButton("DISCONNECT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference dbr1 = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volId);
                dbr1.removeValue();

                // refresh the page to remove deleted chat
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

    private void populateChats() {
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HashMap<String, Object> chatsMap = (HashMap<String, Object>) snapshot.getValue();

                    arrayAdapter.clear();
                    HashMap<String, Boolean> chatsHashmap = new HashMap<>();
                    // contains data from a firebase location.
                    if (chatsMap != null) {
                        for (String key : chatsMap.keySet()) {

                            HashMap<String, Object> volChat = (HashMap<String, Object>) chatsMap.get(key);
                            String volId = key;

                            DatabaseReference volunteerAccDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(volId);
                            volunteerAccDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    HashMap<String, String> volInfo = (HashMap<String, String>) task.getResult().getValue();

                                    String volFirstName = volInfo.get("first_name");
                                    String volLastName = volInfo.get("last_name");
                                    String volEmail = volInfo.get("email");
                                    Boolean readChat = (Boolean) volChat.get("organization_read");

                                    chatsHashmap.put(volFirstName + " " + volLastName + ": " + volEmail, readChat);

                                    volInfo.put("id", volId);
                                    volInfo.put("id", volId);
                                    volInfo.put("last_name", volLastName);
                                    volInfo.put("first_name", volFirstName);
                                    volIdMap.put(volEmail, volInfo);
                                    arrayAdapter.clear();
                                    for (String key : chatsHashmap.keySet()) {
                                        if (chatsHashmap.get(key) != null && !chatsHashmap.get(key)) {
                                            arrayAdapter.insert(key, 0);
                                        } else {
                                            arrayAdapter.add(key);
                                        }
                                    }
    //                            arrayAdapter.addAll(chatsHashmap.keySet());
                                    setReadNotifications();
                                    Log.d(TAG, "CHATMAP: " + chatsHashmap.toString());

                                }
                            });

                        }
                    }

                        arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "FAIL");
            }
        });
    }

    private void setReadNotifications() {
        for (int i = 0; i < matchesListView.getCount(); i++) {
            Log.d("ERROR", arrayAdapter.getItem(i).toString());
            String email = arrayAdapter.getItem(i).toString().split(":")[1].trim();
            String volunteerId = volIdMap.get(email).get("id");

            final int idx = i;
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("organization_read");
            dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    View v = matchesListView.getChildAt(idx);
                    if (task.getResult().exists()) {
                        boolean read = (boolean) task.getResult().getValue();
                        if (v != null) {
                            if (!read) {
                                v.setBackgroundColor(getResources().getColor(R.color.light_green));
                                arrayAdapter.remove(v);
                                arrayAdapter.notifyDataSetChanged();


                            } else {
                                v.setBackgroundColor(Color.TRANSPARENT);
                            }
                        }
                    }
                }
            });
        }

        arrayAdapter.notifyDataSetChanged();

    }

}