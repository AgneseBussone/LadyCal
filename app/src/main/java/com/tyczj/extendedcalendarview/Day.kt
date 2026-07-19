package com.tyczj.extendedcalendarview

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.widget.BaseAdapter

import java.util.Calendar

class Day(private val context: Context?, val year: Int, val month: Int, val day: Int) {

    private var adapter: BaseAdapter? = null
    var periodId: Long = -1
        private set
    var meds: Int = 0
    var isExpected: Boolean = false

    val isPeriod: Boolean
        get() = periodId != -1L

    val medicineDbEntry: ContentValues
        get() {
            val values = ContentValues()
            values.put(DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC, dayUTC)
            values.put(DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID, periodId)
            values.put(DatabaseStructure.MedEntry.COLUMN_NAME_QUANTITY, meds)
            return values
        }

    val dayUTC: Long
        get() {
            val cal = Calendar.getInstance()
            cal.clear()
            cal.set(year, month, day)
            return cal.timeInMillis
        }

    fun loadDay() {
        GetPeriod().execute(this)
    }

    fun setAdapter(adapter: BaseAdapter) {
        this.adapter = adapter
    }

    private inner class GetPeriod : AsyncTask<Day, Void, Void>() {

        override fun doInBackground(vararg params: Day): Void? {
            val db = PeriodDatabase.getInstance(context)
            // Mark if it's a period day
            periodId = db.isPeriod(params[0])

            // Look into the meds table to see if meds were taken this day
            meds = db.searchMeds(params[0].dayUTC)

            return null
        }

        override fun onPostExecute(par: Void?) {
            adapter!!.notifyDataSetChanged()
        }
    }
}
