package com.beacat.calendar.ladycal

import android.content.Context
import android.os.AsyncTask

import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

/**
 * Task to be used to delete a period from the db.
 */

class DeletePeriodTask(
    private val context: Context // needed to insert the new entry into the db
) : AsyncTask<Period, Void, Void>() { //params, progress, result

    override fun doInBackground(vararg params: Period): Void? {
        PeriodDatabase.getInstance(context).deletePeriod(params[0])
        return null
    }

    override fun onPostExecute(v: Void?) {
    }
}
