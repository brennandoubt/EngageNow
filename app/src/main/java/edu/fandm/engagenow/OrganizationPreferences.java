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

public class OrganizationPreferences extends OrganizationBaseClass {
    private final String TAG = "ORG_PREFERENCES";

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
                // get data from last Register activity
                Intent i = getIntent();
                String email = i.getStringExtra("email");
                String password = i.getStringExtra("password");
                String accountType = i.getStringExtra("account_type");

                Task s = fbAuth.createUserWithEmailAndPassword(email, password);
                s.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
//                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        if (task.isSuccessful()) {
                            FirebaseUser user = fbAuth.getCurrentUser();
                            String userId = user.getUid();

                            // update user's data with the inputs given in each field (starting with user's name)
                            String name_inputted = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();
                            Map<String, Object> user_first_name_map = new HashMap<>();
                            user_first_name_map.put(userId, name_inputted);

                            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_first_name");
                            dbr.updateChildren(user_first_name_map);

//                            in database, store the type of account that the user is
                            Map<String, Object> accountTypeMap = new HashMap<>();
                            accountTypeMap.put(userId, accountType);

                            dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
                            dbr.updateChildren(accountTypeMap);

                            // go into preferences activity
                            Toast.makeText(getApplicationContext(), "New organization user created", Toast.LENGTH_LONG).show();

                            dbr  = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId);
                            HashMap<String, Object> m = new HashMap<>();

                            m.put("email", email);
                            dbr.updateChildren(m);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Failed to create new organization user", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}