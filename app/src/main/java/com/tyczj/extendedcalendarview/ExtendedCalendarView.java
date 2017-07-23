package com.tyczj.extendedcalendarview;

import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CalendarView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beacat.calendar.ladycal.R;

import java.util.Calendar;
import java.util.Locale;

/** Class that contains the calendar view:
*   - 2 ImageView for the scrolling og the months (prev and next)
*   - 1 TextView for the name of the current month
*   - 1 GridView that contains the labels for the week days and the numbers
*/

public class ExtendedCalendarView extends RelativeLayout implements OnItemClickListener,
	OnClickListener{

	private Context context;
	private OnDayClickListener dayListener;
	private GridView calendarGV;
	private CalendarAdapter mAdapter;
	private Calendar calendar;
	private TextView monthTV;
	private RelativeLayout base;
	private ImageView next,prev;
	private int gestureType = NO_GESTURE;
	private final GestureDetector calendarGesture = new GestureDetector(context,new GestureListener());
    Vibrator vibe;

    private int prevMonthId;
    private int nextMonthId;
	
	public static final int NO_GESTURE = 0;
	public static final int LEFT_RIGHT_GESTURE = 1;
	public static final int UP_DOWN_GESTURE = 2;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    public interface OnDayClickListener{
		void onDayClicked(Day day);
	}

	public ExtendedCalendarView(Context context) {
		super(context);
		this.context = context;
		init();
	}
	
	public ExtendedCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}
	
	public ExtendedCalendarView(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

    @SuppressWarnings("deprecation")
	private void init(){
        if(!this.isInEditMode()) {
            calendar = Calendar.getInstance();
            vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) ;

            base = new RelativeLayout(context);
            base.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            base.setMinimumHeight(50);

            base.setId(View.generateViewId());

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = 16;
            params.topMargin = 50;
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            prev = new ImageView(context);
            prevMonthId = View.generateViewId();
            prev.setId(prevMonthId);
            prev.setLayoutParams(params);
            prev.setImageResource(R.drawable.navigation_previous_item);
            prev.setOnClickListener(this);
            base.addView(prev);

            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            monthTV = new TextView(context);
            monthTV.setId(View.generateViewId());
            monthTV.setLayoutParams(params);
            monthTV.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large);
            monthTV.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + calendar.get(Calendar.YEAR));
            monthTV.setTextSize(25);
            monthTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark_baseTheme));//TODO: change

            base.addView(monthTV);

            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.rightMargin = 16;
            params.topMargin = 50;
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            next = new ImageView(context);
            next.setImageResource(R.drawable.navigation_next_item);
            next.setLayoutParams(params);
            nextMonthId = View.generateViewId();
            next.setId(nextMonthId);
            next.setOnClickListener(this);
            base.addView(next);

            addView(base);

            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 20;
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.BELOW, base.getId());

            calendarGV = new GridView(context);
            calendarGV.setLayoutParams(params);
            calendarGV.setVerticalSpacing(4);
            calendarGV.setHorizontalSpacing(4);
            calendarGV.setNumColumns(7);
            calendarGV.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
            calendarGV.setDrawSelectorOnTop(true);

           mAdapter = new CalendarAdapter(context, calendar);
            calendarGV.setAdapter(mAdapter);
            calendarGV.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return calendarGesture.onTouchEvent(event);
                }
            });

            addView(calendarGV);
        }
        else{
            CalendarView placeholder = new CalendarView(context);
            placeholder.setMinimumHeight(50);
            addView(placeholder);
        }
	}

	private class GestureListener extends SimpleOnGestureListener {
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
	    	
	    	if(gestureType == LEFT_RIGHT_GESTURE){
	    		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		            nextMonth();
		            return true; // Right to left
		        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		            previousMonth();
                    return true; // Left to right
		        }
	    	}else if(gestureType == UP_DOWN_GESTURE){
	        	if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		        	nextMonth();
		            return true; // Bottom to top
		        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		        	previousMonth();
		            return true; // Top to bottom
		        }
	        }
	        return false;
	    }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(dayListener != null){
			Day d = (Day) mAdapter.getItem(position);
            // if it's a label, d == null. It'll reset the selectedDay in MainActivity
            dayListener.onDayClicked(d);
		}
	}
	
	/**
	 * 
	 * @param listener
	 * 
	 * Set a listener for when you press on a day in the month
	 */
	public void setOnDayClickListener(OnDayClickListener listener){
		if(calendarGV != null){
			dayListener = listener;
			calendarGV.setOnItemClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
        vibe.vibrate(30);
        int id = v.getId();
        if(id == prevMonthId)
			previousMonth();
        else if(id == nextMonthId)
			nextMonth();
    }
	
	private void previousMonth(){
        //noinspection WrongConstant
        if(calendar.get(Calendar.MONTH) == calendar.getActualMinimum(Calendar.MONTH)) {
			calendar.set((calendar.get(Calendar.YEAR)-1), calendar.getActualMaximum(Calendar.MONTH),1);
		} else {
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
		}
        rebuildCalendar();
    }
	
	private void nextMonth(){
        //noinspection WrongConstant
        if(calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)) {
			calendar.set((calendar.get(Calendar.YEAR)+1), calendar.getActualMinimum(Calendar.MONTH),1);
		} else {
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
		}
        rebuildCalendar();
    }
	
	private void rebuildCalendar(){
		if(monthTV != null){
			monthTV.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + calendar.get(Calendar.YEAR));
			refreshCalendar();
		}
	}
	
	/**
	 * Refreshes the calendar data
	 */
	public void refreshCalendar(){
        calendarGV.clearChoices();  // clear the selection, if any
        if(dayListener != null){
            dayListener.onDayClicked(null); // reset the selected day in MainActivity
        }
		mAdapter.refreshDays();
		mAdapter.notifyDataSetChanged();
	}

    public void resetDate(){
        Calendar current = Calendar.getInstance();
        calendar.set(Calendar.MONTH, current.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, current.get(Calendar.YEAR));
        rebuildCalendar();
    }

    public void gotoDate(long date){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));

        rebuildCalendar();
    }

    public void setFirstDayOfWeek(int day){
        calendar.setFirstDayOfWeek(day);
        mAdapter.setFirstDayWeek(day);
        refreshCalendar();
    }

    public Day getToday(){ return mAdapter.getToday(); }

    /**
     * Utility method to calculate the difference in days between two days in UTC format.
     * Day1 - day2
     * @param day1
     * @param day2
     * @return
     */
    public static long getDifferenceInDays(long day1, long day2){
        long diff = day1 - day2;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return (hours / 24); //days
    }

    /**
	 * 
	 * @param gestureType
	 * 
	 * Allow swiping the calendarGV left/right or up/down to change the monthTV.
	 * 
	 * Default value no gesture
	 */
	public void setGesture(int gestureType){
		this.gestureType = gestureType;
	}

}
