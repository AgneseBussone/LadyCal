package com.beacat.calendar.ladycal;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.PeriodDatabase;

/**
 * Task that adds a med record.
 * Takes as input the day; the med field must be filled by the caller.
 */

public class AddMedTask extends AsyncTask<Day, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db
    private ExtendedCalendarView calendarView; //needed to refresh the view after the insertion of new period from the main view

    public AddMedTask(Context context, ExtendedCalendarView calendarView) {
        super();
        this.context = context;
        this.calendarView = calendarView;
    }

    @Override
    protected Void doInBackground(Day... params) {

        PeriodDatabase db = PeriodDatabase.getInstance(context);

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
