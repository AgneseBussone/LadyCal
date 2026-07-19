package com.tyczj.extendedcalendarview

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.beacat.calendar.ladycal.BuildConfig

import java.util.ArrayList

/**
 * Class that manage the database.
 * This is a singleton class, so we can be sure that the same database is used
 * by every part of the application.
 */

class PeriodDatabase private constructor(context: Context?) :
    SQLiteOpenHelper(context, DatabaseStructure.DATABASE_NAME, null, DatabaseStructure.DATABASE_VERSION) {

    private val TAG = PeriodDatabase::class.java.simpleName

    var periodLength: Int = 0
        private set
    var cycleLength: Int = 0
        private set

    companion object {
        private var instance: PeriodDatabase? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context?): PeriodDatabase {
            if (instance == null) {
                instance = PeriodDatabase(context)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE " + DatabaseStructure.PeriodEntry.TABLE_NAME + "(" +
                DatabaseStructure.PeriodEntry._ID + " integer primary key autoincrement, " +
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START + " INTEGER, " +
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH + " INTEGER, " +
                DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH + " INTEGER, " +
                DatabaseStructure.PeriodEntry.COLUMN_NAME_END + " INTEGER);")
        db.execSQL("CREATE TABLE " + DatabaseStructure.MedEntry.TABLE_NAME + "(" +
                DatabaseStructure.MedEntry._ID + " integer primary key autoincrement, " +
                DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID + " INTEGER, " +
                DatabaseStructure.MedEntry.COLUMN_NAME_QUANTITY + " INTEGER, " +
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Called when the constructor is called with a newer version for the db. In such case,
        // you have to update the existing db with new tables/columns. See ALTER TABLE.
    }

    /**
     * Insert a new period into the database
     * @param period
     */
    fun addPeriod(period: Period) {
        if (searchPeriodId(period) == -1L) {

            // Create and/or open the database for writing
            val db = writableDatabase

            // Sometimes I experienced a wrong insertion, probably due to the fact that it reads the db,
            // calculates and then updates. If a useful entry is inserted after the first query, it'll insert
            // a non consistent info because the calculation is based on an old value.
            db.beginTransaction()
            try {

                // cycle length: difference between the starting day and the starting day of the NEXT period
                if (period.cycleLength == -1L) {
                    var nextStart: Long = 0
                    // search the next period (history insertion)
                    val query = String.format(
                            "SELECT MIN(%s) FROM %s WHERE %s > %s",
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            DatabaseStructure.PeriodEntry.TABLE_NAME,
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            period.startDay.toString())

                    val c = db.rawQuery(query, null)
                    if (c.count > 0) {
                        c.moveToFirst()
                        nextStart = c.getLong(0)
                    }
                    c.close()
                    if (nextStart == 0L) {
                        // apparently MIN returns 0 if nothing was found by the query.
                        // Set the default in this case.
                        period.cycleLength = cycleLength.toLong()
                    } else {
                        period.cycleLength = ExtendedCalendarView.getDifferenceInDays(nextStart, period.startDay)
                    }
                }

                // update the previous period with the real value
                var prevPeriodId: Long = -1
                var days: Long = -1
                val query = String.format(
                        "SELECT %s, %s FROM %s WHERE %s = (SELECT MAX(%s) FROM %s WHERE %s < %s)",
                        DatabaseStructure.PeriodEntry._ID,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        DatabaseStructure.PeriodEntry.TABLE_NAME,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        DatabaseStructure.PeriodEntry.TABLE_NAME,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        period.startDay.toString())
                val c = db.rawQuery(query, null)
                if (c.count > 0) {
                    c.moveToFirst()
                    prevPeriodId = c.getLong(0)
                    days = ExtendedCalendarView.getDifferenceInDays(period.startDay, c.getLong(1))
                }
                c.close()

                db.insertOrThrow(DatabaseStructure.PeriodEntry.TABLE_NAME, null, period.toDbEntry())
                if (prevPeriodId != -1L) {
                    val v = ContentValues()
                    v.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH, days)
                    db.update(DatabaseStructure.PeriodEntry.TABLE_NAME, v, "_id = ?", arrayOf(prevPeriodId.toString()))
                }
                db.setTransactionSuccessful()

            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Error while trying to add period to database")
                }
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * Insert a new medicine record or update if necessary
     * @param day
     */
    fun addMed(day: Day) {
        // Create and/or open the database for writing
        val db = writableDatabase
        var id: Long = -1

        // Check if this day already exists in the db
        val query = String.format(
                "SELECT %s FROM %s WHERE %s = %s",
                DatabaseStructure.MedEntry._ID,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                day.dayUTC.toString())

        val c = db.rawQuery(query, null)

        if (c.moveToFirst()) {
            id = c.getLong(0)
        }
        c.close()

        db.beginTransaction()
        try {
            if (id == -1L) {
                val m = db.insertOrThrow(DatabaseStructure.MedEntry.TABLE_NAME, null, day.medicineDbEntry)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("add med medId=%d: pId=%d", m, day.periodId))
                }
            } else {
                val r = db.update(DatabaseStructure.MedEntry.TABLE_NAME, day.medicineDbEntry, "_id = ?", arrayOf(id.toString()))
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("add med #row=%d, medId=%d", r, id))
                }
            }
            db.setTransactionSuccessful()

        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to add medicine to database")
            }
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Delete a period from the database
     * @param period
     */
    fun deletePeriod(period: Period) {
        // Create and/or open the database for writing
        val db = writableDatabase

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction()
        try {
            val id = searchPeriodId(period)
            if (id != -1L) {
                db.delete(DatabaseStructure.PeriodEntry.TABLE_NAME, "_id = ?", arrayOf(id.toString()))
                db.delete(DatabaseStructure.MedEntry.TABLE_NAME, DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID + " = ?", arrayOf(id.toString()))
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete period from database")
            }
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Update a period dates from the database
     * @param old
     * @param updated
     */
    fun updatePeriod(old: Period, updated: Period) {
        val id = searchPeriodId(old)
        if (id != -1L) {
            // Create and/or open the database for writing
            val db = writableDatabase

            // See comments for addPeriod()...
            db.beginTransaction()
            try {

                var prevPeriodId: Long = -1
                var prevCycleLength: Long = cycleLength.toLong()

                // cycle length: only the start days count
                if (old.startDay != updated.startDay) {
                    // offset between the changes
                    // positive = old > updated -> current cycle longer, previous shorter
                    // negative = old < updated -> current cycle shorter, previous longer
                    val offset = ExtendedCalendarView.getDifferenceInDays(old.startDay, updated.startDay)

                    // in case is the last period inserted, the current cycle length is a guess, so must remain the same
                    var query = String.format(
                            "SELECT MAX(%s) FROM %s",
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            DatabaseStructure.PeriodEntry.TABLE_NAME)
                    var c = db.rawQuery(query, null)
                    if (c.moveToFirst()) {
                        if (c.getLong(0) != old.startDay) {
                            // not the last period, so update the length
                            updated.cycleLength = old.cycleLength + offset
                        } else {
                            // last period, so insert the guess
                            updated.cycleLength = cycleLength.toLong()
                        }
                    }
                    c.close()

                    // get the value of the PREVIOUS
                    query = String.format(
                            "SELECT %s, %s FROM %s WHERE %s = (SELECT MAX(%s) FROM %s WHERE %s < %s)",
                            DatabaseStructure.PeriodEntry._ID,
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH,
                            DatabaseStructure.PeriodEntry.TABLE_NAME,
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            DatabaseStructure.PeriodEntry.TABLE_NAME,
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            old.startDay.toString())
                    c = db.rawQuery(query, null)
                    if (c.moveToFirst()) {
                        prevPeriodId = c.getLong(0)
                        prevCycleLength = c.getLong(1)
                        prevCycleLength -= offset
                    }
                    c.close()
                }

                db.update(DatabaseStructure.PeriodEntry.TABLE_NAME, updated.toDbEntry(), "_id = ?", arrayOf(id.toString()))
                if (prevPeriodId != -1L) {
                    val v = ContentValues()
                    v.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH, prevCycleLength)
                    db.update(DatabaseStructure.PeriodEntry.TABLE_NAME, v, "_id = ?", arrayOf(prevPeriodId.toString()))
                }

                // check if the meds tab needs to be updated: delete all days that are not period anymore
                val whereClause = DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID + " = ? AND (" +
                        DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " < ? OR " +
                        DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " > ?)"
                db.delete(DatabaseStructure.MedEntry.TABLE_NAME, whereClause,
                        arrayOf(id.toString(), updated.startDay.toString(), updated.endDay.toString()))

                db.setTransactionSuccessful()
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Error while trying to update period to database")
                }
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * Search the id of the specified period
     * @param period
     * @return the id of the entry or -1 if it not exists
     */
    fun searchPeriodId(period: Period): Long {
        var id: Long = -1
        val db = readableDatabase

        val query = String.format(
                "SELECT %s FROM %s WHERE %s = %s AND %s = %s",
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                period.startDay.toString(),
                DatabaseStructure.PeriodEntry.COLUMN_NAME_END,
                period.endDay.toString())

        val c = db.rawQuery(query, null)

        if (c.moveToFirst()) {
            id = c.getLong(0)
        }
        c.close()
        return id
    }

    /**
     * Search the number of meds for a given day (in UTC)
     * @param day
     * @return
     */
    fun searchMeds(day: Long): Int {
        var meds = 0

        val db = readableDatabase

        val query = String.format(
                "SELECT %s FROM %s WHERE %s = %s",
                DatabaseStructure.MedEntry.COLUMN_NAME_QUANTITY,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                day.toString())

        val c = db.rawQuery(query, null)
        if (c.moveToFirst()) {
            meds = c.getInt(0)
        }
        c.close()
        return meds
    }

    /**
     * Check if the day is part of a period
     * @param day
     * @return the id of the period or -1 if the day does not belong to any period
     */
    fun isPeriod(day: Day): Long {
        var id: Long = -1
        val db = readableDatabase

        val date = day.dayUTC.toString()

        val query = String.format(
                "SELECT %s FROM %s WHERE %s >= %s AND %s <= %s",
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                date,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                date,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_END)

        val c = db.rawQuery(query, null)
        if (c.moveToFirst()) {
            id = c.getLong(0)
        }
        c.close()
        return id
    }

    /**
     * Search in the database for the given period id
     * @param id
     * @return the period object or null
     */
    fun getPeriod(id: Long): Period? {
        var p: Period? = null

        val db = readableDatabase

        val query = String.format(
                "SELECT * FROM %s WHERE _id = %s",
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                id)

        val c = db.rawQuery(query, null)
        if (c.moveToFirst()) {
            p = Period(c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_START)),
                    c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_END)),
                    c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH)),
                    c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH)))
        }
        c.close()

        return p
    }

    /**
     * Get all periods from the db
     * @param orderBy is the ordering flag (ASC or DESC)
     * @return the list with all the periods or null if no period was found
     * */
    fun getAllPeriods(orderBy: String): MutableList<Period>? {
        var list: MutableList<Period>? = null

        val query = String.format("SELECT * FROM %s ORDER BY %s %s",
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                orderBy)
        val db = readableDatabase
        val c = db.rawQuery(query, null)
        if (c.moveToFirst()) {
            list = ArrayList(c.count)
            do {
                val p = Period(c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_START)),
                        c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_END)),
                        c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH)),
                        c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH)))
                list.add(p)
            } while (c.moveToNext())
        }
        c.close()
        return list
    }

    /**
     * Get all medicine from the db
     * @return the list with the data or null if no med was found
     * */
    fun getAllMeds(): List<Med>? {
        var list: MutableList<Med>? = null

        // Select all period with medical records
        var query = String.format("SELECT %s, %s, %s  FROM %s WHERE %s IN(SELECT %s FROM %s) ORDER BY %s ASC",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH,
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START)
        val db = readableDatabase
        val p = db.rawQuery(query, null)
        if (p.moveToFirst()) {
            list = ArrayList(p.count)
            do {
                val med = Med(p.getLong(0), p.getInt(1))

                // select all medical record for the specified period
                query = String.format("SELECT %s, %s FROM %s WHERE %s = %s",
                        DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                        DatabaseStructure.MedEntry.COLUMN_NAME_QUANTITY,
                        DatabaseStructure.MedEntry.TABLE_NAME,
                        DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID,
                        p.getLong(2))
                val m = db.rawQuery(query, null)
                if (m.count > 0) {
                    m.moveToFirst()
                    do {
                        // day_of_medicine - first_day_of_period = day_in_period
                        val d = ExtendedCalendarView.getDifferenceInDays(m.getLong(0), med.date)
                        med.setDay(d.toInt(), m.getInt(1))
                    } while (m.moveToNext())
                }
                m.close()

                list.add(med)

            } while (p.moveToNext())
        }
        p.close()

        return list
    }

    fun getMaxPeriodLength(): Int {
        var l = 0
        // Select all period with medical records
        val query = String.format("SELECT MAX(%s) FROM %s WHERE %s IN(SELECT %s FROM %s)",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID,
                DatabaseStructure.MedEntry.TABLE_NAME)
        val db = readableDatabase
        val p = db.rawQuery(query, null)
        if (p.moveToFirst()) {
            l = p.getInt(0)
        }
        p.close()
        return l
    }

    fun resetDB() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(DatabaseStructure.PeriodEntry.TABLE_NAME, null, null)
            db.delete(DatabaseStructure.MedEntry.TABLE_NAME, null, null)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete tables")
            }
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Search the last period in time
     * @return the starting day of the last period in UTC format
     */
    fun getLastPeriod(): Long {
        var last: Long = 0
        val query = String.format("SELECT MAX(%s) FROM %s",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                DatabaseStructure.PeriodEntry.TABLE_NAME)
        val db = readableDatabase
        val c = db.rawQuery(query, null)
        if (c.moveToFirst()) {
            last = c.getLong(0)
        }
        c.close()
        return last
    }

    /* Getters and setters for the two lengths. They are used at the beginning (to init the db if is empty)
     * and to derive expected periods based on the preference of the user (if she wants to use fixed value,
     * it's not used the avg. The avg is shown anyway in the statistics)
     */
    fun setPeriodLength(defaultLength: Int, useDefaultValue: Boolean) {
        if (useDefaultValue)
            periodLength = defaultLength
        else {
            val l = getPeriodLengthAvg()
            periodLength = if (l != 0) l else defaultLength
        }
    }

    fun setCycleLength(defaultLength: Int, useDefaultValue: Boolean) {
        if (useDefaultValue)
            cycleLength = defaultLength
        else {
            val l = getCycleLengthAvg()
            cycleLength = if (l != 0) l else defaultLength
        }
    }

    /** Calculate the avg basing on the db values
     * @return the avg or 0 if the db is empty or an error occur */
    fun getPeriodLengthAvg(): Int {
        var retval = 0
        val query = String.format("SELECT AVG(%s) FROM %s",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH,
                DatabaseStructure.PeriodEntry.TABLE_NAME)
        val db = readableDatabase
        val c = db.rawQuery(query, null)
        if (c.moveToFirst()) {
            val l = c.getInt(0)
            if (l > 0)
                retval = l
        }
        c.close()
        return retval
    }

    /** Calculate the avg basing on the db values
     * @return the avg or 0 if the db is empty or an error occur */
    fun getCycleLengthAvg(): Int {
        var retval = 0
        val query = String.format("SELECT AVG(%s) FROM %s WHERE %s > 0",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH)
        val db = readableDatabase
        val c = db.rawQuery(query, null)
        if (c.moveToFirst()) {
            val l = c.getInt(0)
            if (l > 0)
                retval = l
        }
        c.close()
        return retval
    }

    fun deleteMed(day: Day) {
        val db = writableDatabase
        var id: Long = -1
        val query = String.format(
                "SELECT %s FROM %s WHERE %s = %s",
                DatabaseStructure.MedEntry._ID,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                day.dayUTC.toString())

        val c = db.rawQuery(query, null)

        if (c.moveToFirst()) {
            id = c.getLong(0)
        }
        c.close()

        db.beginTransaction()
        try {
            if (id != -1L) {
                db.delete(DatabaseStructure.MedEntry.TABLE_NAME, "_id = ?", arrayOf(id.toString()))
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete period from database")
            }
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Delete all entry from Period and Med older than the limit.
     * @param limit
     */
    fun deleteHistory(limit: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(DatabaseStructure.PeriodEntry.TABLE_NAME, DatabaseStructure.PeriodEntry.COLUMN_NAME_START + " <= ?",
                    arrayOf(limit.toString()))
            db.delete(DatabaseStructure.MedEntry.TABLE_NAME, DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " <= ?",
                    arrayOf(limit.toString()))
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete old entries from database")
            }
        } finally {
            db.endTransaction()
        }
    }
}
