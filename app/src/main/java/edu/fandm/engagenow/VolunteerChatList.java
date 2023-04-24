package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VolunteerChatList extends VolunteerBaseClass {
    ListView matchesListView;
    ArrayList<String> listOfMatchesName = new ArrayList<String>();
    HashMap<String, HashMap<String, String>> nameIdMap = new HashMap<>();
    ArrayAdapter arrayAdapter;
    String userId;
    String TAG = "VolunteerChatList";
    HashMap<String, HashMap<String, Object>> orgAccountsMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_chat_list);
        setTitle("Chats");
        matchesListView = (ListView) findViewById(R.id.matches_lv);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfMatchesName);

        matchesListView.setAdapter(arrayAdapter);
        userId = FirebaseAuth.getInstance().getUid();

        populateChats();

        matchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String[] nameEmail = adapterView.getItemAtPosition(position).toString().split(":");

                Intent i = new Intent(getApplicationContext(), VolunteerChat.class);
                i.putExtra("organization_id", nameIdMap.get(nameEmail[1].trim()).get("id"));
                i.putExtra("organization_email", nameEmail[1].trim());
                i.putExtra("organization_name", nameIdMap.get(nameEmail[1].trim()).get("name"));
                String organizationId = nameIdMap.get(nameEmail[1].trim()).get("id");
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(userId).child("volunteer_read");
                dbr.setValue(true);

                startActivity(i);
            }
        });

        matchesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteConversation(view, i);
                return true;
            }
        });
    }

    private void deleteConversation(View view, int idx) {
        String[] nameEmail = arrayAdapter.getItem(idx).toString().split(":");
        String orgName = nameEmail[0].trim();
        String orgEmail = nameEmail[1].trim();

        String orgId = nameIdMap.get(orgEmail).get("id");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DANGER: Remove Match");
        TextView description = new TextView(this);
        description.setText("Are you sure you would like to disconnect with " + orgName + "? This will PERMANENTLY remove your conversation with the organization. You can match with the organization again in the future if you wish.");
        description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        description.setTextSize(15);
        description.setPadding(20, 10, 20, 10);
        builder.setView(description);

        builder.setPositiveButton("DISCONNECT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference dbr1 = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(orgId).child(userId);
                dbr1.removeValue();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

    private void populateChats() {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id");
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "CHANGED");
                arrayAdapter.clear();

                if (snapshot.exists()) {
                    HashMap<String, Boolean> chatMap = new HashMap<>();

                    if (snapshot.exists()) {
                        // for each organization
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Log.d(TAG, ds.getValue().toString());
                            Map<String, Object> m = (Map) ds.getValue();
                            if (m.containsKey(userId)) {
                                String orgId = ds.getKey();
                                Boolean read = (Boolean) ((HashMap<String, HashMap<String, Object>>) ds.getValue()).get(userId).get("volunteer_read");
                                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(orgId);

                                dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        HashMap<String, Object> orgInfo = (HashMap<String, Object>) ((DataSnapshot) task.getResult()).getValue();
                                        String orgEmail = (String) orgInfo.get("email");
                                        String orgName = (String) orgInfo.get("name");

                                        chatMap.put(orgName + ": " + orgEmail, read);
                                        HashMap<String, String> m = new HashMap<>();
                                        m.put("id", orgId);
                                        m.put("name", orgName);
                                        nameIdMap.put(orgEmail, m);
                                        arrayAdapter.clear();
                                        for (String key : chatMap.keySet()) {
                                            if (chatMap.get(key) != null && !chatMap.get(key)) {
                                                arrayAdapter.insert(key, 0);
                                            } else {
                                                arrayAdapter.add(key);
                                            }
                                        }
                                        setReadNotifications();
                                    }
                                });
                            }

                        }

                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setReadNotifications() {
        for (int i = 0; i < matchesListView.getCount(); i++) {
            String email = arrayAdapter.getItem(i).toString().split(":")[1].trim();

            String organizationId = nameIdMap.get(email).get("id");
            final int idx = i;
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages").child("organization_id").child(organizationId).child(userId).child("volunteer_read");
            Task<DataSnapshot> ds = dbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.getResult().exists()) {
                        boolean read = (boolean) task.getResult().getValue();
                        View v = (View) matchesListView.getChildAt(idx);
                        if (!read) {
                            v.setBackgroundColor(getResources().getColor(R.color.light_green));
                        } else {
                            v.setBackgroundColor(Color.TRANSPARENT);
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            });

        }
    }
}