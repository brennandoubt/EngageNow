package edu.fandm.engagenow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class VolunteerPreferences extends AppCompatActivity {
    private final String TAG = "VOLUNTEER_PREFERENCES";

    FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_preferences);

        // get user ID from last activity
        Intent i = getIntent();
        String user_id = i.getStringExtra("user_id");

        Log.d(TAG, "User ID retrieved for updating: " + user_id);

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        Button update_preferenes_button = (Button) findViewById(R.id.update_preferences_button);
        update_preferenes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // update user's data with the inputs given in each field (starting with user's name)
                String name_inputted = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();
                FirebaseUser user = fbAuth.getCurrentUser();

                Map<String, Object> user_first_name_map = new HashMap<>();
                user_first_name_map.put(user_id, name_inputted);

                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_first_name");
                dbr.updateChildren(user_first_name_map);
            }
        });
    }
}