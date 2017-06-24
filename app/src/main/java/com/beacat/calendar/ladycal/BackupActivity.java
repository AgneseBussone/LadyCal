package com.beacat.calendar.ladycal;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

/**
 * Activity that shows backup information. It doesn't perform a backup,
 * because the app relies on the auto backup of Android.
 */

public class BackupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_layout);

        // Change the title in the action bar
        getSupportActionBar().setTitle(R.string.backup);

        // Add back navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openSysSettings(View view) {
        Intent backupIntent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
        startActivity(backupIntent);
    }
}
