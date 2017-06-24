package com.tyczj.extendedcalendarview;

import android.provider.BaseColumns;

/**
 * Contract class that defines the structure of the database.
 * By implementing the BaseColumns interface, the inner classes can inherit a primary key field called _ID
 * that some Android classes such as cursor adaptors will expect it to have.
 */

public final class DatabaseStructure {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseStructure() {}

    public static final String DATABASE_NAME = "PeriodDatabase.db";
    public static final int DATABASE_VERSION = 1;

    /**
     *  Inner classes that define the tables contents
     *  */
    public static class PeriodEntry implements BaseColumns {
        public static final String TABLE_NAME = "Period";

        // Store the days in UTC timestamp
        public static final String COLUMN_NAME_START = "startDay";  // long
        public static final String COLUMN_NAME_END = "endDay";      //long

        // It's useful save these value to calculate avg easily
        public static final String COLUMN_NAME_PERIOD_LENGTH = "periodLength"; // long
        public static final String COLUMN_NAME_CYCLE_LENGTH = "cycleLength";   // long
    }

    public static class MedEntry implements BaseColumns{
        public static final String TABLE_NAME = "Med";
        public static final String COLUMN_NAME_PERIOD_ID = "period_id"; // long
        public static final String COLUMN_NAME_QUANTITY = "quantity";   // int
        public static final String COLUMN_NAME_DAY_UTC = "day";         // long
    }
}
