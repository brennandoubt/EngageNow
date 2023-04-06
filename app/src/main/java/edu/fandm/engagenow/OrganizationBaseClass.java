package edu.fandm.engagenow;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class OrganizationBaseClass extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, m);
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
//                Log.d("HERE",  this.getClass().getSimpleName());
                if (this.getClass().getSimpleName().equals("OrganizationChatList")) {
                    break;
                }
                i = new Intent(getApplicationContext(), OrganizationChatList.class);
                startActivity(i);
                break;
        }
        return true;
    }

}
