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
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
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
    private long lastClickTime;
    private final String TAG = "OrgRegistration";

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
        uploadImageBtn.setOnClickListener(v -> selectImage());

        Button update_preferences_button = (Button) findViewById(R.id.register_account_button);
        update_preferences_button.setOnClickListener(view -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > 1000) {
                registerUser();
            }
            lastClickTime = currentTime;
        });
                
                

           

    }

    //from chatgpt
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
        Intent i = new Intent(getApplicationContext(), EventDashboard.class);
        startActivity(i);
        finish();
    }

    private boolean checkInput(String name, String description){
        if(name.equals("")) {
            showToast("Event Name Cannot Be Empty");
            return false;
        }
        else if(description.equals("")){
            showToast("Event Description Cannot Be Empty");
            return false;
        }
        else if(imageUri == null){
            showToast("Must Select an Image");
            return false;
        }
        else{
            return true;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void storeAccountType(){
        Map<String, Object> accountTypeMap = new HashMap<>();
        accountTypeMap.put(userId, accountType);
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
        dbr.updateChildren(accountTypeMap);
    }

    //https://firebase.google.com/docs/cloud-messaging
    private void getNotificationToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d("Token registration", "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token that is associated with the device
            String notificationToken = task.getResult();
            Log.d("GENERATE TOKEN", notificationToken);
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId);
            Map<String, Object> orgDBHashmap = new HashMap<>();
            orgDBHashmap.put("notification", notificationToken);
            dbr.updateChildren(orgDBHashmap);

        });
    }




    private void registerUser() {
        String entered_registration_code = ((EditText) findViewById(R.id.org_registration_code)).getText().toString().trim();

//        Verify the user entered the correct org registration code
        DatabaseReference registrationCodeDbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_registration_code");
        registrationCodeDbr.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, (String) task.getResult().getValue());
                    Log.d(TAG, entered_registration_code);
                    String registrationCode = (String) task.getResult().getValue();
                    if (registrationCode.equals(entered_registration_code)) {
                        createUserAccount();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Invalid Registration Code. Cannot Create Account", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void createUserAccount() {
        //extract data first
        String name = ((EditText) findViewById(R.id.name_preference_et)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.description_et)).getText().toString().trim();
        String website = ((EditText) findViewById(R.id.website_link_et)).getText().toString().trim();

        //if the website is inputted, make sure its valid
        if(!website.equals("") && !(Patterns.WEB_URL.matcher(website).matches()) ){
            showToast("Invalid Website URL");
            return;
        }

        // verify all fields have been filled out
        if(!checkInput(name, description)) return;

        //create the user
        Task s = fbAuth.createUserWithEmailAndPassword(email, password);
        s.addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Toast.makeText(getApplicationContext(), "New organization user created", Toast.LENGTH_LONG).show();

                FirebaseUser user = fbAuth.getCurrentUser();
                userId = user.getUid();
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId);
                Map<String, Object> orgDBHashmap = new HashMap<>();

                //get device token needed to send and receive notification
                getNotificationToken();

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
                if (task.getException().getMessage().equals("The email address is already in use by another account.")) {
                    Toast.makeText(getApplicationContext(), "Failed to create new user: Email address in use", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Failed to create new user", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}