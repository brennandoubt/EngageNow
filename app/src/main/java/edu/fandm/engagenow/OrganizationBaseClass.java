package edu.fandm.engagenow;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public abstract class OrganizationBaseClass extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.organizationmenu, m);
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
                if (this.getClass().getSimpleName().equals("OrganizationChatList")) {
                    break;
                }
                i = new Intent(getApplicationContext(), OrganizationChatList.class);
                startActivity(i);
                finish();
                break;
            case R.id.possible_matches:
                // don't run if already on class
                if (this.getClass().getSimpleName().equals("OrganizationPotentialMatches")) {
                    break;
                }
                i = new Intent(getApplicationContext(), OrganizationPotentialMatches.class);
                startActivity(i);
                finish();
                break;
            case R.id.create_event:
                // don't run if already on class
                if (this.getClass().getSimpleName().equals("EventRegistration")) {
                    break;
                }
                i = new Intent(getApplicationContext(), EventRegistration.class);
                startActivity(i);
                finish();
                break;
            case R.id.event_dashboard:
                // don't run if already on class
                if (this.getClass().getSimpleName().equals("EventDashboard")) {
                    break;
                }
                i = new Intent(getApplicationContext(), EventDashboard.class);
                startActivity(i);
                finish();
                break;

        }
        return true;
    }

}
