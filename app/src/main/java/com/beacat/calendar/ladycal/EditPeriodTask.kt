package com.beacat.calendar.ladycal

import android.content.Context
import android.os.AsyncTask

import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

/**
 * Task to be used to update a period from the db.
 * It takes two Period objects as input: the first that contains the old values and the second with the new values
 */

class EditPeriodTask(
    private val context: Context // needed to insert the new entry into the db
) : AsyncTask<Period, Void, Void>() { //params, progress, result

    override fun doInBackground(vararg params: Period): Void? {
        PeriodDatabase.getInstance(context).updatePeriod(params[0], params[1])
        return null
    }

    override fun onPostExecute(v: Void?) {
    }
}
