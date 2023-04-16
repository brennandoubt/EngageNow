package edu.fandm.engagenow;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

public class OrganizationRegistration extends AppCompatActivity {
    FirebaseAuth fbAuth;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_registration);

        // get user ID from last activity
        Intent i = getIntent();
        String user_id = i.getStringExtra("user_id");

        // initialize firebase app
        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        //https://medium.com/javarevisited/lets-develop-an-android-app-to-upload-files-and-images-on-cloud-f9670d812060
        //tutorial on how to upload image
        Button uploadImageBtn = (Button) findViewById(R.id.upload_image_button);
        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        registerUser();
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
                    imageView = (ImageView)findViewById(R.id.selected_image);
                    imageView.setImageURI(result);

                }
            });

    private void registerUser() {
        Button update_preferences_button = (Button) findViewById(R.id.register_account_button);
        update_preferences_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get data from last Register activity
                Intent i = getIntent();
                String email = i.getStringExtra("email");
                String password = i.getStringExtra("password");
                String accountType = i.getStringExtra("account_type");

                String name = ((EditText) findViewById(R.id.name_preference_et)).getText().toString();
                String description = ((EditText) findViewById(R.id.description_et)).getText().toString();
                String website = ((EditText) findViewById(R.id.website_link_et)).getText().toString();
                // verify all fields have been filled out
                if (name.equals("") || description.equals("") || website.equals("") || imageView == null) {
                    Toast.makeText(getApplicationContext(), "All Fields Are Required!", Toast.LENGTH_LONG).show();
                    return;
                }

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

                            orgDBHashmap.put("name", name);
                            orgDBHashmap.put("description", description);
                            orgDBHashmap.put("email", email);
                            orgDBHashmap.put("website", website);

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
}