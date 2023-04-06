package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Set;

public class OrganizationChatList extends OrganizationBaseClass {
    ListView matchesListView;
    ArrayList<String> listOfMatches = new ArrayList<String>();
    HashMap<String, String> volIdMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    String userName;
    String TAG = "OrgChatList";
    static String uid;
    // represents a particular location in database and can be used for reading or writing data to that database location
    private DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_match_list);
        uid = FirebaseAuth.getInstance().getUid();
        dbr = dbr.child(uid);
        matchesListView = (ListView) findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfMatches);

        matchesListView.setAdapter(arrayAdapter);

        Log.d(TAG, dbr.toString());
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<String>();
                // contains data from a firebase location.
                Iterator i = snapshot.getChildren().iterator();
                while(i.hasNext()) {
                    String volunteerKey = ( (DataSnapshot) i.next()).getKey();

                    DatabaseReference volunteerAccDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(volunteerKey).child("email");
                    volunteerAccDbr.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("YOU", snapshot.toString());
                            String volEmail = snapshot.getValue().toString();
                            set.add(volEmail);
                            volIdMap.put(volEmail, volunteerKey);
                            arrayAdapter.clear();
                            arrayAdapter.addAll(set);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }


                arrayAdapter.notifyDataSetChanged();
                Log.d(TAG, set.toArray().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "FAIL");
            }
        });


        matchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent i = new Intent(getApplicationContext(), OrganizationChat.class);
                userName = ((TextView)view).getText().toString();
                i.putExtra("selected_volunteer", userName);
                i.putExtra("user_name", userName);
                Log.d("HERE", userName);
                i.putExtra("volunteer_id", volIdMap.get(userName));
                startActivity(i);
            }
        });

        Button potMatchButton = (Button) findViewById(R.id.pot_match_button);
        potMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), OrganizationPotentialMatches.class);
                startActivity(i);
            }
        });

    }

    private void getUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText userNameET = new EditText(this);

        builder.setView(userNameET);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userName = userNameET.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getUserName();
            }
        });
        builder.show();
    }

}