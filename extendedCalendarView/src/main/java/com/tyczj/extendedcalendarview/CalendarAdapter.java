package com.tyczj.extendedcalendarview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

@SuppressWarnings("WrongConstant")
class CalendarAdapter extends BaseAdapter{

    // private final String TAG = this.getClass().getSimpleName();

    // Number of cells needed to display all possible combination for a month
    private final int NUM_CELLS = 42;

    // Number of cells reserved to labels for weekday names
    private final int NUM_LABELS = 7;

    // Variable used to create the grid of the days
	private int firstDayOfWeek = 1;  // sunday

    private Day today = null;

    private Context context;
    private ArrayList<Day> dayList = new ArrayList<>(NUM_CELLS);
    private Calendar cal; //Calendar object coming from ExtendedCalendarView

	CalendarAdapter(Context context, Calendar cal){
		this.cal = cal;
		this.context = context;
        this.cal.set(Calendar.DAY_OF_MONTH, 1);
		refreshDays();
	}

	@Override
	public int getCount() {
        return NUM_CELLS + NUM_LABELS;
	}

	@Override
	public Object getItem(int position) {
		if(position < NUM_LABELS)
            return null; // label
        if((position - NUM_LABELS) < dayList.size())
            return dayList.get(position - NUM_LABELS);
        else
            return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

    void setFirstDayWeek(int day){
        cal.setFirstDayOfWeek(day);
        firstDayOfWeek = day;
    }


    /* Called for every element in the calendar gridView (label + days) */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(position < NUM_LABELS){

			/* Create the week label */

			v = vi.inflate(R.layout.day_label, null);
			TextView day = (TextView)v.findViewById(R.id.textView1);
			if(cal.getFirstDayOfWeek() == Calendar.MONDAY) {
                if (position == 0) {
                    day.setText(R.string.monday);
                } else if (position == 1) {
                    day.setText(R.string.tuesday);
                } else if (position == 2) {
                    day.setText(R.string.wednesday);
                } else if (position == 3) {
                    day.setText(R.string.thursday);
                } else if (position == 4) {
                    day.setText(R.string.friday);
                } else if (position == 5) {
                    day.setText(R.string.saturday);
                } else if (position == 6) {
                    day.setText(R.string.sunday);
                }
            }
            else{
                if (position == 0) {
                    day.setText(R.string.sunday);
                } else if (position == 1) {
                    day.setText(R.string.monday);
                } else if (position == 2) {
                    day.setText(R.string.tuesday);
                } else if (position == 3) {
                    day.setText(R.string.wednesday);
                } else if (position == 4) {
                    day.setText(R.string.thursday);
                } else if (position == 5) {
                    day.setText(R.string.friday);
                } else if (position == 6) {
                    day.setText(R.string.saturday);
                }
            }
		}else{

			/* Create the day */

            int day_index = position - NUM_LABELS;

            if(day_index < dayList.size()) {

                v = vi.inflate(R.layout.day_view, null);
                ImageView day_frame = (ImageView) v.findViewById(R.id.day_frame);
                Calendar current_calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

                Day day = dayList.get(day_index);

                boolean isToday = (day.getYear() == current_calendar.get(Calendar.YEAR)) &&
                        (day.getMonth() == current_calendar.get(Calendar.MONTH)) &&
                        (day.getDay() == current_calendar.get(Calendar.DAY_OF_MONTH));

                // textView with the number
                TextView dayTV = (TextView) v.findViewById(R.id.textView1);
                RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.rl);
                ImageView med = (ImageView) v.findViewById(R.id.med_image);

                if (day.getDay() == 0) {
                    rl.setVisibility(View.GONE);
                } else {
                    dayTV.setVisibility(View.VISIBLE);
                    dayTV.setText(String.valueOf(day.getDay()));

                    if (day.getMeds() > 0) {
                        med.setVisibility(View.VISIBLE);
                    }

			    /* Set today background */
                    if (isToday) {
                        today = day;
                        if (today.isPeriod()) {
                            day_frame.setBackgroundResource(R.drawable.today_period);
                        } else {
                            day_frame.setBackgroundResource(R.drawable.today);
                        }
                    } else {
                        // Change the background resource and make it visible
                        if (day.isPeriod()) {
                            // day before today = mark as period
                            if (today == null) {
                                // for sure is a day before today
                                day_frame.setBackgroundResource(R.drawable.period);
                                dayTV.setTextColor(Color.WHITE);
                            } else {
                                if (day.getDayUTC() < today.getDayUTC()) {
                                    day_frame.setBackgroundResource(R.drawable.period);
                                    dayTV.setTextColor(Color.WHITE);
                                } else {
                                    // day after today = mark as expected
                                    day_frame.setBackgroundResource(R.drawable.expected);
                                }
                            }
                        } else if (day.isExpected()) {
                            day_frame.setBackgroundResource(R.drawable.expected);
                        }
                    }
                }
            }
		}
		
