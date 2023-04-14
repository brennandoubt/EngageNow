package edu.fandm.engagenow;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrganizationPreferences extends OrganizationBaseClass {


    FirebaseAuth fbAuth;
    private final String TAG = "OrganizationPreferences";

    private Uri imageUri;

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

        //generate the items in spinner
        populateSpinner();

        //https://medium.com/javarevisited/lets-develop-an-android-app-to-upload-files-and-images-on-cloud-f9670d812060
        //tutorial on how to upload image
        Button uploadImageBtn = (Button) findViewById(R.id.upload_image_button);
        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

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
                        //FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "New organization user created", Toast.LENGTH_LONG).show();
                            FirebaseUser user = fbAuth.getCurrentUser();
                            String userId = user.getUid();

                            // Extract the data from the views
                            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId);
                            Map<String, Object> orgDBHashmap = new HashMap<>();

                            //EdiText
                            String name = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();
                            String description = ((EditText) findViewById(R.id.description_et)).getText().toString();
                            String otherLanguageET = ((EditText) findViewById(R.id.other_specify_et)).getText().toString();

                            orgDBHashmap.put("name", name);
                            orgDBHashmap.put("description", description);
                            orgDBHashmap.put("other_info", otherLanguageET);

                            //Checkboxes
                            boolean hasFbiClearance = ((CheckBox) findViewById(R.id.fbi_cb)).isChecked();
                            boolean hasChildClearance = ((CheckBox) findViewById(R.id.child_cb)).isChecked();
                            boolean hasCriminalClearance = ((CheckBox) findViewById(R.id.criminal_rb)).isChecked();

                            boolean hasLaborSkill = ((CheckBox) findViewById(R.id.labor_skill_cb)).isChecked();
                            boolean hasCareTakingSkill = ((CheckBox) findViewById(R.id.careTaking_skill_cb)).isChecked();
                            boolean hasFoodServiceSkill = ((CheckBox) findViewById(R.id.food_skill_cb)).isChecked();

                            boolean hasSpanish = ((RadioButton) findViewById(R.id.spanish_language_rb)).isChecked();
                            boolean hasChinese = ((RadioButton) findViewById(R.id.chinese_language_rb)).isChecked();
                            boolean hasGerman = ((RadioButton) findViewById(R.id.german_language_rb)).isChecked();
                            boolean hasEnglish = ((RadioButton) findViewById(R.id.english_language_rb)).isChecked();

                            boolean hasVehicle = ((CheckBox)findViewById(R.id.vehicle_cb)).isChecked();

                            orgDBHashmap.put("fbi_clearance", hasFbiClearance);
                            orgDBHashmap.put("child_clearance", hasChildClearance);
                            orgDBHashmap.put("criminal_clearance", hasCriminalClearance);
                            orgDBHashmap.put("labor_skill", hasLaborSkill);
                            orgDBHashmap.put("care_taking_skill", hasCareTakingSkill);
                            orgDBHashmap.put("food_service_skill", hasFoodServiceSkill);
                            orgDBHashmap.put("spanish", hasSpanish);
                            orgDBHashmap.put("chinese", hasChinese);
                            orgDBHashmap.put("german", hasGerman);
                            orgDBHashmap.put("english", hasEnglish);
                            orgDBHashmap.put("vehicle", hasVehicle);

                            //Spinner
                            Spinner ageGroupSpinner = (Spinner) findViewById(R.id.age_group_spinner);
                            Spinner timeCommitmentSpinner = (Spinner) findViewById(R.id.time_commitment_spinner);
                            Spinner availabilitySpinner =  (Spinner) findViewById(R.id.availability_spinner);

                            String ageGroup = ageGroupSpinner.getSelectedItem().toString();
                            String timeCommitment = timeCommitmentSpinner.getSelectedItem().toString();
                            String availability = availabilitySpinner.getSelectedItem().toString();

                            orgDBHashmap.put("age_group", ageGroup);
                            orgDBHashmap.put("time_commitment", timeCommitment);
                            orgDBHashmap.put("availability", availability);
                            orgDBHashmap.put("email", email);

                            //push the data to firebase
                            dbr.updateChildren(orgDBHashmap);

                            //in database, store the type of account that the user is
                            Map<String, Object> accountTypeMap = new HashMap<>();
                            accountTypeMap.put(userId, accountType);
                            dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
                            dbr.updateChildren(accountTypeMap);



                            //Launch the organization chat activity
                            Intent i = new Intent(getApplicationContext(), OrganizationChatList.class);
                            startActivity(i);
                            finish();


                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Failed to create new organization user", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    imageUri = result;
                    ImageView v = (ImageView)findViewById(R.id.selectedImage);
                    v.setImageURI(result);
                }
            });

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch("image/*");
    }

//    private void uploadImage() {
//        if(imageUri != null){
//            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("images/"+ user_id);
//
//            //store the file
//            dbr.putFile(imageUri).addOnCompleteListener
//
//        }
//    }





}