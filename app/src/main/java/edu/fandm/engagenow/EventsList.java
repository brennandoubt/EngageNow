package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventsList extends VolunteerBaseClass {
    private final String TAG = "EVENTS_LIST";

    ArrayList<String> events_list = new ArrayList<>();
    HashMap<String, HashMap<String, Object>> events_map = new HashMap<>();
    ArrayAdapter aa;

    ExpandableListAdapter ela;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);
        /**
         * ---Node paths in Firebase's Realtime Database---
         * An organization's events - _root/organization_accounts/[uid]/events/_
         *
         * This activity is started from VolunteerBaseClass as a menu item (goes here when the menu item is clicked)
         *  - retrieve each organization's uid from _root/organization_accounts/[uids]_ in Realtime Database
         *
         * Are the events displayed the same way they are in the organization's events list activity? (assuming they are for now)
         */
        String uid_of_organization = "JoHtDtO4wXd8Qq3DgztLg5Bu6uB3"; // test example using the "borg@example.com" organization account (with name "test org")

        // initiate expandable list view
        ExpandableListView events_elv = (ExpandableListView) findViewById(R.id.events_list_elv);

        // create lists for group and child items
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();

        // add data in group and child list
    }

    /**
     * Adapted from Noah's populateEvents function in EventDashboard.java
     */
    private void populate_events(String org_uid) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(org_uid).child("events");
        dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                events_map = (HashMap<String, HashMap<String, Object>>) task.getResult().getValue();
                aa.clear();
                if (events_map != null) {
                    Log.d(TAG, events_map.toString());
                    Set<String> event_names = new HashSet<>();
                    for (String key : events_map.keySet()) {
                        Log.d(TAG, events_map.get(key).toString());
                        event_names.add(key);
                    }
                    Log.d(TAG, event_names.toString());
                    aa.addAll(event_names);

                    // getting organization's name
                    TextView instructions = findViewById(R.id.desc_events_list_tv);
                    instructions.setText("These are [organization name]'s events. Tap an event to see its details.");
                } else {
                    aa.clear();
                    TextView instructions = findViewById(R.id.description);
                    instructions.setText("Sorry, there are no active events for this organization at the moment.");
                }
            }
        });
    }

    private void populate_events_exp() {
        DatabaseReference organizations_dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts");
        organizations_dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot child : task.getResult().getChildren()) {
                    Log.d(TAG, child.getKey().toString());

                }
            }
        });
    }
}