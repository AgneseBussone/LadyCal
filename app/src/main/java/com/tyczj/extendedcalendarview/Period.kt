package com.tyczj.extendedcalendarview

import android.content.ContentValues

/**
 * Class that represent a period.
 */

class Period(val startDay: Long, val endDay: Long, val periodLength: Long, var cycleLength: Long) {

    /* Assumes start day and end day in UTC */
    constructor(startDay: Long, endDay: Long) : this(
        startDay,
        endDay,
        ExtendedCalendarView.getDifferenceInDays(endDay, startDay) + 1,
        -1
    )

    internal fun toDbEntry(): ContentValues {
        val values = ContentValues()
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_START, startDay)
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_END, endDay)
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH, periodLength)
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH, cycleLength)
        return values
    }
}
