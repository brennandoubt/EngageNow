package edu.fandm.engagenow;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class OrganizationRegistration extends AppCompatActivity {

    FirebaseAuth fbAuth;
    Uri imageUri;
    StorageReference storageReference;
    private String userId;
    private String email;
    private String password;
    private String accountType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_registration);
        setTitle("Organization Registration");
        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        // get user ID from last activity
        getRegistrationInfo();

        Button uploadImageBtn = (Button) findViewById(R.id.upload_image_button);
        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Button update_preferences_button = (Button) findViewById(R.id.register_account_button);
        update_preferences_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch("image/*");
    }

    private ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    ImageView imageView = (ImageView)findViewById(R.id.selected_image);
                    imageView.setImageURI(result);
                    imageUri = result;

                }
            });

    private void uploadImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("images");

        UploadTask uploadTask = storageReference.child(userId).putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d("UPLOAD", "uploaded image successfully");

        });


    }

    private void getRegistrationInfo(){
        Intent i = getIntent();
        email = i.getStringExtra("email");
        password = i.getStringExtra("password");
        accountType = i.getStringExtra("account_type");

    }

    private void launchActivity(){
        Intent i = new Intent(getApplicationContext(), OrganizationRegistration.class);
        startActivity(i);
        finish();
    }

    private boolean checkInput(String name, String description, String website){
        if (name.equals("") || description.equals("") || website.equals("") || imageUri == null) {
            return false;
        }
        return true;
    }

    private void storeAccountType(){
        Map<String, Object> accountTypeMap = new HashMap<>();
        accountTypeMap.put(userId, accountType);
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
        dbr.updateChildren(accountTypeMap);
    }


    private void registerUser() {

        //extract data first
        String name = ((EditText) findViewById(R.id.name_preference_et)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.description_et)).getText().toString().trim();
        String website = ((EditText) findViewById(R.id.website_link_et)).getText().toString().trim();

        // verify all fields have been filled out
        if(!checkInput(name, description, website)){
            Toast.makeText(getApplicationContext(), "All Fields Are Required!", Toast.LENGTH_LONG).show();
            return;
        }

        //create the user
        Task s = fbAuth.createUserWithEmailAndPassword(email, password);
        s.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()) {

                    Toast.makeText(getApplicationContext(), "New organization user created", Toast.LENGTH_LONG).show();

                    FirebaseUser user = fbAuth.getCurrentUser();
                    userId = user.getUid();
                    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId);
                    Map<String, Object> orgDBHashmap = new HashMap<>();

                    orgDBHashmap.put("name", name);
                    orgDBHashmap.put("description", description);
                    orgDBHashmap.put("email", email);
                    orgDBHashmap.put("website", website);

                    //push the data to firebase
                    dbr.updateChildren(orgDBHashmap);

                    //uploadImage
                    uploadImage();

                    storeAccountType();

                    //Launch the organization chat activity
                    launchActivity();


                } else {
                    Toast.makeText(getApplicationContext(), "Failed to create new organization user", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}