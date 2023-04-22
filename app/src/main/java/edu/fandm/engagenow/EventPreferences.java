package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EventPreferences extends OrganizationBaseClass {
    private final String TAG = "EVENT_PREFERENCES";
    FirebaseAuth fbAuth;
    private String userId;
    private long lastClickTime;

    private void populateSpinner(){
        //time commitment drop down
        Spinner timeDropDown = findViewById(R.id.ptime_commitment_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_commitment_select, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        timeDropDown.setAdapter(adapter);

        //age group drop down
        Spinner ageGroupDropDown = findViewById(R.id.page_group_spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.age_group_select, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(R.layout.custom_spinner_item);
        ageGroupDropDown.setAdapter(adapter1);

        //availability drop down
        Spinner availabilityGroupDropDown = findViewById(R.id.pavailability_spinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.availability_select, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(R.layout.custom_spinner_item);
        availabilityGroupDropDown.setAdapter(adapter2);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkInput(String ageGroup, String timeCommitment, String availability){

       if(timeCommitment.equals("Select Time Commitment")){
            showToast("Must Select Time Commitment");
            return false;
        }
        else if(ageGroup.equals("Select Age Group")){
            showToast("Must Select Age Group");
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

    private void updateEvent(String event_name){
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId).child("events").child(event_name);
        dbr.get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                addEvent(event_name);
            }
        });
    }

    private void addEvent(String event_name) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId).child("events").child(event_name);

        Map<String, Object> orgDBHashmap = new HashMap<>();

        //extract data
        Spinner ageGroupSpinner = (Spinner) findViewById(R.id.page_group_spinner);
        Spinner timeCommitmentSpinner = (Spinner) findViewById(R.id.ptime_commitment_spinner);
        Spinner availabilitySpinner = (Spinner) findViewById(R.id.pavailability_spinner);

        String ageGroup = ageGroupSpinner.getSelectedItem().toString();
        String timeCommitment = timeCommitmentSpinner.getSelectedItem().toString();
        String availability = availabilitySpinner.getSelectedItem().toString();

        //If the input is invalid,  make the user fill it out
        if (!checkInput(ageGroup, timeCommitment, availability)) {
            return;
        }

        Log.d(TAG, Integer.toString(((DatePicker) findViewById(R.id.pstart_date)).getMonth()) + ((DatePicker) findViewById(R.id.pstart_date)).getDayOfMonth() + ((DatePicker) findViewById(R.id.pstart_date)).getYear());

        DatePicker datePicker = ((DatePicker) findViewById(R.id.pstart_date));
        String date = datePicker.getMonth() + "/" + datePicker.getDayOfMonth() + "/" + datePicker.getYear();
        String otherInfo = ((EditText) findViewById(R.id.pother_specify_et)).getText().toString().trim();
        boolean hasFbiClearance = ((CheckBox) findViewById(R.id.pfbi_cb)).isChecked();
        boolean hasChildClearance = ((CheckBox) findViewById(R.id.pchild_cb)).isChecked();
        boolean hasCriminalClearance = ((CheckBox) findViewById(R.id.pcriminal_rb)).isChecked();

        boolean hasLaborSkill = ((CheckBox) findViewById(R.id.plabor_skill_cb)).isChecked();
        boolean hasCareTakingSkill = ((CheckBox) findViewById(R.id.pcareTaking_skill_cb)).isChecked();
        boolean hasFoodServiceSkill = ((CheckBox) findViewById(R.id.pfood_skill_cb)).isChecked();

        boolean hasSpanish = ((CheckBox) findViewById(R.id.pspanish_language_rb)).isChecked();
        boolean hasChinese = ((CheckBox) findViewById(R.id.pchinese_language_rb)).isChecked();
        boolean hasGerman = ((CheckBox) findViewById(R.id.pgerman_language_rb)).isChecked();
        boolean hasEnglish = ((CheckBox) findViewById(R.id.penglish_language_rb)).isChecked();
        boolean hasVehicle = ((CheckBox) findViewById(R.id.pvehicle_cb)).isChecked();

        orgDBHashmap.put("event_name", event_name);
        orgDBHashmap.put("start_date", date);
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
        orgDBHashmap.put("other_info", otherInfo);

        //push the data to firebase
        dbr.updateChildren(orgDBHashmap);

        showToast("Event Updated: " + event_name);
        launchActivity();
    }

    private void launchActivity() {
        Intent i = new Intent(getApplicationContext(), EventDashboard.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_preferences);
        setTitle("Create Event");

        Intent i = getIntent();
        String event_name = i.getStringExtra("event_name");
        Log.d(TAG, "Retrieved event: " + event_name);

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fbAuth.getCurrentUser();
        userId = user.getUid();

        //DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId).child("events").child(event_name);

        //Generate the UI
        populateSpinner();
        ((DatePicker) findViewById(R.id.pstart_date)).setMinDate(System.currentTimeMillis());
        Button update_preferences_button = (Button) findViewById(R.id.pupdate_preferences_button);
        update_preferences_button.setOnClickListener(View -> {

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > 1000) {
                updateEvent(event_name);
            }
            lastClickTime = currentTime;
        });
    }
}