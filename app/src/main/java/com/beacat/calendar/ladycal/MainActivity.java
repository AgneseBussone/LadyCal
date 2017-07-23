package com.beacat.calendar.ladycal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.PeriodDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // constant required to retrieve data in case the user wants to insert meds data from history activity
    public static final int CHANGE_MEDS_CODE = 1;
    public static final String CHANGE_MEDS_STRING_ID = "startDay";

    // private final String TAG = this.getClass().getSimpleName();
    private ExtendedCalendarView calendar;
    private Day selectedDay = null;
    private int day;
    private int gesture;
    private ActionBar bar;
    private SharedPreferences sharedPref;
    PeriodDatabase db;
    private int themeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Save the default values for preferences only the first time the application is open
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db  = PeriodDatabase.getInstance(getApplicationContext());

        // Read the preferences before creating the view
        initPreferences();

        // set the theme
        setTheme(themeId);

        // Create the view and all the objects in it
        setContentView(R.layout.activity_main);
        calendar = (ExtendedCalendarView)findViewById(R.id.calendar);

        calendar.setOnDayClickListener(new ExtendedCalendarView.OnDayClickListener() {
            @Override
            public void onDayClicked(Day day) {
                selectedDay = day;
            }
        });

        bar = getSupportActionBar();

        if(isFirstTime()){
            // show tutorial
            Intent i = new Intent(this, TutorialActivity.class);
            startActivity(i);

            // set the rate counting
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("askForRate", getResources().getInteger(R.integer.askForRate_max_value));
            editor.apply();
        }
        else{
            // check if it's time to ask for rate
            checkRateCounting();
        }
    }

    private void checkRateCounting(){
        int count = sharedPref.getInt(getString(R.string.KEY_ASK_FOR_RATE), getResources().getInteger(R.integer.askForRate_max_value));
        if(count > 0){ // -1 means no more asking
            count--;
            if(count == 0){
                // ask
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Do you like this app?");
                alertDialog.setMessage("If so, live a rate");
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        switch (which){
                            case AlertDialog.BUTTON_POSITIVE:
                                // open the rate activity
                                Intent i = new Intent(MainActivity.this, RateActivity.class);
                                startActivity(i);
                                // fallthrough
                            case AlertDialog.BUTTON_NEGATIVE:
                                // do not ask it again
                                editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), getResources().getInteger(R.integer.askForRate_null));
                                break;
                            case AlertDialog.BUTTON_NEUTRAL:
                                // restart the counting
                                editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), getResources().getInteger(R.integer.askForRate_max_value));
                                break;
                        }
                        editor.apply();
                    }
                };
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Rate it", listener);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No, thanks", listener);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Not now", listener);

                alertDialog.show();

            }
            else {
                // store the new value
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), count);
                editor.apply();
            }
        }
    }

    private boolean isFirstTime()
    {
        boolean firstUse = sharedPref.getBoolean(getString(R.string.KEY_FIRST_USE), true);
        if(BuildConfig.DEBUG) {
//            firstUse = true; //TEST ONLY
        }
        if (firstUse) {
            // first time
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.KEY_FIRST_USE), false);
            editor.apply();
        }
        return firstUse;
    }

    /* Save the reminders date in the shared preferences in case the device is rebooted.
    *  The end period reminder is not essential; it doesn't matter if it's not shown. */
    private void saveReminderDate(int type, long date){
        SharedPreferences.Editor editor = sharedPref.edit();
        switch(type) {
            case Reminder.NOTIFICATION_CODE_FRIENDLY:
                editor.putLong(getString(R.string.KEY_FRIENDLY_REM_DATE), date);
                break;
            case Reminder.NOTIFICATION_CODE_START:
                editor.putLong(getString(R.string.KEY_PERIOD_REM_DATE), date);
                break;
        }
        editor.apply();
    }

    private void initPreferences() {
        // Only when creating the app, check if there are some entry to delete from the db
        int history = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_HISTORY), getString(R.string.pref_history_default)));
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, (0 - history));

        db.deleteHistory(c.getTimeInMillis());

        readPreferences();
    }

    private void readPreferences() {
        // Set the first day of the week
        day = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_DAY), getString(R.string.pref_startWeekDay_default)));

        // Swipe
        gesture = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_SWIPE), getString(R.string.pref_swipe_default)));

        // Set the value for the period length and for the cycle length
        calculatePeriodAndCycleLength(sharedPref);

        checkReminders(sharedPref);

        // theme
        themeId = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_THEME), String.valueOf(R.style.AppTheme)));
    }

    @Override
    protected void onResume(){
        super.onResume();
        readPreferences(); // update the view if some preferences changed
        if(calendar != null) {
            calendar.setFirstDayOfWeek(day);
            calendar.setGesture(gesture);

            // this listener will be called after the view has being drawn. I need this trick because the async task
            // need the object today and it'll be available after the calendar is visible.
            calendar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                public void onGlobalLayout() {
                    // remove the listener or it will be called every time the view is drawn
                    calendar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // start the async task
                    SetMessagesTask task = new SetMessagesTask(MainActivity.this, bar, calendar.getToday());
                    task.execute();
                }
            });
        }
    }

    public void resetDateToday(View view) {
        calendar.resetDate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_history: {
                Intent intent = new Intent(this, HistoryActivity.class);
                intent.putExtra("themeId", themeId);
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(MainActivity.this,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivityForResult(intent, CHANGE_MEDS_CODE, bundle);
                return true;
            }
            case R.id.settings: {
                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtra("themeId", themeId);
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(MainActivity.this,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivity(i, bundle);
                return true;
            }
            case R.id.statistics: {
                Intent i = new Intent(this, StatisticsActivity.class);
                i.putExtra("themeId", themeId);
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(MainActivity.this,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivity(i, bundle);
                return true;
            }
            case R.id.tutorial: {
                Intent i = new Intent(this, TutorialActivity.class);
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(MainActivity.this,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivity(i, bundle);
                return true;
            }
            case R.id.feedback: {
                // send an email to the developer
                String data = "Device info:" +
                        "Model: " + Build.MODEL + "\n" +
                        "Manufacturer: " + Build.MANUFACTURER + "\n" +
                        "Brand: " + Build.BRAND + "\n" +
                        "Android: " + Build.VERSION.RELEASE + "(skd " + Build.VERSION.SDK_INT + ")\n" +
                        "App version code: " + BuildConfig.VERSION_CODE + "\n" +
                        "App version name: " + BuildConfig.VERSION_NAME + "\n\n";
                String uriText =
                        "mailto:agnesebussone+appsupport@gmail.com" +
                                "?subject=" + Uri.encode("LadyCal feedback") +
                                "&body=" + Uri.encode(data);

                Uri uri = Uri.parse(uriText);

                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);
                startActivity(Intent.createChooser(sendIntent, "Send email with"));
                return true;
            }
            case R.id.rate: {
                Intent i = new Intent(this, RateActivity.class);
                i.putExtra("themeId", themeId);
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(MainActivity.this,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivity(i, bundle);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (CHANGE_MEDS_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    long date = data.getLongExtra(CHANGE_MEDS_STRING_ID, -1);
                    if(date != -1){
                        calendar.gotoDate(date);
                    }
                }
                break;
            }
        }
    }

    public void startPeriod(View view) {
        if(selectedDay != null){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDay.getDayUTC());
            String day_string = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(cal.getTime());
            if(selectedDay.isPeriod()){
                end_period(selectedDay, day_string);
            }
            else{
                start_period(selectedDay, day_string);
            }
        }
        else{
            Day today = calendar.getToday();
            if(today.isPeriod()){
                end_period(today, "today");
            }
            else{
                start_period(today, "today");
            }
        }

        calendar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {
                // remove the listener or it will be called every time the view is drawn
                calendar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // start the async task
                SetMessagesTask task = new SetMessagesTask(MainActivity.this, bar, calendar.getToday());
                task.execute();
            }
        });

    }

    private void end_period(final Day day, String day_string){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(R.string.dialog_end_period);

        alertDialog.setMessage(day_string);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // check if it's not in the future
                if(day.getDayUTC() > calendar.getToday().getDayUTC()){
                    Toast.makeText(MainActivity.this, "Operations in the future not allowed", Toast.LENGTH_LONG).show();
                    // get rid of the selection icon in the view
                    calendar.refreshCalendar();
                    dialog.dismiss();
                    return;
                }
                new EndPeriodTask(MainActivity.this, calendar).execute(day);
                dialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // get rid of the selection icon in the view
                        calendar.refreshCalendar();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void start_period(final Day day, String day_string){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(R.string.dialog_add_period_title);

        alertDialog.setMessage(day_string);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Day endDay;
                        Calendar cal = Calendar.getInstance();

                        // check if it's not in the future
                        if(day.getDayUTC() > cal.getTimeInMillis()){
                            Toast.makeText(MainActivity.this, "Operations in the future not allowed", Toast.LENGTH_LONG).show();
                            // get rid of the selection icon in the view
                            calendar.refreshCalendar();
                            dialog.dismiss();
                            return;
                        }

                        // set the calendar to the start day
                        cal.setTimeInMillis(day.getDayUTC());

                        // add the number of days of the period length to calculate the end day (- 1 is to consider the selected day as first day)
                        cal.add(Calendar.DATE, (db.getPeriodLength() - 1));
                        endDay = new Day(null, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

                        new AddPeriodTask(MainActivity.this, calendar).execute(day, endDay);

                        dialog.dismiss();

                        // Set end period reminder
                        if(sharedPref.getBoolean(getString(R.string.KEY_PERIOD_REM), false)){
                            // cal is set to the end of the period
                            if(Calendar.getInstance().getTimeInMillis() < cal.getTimeInMillis()){
                                Reminder.scheduleNotification(Reminder.getNotification(Reminder.NOTIFICATION_CODE_END, MainActivity.this),
                                        cal.getTimeInMillis(),
                                        Reminder.NOTIFICATION_CODE_END,
                                        MainActivity.this);
                                if(BuildConfig.DEBUG) {
//                                    Log.d(TAG, "scheduled end reminder");
                                }
                            }
                        }
                        // Set the friendly and the start for the next period
                        checkReminders(sharedPref);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // get rid of the selection icon in the view
                        calendar.refreshCalendar();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void addMed(View view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_meds_dialog, null);
        dialogBuilder.setView(dialogView);

        final TextView num = (TextView)dialogView.findViewById(R.id.number);

        String day = "today";
        if(selectedDay !=  null){
            Calendar cal = Calendar.getInstance();
            cal.set(selectedDay.getYear(), selectedDay.getMonth(), selectedDay.getDay());
            day = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(cal.getTime());
            num.setText(String.valueOf(selectedDay.getMeds()));
        }
        else{
            num.setText(String.valueOf(calendar.getToday().getMeds()));
        }

        TextView tv = (TextView)dialogView.findViewById(R.id.day);
        tv.setText(day);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int n = Integer.parseInt(num.getText().toString());

                Day day;
                Calendar cal = Calendar.getInstance();
                if(selectedDay == null){
                    // get today date
                    day = calendar.getToday();
                }
                else{
                    day = selectedDay;
                    // check if it's not in the future
                    if(day.getDayUTC() > cal.getTimeInMillis()){
                        Toast.makeText(MainActivity.this, "Operations in the future not allowed", Toast.LENGTH_LONG).show();
                        // get rid of the selection icon in the view
                        calendar.refreshCalendar();
                        dialog.dismiss();
                        return;
                    }
                }

                if(day.isPeriod()) {
                    day.setMeds(n);
                    new AddMedTask(MainActivity.this, calendar).execute(day);
                }
                else{
                    Toast.makeText(MainActivity.this, "You cannot set medicine for non period day", Toast.LENGTH_LONG).show();
                    // get rid of the selection icon in the view
                    calendar.refreshCalendar();
                }
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get rid of the selection icon in the view
                calendar.refreshCalendar();
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void calculatePeriodAndCycleLength(SharedPreferences sharedPref){

        boolean useFixed = sharedPref.getBoolean(getString(R.string.KEY_PERIOD), false);
        int periodLength = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_PERIOD_VALUE), getString(R.string.pref_periodLength_default)));
        db.setPeriodLength(periodLength, useFixed);

        useFixed = sharedPref.getBoolean(getString(R.string.KEY_CYCLE), false);
        int cycleLength = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_CYCLE_VALUE), getString(R.string.pref_cycleLength_default)));
        db.setCycleLength(cycleLength, useFixed);
    }

    private void checkReminders(SharedPreferences sharedPref) {
        boolean friendlyRem = sharedPref.getBoolean(getString(R.string.KEY_FRIENDLY_REM), false);
        boolean periodRem = sharedPref.getBoolean(getString(R.string.KEY_PERIOD_REM), false);

        if(friendlyRem || periodRem){
            Calendar c  = Calendar.getInstance();
            long now = c.getTimeInMillis();
            long last = db.getLastPeriod();
            if (last != 0) {
                c.setTimeInMillis(last);
                c.add(Calendar.DATE, (db.getCycleLength() - 3)); // friendly reminder date
                if (now <= c.getTimeInMillis() && friendlyRem) {
                    long date = c.getTimeInMillis();
                    Reminder.scheduleNotification(Reminder.getNotification(Reminder.NOTIFICATION_CODE_FRIENDLY, MainActivity.this),
                            date,
                            Reminder.NOTIFICATION_CODE_FRIENDLY,
                            MainActivity.this);
                    saveReminderDate(Reminder.NOTIFICATION_CODE_FRIENDLY, date);
                    if(BuildConfig.DEBUG) {
//                        Log.d(TAG, "scheduled friendly reminder");
                    }
                }
                if (periodRem) {
                    c.add(Calendar.DATE, 3); // start period reminder date
                    if(now <= c.getTimeInMillis()) {
                        long date = c.getTimeInMillis();
                        Reminder.scheduleNotification(Reminder.getNotification(Reminder.NOTIFICATION_CODE_START, MainActivity.this),
                                date,
                                Reminder.NOTIFICATION_CODE_START,
                                MainActivity.this);
                        saveReminderDate(Reminder.NOTIFICATION_CODE_START, date);
                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "scheduled start reminder");
                        }
                    }
                }
            }
        }
    }

    public void decreaseMed(View view) {
        View root = view.getRootView();
        TextView num = (TextView)root.findViewById(R.id.number);
        int n = Integer.parseInt(num.getText().toString());
        n--;
        if(n >= 0){
            num.setText(String.valueOf(n));
        }
    }

    public void increaseMed(View view) {
        View root = view.getRootView();
        TextView num = (TextView)root.findViewById(R.id.number);
        int n = Integer.parseInt(num.getText().toString());
        n++;
        if(n <= 20){
            num.setText(String.valueOf(n));
        }
    }
}
