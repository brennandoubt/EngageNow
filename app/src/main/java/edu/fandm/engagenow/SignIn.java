package edu.fandm.engagenow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignIn extends AppCompatActivity {
    final String TAG = "Sign in activity";
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setTitle("Sign In");


        Button login = (Button) findViewById(R.id.logInBtn);
        login.setOnClickListener(view -> {

            //This is to prevent the user from spamming the buttons
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > 750) {
                String email = ((EditText) findViewById(R.id.et_emailAddress)).getText().toString().trim();
                String password = ((EditText) findViewById(R.id.et_password)).getText().toString().trim();

                //validate the input
                if (!isValidInput(email, password)) return;

                //sign user in  https://firebase.google.com/docs/auth/android/password-auth#java_2
                signIn(email, password);
            }
            lastClickTime = currentTime;

        });

        TextView registerAccount = (TextView) findViewById(R.id.btn_signUp);
        registerAccount.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), Register.class);
            startActivity(i);
            finish();
        });

        TextView forgotPassword = (TextView) findViewById(R.id.btn_forgotPassword);
        forgotPassword.setOnClickListener(view -> showForgotPasswordDialog());
    }

    private void signIn(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = auth.getCurrentUser();

                //access data from database https://www.youtube.com/watch?v=E9drbKeVG7Y
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("account_type");
                dbr.child(user.getUid()).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        if (task1.getResult().exists()) {
                            String accountType = String.valueOf(task1.getResult().getValue());

                            if (accountType.equals("volunteer_account")) {
                                Intent i = new Intent(getApplicationContext(), VolunteerSwiping.class);
                                startActivity(i);
                                finish();
                            }
                            else {
                                Intent i = new Intent(getApplicationContext(), EventDashboard.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    }
                    else {
                        Toast.makeText(SignIn.this, "Could Not Sign In", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                Toast.makeText(SignIn.this, "Could Not Sign In", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidInput(String email, String password) {
        if (email.equals("")) {
            Toast.makeText(getApplicationContext(), "Enter Email", Toast.LENGTH_LONG).show();
            return false;
        }
        else if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
        builder.setTitle("Forgot Password");
        EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("CONFIRM", (dialogInterface, i) -> {
            if (input.getText().toString().equals("")) {
                return;
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(input.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(getApplicationContext(), "Email sent to " + input.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.cancel());

        builder.show();
    }

}