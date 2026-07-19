package com.beacat.calendar.ladycal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View

/**
 * Activity that shows backup information. It doesn't perform a backup,
 * because the app relies on the auto backup of Android.
 */

class BackupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = intent
        if (i != null) {
            setTheme(i.getIntExtra(getString(R.string.KEY_THEME), R.style.AppTheme))
        }
        setContentView(R.layout.backup_layout)

        // Change the title in the action bar
        supportActionBar!!.setTitle(R.string.backup)

        // Add back navigation
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun openSysSettings(view: View) {
        val backupIntent = Intent(Settings.ACTION_PRIVACY_SETTINGS)
        startActivity(backupIntent)
    }
}
