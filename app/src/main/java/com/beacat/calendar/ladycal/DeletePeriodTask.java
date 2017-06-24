package com.beacat.calendar.ladycal;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

/**
 * Task to be used to delete a period from the db.
 */

public class DeletePeriodTask extends AsyncTask<Period, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db

    public DeletePeriodTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected Void doInBackground(Period... params) {
        PeriodDatabase.getInstance(context).deletePeriod(params[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
    }
}
