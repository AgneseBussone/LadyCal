package com.tyczj.extendedcalendarview

import android.provider.BaseColumns

/**
 * Contract class that defines the structure of the database.
 * By implementing the BaseColumns interface, the inner classes can inherit a primary key field called _ID
 * that some Android classes such as cursor adaptors will expect it to have.
 */

object DatabaseStructure {

    const val DATABASE_NAME = "PeriodDatabase.db"
    const val DATABASE_VERSION = 1

    /**
     *  Inner objects that define the tables contents
     *  */
    object PeriodEntry : BaseColumns {
        const val _ID = BaseColumns._ID
        const val TABLE_NAME = "Period"

        // Store the days in UTC timestamp
        const val COLUMN_NAME_START = "startDay"  // long
        const val COLUMN_NAME_END = "endDay"      // long

        // It's useful save these value to calculate avg easily
        const val COLUMN_NAME_PERIOD_LENGTH = "periodLength" // long
        const val COLUMN_NAME_CYCLE_LENGTH = "cycleLength"   // long
    }

    object MedEntry : BaseColumns {
        const val _ID = BaseColumns._ID
        const val TABLE_NAME = "Med"
        const val COLUMN_NAME_PERIOD_ID = "period_id" // long
        const val COLUMN_NAME_QUANTITY = "quantity"   // int
        const val COLUMN_NAME_DAY_UTC = "day"         // long
    }
}
