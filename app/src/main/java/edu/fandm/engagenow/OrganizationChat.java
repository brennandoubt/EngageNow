package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class OrganizationChat extends OrganizationBaseClass {
    Button sendMessageButton;
    EditText messageEditText;
    ListView chatListView;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ArrayList<String> chatList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    String orgName, selectedVolunteer, user_message_key, uid, volunteerId;
    FirebaseUser currentUser;
    boolean deleted = false;
    android.content.Context context = this;
    static boolean active = false;


    final String TAG = "OrganizationChat";
    private DatabaseReference dbr;
// Followed this chat app tutorial https://www.youtube.com/watch?v=oyTg0_k2AZU&t=27s
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_chat);
//        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        uid = FirebaseAuth.getInstance().getUid();
        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("name");
        dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                orgName = task.getResult().getValue().toString();
            }
        });

        sendMessageButton = (Button) findViewById(R.id.send_button);
        messageEditText = (EditText) findViewById(R.id.message_et);

        chatListView = (ListView) findViewById(R.id.chat_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, chatList);
        chatListView.setAdapter(arrayAdapter);

//        get information from intent
        selectedVolunteer = getIntent().getExtras().get("selected_volunteer_name").toString();
        volunteerId = getIntent().getExtras().get("volunteer_id").toString();
        setTitle("Chat: " + selectedVolunteer);
//        Log.d(TAG, selectedVolunteer);
        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeMessage();
            }
        });

        dbr.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateConversation(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateConversation(snapshot);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (!active) {
                    return;
                }

                deleted = true;

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("Disconnected");
                TextView notice = new TextView(context);
                notice.setText("The Volunteer has unmatched with you. You will not longer be matched with the volunteer. The chat messages will be deleted. The volunteer can match with your organization in the future.");
                notice.setTextSize(20);
                notice.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                notice.setPadding(60, 5, 5, 5);
                dialog.setView(notice);

                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), OrganizationChatList.class);
                        finish();
                        startActivity(intent);
                    }
                });

                dialog.show();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void storeMessage() {
        String msg = messageEditText.getText().toString();
        if (msg.equals("")) {
            return;
        }
        String email = user.getEmail();
        Map<String, Object> map = new HashMap<String, Object>();
        // unique key for each message sent and received
        user_message_key = dbr.push().getKey();
        dbr.updateChildren(map);

        DatabaseReference dbr2 = dbr.child(user_message_key);
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("msg", msg);
        map2.put("user", orgName);
        dbr2.updateChildren(map2);
        messageEditText.setText("");
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("volunteer_read");
        dbr.setValue(false);
    }


    public void updateConversation(DataSnapshot dataSnapshot) {
        String userName;
        Object msg;
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            msg = ((DataSnapshot) i.next()).getValue();

            userName = (String) ((DataSnapshot) i.next()).getValue();
            if (userName.equals("volunteer_read") || userName.equals("organization_read")) {
                Log.d(TAG, "break");
                break;
            }

            arrayAdapter.add(userName + ": " + (String) msg);
            arrayAdapter.notifyDataSetChanged();
        }
//        colorText();
//        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("organization_read");
//        dbr.setValue(true);
    }

//    public void colorText() {
//        for (int i = 0; i < arrayAdapter.getCount(); i++) {
//            View v = chatListView.getChildAt(i);
//            String name = ((String) chatListView.getItemAtPosition(i)).split(":")[0];
////            Log.d(TAG, name + " : " + orgName);
//
//            if (v != null && name.equals(orgName)) {
////                Log.d(TAG, arrayAdapter.getItem(i).toString() + " - " + name);
////                Log.d(TAG, chatListView.getItemAtPosition(i).toString());
////                Log.d(TAG, "");
//                v.setBackgroundColor(Color.GREEN);
//            }
//            else if (v != null) {
////                Log.d(TAG, "ELSE " + chatListView.getItemAtPosition(i).toString());
//                v.setBackgroundColor(Color.RED);
//            }
//        }
//        arrayAdapter.notifyDataSetChanged();
//    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!deleted) {
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("organization_read");
            dbr.setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        active = false;
        if (!deleted) {
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId).child("organization_read");
            dbr.setValue(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }



}