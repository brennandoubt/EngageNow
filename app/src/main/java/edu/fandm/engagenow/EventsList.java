package edu.fandm.engagenow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class EventsList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        /**
         * ---Node paths in Firebase's Realtime Database---
         *
         * An organization's events - _root/organization_accounts/[uid]/events/_
         *
         *
         * ---Questions about Implementing in App---
         *
         * 1) Which activity/click is this activity started from? (add Intent code there)
         *      - put organization's uid in the Intent used to start this activity
         *      - retrieve organization's uid from this Intent here
         * 2) Are the events displayed the same way they are in the organization's events list activity?
         *
         */
    }
}