package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VolunteerChat extends VolunteerBaseClass {
    Button sendMessageButton;
    EditText messageEditText;
    ListView chatListView;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ArrayList<String> chatList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    String userName, organizationId, organizationEmail, organizationName, user_message_key, uid;
    FirebaseUser currentUser;
    boolean deleted = false;
    android.content.Context context = this;

    final String TAG = "VolunteerChat";
    private DatabaseReference dbr;
    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_chat);
        uid = FirebaseAuth.getInstance().getUid();
        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(uid).child("first_name");
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(uid).child("last_name");
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName += " " + snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendMessageButton = (Button) findViewById(R.id.send_button);
        messageEditText = (EditText) findViewById(R.id.message_et);

        chatListView = (ListView) findViewById(R.id.chat_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, chatList);
        chatListView.setAdapter(arrayAdapter);

        organizationId = getIntent().getExtras().get("organization_id").toString();
        organizationEmail = getIntent().getExtras().get("organization_email").toString();
        organizationName = getIntent().getExtras().get("organization_name").toString();
        setTitle("Chat: " + organizationName);


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageEditText.getText().toString();
                if (msg.equals("")) {
                    return;
                }
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(uid);
                HashMap<String, Object> m = new HashMap<>();
                m.put("organization_read", false);
                dbr.updateChildren(m);

                String email = user.getEmail();
                Map<String, Object> map = new HashMap<String, Object>();
                // unique key for each message sent and received
                user_message_key = dbr.push().getKey();
                dbr.updateChildren(map);

                DatabaseReference dbr2 = dbr.child(user_message_key);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put("msg", msg);
                map2.put("user", userName);
                dbr2.updateChildren(map2);
                
                messageEditText.setText("");


            }
        });

        setUpChat();
    }

    private void setUpChat() {
        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(uid);

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
                notice.setText("The organization has ended the chat. You will not longer be matched with the organization. The chat messages will be deleted. You can match with this organization in the future.");
                notice.setTextSize(20);
                notice.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                notice.setPadding(60, 5, 5, 5);
                dialog.setView(notice);

                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), VolunteerChatList.class);
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


    public void updateConversation(DataSnapshot dataSnapshot) {
        String msg, userName;
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            msg = (String) ((DataSnapshot) i.next()).getValue();
            userName = (String) ((DataSnapshot) i.next()).getValue();

            arrayAdapter.add(userName + ": " + msg);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!deleted) {
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(uid).child("volunteer_read");
            dbr.setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        active = false;
        if (!deleted) {
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(uid).child("volunteer_read");
            dbr.setValue(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }
}