		return v;
	}

    /**
     * Creates the calendar view and fill the dayList with the information from the db
     */
    void refreshDays()
    {
        // clear items
    	dayList.clear();

        int firstDay = cal.get(Calendar.DAY_OF_WEEK);
    	int numDayInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        // populate empty cells before the first day of the month
        if(firstDay == Calendar.SUNDAY && firstDayOfWeek == Calendar.MONDAY){
            // special case
            for (int i = 0; i < 6; i++) {
                Day d = new Day(context, 0, 0, 0);
                dayList.add(d);
            }
        }
        else {
            for (int i = 0; i < (firstDay - firstDayOfWeek); i++) {
                Day d = new Day(context, 0, 0, 0);
                dayList.add(d);
            }
        }
        // populate days of the month
        for(int dayNumber = 1; dayNumber <= numDayInMonth; dayNumber++) {
        	Day d = new Day(context, year, month, dayNumber);
        	d.setAdapter(this);
        	d.loadDay(); // Read the information from the db
            dayList.add(d);
        }

        // populate empty cells after the last day
        for(int i = dayList.size(); i < NUM_CELLS; i++){
            Day d = new Day(context, 0, 0, 0);
            dayList.add(d);
        }

        // set expected period(s) in the month
        calculateExpected(month, year, numDayInMonth);
    }

    /**
     * Method to calculate the expected period if needed.
     * If the current month is after the last period, we need to calculate the expected
     */
    private void calculateExpected(int showedMonth, int showedYear, int lastDay){
        PeriodDatabase db = PeriodDatabase.getInstance(context);
        Calendar c = Calendar.getInstance();

        c.set(showedYear, showedMonth, 1);
        long firstDayOfTheMonth = c.getTimeInMillis(); // first day of the showed month in UTC
        c.set(showedYear, showedMonth, lastDay);
        long lastDayOfTheMonth = c.getTimeInMillis(); // last day of the showed month in UTC

        long lastPeriod = db.getLastPeriod();

        if(lastPeriod <= 0 ||                       // no history
                lastDayOfTheMonth < lastPeriod)     // this month is before the last period
            return;
        else{
            int cycle = db.getCycleLength();
            int period = db.getPeriodLength();
            long startExpected, endExpected;

            c.setTimeInMillis(lastPeriod);
            do {
                c.add(Calendar.DATE, cycle);
                startExpected = c.getTimeInMillis();
                c.add(Calendar.DATE, period);
                endExpected = c.getTimeInMillis();
                if ((startExpected >= firstDayOfTheMonth && startExpected <= lastDayOfTheMonth) ||      // starts this month
                        (endExpected >= firstDayOfTheMonth && endExpected <= lastDayOfTheMonth)) {      // ends this month
                    // mark these days as expected
                    for(Day d : dayList){
                        long utc = d.getDayUTC();
                        if(utc >= startExpected && utc < endExpected)
                            d.setIsExpected(true);
                    }
                    // even if we found an expected, go on and calculate the next one, in case it's in the same month
                }
                c.setTimeInMillis(startExpected);
            }while(startExpected < lastDayOfTheMonth);
        }
    }

    public Day getToday(){ return today; }
}
