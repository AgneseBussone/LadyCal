package com.beacat.calendar.ladycal;

import android.content.Context;
import android.os.AsyncTask;

import com.tyczj.extendedcalendarview.PeriodDatabase;

/**
 *
 */
public class DeleteAllPeriodsTask extends AsyncTask<Void, Void, Void> { //params, progress, result

    private Context context; // needed to insert the new entry into the db

    public DeleteAllPeriodsTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        PeriodDatabase db = PeriodDatabase.getInstance(context);
        db.resetDB();
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
    }
}