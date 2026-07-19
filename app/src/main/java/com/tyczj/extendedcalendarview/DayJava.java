package com.tyczj.extendedcalendarview;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

import java.util.Calendar;

public class DayJava{

    private int day;
    private int year;
    private int month;
	private Context context;
	private BaseAdapter adapter;
    private long period_id;
    private int meds;
    private boolean is_expected;

	public DayJava(Context context, int year, int month, int day){
        this.day = day;
        this.month = month;
        this.year = year;
		this.context = context;
	    period_id = -1;
        meds = 0;
        is_expected = false;
    }

    public boolean isPeriod(){return (period_id != -1); }

    public void setIsExpected(boolean isExpected){is_expected = isExpected;}

    public boolean isExpected(){return is_expected;}

    public void setMeds(int quantity){ meds = quantity; }

    public int getMeds(){
        return meds;
    }

    public int getMonth(){
        return month;
    }

    public int getYear(){
        return year;
    }

    public int getDay(){
        return day;
    }

    public long getPeriodId(){ return period_id; }

    ContentValues getMedicineDbEntry(){
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureJava.MedEntry.COLUMN_NAME_DAY_UTC, getDayUTC());
        values.put(DatabaseStructureJava.MedEntry.COLUMN_NAME_PERIOD_ID, period_id);
        values.put(DatabaseStructureJava.MedEntry.COLUMN_NAME_QUANTITY, meds);
        return values;
    }

    public long getDayUTC(){
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }

    public void loadDay(){
        new GetPeriod().execute(this);
    }

	public void setAdapter(BaseAdapter adapter){
		this.adapter = adapter;
	}
	
	private class GetPeriod extends AsyncTask<DayJava,Void,Void>{

		@Override
		protected Void doInBackground(DayJava... params) {
            PeriodDatabaseJava db = PeriodDatabaseJava.getInstance(context);
            // Mark if it's a period day
            period_id = db.isPeriod(params[0]);

            // Look into the meds table to see if meds were taken this day
            meds = db.searchMeds(params[0].getDayUTC());

            return null;
		}
		
		protected void onPostExecute(Void par){
			adapter.notifyDataSetChanged();
		}
		
	}
	

}
