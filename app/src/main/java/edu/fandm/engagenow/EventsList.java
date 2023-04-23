package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EventsList extends AppCompatActivity {
    private final String TAG = "EVENTS_LIST";

    ArrayList<String> events_list = new ArrayList<>();
    HashMap<String, HashMap<String, Object>> events_map = new HashMap<>();

    ArrayAdapter aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);
        /**
         * ---Node paths in Firebase's Realtime Database---
         * An organization's events - _root/organization_accounts/[uid]/events/_
         *
         * ---Questions about Implementing in App---
         * Which activity/click is this activity started from? (add Intent code there)
         *  ...started from VolunteerBaseClass as a menu item (goes here when the menu item is clicked)
         *      - put organization's uid in the Intent used to start this activity
         *      - retrieve organization's uid from this Intent here
         * Are the events displayed the same way they are in the organization's events list activity? (assuming they are for now)
         */
        String uid_of_organization = "JoHtDtO4wXd8Qq3DgztLg5Bu6uB3"; // test example using the "borg@example.com" organization account (with name "test org")

        ListView events_lv = findViewById(R.id.vevents_list_lv);
        aa = new ArrayAdapter(this, android.R.layout.simple_list_item_1, events_list);
        events_lv.setAdapter(aa);
        populate_events(uid_of_organization);

        // 0) put the requested organization's uid in this Intent (in the activity where this Intent is created)

        // 1) get organization's uid from this Intent

        // 2) go to organization's events data location inside the Realtime Database

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
                    TextView instructions = findViewById(R.id.vevents_list_tv);
                    instructions.setText("These are [organization name]'s events. Tap an event to see its details.");
                } else {
                    aa.clear();
                    TextView instructions = findViewById(R.id.description);
                    instructions.setText("Sorry, there are no active events for this organization at the moment.");
                }
            }
        });
    }
}