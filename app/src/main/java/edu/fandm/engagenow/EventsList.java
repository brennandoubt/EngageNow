package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventsList extends VolunteerBaseClass {
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

    /*
     * Code below for Expandable List View is adapted from this source:
     * https://abhiandroid.com/ui/simpleexpandablelistadapter-example-android-studio.html
     */
    String[] groupFrom = {NAME};
    int[] groupTo = {R.id.heading};
    String[] childFrom = {NAME};
    int[] childTo = {R.id.childItem};

    //ArrayList<DataSnapshot> all_events_list = new ArrayList<>();
    HashMap<String, HashMap<String, Object>> all_events_map = new HashMap<>();
    ArrayList<String> all_org_ids = new ArrayList<>();
    String curr_org_id_clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        // initiate expandable list view
        ExpandableListView elv = (ExpandableListView) findViewById(R.id.events_list_elv);

        // set up the adapter
        ExpandableListAdapter ela = new SimpleExpandableListAdapter(this, groupData,
                R.layout.group_items,
                groupFrom, groupTo,
                childData, R.layout.child_items, childFrom, childTo);
        elv.setAdapter(ela);

        populate_events_exp(); // update events list data in the list adapter


        elv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(), "Group Name Is :" + groupItems.get(i), Toast.LENGTH_LONG).show();
//                Log.d(TAG, "Group Name Is: " + groupItems.get(i));
                curr_org_id_clicked = all_org_ids.get(i);

