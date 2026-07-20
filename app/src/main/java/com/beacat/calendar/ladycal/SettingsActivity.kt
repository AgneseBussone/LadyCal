package com.beacat.calendar.ladycal

import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

/**
 * Activity for setting the preferences
 */
class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val fragment_tag = "setting_fragment"

    // theme data
    private var themeId = 0
    private lateinit var defaultComponent: ComponentName
    private lateinit var blueComponent: ComponentName
    private lateinit var greenComponent: ComponentName
    private lateinit var purpleComponent: ComponentName
    private lateinit var pm: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = intent
        if (i != null) {
            themeId = i.getIntExtra(getString(R.string.KEY_THEME), R.style.AppTheme)
            setTheme(themeId)
        }

        defaultComponent = ComponentName(this, "com.beacat.calendar.ladycal.SplashDefault")
        blueComponent = ComponentName(this, "com.beacat.calendar.ladycal.SplashBlue")
        greenComponent = ComponentName(this, "com.beacat.calendar.ladycal.SplashGreen")
        purpleComponent = ComponentName(this, "com.beacat.calendar.ladycal.SplashPurple")
        pm = this.packageManager

        // Display the fragment as the main content.
        val bundle = Bundle()
        bundle.putInt(getString(R.string.KEY_THEME), themeId)
        val settingsFragment = SettingsFragment()
        settingsFragment.arguments = bundle
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, settingsFragment, fragment_tag)
                .commit()

        // Change the title in the action bar
        supportActionBar!!.setTitle(R.string.pref_settingsTitle)

        // Add back navigation
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun resetAppIcon() {
        // enable default splashscreen
        enableComponent(defaultComponent, PackageManager.DONT_KILL_APP)
        // disable all the aliases
        disableComponent(blueComponent, PackageManager.DONT_KILL_APP)
        disableComponent(greenComponent, PackageManager.DONT_KILL_APP)
        disableComponent(purpleComponent, PackageManager.DONT_KILL_APP)
    }

    private fun enableBlue() {
        // enable blue
        enableComponent(blueComponent, PackageManager.DONT_KILL_APP)
        // disable all the others
        disableComponent(defaultComponent, PackageManager.DONT_KILL_APP)
        disableComponent(greenComponent, PackageManager.DONT_KILL_APP)
        disableComponent(purpleComponent, PackageManager.DONT_KILL_APP)
    }

    private fun enableGreen() {
        // enable green
        enableComponent(greenComponent, PackageManager.DONT_KILL_APP)
        // disable all the others
        disableComponent(defaultComponent, PackageManager.DONT_KILL_APP)
        disableComponent(blueComponent, PackageManager.DONT_KILL_APP)
        disableComponent(purpleComponent, PackageManager.DONT_KILL_APP)
    }

    private fun enablePurple() {
        // enable purple
        enableComponent(purpleComponent, PackageManager.DONT_KILL_APP)
        // disable all the others
        disableComponent(defaultComponent, PackageManager.DONT_KILL_APP)
        disableComponent(greenComponent, PackageManager.DONT_KILL_APP)
        disableComponent(blueComponent, PackageManager.DONT_KILL_APP)
    }

    private fun enableComponent(componentName: ComponentName, flag: Int) {
        pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                flag)
    }

    private fun disableComponent(componentName: ComponentName, flag: Int) {
        pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                flag)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.resetAll -> {
                val alertDialog = AlertDialog.Builder(this@SettingsActivity).create()
                alertDialog.setTitle(R.string.dialog_reset_pref)

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
                    // reset the default values of preferences
                    val sp = getDefaultSharedPreferences(applicationContext)

                    // save the preferences that must be kept
                    val firstUse = sp.getBoolean(getString(R.string.KEY_FIRST_USE), false)
                    val askForRate = sp.getInt(getString(R.string.KEY_ASK_FOR_RATE), resources.getInteger(R.integer.askForRate_max_value))

                    val editor = sp.edit()
                    editor.clear()
                    editor.putBoolean(getString(R.string.KEY_FIRST_USE), firstUse)
                    editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), askForRate)
                    editor.apply()
                    PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, true)

                    // Delete the old fragment and replace with a new one. This will update the summaries
                    // (I couldn't find a smarter way to do that...)
                    fragmentManager.beginTransaction()
                            .replace(android.R.id.content, SettingsFragment(), fragment_tag)
                            .commit()
                    dialog.dismiss()
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL") { dialog, _ -> dialog.dismiss() }
                alertDialog.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity)
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun onSharedPreferenceChangedImpl(sharedPreferences: SharedPreferences, key: String) {
        if (key == getString(R.string.KEY_THEME)) {
            val color = sharedPreferences.getString(key, getString(R.string.pref_theme_default))
            val dialog = AlertDialog.Builder(this@SettingsActivity).create()
            dialog.setTitle("Restart required")
            dialog.setMessage(getString(R.string.restart_message, color))
            dialog.setOnDismissListener {
                // enable the correct splash alias
                if (color == getString(R.string.pref_theme_default))
                    resetAppIcon()
                else if (color == getString(R.string.pref_theme_blue))
                    enableBlue()
                else if (color == getString(R.string.pref_theme_green))
                    enableGreen()
                else if (color == getString(R.string.pref_theme_purple))
                    enablePurple()

                // Restart the app
                Toast.makeText(this@SettingsActivity, "Restarting app.....", Toast.LENGTH_SHORT).show()
                val i = baseContext.packageManager
                        .getLaunchIntentForPackage(packageName)
                i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i)
            }
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "CONTINUE", null as DialogInterface.OnClickListener?)
            dialog.show()
        }
    }

    override fun onSharedPreferenceChanged(
        p0: SharedPreferences?,
        p1: String?
    ) {
        if (p0 != null && p1 != null) {
            onSharedPreferenceChangedImpl(p0, p1)
        }
    }

    /**
     * Inner class that implements the fragment and sets the listeners for changes
     */
    class SettingsFragment : PreferenceFragment() {

        private var themeId = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val bundle = arguments
            themeId = bundle.getInt(getString(R.string.KEY_THEME), R.style.AppTheme)

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)


            // Start day
            val pref_day = findPreference(getString(R.string.KEY_DAY))
            val monday_sunday = resources.getStringArray(R.array.pref_startWeekDay_entries)

            // Set the summary with the user-friendly string
            var index = Integer.parseInt(pref_day.sharedPreferences.getString(getString(R.string.KEY_DAY), getString(R.string.pref_startWeekDay_default))!!)

            pref_day.summary = monday_sunday[index - 1]

            // Set the listener
            pref_day.setOnPreferenceChangeListener { preference, newValue ->
                // Update the summary
                val i = Integer.parseInt(newValue.toString())
                preference.summary = monday_sunday[i - 1]

                // Save the preference
                true
            }


            // Swipe direction
            val pref_swipe = findPreference(getString(R.string.KEY_SWIPE))
            val direction = resources.getStringArray(R.array.pref_swipe_entries)
            index = Integer.parseInt(pref_swipe.sharedPreferences.getString(getString(R.string.KEY_SWIPE), getString(R.string.pref_swipe_default))!!)
            pref_swipe.summary = direction[index]
            pref_swipe.setOnPreferenceChangeListener { preference, newValue ->
                // Update the summary
                val i = Integer.parseInt(newValue.toString())
                preference.summary = direction[i]

                true
            }


            // Period length
            val pref_period_value = findPreference(getString(R.string.KEY_PERIOD_VALUE))
            val period = Integer.parseInt(pref_period_value.sharedPreferences.getString(getString(R.string.KEY_PERIOD_VALUE), getString(R.string.pref_periodLength_default))!!)
            pref_period_value.summary = period.toString()
            pref_period_value.setOnPreferenceChangeListener { _, newValue ->
                pref_period_value.summary = newValue.toString()
                true
            }


            // Cycle length
            val pref_cycle_value = findPreference(getString(R.string.KEY_CYCLE_VALUE))
            val cycle = Integer.parseInt(pref_cycle_value.sharedPreferences.getString(getString(R.string.KEY_CYCLE_VALUE), getString(R.string.pref_cycleLength_default))!!)
            pref_cycle_value.summary = cycle.toString()
            pref_cycle_value.setOnPreferenceChangeListener { _, newValue ->
                pref_cycle_value.summary = newValue.toString()
                true
            }


            // History
            val pref_history = findPreference(getString(R.string.KEY_HISTORY))
            val history = resources.getStringArray(R.array.pref_history_entries)
            val h = Integer.parseInt(pref_swipe.sharedPreferences.getString(getString(R.string.KEY_HISTORY), getString(R.string.pref_history_default))!!)
            if (h == 6) {
                pref_history.summary = history[0]
            } else {
                pref_history.summary = history[1]
            }
            pref_history.setOnPreferenceChangeListener { preference, newValue ->
                // Update the summary
                val i = Integer.parseInt(newValue.toString())
                when (i) {
                    6 -> preference.summary = history[0]
                    12 -> preference.summary = history[1]
                }
                true
            }


            // Backup
            val pref_backup = findPreference(getString(R.string.KEY_BACKUP))
            pref_backup.setOnPreferenceClickListener {
                val i = Intent(activity.applicationContext, BackupActivity::class.java)
                i.putExtra(getString(R.string.KEY_THEME), themeId)
                startActivity(i)
                false
            }
            pref_backup.setOnPreferenceChangeListener { _, _ -> true }

            // Theme
            val pref_theme = findPreference(getString(R.string.KEY_THEME))
            pref_theme.summary = pref_theme.sharedPreferences.getString(getString(R.string.KEY_THEME), getString(R.string.pref_theme_default))
        }
    }
}
