package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    ArrayList<String> listOfMatches = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    String userName;
    String TAG = "VolunteerChatList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_chat_list);

        matchesListView = (ListView) findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfMatches);

        matchesListView.setAdapter(arrayAdapter);
        String userId = FirebaseAuth.getInstance().getUid();

        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id");
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();
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


                                        set.add(orgEmail);
                                        arrayAdapter.clear();
                                        arrayAdapter.addAll(set);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            Log.d(TAG, set.toString());

                        }

                    }

                    arrayAdapter.notifyDataSetChanged();
                    Log.d("EXISTS", set.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}