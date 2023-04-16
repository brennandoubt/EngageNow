package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class VolunteerRegistration extends AppCompatActivity {
    private final String TAG = "VOLUNTEER_REGISTRATION";
    static FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_registration);

        populate_spinners();

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        Button register_account_button = (Button) findViewById(R.id.register_account_button);
        register_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                            // moving to volunteer swiping activity after user is registered
                            Intent vsi = new Intent(getApplicationContext(), VolunteerSwiping.class);
                            startActivity(vsi);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Failed to create new user", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void populate_spinners() {
        // populate time commitment volunteer spinner
        Spinner time_commitment_spinner = (Spinner) findViewById(R.id.time_commitment_volunteer_spinner);
        ArrayAdapter<CharSequence> time_commitment_aa = ArrayAdapter.createFromResource(this,
                R.array.time_commitment_select, android.R.layout.simple_spinner_item);
        time_commitment_aa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        time_commitment_spinner.setAdapter(time_commitment_aa);

        // populate age group volunteer spinner
        Spinner age_spinner = (Spinner) findViewById(R.id.age_group_volunteer_spinner);
        ArrayAdapter<CharSequence> age_aa = ArrayAdapter.createFromResource(this,
                R.array.age_group_select, android.R.layout.simple_spinner_item);
        age_aa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        age_spinner.setAdapter(age_aa);

        // populate travel distance volunteer spinner
        Spinner travel_distance_spinner = (Spinner) findViewById(R.id.travel_distance_volunteer_spinner);
        ArrayAdapter<CharSequence> travel_distance_aa = ArrayAdapter.createFromResource(this,
                R.array.travel_distance_select, android.R.layout.simple_spinner_item);
        travel_distance_aa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        travel_distance_spinner.setAdapter(travel_distance_aa);
    }

}