package com.beacat.calendar.ladycal;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

import java.util.Calendar;

/**
 * Task to be used when a new period has to be inserted into the db.
 * It takes as input two Days objects (start and end).
 */

public class AddPeriodTask extends AsyncTask<Day, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db
    private ExtendedCalendarView calendarView; //needed to refresh the view after the insertion of new period from the main view

    public AddPeriodTask(Context context, ExtendedCalendarView calendarView) {
        super();
        this.context = context;
        this.calendarView = calendarView;
    }

    @Override
    protected Void doInBackground(Day... params) {
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(params[0].getYear(), params[0].getMonth(), params[0].getDay());
        long start = cal.getTimeInMillis();
        cal.set(params[1].getYear(), params[1].getMonth(), params[1].getDay());
        long end = cal.getTimeInMillis();

        if(start <= end) {

            PeriodDatabase db = PeriodDatabase.getInstance(context);

            db.addPeriod(new Period(start, end));
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
