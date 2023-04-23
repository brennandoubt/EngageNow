package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

public class EventsList extends AppCompatActivity {
    private final static String TAG = "EVENTS_LIST";

    ArrayList<String> events_list = new ArrayList<>();
    HashMap<String, HashMap<String, Object>> events_map = new HashMap<>();
    ArrayAdapter aa;


    private final static String NAME = "NAME";
    HashMap<String, List<String>> emap = new HashMap<>();

    // group and child items
    private List<String> groupItems = new ArrayList<>();
    private List<List<String>> childItems = new ArrayList<>();

    // group and child data mappings
    List<Map<String, String>> groupData = new ArrayList<>();
    List<List<Map<String, String>>> childData = new ArrayList<>();

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

        /*
         * Code below adapted from this source:
         * https://abhiandroid.com/ui/simpleexpandablelistadapter-example-android-studio.html
         */
        String groupFrom[] = {NAME};
        int groupTo[] = {R.id.heading};
        String childFrom[] = {NAME};
        int childTo[] = {R.id.childItem};

        // add data in group and child list
        populate_events_exp();
        //List<String> headers = new ArrayList<>(emap.keySet()); // headers for each events list is just the key values for each pairing in map

        for (int i = 0; i < groupItems.size(); i++) {
            Map<String, String> curGroupMap = new HashMap<>();
            groupData.add(curGroupMap);
            curGroupMap.put(NAME, groupItems.get(i));

            List<Map<String, String>> children = new ArrayList<>();
            for (int j = 0; j < childItems.get(i).size(); j++) {
                Map<String, String> curChildMap = new HashMap<>();
                children.add(curChildMap);
                curChildMap.put(NAME, childItems.get(i).get(j));
            }
            childData.add(children);
        }
        Log.d(TAG, "List mappings for groups: " + emap.toString());

        // initiate expandable list view
        ExpandableListView elv = (ExpandableListView) findViewById(R.id.events_list_elv);

        // set up the adapter
        ExpandableListAdapter ela = new SimpleExpandableListAdapter(this, groupData,
                R.layout.group_items,
                groupFrom, groupTo,
                childData, R.layout.child_items, childFrom, childTo);
        elv.setAdapter(ela);

        elv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Group Name Is :" + groupItems.get(i), Toast.LENGTH_LONG).show();

                return false;
            }
        });
        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                Toast.makeText(getApplicationContext(), "Child Name Is :" + childItems.get(i).get(i1), Toast.LENGTH_LONG).show();

                return false;
            }
        });
    }

    private void populate_events_exp() {
        DatabaseReference organizations_dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts");
        organizations_dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot child : task.getResult().getChildren()) {
                    Log.d(TAG, child.getKey().toString()); // child keys are organization user id nodes
                    String org_uid = child.getKey().toString();

                    // get this org's name to add to headers for expandable list view
                    DataSnapshot name_ds = child.child("name");
                    Log.d(TAG, "Name: " + name_ds.getValue().toString());
                    String org_name = name_ds.getValue().toString(); // each organization's name

                    groupItems.add(org_name);

                    // list of events (value) for each organization (key) in the events map
                    List<String> org_events = new ArrayList<>();
                    DataSnapshot events_ds = child.child("events");
                    for (DataSnapshot e : events_ds.getChildren()) {
                        String event = e.getKey().toString();  // each event for this organization
                        Log.d(TAG, "Event: " + event);

                        org_events.add(event);
                    }
                    emap.put(org_name, org_events); // put organization's name-events pairings into data map for expandable list view

                    childItems.add(org_events);
                }
            }
        });
    }
}