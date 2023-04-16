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
                String accountTypeSelection = dropdownSelect.getSelectedItem().toString();

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
                if (accountTypeSelection.equals("Select Account Type")) {
                    Toast.makeText(getApplicationContext(), "Select Account Type", Toast.LENGTH_LONG).show();
                    return;
                }

//                better formatting for database
                String accountType;
                if (accountTypeSelection.equals("Volunteer Account")) {
                    accountType = "volunteer_account";

                    Intent vi = new Intent(getApplicationContext(), VolunteerRegistration.class);
                    vi.putExtra("email", email);
                    vi.putExtra("password", password);
                    vi.putExtra("account_type", accountType);

                    startActivity(vi);
                }
                else {
                    accountType = "organization_account";

                    Intent oi = new Intent(getApplicationContext(), OrganizationRegistration.class);
                    oi.putExtra("email", email);
                    oi.putExtra("password", password);
                    oi.putExtra("account_type", accountType);

                    startActivity(oi);
                }
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