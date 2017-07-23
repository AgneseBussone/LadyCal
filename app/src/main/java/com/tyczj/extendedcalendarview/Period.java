package com.tyczj.extendedcalendarview;

import android.content.ContentValues;


/**
 * Class that represent a period.
 */

public class Period {

    // UTC timestamp
    private long startDay;
    private long endDay;

    private long periodLength;
    private long cycleLength;

    /* Assumes start day and end day in UTC */
    public Period(long startDay, long endDay){
        this.startDay = startDay;
        this.endDay = endDay;
        periodLength = ExtendedCalendarView.getDifferenceInDays(endDay, startDay) + 1;
        cycleLength = -1;
    }

    /* In case all values are known */
    public Period(long startDay, long endDay, long periodLength, long cycleLength) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.periodLength = periodLength;
        this.cycleLength = cycleLength;
    }

        ContentValues toDbEntry(){
        ContentValues values = new ContentValues();
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_START, startDay);
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_END, endDay);
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_PERIOD_LENGTH, periodLength);
        values.put(DatabaseStructure.PeriodEntry.COLUMN_NAME_CYCLE_LENGTH, cycleLength);
        return values;
    }

    public long getStartDay(){  return startDay; }

    public long getEndDay(){ return endDay; }

    public long getPeriodLength(){return periodLength;}

    public long getCycleLength(){return cycleLength;}

    public void setCycleLength(long length){ this.cycleLength = length; }
}
