package com.beacat.calendar.ladycal;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import static com.beacat.calendar.ladycal.R.string.KEY_THEME;
import static com.beacat.calendar.ladycal.R.style.AppTheme;

/**
 * Activity that shows backup information. It doesn't perform a backup,
 * because the app relies on the auto backup of Android.
 */

public class BackupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if(i != null){
            setTheme(i.getIntExtra(getString(KEY_THEME), AppTheme));
        }
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
