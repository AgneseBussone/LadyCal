package com.beacat.calendar.ladycal;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

/**
 * Task to be used to update a period from the db.
 * It takes two Period objects as input: the first that contains the old values and the second with the new values
 */

public class EditPeriodTask extends AsyncTask<Period, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db

    public EditPeriodTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected Void doInBackground(Period... params) {
        PeriodDatabase.getInstance(context).updatePeriod(params[0], params[1]);
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
    }
}
