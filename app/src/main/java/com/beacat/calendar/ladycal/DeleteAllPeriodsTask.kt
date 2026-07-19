package com.beacat.calendar.ladycal

import android.content.Context
import android.os.AsyncTask

import com.tyczj.extendedcalendarview.PeriodDatabase

/**
 *
 */
class DeleteAllPeriodsTask(
    private val context: Context // needed to insert the new entry into the db
) : AsyncTask<Void, Void, Void>() { //params, progress, result

    override fun doInBackground(vararg params: Void): Void? {
        val db = PeriodDatabase.getInstance(context)
        db.resetDB()
        return null
    }

    override fun onPostExecute(v: Void?) {
    }
}
