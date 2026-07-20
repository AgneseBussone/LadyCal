package com.beacat.calendar.ladycal.oldjava;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.oldjava.DayJava;
import com.tyczj.extendedcalendarview.oldjava.ExtendedCalendarViewJava;
import com.tyczj.extendedcalendarview.oldjava.PeriodJava;
import com.tyczj.extendedcalendarview.oldjava.PeriodDatabaseJava;

import java.util.Calendar;

/**
 * Task to be used when a new period has to be inserted into the db.
 * It takes as input two Days objects (start and end).
 */

public class AddPeriodTaskJava extends AsyncTask<DayJava, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db
    private ExtendedCalendarViewJava calendarView; //needed to refresh the view after the insertion of new period from the main view

    public AddPeriodTaskJava(Context context, ExtendedCalendarViewJava calendarView) {
        super();
        this.context = context;
        this.calendarView = calendarView;
    }

    @Override
    protected Void doInBackground(DayJava... params) {
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(params[0].getYear(), params[0].getMonth(), params[0].getDay());
        long start = cal.getTimeInMillis();
        cal.set(params[1].getYear(), params[1].getMonth(), params[1].getDay());
        long end = cal.getTimeInMillis();

        if(start <= end) {

            PeriodDatabaseJava db = PeriodDatabaseJava.getInstance(context);

            db.addPeriod(new PeriodJava(start, end));
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v){
        if(calendarView != null) {
            // update the view
            calendarView.refreshCalendar();
        }
    }
}
