package com.beacat.calendar.ladycal


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import com.tyczj.extendedcalendarview.Day
import com.tyczj.extendedcalendarview.ExtendedCalendarView
import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity for adding past periods to the database.
 * It shows also all the periods already into the db.
 */

class HistoryActivity : AppCompatActivity() {

    private var listView: ListView? = null
    private var entries: MutableList<Period>? = null
    private var adapter: MyArrayAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = intent
        if (i != null) {
            setTheme(i.getIntExtra(getString(R.string.KEY_THEME), R.style.AppTheme))
        }
        setContentView(R.layout.history)

        listView = findViewById(R.id.periods_list)

        // Change the title in the action bar
        supportActionBar!!.setTitle(R.string.historyTitle)

        // Add back navigation
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        /* Use an Async task to load all the periods showing a spinner in the meantime */
        @SuppressLint("StaticFieldLeak")
        val task = object : AsyncTask<Void, Void, Void>() {
            val linlaHeaderProgress = findViewById<RelativeLayout>(R.id.progress_layout)

            override fun onPreExecute() {
                // show the spinner
                linlaHeaderProgress.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg params: Void): Void? {
                entries = PeriodDatabase.getInstance(applicationContext).getAllPeriods("DESC")
                if (BuildConfig.DEBUG) {
//                    SystemClock.sleep(5000); // TEST ONLY
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                if (entries != null) {
                    adapter = MyArrayAdapter(this@HistoryActivity, entries!!)
                    listView!!.adapter = adapter
                }
                // hide the spinner
                linlaHeaderProgress.visibility = View.GONE
            }
        }

        task.execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.history_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.deleteAll -> {
                val alertDialog = AlertDialog.Builder(this@HistoryActivity).create()
                alertDialog.setTitle(R.string.dialog_delete_all_period)

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
                    DeleteAllPeriodsTask(this@HistoryActivity).execute()
                    entries!!.clear()
                    adapter!!.notifyDataSetChanged()
                    dialog.dismiss()
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL") { dialog, _ -> dialog.dismiss() }
                alertDialog.show()
                return true
            }

            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Callback to add a period
     * @param view
     */
    fun addPeriod(view: View) {
        val builder = AlertDialog.Builder(this@HistoryActivity)
        val dialogView = layoutInflater.inflate(R.layout.add_period, null)
        builder.setView(dialogView)

        val start = dialogView.findViewById<DatePicker>(R.id.startDate)
        val end = dialogView.findViewById<DatePicker>(R.id.endDate)

        // set max date: it's not possible to set period in the future
        start.maxDate = Calendar.getInstance().timeInMillis
        end.maxDate = Calendar.getInstance().timeInMillis

        /* Workaround to leave the dialog open */
        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("CANCEL", null)
        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val startDay = Day(null, start.year, start.month, start.dayOfMonth)
            val endDay = Day(null, end.year, end.month, end.dayOfMonth)

            if (startDay.dayUTC <= endDay.dayUTC) {
                AddPeriodTask(this@HistoryActivity, null).execute(startDay, endDay)

                // It is an insertion at the end of the list, I don't care about sorting
                // because the next time the user call the activity the list will be sorted
                entries!!.add(Period(startDay.dayUTC, endDay.dayUTC))
                (listView!!.adapter as MyArrayAdapter).notifyDataSetChanged()

                dialog.dismiss()
            } else {
                Toast.makeText(this@HistoryActivity, "Error: start day after end day", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Inner class used by the list
     */
    private inner class MyArrayAdapter(private val context: Context, private val data: MutableList<Period>) : BaseAdapter() {

        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): Any {
            return data[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            if (convertView == null) {
                // inflate the layout
                convertView = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
            }

            val item = getItem(position) as Period
            val period = convertView!!.findViewById<TextView>(R.id.period)
            val length = convertView.findViewById<TextView>(R.id.days)
            val deleteBtn = convertView.findViewById<Button>(R.id.deleteBtn)
            val editBtn = convertView.findViewById<Button>(R.id.editBtn)
            val medBtn = convertView.findViewById<Button>(R.id.editMedBtn)

            val cal = Calendar.getInstance()
            cal.timeInMillis = item.startDay
            period.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            period.setTextColor(UtilityClass.getPeriodListPrimaryTextColor(this@HistoryActivity))
            val days = ExtendedCalendarView.getDifferenceInDays(item.endDay, item.startDay) + 1
            length.text = "Length: " + days
            length.setTextColor(UtilityClass.getPeriodListSecondaryTextColor(this@HistoryActivity))

            /* Delete button */
            deleteBtn.setOnClickListener {
                val alertDialog = AlertDialog.Builder(this@HistoryActivity).create()
                alertDialog.setTitle(R.string.dialog_delete_period)

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
                    DeletePeriodTask(this@HistoryActivity).execute(item)
                    data.removeAt(position)
                    notifyDataSetChanged()
                    dialog.dismiss()
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL") { dialog, _ -> dialog.dismiss() }
                alertDialog.show()
            }

            /* Edit button */
            editBtn.setOnClickListener {
                val builder = AlertDialog.Builder(this@HistoryActivity)
                val dialogView = layoutInflater.inflate(R.layout.add_period, null)
                builder.setView(dialogView)

                val start = dialogView.findViewById<DatePicker>(R.id.startDate)
                val end = dialogView.findViewById<DatePicker>(R.id.endDate)
                // set max date: it's not possible to set period in the future
                start.maxDate = Calendar.getInstance().timeInMillis
                end.maxDate = Calendar.getInstance().timeInMillis

                cal.timeInMillis = item.startDay
                start.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null)
                cal.timeInMillis = item.endDay
                end.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null)

                /* Workaround to leave the dialog open */
                builder.setPositiveButton("OK", null)
                builder.setNegativeButton("CANCEL", null)
                val dialog = builder.create()
                dialog.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val startDay = Day(null, start.year, start.month, start.dayOfMonth)
                    val endDay = Day(null, end.year, end.month, end.dayOfMonth)

                    if (startDay.dayUTC <= endDay.dayUTC) {

                        // the cycle length will be updated if necessary
                        val new_period = Period(startDay.dayUTC,
                                endDay.dayUTC,
                                ExtendedCalendarView.getDifferenceInDays(endDay.dayUTC, startDay.dayUTC) + 1,
                                item.cycleLength)

                        EditPeriodTask(this@HistoryActivity).execute(item, new_period)

                        data.removeAt(position)
                        data.add(position, new_period)

                        notifyDataSetChanged()

                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@HistoryActivity, "Error: start day after end day", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            /* Medicine button */
            medBtn.setOnClickListener {
                val alertDialog = AlertDialog.Builder(this@HistoryActivity).create()
                alertDialog.setTitle(R.string.dialog_edit_medicine)

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES") { dialog, _ ->
                    val resultIntent = Intent()
                    resultIntent.putExtra(MainActivity.CHANGE_MEDS_STRING_ID, item.startDay)
                    setResult(Activity.RESULT_OK, resultIntent)

                    dialog.dismiss()

                    finish()
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO, STAY HERE") { dialog, _ -> dialog.dismiss() }
                alertDialog.show()
            }

            return convertView
        }
    }
}
