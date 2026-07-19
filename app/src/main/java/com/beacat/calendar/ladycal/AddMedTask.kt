package com.beacat.calendar.ladycal

import android.content.Context
import android.os.AsyncTask

import com.tyczj.extendedcalendarview.Day
import com.tyczj.extendedcalendarview.ExtendedCalendarView
import com.tyczj.extendedcalendarview.PeriodDatabase

/**
 * Task that adds a med record.
 * Takes as input the day; the med field must be filled by the caller.
 */

class AddMedTask(
    private val context: Context, // needed to insert the new entry into the db
    private val calendarView: ExtendedCalendarView? //needed to refresh the view after the insertion of new period from the main view
) : AsyncTask<Day, Void, Void>() { //params, progress, result

    override fun doInBackground(vararg params: Day): Void? {

        val db = PeriodDatabase.getInstance(context)

        if (params[0].meds > 0) {
            db.addMed(params[0])
        } else {
            db.deleteMed(params[0])
        }

        return null
    }

    override fun onPostExecute(v: Void?) {
        // update the view
        calendarView?.refreshCalendar()
    }
}
