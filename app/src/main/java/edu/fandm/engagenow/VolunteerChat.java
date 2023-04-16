package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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

    final String TAG = "VolunteerChat";
    private DatabaseReference dbr;
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

        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(uid);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                map2.put("user", userName);
                dbr2.updateChildren(map2);
                
                messageEditText.setText("");

                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(uid).child("organization_read");
                dbr.setValue(false);
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

//        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(uid).child("volunteer_read");
//        dbr.setValue(true);
    }

}