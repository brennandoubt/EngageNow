package edu.fandm.engagenow;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EventRegistration extends OrganizationBaseClass {

    FirebaseAuth fbAuth;
    private String userId;

    private void populateSpinner(){
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
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkInput(String name, String description, String ageGroup, String timeCommitment, String availability){

        if(name.equals("")) {
            showToast("Event Name Cannot Be Empty");
            return false;
        }
        else if(description.equals("")){
            showToast("Event Description Cannot Be Empty");
            return false;
        }
        else if(ageGroup.equals("Select Age Group")){
            showToast("Must Select Age Group");
            return false;
        }
        else if(timeCommitment.equals("Select Time Commitment")){
            showToast("Must Select Time Commitment");
            return false;
        }
        else if(availability.equals("Select Availability")){
            showToast("Must Select Availability");
            return false;
        }
        else{
            return true;
        }
    }

    private boolean registerEvent(){

        String event_name = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();

        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId).child("events").child(event_name);

        Map<String, Object> orgDBHashmap = new HashMap<>();

        //extract data
        String description = ((EditText) findViewById(R.id.description_et)).getText().toString();

        Spinner ageGroupSpinner = (Spinner) findViewById(R.id.age_group_spinner);
        Spinner timeCommitmentSpinner = (Spinner) findViewById(R.id.time_commitment_spinner);
        Spinner availabilitySpinner =  (Spinner) findViewById(R.id.availability_spinner);

        String ageGroup = ageGroupSpinner.getSelectedItem().toString();
        String timeCommitment = timeCommitmentSpinner.getSelectedItem().toString();
        String availability = availabilitySpinner.getSelectedItem().toString();

        //If the input is invalid,  make the user fill it out
        if(!checkInput(event_name, description, ageGroup, timeCommitment, availability)){
            return false;
        }

        String otherInfo = ((EditText) findViewById(R.id.other_specify_et)).getText().toString();
        boolean hasFbiClearance = ((CheckBox) findViewById(R.id.fbi_cb)).isChecked();
        boolean hasChildClearance = ((CheckBox) findViewById(R.id.child_cb)).isChecked();
        boolean hasCriminalClearance = ((CheckBox) findViewById(R.id.criminal_rb)).isChecked();

        boolean hasLaborSkill = ((CheckBox) findViewById(R.id.labor_skill_cb)).isChecked();
        boolean hasCareTakingSkill = ((CheckBox) findViewById(R.id.careTaking_skill_cb)).isChecked();
        boolean hasFoodServiceSkill = ((CheckBox) findViewById(R.id.food_skill_cb)).isChecked();

        boolean hasSpanish = ((CheckBox) findViewById(R.id.spanish_language_rb)).isChecked();
        boolean hasChinese = ((CheckBox) findViewById(R.id.chinese_language_rb)).isChecked();
        boolean hasGerman = ((CheckBox) findViewById(R.id.german_language_rb)).isChecked();
        boolean hasEnglish = ((CheckBox) findViewById(R.id.english_language_rb)).isChecked();
        boolean hasVehicle = ((CheckBox)findViewById(R.id.vehicle_cb)).isChecked();

        orgDBHashmap.put("event_name", event_name);
        orgDBHashmap.put("description", description);
        orgDBHashmap.put("other_info", otherInfo);
        orgDBHashmap.put("fbi_clearance", hasFbiClearance);
        orgDBHashmap.put("child_clearance", hasChildClearance);
        orgDBHashmap.put("criminal_history", hasCriminalClearance);
        orgDBHashmap.put("labor_skill", hasLaborSkill);
        orgDBHashmap.put("care_taking_skill", hasCareTakingSkill);
        orgDBHashmap.put("food_service_skill", hasFoodServiceSkill);
        orgDBHashmap.put("spanish", hasSpanish);
        orgDBHashmap.put("chinese", hasChinese);
        orgDBHashmap.put("german", hasGerman);
        orgDBHashmap.put("english", hasEnglish);
        orgDBHashmap.put("vehicle", hasVehicle);
        orgDBHashmap.put("age_group", ageGroup);
        orgDBHashmap.put("time_commitment", timeCommitment);
        orgDBHashmap.put("availability", availability);

        //push the data to firebase
        dbr.updateChildren(orgDBHashmap);
        return true;
    }

    private void launchActivity() {
        Intent i = new Intent(getApplicationContext(), EventDashboard.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_registration);

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fbAuth.getCurrentUser();
        userId = user.getUid();

        //Generate the UI
        populateSpinner();

        Button update_preferences_button = (Button) findViewById(R.id.update_preferences_button);
        update_preferences_button.setOnClickListener(View -> {
            if (!registerEvent()) {
                return;
            }
            Toast.makeText(getApplicationContext(), "New event created", Toast.LENGTH_SHORT).show();
            launchActivity();
        });
    }
}











