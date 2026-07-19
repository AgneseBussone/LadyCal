package com.beacat.calendar.ladycal

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.ActionBar

import com.tyczj.extendedcalendarview.Day
import com.tyczj.extendedcalendarview.ExtendedCalendarView
import com.tyczj.extendedcalendarview.PeriodDatabase

/**
 * Task to update the messages in the main view.
 */

class SetMessagesTask(
    private val context: Context, // needed to insert the new entry into the db
    private val bar: ActionBar?,
    private val today: Day?
) : AsyncTask<Void, Void, String>() { //params, progress, result

    override fun onPreExecute() {
        bar!!.subtitle = ""
    }

    override fun doInBackground(vararg params: Void): String {
        val db = PeriodDatabase.getInstance(context)
        var mex = ""

        val start = db.getLastPeriod()
        if (start != 0L) {
            var offset = ExtendedCalendarView.getDifferenceInDays(today!!.dayUTC, start)

            if (today.isPeriod) {
                offset++
                // N-th day of period
                mex += offset.toString()
                when (offset.toInt()) {
                    1 -> mex += " st "
                    2 -> mex += " nd "
                    3 -> mex += " rd "
                    else -> mex += " th "
                }
                mex += "day of period"
            } else {
                // N days until next period / late
                var cycleLength = db.cycleLength
                cycleLength -= offset.toInt()
                if (cycleLength == 0) {
                    mex = "Today should be the first day"
                } else if (cycleLength > 0) {
                    mex = context.resources.getQuantityString(R.plurals.day_until_next_period, cycleLength, cycleLength)
                } else {
                    cycleLength = Math.abs(cycleLength)
                    mex = context.resources.getQuantityString(R.plurals.day_late, cycleLength, cycleLength)
                }
            }
        }

        return mex
    }

    override fun onPostExecute(mex: String) {
        bar!!.subtitle = mex
    }
}