//                Log.d(TAG, "Group ID is: " + curr_org_id_clicked);
                //curr_org_id_clicked = groupItems.get(i);
                return false;
            }
        });
        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //Toast.makeText(getApplicationContext(), "Child Name Is :" + childItems.get(i).get(i1), Toast.LENGTH_LONG).show();
                String event_name = childItems.get(i).get(i1);
                Log.d(TAG, "Child Name Is: " + event_name);

                String org_id_for_this_event = all_org_ids.get(i); // org id that owns this event
                Log.d(TAG, "Organization for this child event: " + org_id_for_this_event);

                String event_data = get_event_info(event_name);

                AlertDialog.Builder dialog = new AlertDialog.Builder(EventsList.this);
                dialog.setCancelable(true);
                dialog.setTitle("Event: " + event_name);

                TextView info = new TextView(EventsList.this);
                info.setText(event_data);
                info.setTextSize(20);
                info.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                info.setPadding(60, 5, 5, 5);
                dialog.setView(info);

                //dialog.setTitle("Event: " + )
                dialog.setPositiveButton("Like Event", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(EventsList.this, "Event Liked!", Toast.LENGTH_SHORT).show();

                        // add this user to this event's potential matches
                        DatabaseReference group_org_database_location = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(org_id_for_this_event).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Map<String, Object> m = new HashMap<>();
                        m.put(event_name, true);
                        group_org_database_location.updateChildren(m);

                        Log.d(TAG, "Updated potential matches for event: " + event_name + " under this org uid: " + curr_org_id_clicked);
                    }
                });
                dialog.show();

                return false;
            }
        });
    }

    private void populate_events_exp() {
        DatabaseReference organizations_dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts");
        organizations_dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                // retrieving group and child list items from database
                for (DataSnapshot child : task.getResult().getChildren()) {
                    Log.d(TAG, child.getKey().toString()); // child keys are organization user id nodes
                    String org_uid = child.getKey().toString();
                    all_org_ids.add(org_uid); // org id's index in all_org_ids corresponds to org name's groupItems

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

                        Log.d(TAG, "Event data: " + e.toString());
                        all_events_map.put(e.getKey(), (HashMap<String, Object>) e.getValue());

                        org_events.add(event);
                    }
                    emap.put(org_name, org_events); // put organization's name-events pairings into data map for expandable list view
                    childItems.add(org_events);
                }
                Log.d(TAG, "All events mapped: " + all_events_map.toString());

                // re-formatting list items to map organizations to their events
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
                ExpandableListAdapter ela = new SimpleExpandableListAdapter(getApplicationContext(), groupData,
                        R.layout.group_items,
                        groupFrom, groupTo,
                        childData, R.layout.child_items, childFrom, childTo);
                elv.setAdapter(ela);
            }
        });
    }

    private String get_event_info(String event_name) {
        StringBuilder sb = new StringBuilder();

        HashMap<String, Object> eventInfo = all_events_map.get(event_name);

        sb.append("Description: " + eventInfo.get("description") + "\n");
        sb.append("Location/Start Time: " + eventInfo.get("location_start_time") + "\n");
        sb.append("Start Date: " + eventInfo.get("start_date") + "\n");
        sb.append("Time Commitment: " + eventInfo.get("time_commitment") + "\n");
        sb.append("Age Group: " + eventInfo.get("age_group") + "\n");
        sb.append("Availability: " + eventInfo.get("availability") + "\n");

        if (String.valueOf(eventInfo.get("fbi_clearance")).equals("true")) {
            sb.append("Requires FBI Clearance\n");
        }
        if (String.valueOf(eventInfo.get("child_clearance")).equals("true")) {
            sb.append("Requires Child Clearance\n");
        }
        if (String.valueOf(eventInfo.get("criminal_history")).equals("true")) {
            sb.append("Criminal History Checked\n");
        }
        if (String.valueOf(eventInfo.get("labor_skill")).equals("true")) {
            sb.append("Requires Labor\n");
        }
        if (String.valueOf(eventInfo.get("care_taking_skill")).equals("true")) {
            sb.append("Requires Care-taking\n");
        }
        if (String.valueOf(eventInfo.get("food_service_skill")).equals("true")) {
            sb.append("Can do Food Service\n");
        }

        if (String.valueOf(eventInfo.get("english")).equals("true")) {
            sb.append("Must speak English\n");
        }
        if (String.valueOf(eventInfo.get("spanish")).equals("true")) {
            sb.append("Must speak Spanish\n");
        }
        if (String.valueOf(eventInfo.get("chinese")).equals("true")) {
            sb.append("Must speak Chinese\n");
        }
        if (String.valueOf(eventInfo.get("german")).equals("true")) {
            sb.append("Must speak German\n");
        }

        if (String.valueOf(eventInfo.get("vehicle")).equals("true")) {
            sb.append("Need a Vehicle\n");
        }

        if (!String.valueOf(eventInfo.get("other_info")).equals("")) {
            sb.append("Other Info: " + eventInfo.get("other_info") + "\n");
        }

        return sb.toString();
    }

    // Copied helper function from EventDashboard.java
    /*private String getEventInfo(String event) {
        StringBuilder sb = new StringBuilder();

        HashMap<String, Object> eventInfo = eventsMap.get(event);

        sb.append("Description: " + eventInfo.get("description") + "\n");

        sb.append("Location/Start Time: " + eventInfo.get("location_start_time") + "\n");

        sb.append("Start Date: " + eventInfo.get("start_date") + "\n");

        sb.append("Time Commitment: " + eventInfo.get("time_commitment") + "\n");

        sb.append("Age Group: " + eventInfo.get("age_group") + "\n");

        sb.append("Availability: " + eventInfo.get("availability") + "\n");

        sb.append("Fbi Clearance: " + eventInfo.get("fbi_clearance") + "\n");

        sb.append("Child Clearance: " + eventInfo.get("child_clearance") + "\n");

        sb.append("Criminal History: " + eventInfo.get("criminal_history") + "\n");

        sb.append("Labor Skill: " + eventInfo.get("labor_skill") + "\n");

        sb.append("Care Taking Skill: " + eventInfo.get("care_taking_skill") + "\n");

        sb.append("Food Service Skill: " + eventInfo.get("food_service_skill") + "\n");

        sb.append("English: " + eventInfo.get("english") + "\n");

        sb.append("Spanish: " + eventInfo.get("spanish") + "\n");

        sb.append("Chinese: " + eventInfo.get("chinese") + "\n");

        sb.append("German: " + eventInfo.get("german") + "\n");

        sb.append("Vehicle: " + eventInfo.get("vehicle") + "\n");

        sb.append("Other Info: " + eventInfo.get("other_info") + "\n");

        return sb.toString();
    }*/
}