package edu.fandm.engagenow;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.os.Handler;
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
import com.google.firebase.database.ChildEventListener;
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
    ArrayList<String> conversationsList = new ArrayList<String>();
//    contains volunteer accounts hashmap (volId:hashmap of data for vol)
    static HashMap<String, HashMap<String, String>> volIdKeyDataMap = new HashMap<>();
//    contains volunteer accounts data retrieved by email (email:hashmap of data for vol)
    static HashMap<String, HashMap<String, String>> volEmailKeyDataMap = new HashMap<>();
    static HashMap<String, String> volEmailIdMap = new HashMap<>();

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

        setUp();

        DatabaseReference nameDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("name");
        nameDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                orgName = task.getResult().getValue().toString();
            }
        });

        DatabaseReference volDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts");
        volDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    volIdKeyDataMap = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
                    for (String volId : volIdKeyDataMap.keySet()) {
                        volEmailIdMap.put(volIdKeyDataMap.get(volId).get("email"), volId);
                        volEmailKeyDataMap.put(volIdKeyDataMap.get(volId).get("email"), volIdKeyDataMap.get(volId));
                    }
//                    Log.d("KEY", volIdKeyDataMap.toString());
//                    Log.d("EMAIL", volEmailKeyDataMap.toString());
                    populateChats();

                }
            }
        });

    }

    private void setUp() {
        uid = FirebaseAuth.getInstance().getUid();
        matchesListView = (ListView) findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, conversationsList);

        matchesListView.setAdapter(arrayAdapter);
        this.CTX = getApplicationContext();

        matchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent i = new Intent(getApplicationContext(), OrganizationChat.class);
                String[] nameEmail = ((TextView)view).getText().toString().split(":");
                String name = nameEmail[0].trim();
                String email = nameEmail[1].trim();
                String volunteerId = volEmailIdMap.get(email);
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

        String volId = volEmailIdMap.get(volEmail);

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
                Log.d(TAG, volId);
                DatabaseReference dbr1 = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volId);
                dbr1.removeValue();
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
//        arrayAdapter.clear();
        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid);
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "CHANGE");
                if (snapshot.exists()) {
                    HashMap<String, Object> conversationsMap = (HashMap<String, Object>) snapshot.getValue();
                    ArrayList<String> newVolChatList = new ArrayList<>();
                    ArrayList<Boolean> readStatusList = new ArrayList<>();

//                    TODO: make sure it works for null chatMap value
                    if (conversationsMap != null) {
                        for (String volId : conversationsMap.keySet()) {
                            String volNameEmail = volIdKeyDataMap.get(volId).get("first_name") + " " + volIdKeyDataMap.get(volId).get("last_name") + ": " + volIdKeyDataMap.get(volId).get("email");
                            newVolChatList.add(volNameEmail);
                            readStatusList.add((Boolean) ((HashMap<String, Object>) conversationsMap.get(volId)).get("organization_read"));
                        }
                        Log.d(TAG, newVolChatList.toString());
                        Log.d(TAG, conversationsList.toString());
                        Log.d(TAG, readStatusList.toString());

                        compareConversationsLists(newVolChatList, readStatusList);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setReadNotifications(newVolChatList, readStatusList);
                            }
                        }, 500);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Log.d(TAG, "FAIL");
            }
        });
    }

    private void compareConversationsLists(ArrayList<String> newVolChatList, ArrayList<Boolean> readStatusList) {
//        remove chats that have been deleted
        for (int i = 0; i < conversationsList.size(); i++) {
            if (!newVolChatList.contains(conversationsList.get(i))) {
                conversationsList.remove(i);
            }
        }
//        add new chats, if they are unread add them at beginning
        for (int i = 0; i < newVolChatList.size(); i++) {
            if (!conversationsList.contains(newVolChatList.get(i))) {
                if (!readStatusList.get(i)) {
                    conversationsList.add(0, newVolChatList.get(i));
                }
                else {
                    conversationsList.add(newVolChatList.get(i));
                }
            }
//            move unread chats to top
            else if (!readStatusList.get(i)) {
                conversationsList.remove(newVolChatList.get(i));
                conversationsList.add(0, newVolChatList.get(i));
            }
        }
        arrayAdapter.notifyDataSetChanged();
    }

    //    color the chats that are unread
    private void setReadNotifications(ArrayList<String> newVolChatList, ArrayList<Boolean> readStatusList) {
        for (int i = 0; i < newVolChatList.size(); i++) {
            int idx = conversationsList.indexOf(newVolChatList.get(i));
            View v = matchesListView.getChildAt(idx);
            Log.d(TAG, Integer.toString(i));
            Log.d(TAG, matchesListView.toString());
            // not read
            if (!readStatusList.get(i) && v != null) {
                v.setBackgroundColor(getResources().getColor(R.color.light_green));

            }
            else if (v != null){
                v.setBackgroundColor(Color.TRANSPARENT);

            }
        }

        arrayAdapter.notifyDataSetChanged();

    }


}


