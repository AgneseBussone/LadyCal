package com.tyczj.extendedcalendarview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.beacat.calendar.ladycal.R

import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Suppress("WrongConstant")
internal class CalendarAdapter(private val context: Context, private val cal: Calendar) : BaseAdapter() {

    // private final String TAG = this.getClass().getSimpleName();

    // Number of cells needed to display all possible combination for a month
    private val NUM_CELLS = 42

    // Number of cells reserved to labels for weekday names
    private val NUM_LABELS = 7

    // Variable used to create the grid of the days
    private var firstDayOfWeek = 1  // sunday

    private var today: Day? = null

    private val dayList = ArrayList<Day>(NUM_CELLS)

    init {
        this.cal.set(Calendar.DAY_OF_MONTH, 1)
        refreshDays()
    }

    override fun getCount(): Int {
        return NUM_CELLS + NUM_LABELS
    }

    override fun getItem(position: Int): Any? {
        if (position < NUM_LABELS)
            return null // label
        return if ((position - NUM_LABELS) < dayList.size)
            dayList[position - NUM_LABELS]
        else
            null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun setFirstDayWeek(day: Int) {
        cal.firstDayOfWeek = day
        firstDayOfWeek = day
    }

    /* Called for every element in the calendar gridView (label + days) */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v = convertView
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (position < NUM_LABELS) {

            /* Create the week label */

            v = vi.inflate(R.layout.day_label, null)
            val day = v.findViewById<TextView>(R.id.textView1)
            if (cal.firstDayOfWeek == Calendar.MONDAY) {
                if (position == 0) {
                    day.setText(R.string.monday)
                } else if (position == 1) {
                    day.setText(R.string.tuesday)
                } else if (position == 2) {
                    day.setText(R.string.wednesday)
                } else if (position == 3) {
                    day.setText(R.string.thursday)
                } else if (position == 4) {
                    day.setText(R.string.friday)
                } else if (position == 5) {
                    day.setText(R.string.saturday)
                } else if (position == 6) {
                    day.setText(R.string.sunday)
                }
            } else {
                if (position == 0) {
                    day.setText(R.string.sunday)
                } else if (position == 1) {
                    day.setText(R.string.monday)
                } else if (position == 2) {
                    day.setText(R.string.tuesday)
                } else if (position == 3) {
                    day.setText(R.string.wednesday)
                } else if (position == 4) {
                    day.setText(R.string.thursday)
                } else if (position == 5) {
                    day.setText(R.string.friday)
                } else if (position == 6) {
                    day.setText(R.string.saturday)
                }
            }
        } else {

            /* Create the day */

            val day_index = position - NUM_LABELS

            if (day_index < dayList.size) {

                v = vi.inflate(R.layout.day_view, null)
                val day_frame = v.findViewById<ImageView>(R.id.day_frame)
                val current_calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())

                val day = dayList[day_index]

                val isToday = (day.year == current_calendar.get(Calendar.YEAR)) &&
                        (day.month == current_calendar.get(Calendar.MONTH)) &&
                        (day.day == current_calendar.get(Calendar.DAY_OF_MONTH))

                // textView with the number
                val dayTV = v.findViewById<TextView>(R.id.textView1)
                val rl = v.findViewById<RelativeLayout>(R.id.rl)
                val med = v.findViewById<ImageView>(R.id.med_image)

                if (day.day == 0) {
                    rl.visibility = View.GONE
                } else {
                    dayTV.visibility = View.VISIBLE
                    dayTV.text = day.day.toString()

                    if (day.meds > 0) {
                        med.visibility = View.VISIBLE
                    }

                    /* Set today background */
                    if (isToday) {
                        today = day
                        if (today!!.isPeriod) {
                            day_frame.setBackgroundResource(R.drawable.today_period)
                        } else {
                            day_frame.setBackgroundResource(R.drawable.today)
                        }
                    } else {
                        // Change the background resource and make it visible
                        if (day.isPeriod) {
                            // day before today = mark as period
                            if (today == null) {
                                // for sure is a day before today
                                day_frame.setBackgroundResource(R.drawable.period)
                                dayTV.setTextColor(Color.WHITE)
                            } else {
                                if (day.dayUTC < today!!.dayUTC) {
                                    day_frame.setBackgroundResource(R.drawable.period)
                                    dayTV.setTextColor(Color.WHITE)
                                } else {
                                    // day after today = mark as expected
                                    day_frame.setBackgroundResource(R.drawable.expected)
                                }
                            }
                        } else if (day.isExpected) {
                            day_frame.setBackgroundResource(R.drawable.expected)
                        }
                    }
                }
            }
        }

        return v!!
    }

    /**
     * Creates the calendar view and fill the dayList with the information from the db
     */
    fun refreshDays() {
        // clear items
        dayList.clear()

        val firstDay = cal.get(Calendar.DAY_OF_WEEK)
        val numDayInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        // populate empty cells before the first day of the month
        if (firstDay == Calendar.SUNDAY && firstDayOfWeek == Calendar.MONDAY) {
            // special case
            for (i in 0 until 6) {
                val d = Day(context, 0, 0, 0)
                dayList.add(d)
            }
        } else {
            for (i in 0 until (firstDay - firstDayOfWeek)) {
                val d = Day(context, 0, 0, 0)
                dayList.add(d)
            }
        }
        // populate days of the month
        for (dayNumber in 1..numDayInMonth) {
            val d = Day(context, year, month, dayNumber)
            d.setAdapter(this)
            d.loadDay() // Read the information from the db
            dayList.add(d)
        }

        // populate empty cells after the last day
        for (i in dayList.size until NUM_CELLS) {
            val d = Day(context, 0, 0, 0)
            dayList.add(d)
        }

        // set expected period(s) in the month
        calculateExpected(month, year, numDayInMonth)
    }

    /**
     * Method to calculate the expected period if needed.
     * If the current month is after the last period, we need to calculate the expected
     */
    private fun calculateExpected(showedMonth: Int, showedYear: Int, lastDay: Int) {
        val db = PeriodDatabase.getInstance(context)
        val c = Calendar.getInstance()

        c.set(showedYear, showedMonth, 1)
        val firstDayOfTheMonth = c.timeInMillis // first day of the showed month in UTC
        c.set(showedYear, showedMonth, lastDay)
        val lastDayOfTheMonth = c.timeInMillis // last day of the showed month in UTC

        val lastPeriod = db.getLastPeriod()

        if (lastPeriod <= 0 ||                      // no history
                lastDayOfTheMonth < lastPeriod)     // this month is before the last period
            return
        else {
            val cycle = db.cycleLength
            val period = db.periodLength
            var startExpected: Long
            var endExpected: Long

            c.timeInMillis = lastPeriod
            do {
                c.add(Calendar.DATE, cycle)
                startExpected = c.timeInMillis
                c.add(Calendar.DATE, period)
                endExpected = c.timeInMillis
                if ((startExpected in firstDayOfTheMonth..lastDayOfTheMonth) ||     // starts this month
                        (endExpected in firstDayOfTheMonth..lastDayOfTheMonth)) {   // ends this month
                    // mark these days as expected
                    for (d in dayList) {
                        val utc = d.dayUTC
                        if (utc >= startExpected && utc < endExpected)
                            d.isExpected = true
                    }
                    // even if we found an expected, go on and calculate the next one, in case it's in the same month
                }
                c.timeInMillis = startExpected
            } while (startExpected < lastDayOfTheMonth)
        }
    }

    fun getToday(): Day? {
        return today
    }
}
