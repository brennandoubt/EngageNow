package edu.fandm.engagenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

public class Register extends AppCompatActivity {
    FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

//        https://code.tutsplus.com/tutorials/how-to-add-a-dropdown-menu-in-android-studio--cms-37860
        Spinner accountTypeDropdown = findViewById(R.id.account_type_select_spinner);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.register_select, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        accountTypeDropdown.setAdapter(adapter);

        Button register = (Button) findViewById(R.id.btn_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((EditText) findViewById(R.id.et_emailAddress)).getText().toString().trim();
                String password = ((EditText) findViewById(R.id.et_password)).getText().toString();
                Spinner dropdownSelect = (Spinner) findViewById(R.id.account_type_select_spinner);
                String accountType = dropdownSelect.getSelectedItem().toString();
                if (email.equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Email", Toast.LENGTH_LONG).show();
                    return;
                }
                if (password.equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_LONG).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password must be 6 or more characters", Toast.LENGTH_LONG).show();
                    return;
                }
                if (accountType.equals("Select Account Type")) {
                    Toast.makeText(getApplicationContext(), "Select Account Type", Toast.LENGTH_LONG).show();
                    return;
                }
                Task s = fbAuth.createUserWithEmailAndPassword(email, password);
                s.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
//                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        if (task.isSuccessful()) {
                            FirebaseUser user = fbAuth.getCurrentUser();
                            String userId = user.getUid();

//                            in database, store the type of account that the user is
                            Map<String, Object> accountTypeMap = new HashMap<>();
                            accountTypeMap.put(userId, accountType);

                            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("Account Type");
                            dbr.updateChildren(accountTypeMap);

                            Toast.makeText(getApplicationContext(), "New user created", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), SignIn.class);
                            startActivity(i);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Failed to create new user", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });

        TextView login = (TextView) findViewById(R.id.btn_back_to_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SignIn.class);
                startActivity(i);
                finish();
            }
        });

    }
}