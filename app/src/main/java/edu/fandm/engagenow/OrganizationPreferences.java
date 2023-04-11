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
import android.widget.RadioButton;
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

public class OrganizationPreferences extends OrganizationBaseClass {
    private final String TAG = "ORG_PREFERENCES";

    FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_preferences);

        // get user ID from last activity
        Intent i = getIntent();
        String user_id = i.getStringExtra("user_id");

        Log.d(TAG, "User ID retrieved for updating: " + user_id);

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        //time commitment drop down
        Spinner timeDropDown = findViewById(R.id.time_commitment_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_commitment_select, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        timeDropDown.setAdapter(adapter);

        //age group drop down
        Spinner ageGroupDropDown = findViewById(R.id.age_group_spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.age_group_select, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(R.layout.custom_spinner_item);
        ageGroupDropDown.setAdapter(adapter1);

        //availability drop down
        Spinner availabilityGroupDropDown = findViewById(R.id.availability_spinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.availability_select, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(R.layout.custom_spinner_item);
        availabilityGroupDropDown.setAdapter(adapter2);



        Button update_preferences_button = (Button) findViewById(R.id.update_preferences_button);
        update_preferences_button.setOnClickListener(new View.OnClickListener() {
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

                            // Extract the data from the views first

                            //EdiText
                            String name = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();
                            String description = ((EditText) findViewById(R.id.description_et)).getText().toString();
                            String otherLanguageET = ((EditText) findViewById(R.id.other_specify_et)).getText().toString();

                            //Radio Buttons
                            boolean hasFbiClearance = ((RadioButton) findViewById(R.id.fbi_rb)).isChecked();
                            boolean hasChildClearance = ((RadioButton) findViewById(R.id.child_rb)).isChecked();
                            boolean hasCriminalClearance = ((RadioButton) findViewById(R.id.criminal_rb)).isChecked();

                            boolean hasLaborSkill = ((RadioButton) findViewById(R.id.labor_skill_rb)).isChecked();
                            boolean hasCareTakingSkill = ((RadioButton) findViewById(R.id.careTaking_skill_rb)).isChecked();
                            boolean hasFoodServiceSkill = ((RadioButton) findViewById(R.id.food_skill_rb)).isChecked();

                            boolean hasSpanish = ((RadioButton) findViewById(R.id.spanish_language_rb)).isChecked();
                            boolean hasChinese = ((RadioButton) findViewById(R.id.chinese_language_rb)).isChecked();
                            boolean hasGerman = ((RadioButton) findViewById(R.id.german_language_rb)).isChecked();
                            boolean hasOtherLanguageRB = ((RadioButton) findViewById(R.id.others_language_rb)).isChecked();

                            //CheckBox
                            boolean hasVehicle = ((CheckBox)findViewById(R.id.vehicle_cb)).isChecked();

                            //Spinner











                            Map<String, Object> user_first_name_map = new HashMap<>();
                            user_first_name_map.put(userId, name);

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