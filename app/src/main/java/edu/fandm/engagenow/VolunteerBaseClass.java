package edu.fandm.engagenow;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class VolunteerBaseClass extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.volunteermenu, m);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                i = new Intent(getApplicationContext(), SignIn.class);
                startActivity(i);
                finish();
                break;
            case R.id.chat:
                // don't run if already on class
                if (this.getClass().getSimpleName().equals("VolunteerChatList")) {
                    break;
                }
                i = new Intent(getApplicationContext(), VolunteerChatList.class);
                startActivity(i);
                break;

            case R.id.settings_mi:
                // don't run if already on class
                if (this.getClass().getSimpleName().equals("VolunteerPreferences")) {
                    break;
                }
                VolunteerPreferences.fbAuth = FirebaseAuth.getInstance();
                Intent vi = new Intent(getApplicationContext(), VolunteerPreferences.class);
                startActivity(vi);
                break;
        }
        return true;
    }

}
