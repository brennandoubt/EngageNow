package edu.fandm.engagenow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;


public class Register extends AppCompatActivity {
    FirebaseAuth fbAuth;
    private long lastClickTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register Account");

        FirebaseApp.initializeApp(this);
        fbAuth = FirebaseAuth.getInstance();

        //https://code.tutsplus.com/tutorials/how-to-add-a-dropdown-menu-in-android-studio--cms-37860
        setDropDown();

        Button registerBtn = (Button) findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(v -> {

            //prevent the user from spamming button
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > 750) {
                registerUser();
            }
            lastClickTime = currentTime;

        });

        TextView login = (TextView) findViewById(R.id.btn_back_to_login);
        login.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), SignIn.class);
            startActivity(i);
            finish();
        });

    }

    private void registerUser() {
        String email = ((EditText) findViewById(R.id.et_emailAddress)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.et_password)).getText().toString();
        Spinner dropdownSelect = findViewById(R.id.account_type_select_spinner);
        String accountTypeSelection = dropdownSelect.getSelectedItem().toString();

        if (!validateInput(email, password, accountTypeSelection)) {
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
            finish();
        }
        else {
            accountType = "organization_account";

            Intent oi = new Intent(getApplicationContext(), OrganizationRegistration.class);
            oi.putExtra("email", email);
            oi.putExtra("password", password);
            oi.putExtra("account_type", accountType);

            startActivity(oi);
            finish();

        }
    }

    private boolean validateInput(String email, String password, String accountTypeSelection) {
        if (email.equals("")) {
            Toast.makeText(getApplicationContext(), "Enter Email", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password must be 6 or more characters", Toast.LENGTH_LONG).show();
            return false;
        }
        if (accountTypeSelection.equals("Select Account Type")) {
            Toast.makeText(getApplicationContext(), "Select Account Type", Toast.LENGTH_LONG).show();
            return false;
        }

//        valid input
        return true;
    }

    private void setDropDown() {
        Spinner accountTypeDropdown = findViewById(R.id.account_type_select_spinner);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.register_select, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        accountTypeDropdown.setAdapter(adapter);
    }
}