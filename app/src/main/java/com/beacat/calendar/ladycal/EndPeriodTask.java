package com.beacat.calendar.ladycal;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

/**
 * Task to be used to end a period in a day that is marked as period
 * It takes the ending day as a parameter.
 */

public class EndPeriodTask extends AsyncTask<Day, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db
    private ExtendedCalendarView calendarView; //needed to refresh the view after the insertion of new period from the main view

    public EndPeriodTask(Context context, ExtendedCalendarView calendarView) {
        super();
        this.context = context;
        this.calendarView = calendarView;
    }

    @Override
    protected Void doInBackground(Day... params) {
        PeriodDatabase db = PeriodDatabase.getInstance(context);
        Period period = db.getPeriod(params[0].getPeriodId());

        if(period != null){
            // the cycle length will be update if necessary
            db.updatePeriod(period,
                    new Period(period.getStartDay(), params[0].getDayUTC(),
                    ExtendedCalendarView.getDifferenceInDays(params[0].getDayUTC(), period.getStartDay()) + 1,
                    period.getCycleLength()));
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
