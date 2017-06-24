package com.tyczj.extendedcalendarview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that manage the database.
 * This is a singleton class, so we can be sure that the same database is used
 * by every part of the application.
 */

public class PeriodDatabase extends SQLiteOpenHelper{

     private final String TAG = PeriodDatabase.class.getSimpleName();
    private static PeriodDatabase instance;
    private int periodLength;
    private int cycleLength;

    public static synchronized PeriodDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new PeriodDatabase(context);
        }
        return instance;
    }

    private PeriodDatabase(Context context) {
        super(context, DatabaseStructure.DATABASE_NAME, null, DatabaseStructure.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DatabaseStructure.PeriodEntry.TABLE_NAME + "(" +
                DatabaseStructure.PeriodEntry._ID + " integer primary key autoincrement, " +
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START + " INTEGER, "+
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH + " INTEGER, "+
                DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH + " INTEGER, "+
                DatabaseStructure.PeriodEntry.COLUMN_NAME_END + " INTEGER);");
        db.execSQL("CREATE TABLE " + DatabaseStructure.MedEntry.TABLE_NAME + "(" +
                DatabaseStructure.MedEntry._ID + " integer primary key autoincrement, " +
                DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID + " INTEGER, "+
                DatabaseStructure.MedEntry.COLUMN_NAME_QUANTITY + " INTEGER, " +
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Called when the constructor is called with a newer version for the db. In such case,
        // you have to update the existing db with new tables/columns. See ALTER TABLE.
    }

    /**
     * Insert a new period into the database
     * @param period
     */
    public void addPeriod(Period period){
        if(searchPeriodId(period) == -1 ) {

            // Create and/or open the database for writing
            SQLiteDatabase db = getWritableDatabase();

            // Sometimes I experienced a wrong insertion, probably due to the fact that it reads the db,
            // calculates and then updates. If a useful entry is inserted after the first query, it'll insert
            // a non consistent info because the calculation is based on an old value.
            db.beginTransaction();
            try {

                // cycle length: difference between the starting day and the starting day of the NEXT period
                if (period.getCycleLength() == -1) {
                    long nextStart = 0;
                    // search the next period (history insertion)
                    String query = String.format(
                            "SELECT MIN(%s) FROM %s WHERE %s > %s",
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            DatabaseStructure.PeriodEntry.TABLE_NAME,
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            String.valueOf(period.getStartDay()));

                    Cursor c = db.rawQuery(query, null);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        nextStart = c.getLong(0);
                    }
                    c.close();
                    if (nextStart == 0) {
                        // apparently MIN returns 0 if nothing was found by the query.
                        // Set the default in this case.
                        period.setCycleLength(cycleLength);
                    }
                    else{
                        period.setCycleLength(ExtendedCalendarView.getDifferenceInDays(nextStart, period.getStartDay()));
                    }
                }

                // update the previous period with the real value
                long prevPeriodId = -1;
                long days = -1;
                String query = String.format(
                        "SELECT %s, %s FROM %s WHERE %s = (SELECT MAX(%s) FROM %s WHERE %s < %s)",
                        DatabaseStructure.PeriodEntry._ID,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        DatabaseStructure.PeriodEntry.TABLE_NAME,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        DatabaseStructure.PeriodEntry.TABLE_NAME,
                        DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                        String.valueOf(period.getStartDay()));
                Cursor c = db.rawQuery(query, null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    prevPeriodId = c.getLong(0);
                    days = ExtendedCalendarView.getDifferenceInDays(period.getStartDay(), c.getLong(1));
                }
                c.close();


                    long r = db.insertOrThrow(DatabaseStructure.PeriodEntry.TABLE_NAME, null, period.toDbEntry());
                    if (prevPeriodId != -1) {
                        ContentValues v = new ContentValues();
                        v.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH, days);
                        db.update(DatabaseStructure.PeriodEntry.TABLE_NAME, v, "_id = ?", new String[]{String.valueOf(prevPeriodId)});
                    }
                    db.setTransactionSuccessful();

            } catch (Exception e) {
                if(BuildConfig.DEBUG) {
                    Log.e(TAG, "Error while trying to add period to database");
                }
            } finally {
                db.endTransaction();
            }
        }
    }

    /**
     * Insert a new medicine record or update if necessary
     * @param day
     */
    public void addMed (Day day){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        long id = -1;

        // Check if this day already exists in the db
        String query = String.format(
                "SELECT %s FROM %s WHERE %s = %s",
                DatabaseStructure.MedEntry._ID,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                String.valueOf(day.getDayUTC()));

        Cursor c = db.rawQuery(query, null);

        if(c.moveToFirst()) {
            id = c.getLong(0);
        }
        c.close();

        db.beginTransaction();
        try {
            if(id == -1) {
                long m = db.insertOrThrow(DatabaseStructure.MedEntry.TABLE_NAME, null, day.getMedicineDbEntry());
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("add med medId=%d: pId=%d", m, day.getPeriodId()));
                }
            }
            else{
                int r = db.update(DatabaseStructure.MedEntry.TABLE_NAME, day.getMedicineDbEntry(), "_id = ?", new String[]{String.valueOf(id)});
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("add med #row=%d, medId=%d", r, id));
                }
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to add medicine to database");
            }
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Delete a period from the database
     * @param period
     */
    public void deletePeriod(Period period){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            long id = searchPeriodId(period);
            if( id != -1 ) {
                int p = db.delete(DatabaseStructure.PeriodEntry.TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
                int m = db.delete(DatabaseStructure.MedEntry.TABLE_NAME, DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID + " = ?", new String[]{String.valueOf(id)});
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete period from database");
            }
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Update a period dates from the database
     * @param old
     * @param updated
     */
    public void updatePeriod(Period old, Period updated){
        long id = searchPeriodId(old);
        if( id != -1 ) {
            // Create and/or open the database for writing
            SQLiteDatabase db = getWritableDatabase();

            // See comments for addPeriod()...
            db.beginTransaction();
            try {

                long prevPeriodId = -1;
                long prevCycleLength = cycleLength;

                // cycle length: only the start days count
                if (old.getStartDay() != updated.getStartDay()) {
                    // offset between the changes
                    // positive = old > updated -> current cycle longer, previous shorter
                    // negative = old < updated -> current cycle shorter, previous longer
                    long offset = ExtendedCalendarView.getDifferenceInDays(old.getStartDay(), updated.getStartDay());

                    // in case is the last period inserted, the current cycle length is a guess, so must remain the same
                    String query = String.format(
                            "SELECT MAX(%s) FROM %s",
                            DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                            DatabaseStructure.PeriodEntry.TABLE_NAME);
                    Cursor c = db.rawQuery(query, null);
                    if (c.moveToFirst()) {
                        if(c.getLong(0) != old.getStartDay()){
                            // not the last period, so update the length
                            updated.setCycleLength(old.getCycleLength() + offset);
                        }
                        else{
                            // last period, so insert the guess
                            updated.setCycleLength(cycleLength);
                        }
                    }
                    c.close();

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
                            String.valueOf(old.getStartDay()));
                    c = db.rawQuery(query, null);
                    if (c.moveToFirst()) {
                        prevPeriodId = c.getLong(0);
                        prevCycleLength = c.getLong(1);
                        prevCycleLength -= offset;
                    }
                    c.close();
                }

                db.update(DatabaseStructure.PeriodEntry.TABLE_NAME, updated.toDbEntry(), "_id = ?", new String[]{String.valueOf(id)});
                if (prevPeriodId != -1) {
                    ContentValues v = new ContentValues();
                    v.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH, prevCycleLength);
                    db.update(DatabaseStructure.PeriodEntry.TABLE_NAME, v, "_id = ?", new String[]{String.valueOf(prevPeriodId)});
                }

                // check if the meds tab needs to be updated: delete all days that are not period anymore
                String whereClause = DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID + " = ? AND (" +
                        DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " < ? OR " +
                        DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " > ?)";
                db.delete(DatabaseStructure.MedEntry.TABLE_NAME, whereClause,
                        new String[]{String.valueOf(id), String.valueOf(updated.getStartDay()), String.valueOf(updated.getEndDay())});


                db.setTransactionSuccessful();
            } catch (Exception e) {
                if(BuildConfig.DEBUG) {
                    Log.e(TAG, "Error while trying to update period to database");
                }
            } finally {
                db.endTransaction();
            }
        }
    }

    /**
     * Search the id of the specified period
     * @param period
     * @return the id of the entry or -1 if it not exists
     */
    public long searchPeriodId(Period period){
        long id = -1;
        SQLiteDatabase db = getReadableDatabase();

        String query = String.format(
                "SELECT %s FROM %s WHERE %s = %s AND %s = %s",
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                String.valueOf(period.getStartDay()),
                DatabaseStructure.PeriodEntry.COLUMN_NAME_END,
                String.valueOf(period.getEndDay()));

        Cursor c = db.rawQuery(query, null);

        if(c.moveToFirst()) {
            id = c.getLong(0);
        }
        c.close();
        return id;
    }


    /**
     * Search the number of meds for a given day (in UTC)
     * @param day
     * @return
     */
    public int searchMeds(long day){
        int meds = 0;

        SQLiteDatabase db = getReadableDatabase();

        String query = String.format(
                "SELECT %s FROM %s WHERE %s = %s",
                DatabaseStructure.MedEntry.COLUMN_NAME_QUANTITY,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                String.valueOf(day));

        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            meds = c.getInt(0);
        }
        c.close();
        return meds;
    }
    /**
     * Check if the day is part of a period
     * @param day
     * @return the id of the period or -1 if the day does not belong to any period
     */
    public long isPeriod(Day day){
        long id = -1;
        SQLiteDatabase db = getReadableDatabase();

        String date = String.valueOf(day.getDayUTC());

        String query = String.format(
                "SELECT %s FROM %s WHERE %s >= %s AND %s <= %s",
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                date,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                date,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_END);

        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()) {
            id = c.getLong(0);
        }
        c.close();
        return id;
    }

    /**
     * Search in the database for the given period id
     * @param id
     * @return the period object or null
     */
    public Period getPeriod(long id){
        Period p = null;

        SQLiteDatabase db = getReadableDatabase();

        String query = String.format(
                "SELECT * FROM %s WHERE _id = %s",
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                id);

        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()) {
            p = new Period(c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_START)),
                    c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_END)),
                    c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH)),
                    c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH)));
        }
        c.close();

        return p;
    }


    /**
    * Get all periods from the db
    * @param orderBy is the ordering flag (ASC or DESC)
    * @return the list with all the periods or null if no period was found
    * */
    public List<Period> getAllPeriods(String orderBy){
        List<Period> list = null;

        String query = String.format("SELECT * FROM %s ORDER BY %s %s",
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                orderBy);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            list = new ArrayList<>(c.getCount());
            do{
                Period p = new Period(c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_START)),
                        c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_END)),
                        c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH)),
                        c.getLong(c.getColumnIndex(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH)));
                list.add(p);
            }while(c.moveToNext());
        }
        c.close();
        return list;
    }

    /**
     * Get all medicine from the db
     * @return the list with the data or null if no med was found
     * */
    public List<Med> getAllMeds(){
        List<Med> list = null;

        // Select all period with medical records
        String query = String.format("SELECT %s, %s, %s  FROM %s WHERE %s IN(SELECT %s FROM %s) ORDER BY %s ASC",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH,
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START);
        SQLiteDatabase db = getReadableDatabase();
        Cursor p = db.rawQuery(query, null);
        if(p.moveToFirst()){
            list = new ArrayList<>(p.getCount());
            do{
                Med med = new Med(p.getLong(0), p.getInt(1));

                // select all medical record for the specified period
                query = String.format("SELECT %s, %s FROM %s WHERE %s = %s",
                        DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                        DatabaseStructure.MedEntry.COLUMN_NAME_QUANTITY,
                        DatabaseStructure.MedEntry.TABLE_NAME,
                        DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID,
                        p.getLong(2));
                Cursor m = db.rawQuery(query, null);
                if(m.getCount() > 0){
                    m.moveToFirst();
                    do{
                        // day_of_medicine - first_day_of_period = day_in_period
                        long d = ExtendedCalendarView.getDifferenceInDays(m.getLong(0), med.getDate());
                        med.setDay((int)d, m.getInt(1));
                    }while (m.moveToNext());
                }
                m.close();

                list.add(med);

            }while (p.moveToNext());
        }
        p.close();

        return list;
    }

    public int getMaxPeriodLength(){
        int l = 0;
        // Select all period with medical records
        String query = String.format("SELECT MAX(%s) FROM %s WHERE %s IN(SELECT %s FROM %s)",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry._ID,
                DatabaseStructure.MedEntry.COLUMN_NAME_PERIOD_ID,
                DatabaseStructure.MedEntry.TABLE_NAME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor p = db.rawQuery(query, null);
        if(p.moveToFirst()){
            l = p.getInt(0);
        }
        p.close();
        return l;
    }

    public void resetDB(){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseStructure.PeriodEntry.TABLE_NAME, null, null);
            db.delete(DatabaseStructure.MedEntry.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete tables");
            }
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Search the last period in time
     * @return the starting day of the last period in UTC format
     */
    public long getLastPeriod(){
        long last = 0;
        String query = String.format("SELECT MAX(%s) FROM %s",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_START,
                DatabaseStructure.PeriodEntry.TABLE_NAME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            last = c.getLong(0);
        }
        c.close();
        return last;
    }

    /* Getters and setters for the two lengths. They are used at the beginning (to init the db if is empty)
     * and to derive expected periods based on the preference of the user (if she wants to use fixed value,
     * it's not used the avg. The avg is shown anyway in the statistics)
     */
    public void setPeriodLength(int defaultLength, boolean useDefaultValue){
        if(useDefaultValue)
            periodLength = defaultLength;
        else{
            int l = getPeriodLengthAvg();
            if(l != 0)
                periodLength = l;
            else
                periodLength = defaultLength;
        }
    }

    public void setCycleLength(int defaultLength, boolean useDefaultValue){
        if(useDefaultValue)
            cycleLength = defaultLength;
        else {
            int l = getCycleLengthAvg();
            if(l != 0)
                cycleLength = l;
            else
                cycleLength = defaultLength;
        }
    }

    public int getPeriodLength(){return periodLength;}

    public int getCycleLength(){return cycleLength;}

    /** Calculate the avg basing on the db values
     * @return the avg or 0 if the db is empty or an error occur */
    public int getPeriodLengthAvg(){
        int retval = 0;
        String query = String.format("SELECT AVG(%s) FROM %s",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH,
                DatabaseStructure.PeriodEntry.TABLE_NAME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            int l = c.getInt(0);
            if (l > 0)
                retval = l;
        }
        c.close();
        return retval;
    }

    /** Calculate the avg basing on the db values
     * @return the avg or 0 if the db is empty or an error occur */
    public int getCycleLengthAvg(){
        int retval = 0;
        String query = String.format("SELECT AVG(%s) FROM %s WHERE %s > 0",
                DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH,
                DatabaseStructure.PeriodEntry.TABLE_NAME,
                DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            int l = c.getInt(0);
            if (l > 0)
                retval = l;
        }
        c.close();
        return retval;
    }

    public void deleteMed(Day day) {
        SQLiteDatabase db = getWritableDatabase();
        long id = -1;
        String query = String.format(
                "SELECT %s FROM %s WHERE %s = %s",
                DatabaseStructure.MedEntry._ID,
                DatabaseStructure.MedEntry.TABLE_NAME,
                DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC,
                String.valueOf(day.getDayUTC()));

        Cursor c = db.rawQuery(query, null);

        if(c.moveToFirst()) {
            id = c.getLong(0);
        }
        c.close();

        db.beginTransaction();
        try {
            if( id != -1 ) {
                db.delete(DatabaseStructure.MedEntry.TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete period from database");
            }
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Delete all entry from Period and Med older than the limit.
     * @param limit
     */
    public void deleteHistory(long limit) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try{
            int p = db.delete(DatabaseStructure.PeriodEntry.TABLE_NAME, DatabaseStructure.PeriodEntry.COLUMN_NAME_START + " <= ?",
                    new String[]{String.valueOf(limit)});
            int m = db.delete(DatabaseStructure.MedEntry.TABLE_NAME, DatabaseStructure.MedEntry.COLUMN_NAME_DAY_UTC + " <= ?",
                    new String[]{String.valueOf(limit)});
            db.setTransactionSuccessful();
        }catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(TAG, "Error while trying to delete old entries from database");
            }
        } finally {
            db.endTransaction();
        }
    }
}
