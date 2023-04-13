package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class VolunteerPreferences extends AppCompatActivity {
    private final String TAG = "VOLUNTEER_PREFERENCES";

    static FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_preferences);

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        // get user ID from last activity
        //Intent i = getIntent();
        //String user_id = i.getStringExtra("user_id");
        //Log.d(TAG, "User ID retrieved for updating: " + user_id);

        Button update_preferenes_button = (Button) findViewById(R.id.update_preferences_button);
        update_preferenes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // user already logged-in with preferences
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseUser user = fbAuth.getCurrentUser();
                    String userId = user.getUid();

                    // get preferences typed by user in activity
                    String name_inputted = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();
                    String last_name_inputted = ((EditText) findViewById(R.id.last_name_preference_et)).getText().toString();

                    // store user account preferences under "volunteer_accounts/[user_id]/" in Realtime Database
                    DatabaseReference dbr  = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(userId);
                    HashMap<String, Object> m = new HashMap<>();
                    m.put("first_name", name_inputted);
                    m.put("last_name", last_name_inputted);
                    dbr.updateChildren(m);

                    Toast.makeText(getApplicationContext(), "User preferences updated!", Toast.LENGTH_LONG).show();
                    return;
                }

                // get data from last Register activity (if registering new user)
                Intent i = getIntent();
                String email = i.getStringExtra("email");
                String password = i.getStringExtra("password");

                // registering new user with preferences
                Task s = fbAuth.createUserWithEmailAndPassword(email, password);
                s.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
//                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        if (task.isSuccessful()) {
                            FirebaseUser user = fbAuth.getCurrentUser();
                            String userId = user.getUid();

                            // get preferences typed by user in activity
                            String name_inputted = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();
                            String last_name_inputted = ((EditText) findViewById(R.id.last_name_preference_et)).getText().toString();

                            // store account type under "account_type/" in Realtime Database
                            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
                            Map<String, Object> accountTypeMap = new HashMap<>();
                            accountTypeMap.put(userId, "volunteer_account");
                            dbr.updateChildren(accountTypeMap);

                            // store user account preferences under "volunteer_accounts/[user_id]/" in Realtime Database
                            dbr  = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(userId);
                            HashMap<String, Object> m = new HashMap<>();
                            m.put("account_type", "volunteer_account");
                            m.put("first_name", name_inputted);
                            m.put("last_name", last_name_inputted);
                            m.put("email", email);
                            dbr.updateChildren(m);

                            Toast.makeText(getApplicationContext(), "New user created", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Failed to create new user", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}