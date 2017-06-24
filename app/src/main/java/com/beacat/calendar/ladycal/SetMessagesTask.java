package com.beacat.calendar.ladycal;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;

import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.PeriodDatabase;

/**
 * Task to update the messages in the main view.
 */

public class SetMessagesTask extends AsyncTask<Void, Void, String> { //params, progress, result

    private Context context; // needed to insert the new entry into the db
    private Day today;
    private ActionBar bar;

    public SetMessagesTask(Context context, ActionBar bar, Day today){
        this.context = context;
        this.bar = bar;
        this.today = today;
    }

    @Override
    protected void onPreExecute(){
        bar.setSubtitle("");
    }

    @Override
    protected String doInBackground(Void... params) {
        PeriodDatabase db = PeriodDatabase.getInstance(context);
        String mex = "";

        long start = db.getLastPeriod();
        if(start != 0) {
            long offset = ExtendedCalendarView.getDifferenceInDays(today.getDayUTC(), start);

            if(today.isPeriod()) {
                offset++;
                // N-th day of period
                mex = mex.concat(String.valueOf(offset));
                switch ((int) offset) {
                    case 1:
                        mex = mex.concat(" st ");
                        break;
                    case 2:
                        mex = mex.concat(" nd ");
                        break;
                    case 3:
                        mex = mex.concat(" rd ");
                        break;
                    default:
                        mex = mex.concat(" th ");
                }
                mex = mex.concat("day of period");
            }
            else {
                // N days until next period / late
                int cycleLength = db.getCycleLength();
                cycleLength -= (int)offset;
                if(cycleLength == 0){
                    mex = "Today should be the first day";
                }
                else if(cycleLength > 0) {
                    mex = context.getResources().getQuantityString(R.plurals.day_until_next_period, cycleLength, cycleLength);
                }
                else{
                    cycleLength = Math.abs(cycleLength);
                    mex = context.getResources().getQuantityString(R.plurals.day_late, cycleLength, cycleLength);
                }
            }


        }

        return mex;
    }

    @Override
    protected void onPostExecute(String mex){
        bar.setSubtitle(mex);
    }
}
