package edu.fandm.engagenow;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class OrganizationPreferences extends AppCompatActivity {

    FirebaseAuth fbAuth;
    Uri imageUri;
    StorageReference storageReference;
    private String userId;
    private String email;
    private String password;
    private String accountType;
    private long lastClickTime;
    private final String TAG = "OrgPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_preferences);
        setTitle("Organization Preferences");
        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        // get user ID from last activity
        getRegistrationInfo();

        Button uploadImageBtn = (Button) findViewById(R.id.org_update_upload_image_button);
        uploadImageBtn.setOnClickListener(v -> selectImage());

        Button update_preferences_button = (Button) findViewById(R.id.submit_change_button);
        update_preferences_button.setOnClickListener(view -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > 1000) {
                updateSetting();
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
                    ImageView imageView = (ImageView)findViewById(R.id.org_update_selected_image);
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

    private void updateSetting() {
        //extract data first
        String name = ((EditText) findViewById(R.id.org_update_name_et)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.org_update_descr_et)).getText().toString().trim();
        String website = ((EditText) findViewById(R.id.org_update_website_link_et)).getText().toString().trim();

        //if the website is inputted, make sure its valid
        if(!website.equals("") && !(Patterns.WEB_URL.matcher(website).matches()) ){
            showToast("Invalid Website URL");
            return;
        }

        // verify all fields have been filled out
        if(!checkInput(name, description)) return;

        FirebaseUser user = fbAuth.getCurrentUser();
        userId = user.getUid();
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("organization_accounts").child(userId);
        Map<String, Object> orgDBHashmap = new HashMap<>();

        orgDBHashmap.put("name", name);
        orgDBHashmap.put("description", description);
        orgDBHashmap.put("website", website);

        //push the data to firebase
        dbr.updateChildren(orgDBHashmap);

        //uploadImage
        uploadImage();

        //Launch the organization dashboard activity
        launchActivity();


    }

}