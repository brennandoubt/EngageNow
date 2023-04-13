package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
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
    HashMap<String, String> idEmailMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    static String orgEmail, uid, orgName;
    String TAG = "OrgPotMatch";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_potential_matches);
        uid = FirebaseAuth.getInstance().getUid();
        orgEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("name");
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orgName = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid);
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
                    DatabaseReference userEmailDbr = FirebaseDatabase.getInstance().getReference("volunteer_accounts").child(potentialMatchVolunteerId.getKey());
                    userEmailDbr.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String volFirstName, volLastName, volEmail, displayName;
                            HashMap<String, Object> volInfo = (HashMap<String, Object>) snapshot.getValue();
                            volFirstName = (String) volInfo.get("first_name");
                            volLastName = (String) volInfo.get("last_name");
                            volEmail = (String) volInfo.get("email");
                            displayName = volFirstName + " " + volLastName + ": " + volEmail;
                            set.add(displayName);
                            emailIdMap.put(volEmail, potentialMatchVolunteerId.getValue().toString());
                            idEmailMap.put(potentialMatchVolunteerId.getValue().toString(), displayName);
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

        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid);
        Log.d("HERE", dbr.toString());

        dbr.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TAG", "ADDED");
                Set<String> set = new HashSet<>();
                Log.d("TAG", snapshot.getValue().toString());
                String volId = snapshot.getValue().toString();
                DatabaseReference userInfoDbr = FirebaseDatabase.getInstance().getReference("volunteer_accounts").child(volId);
                userInfoDbr.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String volFirstName, volLastName, volEmail;
                        HashMap<String, Object> volInfo = (HashMap<String, Object>) snapshot.getValue();
                        volFirstName = (String) volInfo.get("first_name");
                        volLastName = (String) volInfo.get("last_name");
                        volEmail = (String) volInfo.get("email");
                        String displayName = volFirstName + " " + volLastName + ": " + volEmail;
                        set.add(displayName);
                        emailIdMap.put(volEmail, volId);
                        idEmailMap.put(volId, displayName);
                        arrayAdapter.clear();
                        arrayAdapter.addAll(set);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TAG", "CHANGED");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "REMOVED");
                Log.d("TAG", snapshot.getKey().toString());
                Log.d(TAG, idEmailMap.get(snapshot.getKey().toString()));
                Log.d(TAG, idEmailMap.toString());

                arrayAdapter.remove(idEmailMap.get(snapshot.getKey()));
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TAG", "MOVED");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "CANCELED");

            }
        });
        potMatchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String[] nameEmail = ((TextView)view).getText().toString().split(":");
                String name = nameEmail[0].trim();
                String email = nameEmail[1].trim();
                String volunteerId = emailIdMap.get(email);
                Log.d(TAG, volunteerId);
                acceptVolunteerDialog(name, email, volunteerId);
            }
        });
    }

    private void acceptVolunteerDialog(String name, String volunteerEmail, String volunteerId) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        dialog.setTitle("Accept/Reject This Match");
        dialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, volunteerId);
                DatabaseReference dbrMessageInfo = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId);
                Log.d(TAG, dbrMessageInfo.toString());
//                Log.d(TAG, emailIdMap.get(volunteerId));
                HashMap<String, Object> readMap = new HashMap<>();
                readMap.put("volunteer_read", false);
                readMap.put("organization_read", false);
                dbrMessageInfo.updateChildren(readMap);
                HashMap<String, Object> m = new HashMap<>();
                String user_message_key = dbrMessageInfo.push().getKey();
                dbrMessageInfo.updateChildren(m);

                DatabaseReference dbr2 = dbrMessageInfo.child(user_message_key);
                Map<String, Object> m2 = new HashMap<String, Object>();
                m2.put("msg", name + " and " + orgName + " have been connected!");
                m2.put("user", "Connected");
                dbr2.updateChildren(m2);
                dbrMessageInfo.child(volunteerId).updateChildren(m);
                m = new HashMap<>();
                m.put(volunteerId, null);
                DatabaseReference d = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid).child(volunteerId);
                d.removeValue();
                Log.d("HERE", d.toString());
            }
        });

        dialog.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap<String, Object> m = new HashMap<>();
                m.put(volunteerId, null);
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid);
                Log.d(TAG, dbr.toString());
                dbr.updateChildren(m);
            }
        });

        dialog.show();

    }

//    private void updateList() {
//        arrayAdapter.remove(snapshot.key);
//        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMathes").child(uid);
//        dbr.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Set<String> set = new HashSet<String>();
//                // contains data from a firebase location.
//                Iterator i = snapshot.getChildren().iterator();
//                while (i.hasNext()) {
//                    DataSnapshot potentialMatchVolunteerId = (DataSnapshot) i.next();
//
//                    // get the keys of the users that want to match
//                    // find that users name
//                    DatabaseReference userEmailDbr = FirebaseDatabase.getInstance().getReference("volunteer_accounts").child(potentialMatchVolunteerId.getKey()).child("email");
//                    userEmailDbr.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            Log.d(TAG, "HERE: " + potentialMatchVolunteerId.getValue().toString());
//                            String email = (String) snapshot.getValue();
//                            set.add(email);
//                            emailIdMap.put(email, potentialMatchVolunteerId.getValue().toString());
//                            arrayAdapter.clear();
//                            arrayAdapter.addAll(set);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//
////
//                }
//
//                arrayAdapter.notifyDataSetChanged();
////                Log.d(TAG, set.toArray().toString());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}