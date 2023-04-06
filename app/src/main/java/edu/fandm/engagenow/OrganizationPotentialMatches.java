package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
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
import java.util.Map;
import java.util.Set;

public class OrganizationPotentialMatches extends OrganizationBaseClass {
    ListView potMatchListView;
    ArrayList<String> listOfPotMatches = new ArrayList<>();
    HashMap<String, String> emailIdMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    static String uid;
    static String orgEmail;
    String TAG = "OrgPotMatch";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_potential_matches);
        uid = FirebaseAuth.getInstance().getUid();
        orgEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid);
        Log.d(TAG, dbr.toString());
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
                    DataSnapshot potentialMatchVolunteerId = (DataSnapshot) i.next();

                        // get the keys of the users that want to match
                            // find that users name
                    DatabaseReference userEmailDbr = FirebaseDatabase.getInstance().getReference("volunteer_accounts").child(potentialMatchVolunteerId.getKey()).child("email");
                    userEmailDbr.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d(TAG, "HERE: " + potentialMatchVolunteerId.getValue().toString());
                            String email = (String) snapshot.getValue();
                            set.add(email);
                            emailIdMap.put(email, potentialMatchVolunteerId.getValue().toString());
                            arrayAdapter.clear();
                            arrayAdapter.addAll(set);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

//
                }


                arrayAdapter.notifyDataSetChanged();
//                Log.d(TAG, set.toArray().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "FAIL");
            }
        });

        potMatchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String email = (String) adapterView.getItemAtPosition(position).toString();
                acceptVolunteerDialog(email, emailIdMap.get(email));
            }
        });
    }

    private void acceptVolunteerDialog(String volunteerEmail, String volunteerId) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        dialog.setCancelable(false);

        dialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId);
                Log.d(TAG, dbr.toString());
//                Log.d(TAG, emailIdMap.get(volunteerId));
                HashMap<String, Object> m = new HashMap<>();

                String user_message_key = dbr.push().getKey();
                dbr.updateChildren(m);

                DatabaseReference dbr2 = dbr.child(user_message_key);
                Map<String, Object> m2 = new HashMap<String, Object>();
                m2.put("msg", volunteerEmail + " and " + orgEmail + " have been connected!");
                m2.put("user", "Connected");
                dbr2.updateChildren(m2);
                dbr.child(volunteerId).updateChildren(m);
            }
        });

        dialog.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap<String, Object> m = new HashMap<>();
                m.put(volunteerId, null);
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches");
                dbr.updateChildren(m);
            }
        });

        dialog.show();

    }
}