package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
    ArrayList<String> listOfMatchesName = new ArrayList<String>();
    HashMap<String, String> nameIdMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    String userName, userId;
    String TAG = "VolunteerChatList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_chat_list);

        matchesListView = (ListView) findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfMatchesName);

        matchesListView.setAdapter(arrayAdapter);
        userId = FirebaseAuth.getInstance().getUid();

        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id");
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> nameSet = new HashSet<>();
                if (snapshot.exists()) {
                    // for each organization
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> m = (Map) ds.getValue();
                        Log.d("HERE", m.toString());
                        if (m.containsKey(userId)) {
                            String orgId = ds.getKey();
                            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(orgId).child("email");

                            dbr.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // check if user in that organization chat
                                    if (snapshot.exists()) {
                                        Log.d(TAG, snapshot.toString());
                                        String orgEmail = snapshot.getValue().toString();


                                        nameSet.add(orgEmail);

                                        nameIdMap.put(orgEmail, orgId);
                                        arrayAdapter.clear();
                                        arrayAdapter.addAll(nameSet);
                                        setReadNotifications();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            Log.d(TAG, nameSet.toString());

                        }

                    }

                    arrayAdapter.notifyDataSetChanged();
                    Log.d("EXISTS", nameSet.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ListView chatListView = (ListView) findViewById(R.id.matches_lv);
        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String email = adapterView.getItemAtPosition(position).toString();
                Log.d(TAG, nameIdMap.get(email));
                Intent i = new Intent(getApplicationContext(), VolunteerChat.class);
                i.putExtra("organization_id", nameIdMap.get(email));
                i.putExtra("organization_email", email);
                String organizationId = nameIdMap.get(email);
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(userId).child("volunteer_read");
                dbr.setValue(true);
                //                i.putExtra("current_user_name", nameIdMap.get(email));
                startActivity(i);
            }
        });


    }

    private void setReadNotifications() {
        for (int i = 0; i < matchesListView.getCount(); i++) {
            String email = arrayAdapter.getItem(i).toString();
            String organizationId = nameIdMap.get(email);
            final int idx = i;
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(userId).child("volunteer_read");
            Task<DataSnapshot> ds = dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    boolean read = (boolean) task.getResult().getValue();
                    View v = (View) matchesListView.getChildAt(idx);
                    if (!read) {
                        v.setBackgroundColor(Color.RED);
                    } else {
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }
                    arrayAdapter.notifyDataSetChanged();

                }
            });

        }
    }
}