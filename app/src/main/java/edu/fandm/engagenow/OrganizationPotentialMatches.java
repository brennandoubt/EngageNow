package edu.fandm.engagenow;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrganizationPotentialMatches extends OrganizationBaseClass {
    ListView potMatchListView;
    ArrayList<String> listOfPotMatches = new ArrayList<>();
    HashMap<String, String> emailIdMap = new HashMap<>();
    HashMap<String, String> idEmailMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    static String orgEmail, uid, orgName;
    HashMap<String, Object> volInfoMap;
    String TAG = "OrgPotMatch";
    HashMap<String, HashMap<String, Object>> potentialMatchesMap = new HashMap<>();
    HashMap<String, HashMap<String, Object>> eventsHashmap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_potential_matches);
        setTitle("Interested Volunteers");
        uid = FirebaseAuth.getInstance().getUid();
        orgEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("name");
        dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                orgName = task.getResult().getValue().toString();
            }
        });

        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("events");
        dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                eventsHashmap = (HashMap<String, HashMap<String, Object>>) task.getResult().getValue();
            }
        });

        potMatchListView = (ListView) findViewById(R.id.potential_matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfPotMatches);
        potMatchListView.setAdapter(arrayAdapter);

        dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid);
        dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().getValue() != null) {
                    potentialMatchesMap = (HashMap<String, HashMap<String, Object>>) ((DataSnapshot) task.getResult()).getValue();
                    populatePotentialMatches();
                }
            }
        });

        potMatchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String[] nameEmailAndEvent = ((TextView)view).getText().toString().split(" / ");
                String[] nameEmail = nameEmailAndEvent[0].split(", ");
                String name = nameEmail[0].trim();
                String email = nameEmail[1].trim();
                String event = nameEmailAndEvent[1];
                String volunteerId = emailIdMap.get(email);

                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(volunteerId);
                dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        volInfoMap = (HashMap<String, Object>) task.getResult().getValue();

                        acceptVolunteerDialog(name, volunteerId, event);
                    }
                });
            }
        });
    }

    private void populatePotentialMatches() {
        arrayAdapter.clear();
        for (String key1 : potentialMatchesMap.keySet()) {
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(key1);
            dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    for (String key2 : potentialMatchesMap.get(key1).keySet()) {
                        HashMap<String, Object> m = (HashMap<String, Object>) task.getResult().getValue();

                        if (m != null && (boolean) potentialMatchesMap.get(key1).get(key2)) {

                            String volName = m.get("first_name") + " " + m.get("last_name");
                            String volEmail = (String) m.get("email");
                            idEmailMap.put(key1, volEmail);
                            emailIdMap.put(volEmail, key1);
                            arrayAdapter.add(volName + ", " + volEmail + " / " + key2);
                        }
                    }
                }
            });

        }
        arrayAdapter.notifyDataSetChanged();
    }
    private void acceptVolunteerDialog(String volName, String volunteerId, String eventName) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        dialog.setTitle("Accept/Reject " + volName + "\n" + "Event: " + eventName);
        try {
            String volInfo = getVolInfo(eventName);
            TextView name = new TextView(this);
//        name.set
            name.setText(volInfo);
            name.setTextSize(20);
            name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            name.setPadding(60, 5, 5, 5);
            dialog.setView(name);

            dialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    DatabaseReference dbrMessageInfo = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(uid).child(volunteerId);

                    HashMap<String, Object> readMap = new HashMap<>();
                    readMap.put("volunteer_read", false);
                    readMap.put("organization_read", false);
                    dbrMessageInfo.updateChildren(readMap);
                    HashMap<String, Object> m = new HashMap<>();
                    String user_message_key = dbrMessageInfo.push().getKey();
                    dbrMessageInfo.updateChildren(m);

                    DatabaseReference dbr2 = dbrMessageInfo.child(user_message_key);
                    Map<String, Object> m2 = new HashMap<String, Object>();
                    m2.put("msg", volName + " and " + orgName + " have been connected! " + volName + " is interested in the '" + eventName + "' event.");
                    m2.put("user", "MATCHED");
                    dbr2.updateChildren(m2);
                    dbrMessageInfo.child(volunteerId).updateChildren(m);

                    DatabaseReference d = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid).child(volunteerId);
                    m = new HashMap<>();
                    m.put(eventName, false);
                    d.updateChildren(m);
                    updatePotentialMatches(volunteerId, eventName);
                }
            });

            dialog.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DatabaseReference d = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid).child(volunteerId);
                    HashMap<String, Object> m = new HashMap<>();
                    m.put(eventName, false);
                    d.updateChildren(m);
                    updatePotentialMatches(volunteerId, eventName);
                }
            });

            dialog.show();
        }
        catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "This event has been deleted", Toast.LENGTH_SHORT).show();
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid).child(volunteerId).child(eventName);
            dbr.removeValue();
            updatePotentialMatches(volunteerId, eventName);
            populatePotentialMatches();
        }

    }

    private void updatePotentialMatches(String volunteerId, String eventName) {
        potentialMatchesMap.get(volunteerId).remove(eventName);
        if (potentialMatchesMap.get(volunteerId).size() == 0) {
            potentialMatchesMap.remove(volunteerId);
        }
//        Log.d(TAG, potentialMatchesMap.toString());
        populatePotentialMatches();
    }

    private String getVolInfo(String eventName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Volunteer Info / Event Info \n");
        sb.append("Name: " + volInfoMap.get("first_name") + " " + volInfoMap.get("last_name") + "\n");
        Log.d(TAG + " HERE", eventsHashmap.toString());
        Log.d(TAG, eventName);
        sb.append("Time Commitment: " + volInfoMap.get("time_commitment") + " / " + eventsHashmap.get(eventName).get("time_commitment") + "\n");
        sb.append("Age: " + volInfoMap.get("age_group") + " / " + eventsHashmap.get(eventName).get("age_group") +  "\n");
        sb.append("FBI Clearance: " + volInfoMap.get("fbi_clearance")  + " / " + eventsHashmap.get(eventName).get("fbi_clearance") + "\n");
        sb.append("Child Clearance: " + volInfoMap.get("child_clearance")  + " / " + eventsHashmap.get(eventName).get("child_clearance") + "\n");
        sb.append("Criminal History: " + volInfoMap.get("criminal_history")  + " / " + eventsHashmap.get(eventName).get("criminal_history") + "\n");
        sb.append("English: " + volInfoMap.get("english")  + " / " + eventsHashmap.get(eventName).get("english") + "\n");
        sb.append("Spanish: " + volInfoMap.get("spanish")  + " / " + eventsHashmap.get(eventName).get("spanish") + "\n");
        sb.append("Chinese: " + volInfoMap.get("chinese")  + " / " + eventsHashmap.get(eventName).get("chinese") + "\n");
        sb.append("German: " + volInfoMap.get("german")  + " / " + eventsHashmap.get(eventName).get("german"));

        return sb.toString();
    }

}