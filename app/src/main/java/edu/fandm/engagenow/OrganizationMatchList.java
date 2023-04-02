package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OrganizationMatchList extends AppCompatActivity {
    ListView matchesListView;
    ArrayList<String> listOfMatches = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    String userName;
    String TAG = "OrgMatchList";
    // represents a particular location in database and can be used for reading or writing data to that database location
    private DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("messages");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_match_list);

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
                    set.add(( (DataSnapshot) i.next()).getKey());
                }

                arrayAdapter.clear();
                arrayAdapter.addAll(set);
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
                i.putExtra("selected_volunteer", ((TextView)view).getText().toString());
                i.putExtra("user_name", userName);
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