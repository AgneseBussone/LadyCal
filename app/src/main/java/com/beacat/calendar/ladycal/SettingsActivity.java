package com.beacat.calendar.ladycal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Activity for setting the preferences
 */
public class SettingsActivity extends AppCompatActivity {

    private String fragment_tag = "setting_fragment";
    private int themeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if(i != null){
            themeId = i.getIntExtra("themeId", R.style.AppTheme);
            setTheme(themeId);
        }

        // Display the fragment as the main content.
        Bundle bundle = new Bundle();
        bundle.putInt("themeId", themeId);
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment, fragment_tag)
                .commit();

        // Change the title in the action bar
        getSupportActionBar().setTitle(R.string.pref_settingsTitle);

        // Add back navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.resetAll:
                AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
                alertDialog.setTitle(R.string.dialog_reset_pref);

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // reset the default values of preferences
                                SharedPreferences sp = getDefaultSharedPreferences(getApplicationContext());

                                // save the preferences that must be kept
                                boolean firstUse = sp.getBoolean(getString(R.string.KEY_FIRST_USE), false);
                                int askForRate = sp.getInt(getString(R.string.KEY_ASK_FOR_RATE), getResources().getInteger(R.integer.askForRate_max_value));

                                SharedPreferences.Editor editor = sp.edit();
                                editor.clear();
                                editor.putBoolean(getString(R.string.KEY_FIRST_USE), firstUse);
                                editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), askForRate);
                                editor.apply();
                                PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, true);

                                // Delete the old fragment and replace with a new one. This will update the summaries
                                // (I couldn't find a smarter way to do that...)
                                getFragmentManager().beginTransaction()
                                        .replace(android.R.id.content, new SettingsFragment(), fragment_tag)
                                        .commit();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Inner class that implements the fragment and sets the listeners for changes
     */
    public static class SettingsFragment extends PreferenceFragment{

        private int themeId;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle bundle = getArguments();
            themeId = bundle.getInt("themeId", R.style.AppTheme);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);


            // Start day
            Preference pref_day = findPreference(getString(R.string.KEY_DAY));
            final String[] monday_sunday = getResources().getStringArray(R.array.pref_startWeekDay_entries);

            // Set the summary with the user-friendly string
            int index = Integer.parseInt(pref_day.getSharedPreferences().getString(getString(R.string.KEY_DAY), getString(R.string.pref_startWeekDay_default)));

            pref_day.setSummary(monday_sunday[index - 1]);

            // Set the listener
            pref_day.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Update the summary
                    int i = Integer.parseInt(newValue.toString());
                    preference.setSummary(monday_sunday[i - 1]);

                    // Save the preference
                    return true;
                }
            });


            // Swipe direction
            Preference pref_swipe = findPreference(getString(R.string.KEY_SWIPE));
            final String[] direction = getResources().getStringArray(R.array.pref_swipe_entries);
            index = Integer.parseInt(pref_swipe.getSharedPreferences().getString(getString(R.string.KEY_SWIPE), getString(R.string.pref_swipe_default)));
            pref_swipe.setSummary(direction[index]);
            pref_swipe.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Update the summary
                    int i = Integer.parseInt(newValue.toString());
                    preference.setSummary(direction[i]);

                    return true;
                }
            });


            // Period length
            final Preference pref_period_value = findPreference(getString(R.string.KEY_PERIOD_VALUE));
            int period = Integer.parseInt(pref_period_value.getSharedPreferences().getString(getString(R.string.KEY_PERIOD_VALUE), getString(R.string.pref_periodLength_default)));
            pref_period_value.setSummary(String.valueOf(period));
            pref_period_value.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    pref_period_value.setSummary(newValue.toString());
                    return true;
                }
            });


            // Cycle length
            final Preference pref_cycle_value = findPreference(getString(R.string.KEY_CYCLE_VALUE));
            int cycle = Integer.parseInt(pref_cycle_value.getSharedPreferences().getString(getString(R.string.KEY_CYCLE_VALUE), getString(R.string.pref_cycleLength_default)));
            pref_cycle_value.setSummary(String.valueOf(cycle));
            pref_cycle_value.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    pref_cycle_value.setSummary(newValue.toString());
                    return true;
                }
            });


            // History
            Preference pref_history = findPreference(getString(R.string.KEY_HISTORY));
            final String[] history = getResources().getStringArray(R.array.pref_history_entries);
            int h = Integer.parseInt(pref_swipe.getSharedPreferences().getString(getString(R.string.KEY_HISTORY), getString(R.string.pref_history_default)));
            if(h == 6){
                pref_history.setSummary(history[0]);
            }
            else{
                pref_history.setSummary(history[1]);
            }
            pref_history.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Update the summary
                    int i = Integer.parseInt(newValue.toString());
                    switch(i){
                        case 6: preference.setSummary(history[0]); break;
                        case 12: preference.setSummary(history[1]); break;
                    }
                    return true;
                }
            });


            // Backup
            Preference pref_backup = findPreference(getString(R.string.KEY_BACKUP));
            pref_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity().getApplicationContext(), BackupActivity.class);
                    i.putExtra("themeId", themeId);
                    startActivity(i);
                    return false;
                }
            });
            pref_backup.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    return true;
                }
            });
        }
    }
}
