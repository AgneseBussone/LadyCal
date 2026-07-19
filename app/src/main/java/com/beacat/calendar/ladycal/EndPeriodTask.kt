package com.beacat.calendar.ladycal

import android.content.Context
import android.os.AsyncTask

import com.tyczj.extendedcalendarview.Day
import com.tyczj.extendedcalendarview.ExtendedCalendarView
import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

/**
 * Task to be used to end a period in a day that is marked as period
 * It takes the ending day as a parameter.
 */

class EndPeriodTask(
    private val context: Context, // needed to insert the new entry into the db
    private val calendarView: ExtendedCalendarView? //needed to refresh the view after the insertion of new period from the main view
) : AsyncTask<Day, Void, Void>() { //params, progress, result

    override fun doInBackground(vararg params: Day): Void? {
        val db = PeriodDatabase.getInstance(context)
        val period = db.getPeriod(params[0].periodId)

        if (period != null) {
            // the cycle length will be update if necessary
            db.updatePeriod(period,
                    Period(period.startDay, params[0].dayUTC,
                    ExtendedCalendarView.getDifferenceInDays(params[0].dayUTC, period.startDay) + 1,
                    period.cycleLength))
        }
        return null
    }

    override fun onPostExecute(v: Void?) {
        // update the view
        calendarView?.refreshCalendar()
    }
}
