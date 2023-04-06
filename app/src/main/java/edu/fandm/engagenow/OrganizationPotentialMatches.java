package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OrganizationPotentialMatches extends AppCompatActivity {
    ListView potMatchListView;
    ArrayList<String> listOfPotMatches = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    static String uid;
    String TAG = "OrgPotMatch";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_potential_matches);
        uid = FirebaseAuth.getInstance().getUid();

        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches");
        potMatchListView = (ListView) findViewById(R.id.potential_matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfPotMatches);
        potMatchListView.setAdapter(arrayAdapter);


        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<String>();
                // contains data from a firebase location.
                Iterator i = snapshot.getChildren().iterator();
                while(i.hasNext()) {
                    set.add(( (DataSnapshot) i.next()).getKey());
                }

                arrayAdapter.clear();
                arrayAdapter.addAll(set);
                arrayAdapter.notifyDataSetChanged();
                Log.d(TAG, set.toArray().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "FAIL");
            }
        });

        potMatchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                acceptVolunteer((String) adapterView.getItemAtPosition(position).toString());
            }
        });
    }

    private void acceptVolunteer(String volunteerId) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid);
        Log.d(TAG, dbr.toString());
        Log.d(TAG, volunteerId);
        HashMap<String, Object> m = new HashMap<>();
        m.put("msg", "Organization: We are glad you would like to work with us");
        dbr.child(volunteerId).updateChildren(m);

    }
}