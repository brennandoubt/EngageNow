package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class VolunteerRegistration extends AppCompatActivity {
    private final String TAG = "VOLUNTEER_REGISTRATION";
    static FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_registration);
        setTitle("Volunteer Registration");
        populate_spinners();

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        Button register_account_button = (Button) findViewById(R.id.register_account_button);
        register_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAccount();
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
                R.array.age_select, android.R.layout.simple_spinner_item);
        age_aa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        age_spinner.setAdapter(age_aa);

        // populate travel distance volunteer spinner
        Spinner travel_distance_spinner = (Spinner) findViewById(R.id.travel_distance_volunteer_spinner);
        ArrayAdapter<CharSequence> travel_distance_aa = ArrayAdapter.createFromResource(this,
                R.array.travel_distance_select, android.R.layout.simple_spinner_item);
        travel_distance_aa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        travel_distance_spinner.setAdapter(travel_distance_aa);
    }

    private void getNotificationToken(String userId){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.d("Token registration", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token that is associated with the device

                String notificationToken = task.getResult();
                Log.d("GENERATE TOKEN", notificationToken);
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(userId);
                Map<String, Object> orgDBHashmap = new HashMap<>();
                orgDBHashmap.put("notification", notificationToken);
                dbr.updateChildren(orgDBHashmap);
            }
        });
    }

    private void registerAccount() {
        // get data from last Register activity (if registering new user)
        Intent i = getIntent();
        String email = i.getStringExtra("email");
        String password = i.getStringExtra("password");

        // get preferences typed by user in activity
        String first_name_inputted = ((EditText) findViewById(R.id.name_preference_et)).getText().toString().trim();
        String last_name_inputted = ((EditText) findViewById(R.id.last_name_preference_et)).getText().toString().trim();
        String time_commitment = ((Spinner) findViewById(R.id.time_commitment_volunteer_spinner)).getSelectedItem().toString();
        String age_group = ((Spinner) findViewById(R.id.age_group_volunteer_spinner)).getSelectedItem().toString();
        String travel_distance = ((Spinner) findViewById(R.id.travel_distance_volunteer_spinner)).getSelectedItem().toString();
        boolean fbi_certification = ((CheckBox) findViewById(R.id.fbi_vcb)).isChecked();
        boolean child_certification = ((CheckBox) findViewById(R.id.child_vcb)).isChecked();
        boolean criminal_history = ((CheckBox) findViewById(R.id.criminal_vcb)).isChecked();
        boolean english = ((CheckBox) findViewById(R.id.english)).isChecked();
        boolean spanish = ((CheckBox) findViewById(R.id.spanish)).isChecked();
        boolean german = ((CheckBox) findViewById(R.id.german)).isChecked();
        boolean chinese = ((CheckBox) findViewById(R.id.chinese)).isChecked();

        if (first_name_inputted.equals("") || last_name_inputted.equals("") || time_commitment.equals("Select Time Commitment") || age_group.equals("Select Age Group") || travel_distance.equals("Select Travel Distance")) {
            Toast.makeText(getApplicationContext(), "All Text and Dropdown Fields Are Required!", Toast.LENGTH_LONG).show();
            return;
        }
        // registering new user with preferences
        Task s = fbAuth.createUserWithEmailAndPassword(email, password);
        s.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
//                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                if (task.isSuccessful()) {
                    FirebaseUser user = fbAuth.getCurrentUser();
                    String userId = user.getUid();

                    //generate the notification token
                    getNotificationToken(userId);

                    // store account type under "account_type/" in Realtime Database
                    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
                    Map<String, Object> accountTypeMap = new HashMap<>();
                    accountTypeMap.put(userId, "volunteer_account");
                    dbr.updateChildren(accountTypeMap);

                    // store user account preferences under "volunteer_accounts/[user_id]/" in Realtime Database
                    dbr  = FirebaseDatabase.getInstance().getReference().getRoot().child("volunteer_accounts").child(userId);
                    HashMap<String, Object> m = new HashMap<>();
                    m.put("account_type", "volunteer_account");
                    m.put("first_name", first_name_inputted);
                    m.put("last_name", last_name_inputted);
                    m.put("email", email);
                    m.put("time_commitment", time_commitment);
                    m.put("age_group", age_group);
                    m.put("travel_distance", travel_distance);
                    m.put("fbi_clearance", fbi_certification);
                    m.put("child_clearance", child_certification);
                    m.put("criminal_history", criminal_history);
                    m.put("english", english);
                    m.put("spanish", spanish);
                    m.put("german", german);
                    m.put("chinese", chinese);

                    dbr.updateChildren(m);

                    Toast.makeText(getApplicationContext(), "New user created", Toast.LENGTH_LONG).show();

                    // moving to volunteer swiping activity after user is registered
                    Intent vsi = new Intent(getApplicationContext(), VolunteerSwiping.class);
                    startActivity(vsi);
                    finish();
                }
                else {
                    if (task.getException().getMessage().equals("The email address is already in use by another account.")) {
                        Toast.makeText(getApplicationContext(), "Failed to create new user: Email address in use", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Failed to create new user", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}