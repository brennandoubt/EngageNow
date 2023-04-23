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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventDashboard extends OrganizationBaseClass {
    ListView eventsListView;
    ArrayList<String> eventsList = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    String uid;
    final String TAG = "Event_Dashboard";
    HashMap<String, HashMap<String, Object>> eventsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_dashboard);
        setTitle("Event Dashboard");

        uid = FirebaseAuth.getInstance().getUid();

        eventsListView = findViewById(R.id.event_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, eventsList);
        eventsListView.setAdapter(arrayAdapter);
        populateEvents();

        ListView eventLv = findViewById(R.id.event_lv);
        eventLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayEvent( ((TextView)view).getText().toString());
            }
        });

        eventLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteEvent(((TextView)view).getText().toString());
                return true;
            }
        });
    }

    private void populateEvents() {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("events");
        dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                eventsMap = (HashMap<String, HashMap<String, Object>>) task.getResult().getValue();
                arrayAdapter.clear();
                if (eventsMap != null) {
                    Log.d(TAG, eventsMap.toString());
                    Set<String> eventNames = new HashSet<>();
                    for (String key : eventsMap.keySet()) {
                        Log.d(TAG, eventsMap.get(key).toString());
                        eventNames.add(key);
                    }
                    Log.d(TAG, eventNames.toString());
                    arrayAdapter.addAll(eventNames);
                    TextView instructions = findViewById(R.id.description);
                    instructions.setText("These are your events. Tap to view. Long click to delete.");
                }
                else {
                    arrayAdapter.clear();
                    TextView instructions = findViewById(R.id.description);
                    instructions.setText("You have no active events. Create events and they will be shown below.");
                }
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    private void displayEvent(String event) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        dialog.setTitle("Event: " + eventsMap.get(event).get("event_name"));

        String eventInfo = getEventInfo(event);
        TextView info = new TextView(this);
        info.setText(eventInfo);
        info.setTextSize(20);
        info.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        info.setPadding(60, 5, 5, 5);
        dialog.setView(info);

        // edit button inside of dialog to edit organization's event
        dialog.setPositiveButton(R.string.org_edit_event_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent epi = new Intent(getApplicationContext(), EventPreferences.class);
                        String event_name = (String) eventsMap.get(event).get("event_name");
                        epi.putExtra("event_name", event_name);
                        startActivity(epi);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        dialog.show();
    }

    private void deleteEvent(String event) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        dialog.setTitle("Delete Event: " + eventsMap.get(event).get("event_name"));

        String eventInfo = getEventInfo(event);
        TextView info = new TextView(this);
        info.setText(eventInfo);
        info.setTextSize(20);
        info.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        info.setPadding(60, 5, 5, 5);
        dialog.setView(info);

        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(uid).child("events").child(event).removeValue();
                DatabaseReference potMatchDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid);
                potMatchDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, HashMap<String, Object>> potMatchesMap = (HashMap<String, HashMap<String, Object>>) task.getResult().getValue();
                            // remove the event from potential matches so if an event with the same name is made in the future, the volunteer will see it
                            if (potMatchesMap != null) {
                                for (String volId : potMatchesMap.keySet()) {
                                    if (potMatchesMap.get(volId).containsKey(event)) {
                                        DatabaseReference eventDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(uid).child(volId);
                                        HashMap<String, Object> m = new HashMap<>();
                                        m.put(event, null);
                                        eventDbr.updateChildren(m);
                                    }
                                }
                            }
                        }
                        Toast.makeText(getApplicationContext(), event + " event has been deleted", Toast.LENGTH_SHORT).show();
                        populateEvents();
                    }
                });


            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
//
        dialog.show();
    }

    private String getEventInfo(String event) {
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
    }
}