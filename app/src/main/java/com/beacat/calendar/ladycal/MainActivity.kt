package com.beacat.calendar.ladycal

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager

import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast

import com.tyczj.extendedcalendarview.Day
import com.tyczj.extendedcalendarview.ExtendedCalendarView
import com.tyczj.extendedcalendarview.PeriodDatabase

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        // constant required to retrieve data in case the user wants to insert meds data from history activity
        const val CHANGE_MEDS_CODE = 1
        const val CHANGE_MEDS_STRING_ID = "startDay"
    }

    // private final String TAG = this.getClass().getSimpleName();
    private var calendar: ExtendedCalendarView? = null
    private var selectedDay: Day? = null
    private var day = 0
    private var gesture = 0
    private var bar: ActionBar? = null
    private lateinit var sharedPref: SharedPreferences
    @Inject
    lateinit var db: PeriodDatabase
    private var themeId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Save the default values for preferences only the first time the application is open
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // Read the preferences before creating the view
        initPreferences()

        // set the theme
        setTheme(themeId)

        // Create the view and all the objects in it
        setContentView(R.layout.activity_main)
        calendar = findViewById(R.id.calendar)

        calendar!!.setOnDayClickListener(object : ExtendedCalendarView.OnDayClickListener {
            override fun onDayClicked(day: Day?) {
                selectedDay = day
            }
        })

        bar = supportActionBar

        if (isFirstTime()) {
            // show tutorial
            val i = Intent(this, TutorialActivity::class.java)
            startActivity(i)

            // set the rate counting
            val editor = sharedPref.edit()
            editor.putInt("askForRate", resources.getInteger(R.integer.askForRate_max_value))
            editor.apply()
        } else {
            // check if it's time to ask for rate
            checkRateCounting()
        }

        createNotificationChannels()
    }

    private fun checkRateCounting() {
        var count = sharedPref.getInt(getString(R.string.KEY_ASK_FOR_RATE), resources.getInteger(R.integer.askForRate_max_value))
        if (count > 0) { // -1 means no more asking
            count--
            if (count == 0) {
                // ask
                val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                alertDialog.setTitle("Do you like this app?")
                alertDialog.setMessage("If so, live a rate")
                val listener = DialogInterface.OnClickListener { _, which ->
                    val editor = sharedPref.edit()
                    when (which) {
                        AlertDialog.BUTTON_POSITIVE -> {
                            // open the rate activity
                            val i = Intent(this@MainActivity, RateActivity::class.java)
                            startActivity(i)
                            // fallthrough in the original Java switch: do not ask it again
                            editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), resources.getInteger(R.integer.askForRate_null))
                        }
                        AlertDialog.BUTTON_NEGATIVE ->
                            // do not ask it again
                            editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), resources.getInteger(R.integer.askForRate_null))
                        AlertDialog.BUTTON_NEUTRAL ->
                            // restart the counting
                            editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), resources.getInteger(R.integer.askForRate_max_value))
                    }
                    editor.apply()
                }
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Rate it", listener)
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No, thanks", listener)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Not now", listener)

                alertDialog.show()

            } else {
                // store the new value
                val editor = sharedPref.edit()
                editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), count)
                editor.apply()
            }
        }
    }

    private fun isFirstTime(): Boolean {
        val firstUse = sharedPref.getBoolean(getString(R.string.KEY_FIRST_USE), true)
        if (BuildConfig.DEBUG) {
//            firstUse = true; //TEST ONLY
        }
        if (firstUse) {
            // first time
            val editor = sharedPref.edit()
            editor.putBoolean(getString(R.string.KEY_FIRST_USE), false)
            editor.apply()
        }
        return firstUse
    }

    /* Save the reminders date in the shared preferences in case the device is rebooted.
    *  The end period reminder is not essential; it doesn't matter if it's not shown. */
    private fun saveReminderDate(type: Int, date: Long) {
        val editor = sharedPref.edit()
        when (type) {
            Reminder.NOTIFICATION_CODE_FRIENDLY ->
                editor.putLong(getString(R.string.KEY_FRIENDLY_REM_DATE), date)
            Reminder.NOTIFICATION_CODE_START ->
                editor.putLong(getString(R.string.KEY_PERIOD_REM_DATE), date)
        }
        editor.apply()
    }

    private fun initPreferences() {
        // Only when creating the app, check if there are some entry to delete from the db
        val history = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_HISTORY), getString(R.string.pref_history_default))!!)
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, (0 - history))

        db.deleteHistory(c.timeInMillis)

        readPreferences()
    }

    private fun readPreferences() {
        // Set the first day of the week
        day = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_DAY), getString(R.string.pref_startWeekDay_default))!!)

        // Swipe
        gesture = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_SWIPE), getString(R.string.pref_swipe_default))!!)

        // Set the value for the period length and for the cycle length
        calculatePeriodAndCycleLength(sharedPref)

        checkReminders(sharedPref)

        // theme
        val color = sharedPref.getString(getString(R.string.KEY_THEME), getString(R.string.pref_theme_default))
        if (color == getString(R.string.pref_theme_default))
            themeId = R.style.AppTheme
        else if (color == getString(R.string.pref_theme_blue))
            themeId = R.style.BlueTheme
        else if (color == getString(R.string.pref_theme_green))
            themeId = R.style.GreenTheme
        else if (color == getString(R.string.pref_theme_purple))
            themeId = R.style.PurpleTheme
    }

    override fun onResume() {
        super.onResume()
        readPreferences() // update the view if some preferences changed
        if (calendar != null) {
            calendar!!.setFirstDayOfWeek(day)
            calendar!!.setGesture(gesture)

            // this listener will be called after the view has being drawn. I need this trick because the async task
            // need the object today and it'll be available after the calendar is visible.
            calendar!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    // remove the listener or it will be called every time the view is drawn
                    calendar!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    // start the async task
                    val task = SetMessagesTask(this@MainActivity, bar, calendar!!.getToday())
                    task.execute()
                }
            })
        }
    }

    fun resetDateToday(view: View) {
        calendar!!.resetDate()
    }

    private fun fireTestNotification() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, 5)
        Reminder.scheduleNotification(Reminder.getNotification(Reminder.NOTIFICATION_CODE_FRIENDLY, this@MainActivity),
                calendar.timeInMillis,
                Reminder.NOTIFICATION_CODE_FRIENDLY,
                this@MainActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra(getString(R.string.KEY_THEME), themeId)
                val bundle = ActivityOptionsCompat.makeCustomAnimation(this@MainActivity,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
                startActivityForResult(intent, CHANGE_MEDS_CODE, bundle)
                return true
            }
            R.id.settings -> {
                val i = Intent(this, SettingsActivity::class.java)
                i.putExtra(getString(R.string.KEY_THEME), themeId)
                val bundle = ActivityOptionsCompat.makeCustomAnimation(this@MainActivity,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
                startActivity(i, bundle)
                return true
            }
            R.id.statistics -> {
                val i = Intent(this, StatisticsActivity::class.java)
                i.putExtra(getString(R.string.KEY_THEME), themeId)
                val bundle = ActivityOptionsCompat.makeCustomAnimation(this@MainActivity,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
                startActivity(i, bundle)
                return true
            }
            R.id.tutorial -> {
                val i = Intent(this, TutorialActivity::class.java)
                val bundle = ActivityOptionsCompat.makeCustomAnimation(this@MainActivity,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
                startActivity(i, bundle)
                return true
            }
            R.id.feedback -> {
                // send an email to the developer
                val data = "Device info:" +
                        "Model: " + Build.MODEL + "\n" +
                        "Manufacturer: " + Build.MANUFACTURER + "\n" +
                        "Brand: " + Build.BRAND + "\n" +
                        "Android: " + Build.VERSION.RELEASE + "(skd " + Build.VERSION.SDK_INT + ")\n" +
                        "App version code: " + BuildConfig.VERSION_CODE + "\n" +
                        "App version name: " + BuildConfig.VERSION_NAME + "\n\n"
                val uriText =
                        "mailto:agnesebussone+appsupport@gmail.com" +
                                "?subject=" + Uri.encode("LadyCal feedback") +
                                "&body=" + Uri.encode(data)

                val uri = Uri.parse(uriText)

                val sendIntent = Intent(Intent.ACTION_SENDTO)
                sendIntent.data = uri
                startActivity(Intent.createChooser(sendIntent, "Send email with"))
                return true
            }
            R.id.rate -> {
                val i = Intent(this, RateActivity::class.java)
                i.putExtra(getString(R.string.KEY_THEME), themeId)
                val bundle = ActivityOptionsCompat.makeCustomAnimation(this@MainActivity,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
                startActivity(i, bundle)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CHANGE_MEDS_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val date = data!!.getLongExtra(CHANGE_MEDS_STRING_ID, -1)
                    if (date != -1L) {
                        calendar!!.gotoDate(date)
                    }
                }
            }
        }
    }

    fun startPeriod(view: View) {
        if (selectedDay != null) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedDay!!.dayUTC
            val day_string = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(cal.time)
            if (selectedDay!!.isPeriod) {
                end_period(selectedDay!!, day_string)
            } else {
                start_period(selectedDay!!, day_string)
            }
        } else {
            val today = calendar!!.getToday()
            if (today!!.isPeriod) {
                end_period(today, "today")
            } else {
                start_period(today, "today")
            }
        }

        calendar!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

            override fun onGlobalLayout() {
                // remove the listener or it will be called every time the view is drawn
                calendar!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // start the async task
                val task = SetMessagesTask(this@MainActivity, bar, calendar!!.getToday())
                task.execute()
            }
        })
    }

    private fun end_period(day: Day, day_string: String) {
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle(R.string.dialog_end_period)

        alertDialog.setMessage(day_string)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", DialogInterface.OnClickListener { dialog, _ ->
            // check if it's not in the future
            if (day.dayUTC > calendar!!.getToday()!!.dayUTC) {
                Toast.makeText(this@MainActivity, "Operations in the future not allowed", Toast.LENGTH_LONG).show()
                // get rid of the selection icon in the view
                calendar!!.refreshCalendar()
                dialog.dismiss()
                return@OnClickListener
            }
            EndPeriodTask(this@MainActivity, calendar).execute(day)
            dialog.dismiss()
        })
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                DialogInterface.OnClickListener { dialog, _ ->
                    // get rid of the selection icon in the view
                    calendar!!.refreshCalendar()
                    dialog.dismiss()
                })
        alertDialog.show()
    }

    private fun start_period(day: Day, day_string: String) {
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle(R.string.dialog_add_period_title)

        alertDialog.setMessage(day_string)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                DialogInterface.OnClickListener { dialog, _ ->
                    val endDay: Day
                    val cal = Calendar.getInstance()

                    // check if it's not in the future
                    if (day.dayUTC > cal.timeInMillis) {
                        Toast.makeText(this@MainActivity, "Operations in the future not allowed", Toast.LENGTH_LONG).show()
                        // get rid of the selection icon in the view
                        calendar!!.refreshCalendar()
                        dialog.dismiss()
                        return@OnClickListener
                    }

                    // set the calendar to the start day
                    cal.timeInMillis = day.dayUTC

                    // add the number of days of the period length to calculate the end day (- 1 is to consider the selected day as first day)
                    cal.add(Calendar.DATE, (db.periodLength - 1))
                    endDay = Day(null, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

                    AddPeriodTask(this@MainActivity, calendar).execute(day, endDay)

                    dialog.dismiss()

                    // Set end period reminder
                    if (sharedPref.getBoolean(getString(R.string.KEY_PERIOD_REM), false)) {
                        // cal is set to the end of the period
                        if (Calendar.getInstance().timeInMillis < cal.timeInMillis) {
                            Reminder.scheduleNotification(Reminder.getNotification(Reminder.NOTIFICATION_CODE_END, this@MainActivity),
                                    cal.timeInMillis,
                                    Reminder.NOTIFICATION_CODE_END,
                                    this@MainActivity)
                            if (BuildConfig.DEBUG) {
//                                    Log.d(TAG, "scheduled end reminder");
                            }
                        }
                    }
                    // Set the friendly and the start for the next period
                    checkReminders(sharedPref)
                })
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                DialogInterface.OnClickListener { dialog, _ ->
                    // get rid of the selection icon in the view
                    calendar!!.refreshCalendar()
                    dialog.dismiss()
                })
        alertDialog.show()
    }

    fun addMed(view: View) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.add_meds_dialog, null)
        dialogBuilder.setView(dialogView)

        val num = dialogView.findViewById<TextView>(R.id.number)

        var day = "today"
        if (selectedDay != null) {
            val cal = Calendar.getInstance()
            cal.set(selectedDay!!.year, selectedDay!!.month, selectedDay!!.day)
            day = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(cal.time)
            num.text = selectedDay!!.meds.toString()
        } else {
            num.text = calendar!!.getToday()!!.meds.toString()
        }

        val tv = dialogView.findViewById<TextView>(R.id.day)
        tv.text = day

        dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
            val n = Integer.parseInt(num.text.toString())

            val day: Day
            val cal = Calendar.getInstance()
            if (selectedDay == null) {
                // get today date
                day = calendar!!.getToday()!!
            } else {
                day = selectedDay!!
                // check if it's not in the future
                if (day.dayUTC > cal.timeInMillis) {
                    Toast.makeText(this@MainActivity, "Operations in the future not allowed", Toast.LENGTH_LONG).show()
                    // get rid of the selection icon in the view
                    calendar!!.refreshCalendar()
                    dialog.dismiss()
                    return@OnClickListener
                }
            }

            if (day.isPeriod) {
                day.meds = n
                AddMedTask(this@MainActivity, calendar).execute(day)
            } else {
                Toast.makeText(this@MainActivity, "You cannot set medicine for non period day", Toast.LENGTH_LONG).show()
                // get rid of the selection icon in the view
                calendar!!.refreshCalendar()
            }
            dialog.dismiss()
        })
        dialogBuilder.setNegativeButton("CANCEL") { dialog, _ ->
            // get rid of the selection icon in the view
            calendar!!.refreshCalendar()
            dialog.dismiss()
        }
        val b = dialogBuilder.create()
        b.show()
    }

    private fun calculatePeriodAndCycleLength(sharedPref: SharedPreferences) {

        var useFixed = sharedPref.getBoolean(getString(R.string.KEY_PERIOD), false)
        val periodLength = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_PERIOD_VALUE), getString(R.string.pref_periodLength_default))!!)
        db.setPeriodLength(periodLength, useFixed)

        useFixed = sharedPref.getBoolean(getString(R.string.KEY_CYCLE), false)
        val cycleLength = Integer.parseInt(sharedPref.getString(getString(R.string.KEY_CYCLE_VALUE), getString(R.string.pref_cycleLength_default))!!)
        db.setCycleLength(cycleLength, useFixed)
    }

    private fun checkReminders(sharedPref: SharedPreferences) {
        val friendlyRem = sharedPref.getBoolean(getString(R.string.KEY_FRIENDLY_REM), false)
        val periodRem = sharedPref.getBoolean(getString(R.string.KEY_PERIOD_REM), false)

        if (friendlyRem || periodRem) {
            val c = Calendar.getInstance()
            val now = c.timeInMillis
            val last = db.getLastPeriod()
            if (last != 0L) {
                c.timeInMillis = last
                c.add(Calendar.DATE, (db.cycleLength - 3)) // friendly reminder date
                if (now <= c.timeInMillis && friendlyRem) {
                    val date = c.timeInMillis
                    Reminder.scheduleNotification(Reminder.getNotification(Reminder.NOTIFICATION_CODE_FRIENDLY, this@MainActivity),
                            date,
                            Reminder.NOTIFICATION_CODE_FRIENDLY,
                            this@MainActivity)
                    saveReminderDate(Reminder.NOTIFICATION_CODE_FRIENDLY, date)
                    if (BuildConfig.DEBUG) {
//                        Log.d(TAG, "scheduled friendly reminder");
                    }
                }
                if (periodRem) {
                    c.add(Calendar.DATE, 3) // start period reminder date
                    if (now <= c.timeInMillis) {
                        val date = c.timeInMillis
                        Reminder.scheduleNotification(Reminder.getNotification(Reminder.NOTIFICATION_CODE_START, this@MainActivity),
                                date,
                                Reminder.NOTIFICATION_CODE_START,
                                this@MainActivity)
                        saveReminderDate(Reminder.NOTIFICATION_CODE_START, date)
                        if (BuildConfig.DEBUG) {
//                            Log.d(TAG, "scheduled start reminder");
                        }
                    }
                }
            }
        }
    }

    fun decreaseMed(view: View) {
        val root = view.rootView
        val num = root.findViewById<TextView>(R.id.number)
        var n = Integer.parseInt(num.text.toString())
        n--
        if (n >= 0) {
            num.text = n.toString()
        }
    }

    fun increaseMed(view: View) {
        val root = view.rootView
        val num = root.findViewById<TextView>(R.id.number)
        var n = Integer.parseInt(num.text.toString())
        n++
        if (n <= 20) {
            num.text = n.toString()
        }
    }

    private fun createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)!!
            val friendly = createNotificationChannel(
                    getString(R.string.pref_friendly_reminder),
                    getString(R.string.pref_friendly_reminder_sub),
                    Reminder.FRIENDLY_CHANNEL_ID)
            notificationManager.createNotificationChannel(friendly)
            val start = createNotificationChannel(
                    getString(R.string.period_reminder_start),
                    getString(R.string.period_reminder_start_desc),
                    Reminder.START_CHANNEL_ID)
            notificationManager.createNotificationChannel(start)
            val end = createNotificationChannel(
                    getString(R.string.period_reminder_end),
                    getString(R.string.period_reminder_end_desc),
                    Reminder.END_CHANNEL_ID)
            notificationManager.createNotificationChannel(end)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(name: String, description: String, id: String): NotificationChannel {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        return channel
    }
}
