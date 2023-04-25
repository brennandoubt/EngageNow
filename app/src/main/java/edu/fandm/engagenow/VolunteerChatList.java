package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VolunteerChatList extends VolunteerBaseClass {
    ListView matchesListView;
    ArrayList<String> conversationsList = new ArrayList<String>();
    static HashMap<String, HashMap<String, String>> orgIdKeyDataMap = new HashMap<>();
    //    contains organizations accounts data retrieved by email (email:hashmap of data for vol)
    static HashMap<String, HashMap<String, String>> orgEmailKeyDataMap = new HashMap<>();
    static HashMap<String, String> orgEmailIdMap = new HashMap<>();
//    HashMap<String, HashMap<String, String>> nameIdMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    String userId;
    String TAG = "VolunteerChatList";
    HashMap<String, HashMap<String, Object>> orgAccountsMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_chat_list);
        setTitle("Chats");

        startUp();

        DatabaseReference orgDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts");
        orgDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    orgIdKeyDataMap = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
                    for (String volId : orgIdKeyDataMap.keySet()) {
                        orgEmailIdMap.put(orgIdKeyDataMap.get(volId).get("email"), volId);
                        orgEmailKeyDataMap.put(orgIdKeyDataMap.get(volId).get("email"), orgIdKeyDataMap.get(volId));
                    }
//                    Log.d("KEY", volIdKeyDataMap.toString());
//                    Log.d("EMAIL", volEmailKeyDataMap.toString());
                    populateChats();

                }
            }
        });


    }

    private void startUp() {

        matchesListView = findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, conversationsList);

        matchesListView.setAdapter(arrayAdapter);
        userId = FirebaseAuth.getInstance().getUid();

        matchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String[] nameEmail = adapterView.getItemAtPosition(position).toString().split(":");
                String orgId = orgEmailIdMap.get(nameEmail[1].trim());

                Intent i = new Intent(getApplicationContext(), VolunteerChat.class);
                i.putExtra("organization_id", orgId);
                i.putExtra("organization_email", nameEmail[1].trim());
                i.putExtra("organization_name", nameEmail[0].trim());

                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(orgId).child(userId).child("volunteer_read");
                dbr.setValue(true);
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
        String orgName = nameEmail[0].trim();
        String orgEmail = nameEmail[1].trim();

        String orgId = orgEmailIdMap.get(nameEmail[1].trim());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DANGER: Remove Match");
        TextView description = new TextView(this);
        description.setText("Are you sure you would like to disconnect with " + orgName + "? This will PERMANENTLY remove your conversation with the organization. You can match with the organization again in the future if you wish.");
        description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        description.setTextSize(15);
        description.setPadding(20, 10, 20, 10);
        builder.setView(description);

        builder.setPositiveButton("DISCONNECT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference dbr1 = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(orgId).child(userId);
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
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id");
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "CHANGED");

                if (snapshot.exists()) {
                    HashMap<String, HashMap<String, Object>> conversationsMap = (HashMap<String, HashMap<String, Object>>) snapshot.getValue();
                    ArrayList<String> newVolChatList = new ArrayList<>();
                    ArrayList<Boolean> readStatusList = new ArrayList<>();
//                    Log.d(TAG, conversationsMap.toString());
                    if (conversationsMap != null) {
                        for (String orgId : conversationsMap.keySet()) {
                            if (conversationsMap.get(orgId).containsKey(userId)) {

                                String volNameEmail = orgIdKeyDataMap.get(orgId).get("name") + ": " + orgIdKeyDataMap.get(orgId).get("email");
                                newVolChatList.add(volNameEmail);
                                readStatusList.add((Boolean) ((HashMap<String, Object>) conversationsMap.get(orgId).get(userId)).get("volunteer_read"));
//                            Log.d(TAG, newVolChatList.toString());
//                            Log.d(TAG, conversationsList.toString());
//                            Log.d(TAG, readStatusList.toString());
                            }
                        }
                    }
                    compareConversationsLists(newVolChatList, readStatusList);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setReadNotifications(newVolChatList, readStatusList);
                        }
                    }, 500);
                }
//                no messages in list
                else {
                    arrayAdapter.clear();
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    update the list of conversations
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