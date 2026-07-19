package com.beacat.calendar.ladycal

import android.content.Context
import android.os.AsyncTask

import com.tyczj.extendedcalendarview.Day
import com.tyczj.extendedcalendarview.ExtendedCalendarView
import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

import java.util.Calendar

/**
 * Task to be used when a new period has to be inserted into the db.
 * It takes as input two Days objects (start and end).
 */

class AddPeriodTask(
    private val context: Context, // needed to insert the new entry into the db
    private val calendarView: ExtendedCalendarView? //needed to refresh the view after the insertion of new period from the main view
) : AsyncTask<Day, Void, Void>() { //params, progress, result

    override fun doInBackground(vararg params: Day): Void? {
        val cal = Calendar.getInstance()
        cal.clear()

        cal.set(params[0].year, params[0].month, params[0].day)
        val start = cal.timeInMillis
        cal.set(params[1].year, params[1].month, params[1].day)
        val end = cal.timeInMillis

        if (start <= end) {

            val db = PeriodDatabase.getInstance(context)

            db.addPeriod(Period(start, end))
        }

        return null
    }

    override fun onPostExecute(v: Void?) {
        // update the view
        calendarView?.refreshCalendar()
    }
}
