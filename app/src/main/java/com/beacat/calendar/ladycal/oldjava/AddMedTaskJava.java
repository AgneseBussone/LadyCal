package com.beacat.calendar.ladycal.oldjava;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.oldjava.DayJava;
import com.tyczj.extendedcalendarview.oldjava.ExtendedCalendarViewJava;
import com.tyczj.extendedcalendarview.oldjava.PeriodDatabaseJava;

/**
 * Task that adds a med record.
 * Takes as input the day; the med field must be filled by the caller.
 */

public class AddMedTaskJava extends AsyncTask<DayJava, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db
    private ExtendedCalendarViewJava calendarView; //needed to refresh the view after the insertion of new period from the main view

    public AddMedTaskJava(Context context, ExtendedCalendarViewJava calendarView) {
        super();
        this.context = context;
        this.calendarView = calendarView;
    }

    @Override
    protected Void doInBackground(DayJava... params) {

        PeriodDatabaseJava db = PeriodDatabaseJava.getInstance(context);

        if(params[0].getMeds() > 0 ) {
            db.addMed(params[0]);
        }
        else{
            db.deleteMed(params[0]);
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
