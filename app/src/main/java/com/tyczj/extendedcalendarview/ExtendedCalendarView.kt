package com.tyczj.extendedcalendarview

import android.content.Context
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.CalendarView
import android.widget.GridView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.beacat.calendar.ladycal.R
import com.beacat.calendar.ladycal.Utilities

import java.util.Calendar
import java.util.Locale

/** Class that contains the calendar view:
 *   - 2 ImageView for the scrolling og the months (prev and next)
 *   - 1 TextView for the name of the current month
 *   - 1 GridView that contains the labels for the week days and the numbers
 */

class ExtendedCalendarView : RelativeLayout, OnItemClickListener, View.OnClickListener {

    private var dayListener: OnDayClickListener? = null
    private var calendarGV: GridView? = null
    private var mAdapter: CalendarAdapter? = null
    private var calendar: Calendar? = null
    private var monthTV: TextView? = null
    private var base: RelativeLayout? = null
    private var next: ImageView? = null
    private var prev: ImageView? = null
    private var gestureType = NO_GESTURE
    // the original Java field initializer ran before the constructor body assigned the
    // context field, so the GestureDetector was built with a null Context; keep that
    private val calendarGesture = GestureDetector(null as Context?, GestureListener())
    internal var vibe: Vibrator? = null

    private var prevMonthId = 0
    private var nextMonthId = 0

    companion object {
        const val NO_GESTURE = 0
        const val LEFT_RIGHT_GESTURE = 1
        const val UP_DOWN_GESTURE = 2
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200

        /**
         * Utility method to calculate the difference in days between two days in UTC format.
         * Day1 - day2
         * @param day1
         * @param day2
         * @return
         */
        @JvmStatic
        fun getDifferenceInDays(day1: Long, day2: Long): Long {
            val diff = day1 - day2
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            return hours / 24 // days
        }
    }

    interface OnDayClickListener {
        fun onDayClicked(day: Day?)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    @Suppress("DEPRECATION")
    private fun init() {
        if (!this.isInEditMode) {
            calendar = Calendar.getInstance()
            vibe = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            base = RelativeLayout(context)
            base!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            base!!.minimumHeight = 50

            base!!.id = View.generateViewId()

            var params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.leftMargin = 16
            params.topMargin = 50
            params.addRule(ALIGN_PARENT_LEFT)
            params.addRule(CENTER_VERTICAL)
            prev = ImageView(context)
            prevMonthId = View.generateViewId()
            prev!!.id = prevMonthId
            prev!!.layoutParams = params
            prev!!.setImageResource(R.drawable.navigation_previous_item)
            if (Utilities.isNightModeOn(context)) {
                prev!!.setColorFilter(context.resources.getColor(R.color.white))
            }
            prev!!.setOnClickListener(this)
            base!!.addView(prev)

            params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.addRule(CENTER_HORIZONTAL)
            params.addRule(CENTER_VERTICAL)
            monthTV = TextView(context)
            monthTV!!.id = View.generateViewId()
            monthTV!!.layoutParams = params
            monthTV!!.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large)
            monthTV!!.text = calendar!!.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + calendar!!.get(Calendar.YEAR)
            monthTV!!.textSize = 25f
            monthTV!!.setTextColor(Utilities.getMonthColor(context))

            base!!.addView(monthTV)

            params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.rightMargin = 16
            params.topMargin = 50
            params.addRule(ALIGN_PARENT_RIGHT)
            params.addRule(CENTER_VERTICAL)
            next = ImageView(context)
            next!!.setImageResource(R.drawable.navigation_next_item)
            if (Utilities.isNightModeOn(context)) {
                next!!.setColorFilter(context.resources.getColor(R.color.white))
            }
            next!!.layoutParams = params
            nextMonthId = View.generateViewId()
            next!!.id = nextMonthId
            next!!.setOnClickListener(this)
            base!!.addView(next)

            addView(base)

            params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.bottomMargin = 20
            params.addRule(ALIGN_PARENT_LEFT)
            params.addRule(ALIGN_PARENT_BOTTOM)
            params.addRule(BELOW, base!!.id)

            calendarGV = GridView(context)
            calendarGV!!.layoutParams = params
            calendarGV!!.verticalSpacing = 4
            calendarGV!!.horizontalSpacing = 4
            calendarGV!!.numColumns = 7
            calendarGV!!.choiceMode = GridView.CHOICE_MODE_SINGLE
            calendarGV!!.setDrawSelectorOnTop(true)

            mAdapter = CalendarAdapter(context, calendar!!)
            calendarGV!!.adapter = mAdapter
            calendarGV!!.setOnTouchListener { _, event -> calendarGesture.onTouchEvent(event) }

            addView(calendarGV)
        } else {
            val placeholder = CalendarView(context)
            placeholder.minimumHeight = 50
            addView(placeholder)
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            if (gestureType == LEFT_RIGHT_GESTURE) {
                if (e1?.x?.minus(e2.x)!! > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    nextMonth()
                    return true // Right to left
                } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    previousMonth()
                    return true // Left to right
                }
            } else if (gestureType == UP_DOWN_GESTURE) {
                if (e1?.y?.minus(e2.y)!! > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    nextMonth()
                    return true // Bottom to top
                } else if (e2.y - e1.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    previousMonth()
                    return true // Top to bottom
                }
            }
            return false
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (dayListener != null) {
            val d = mAdapter!!.getItem(position) as Day?
            // if it's a label, d == null. It'll reset the selectedDay in MainActivity
            dayListener!!.onDayClicked(d)
        }
    }

    /**
     *
     * @param listener
     *
     * Set a listener for when you press on a day in the month
     */
    fun setOnDayClickListener(listener: OnDayClickListener) {
        if (calendarGV != null) {
            dayListener = listener
            calendarGV!!.onItemClickListener = this
        }
    }

    override fun onClick(v: View) {
        vibe!!.vibrate(30)
        val id = v.id
        if (id == prevMonthId)
            previousMonth()
        else if (id == nextMonthId)
            nextMonth()
    }

    private fun previousMonth() {
        //noinspection WrongConstant
        if (calendar!!.get(Calendar.MONTH) == calendar!!.getActualMinimum(Calendar.MONTH)) {
            calendar!!.set((calendar!!.get(Calendar.YEAR) - 1), calendar!!.getActualMaximum(Calendar.MONTH), 1)
        } else {
            calendar!!.set(Calendar.MONTH, calendar!!.get(Calendar.MONTH) - 1)
        }
        rebuildCalendar()
    }

    private fun nextMonth() {
        //noinspection WrongConstant
        if (calendar!!.get(Calendar.MONTH) == calendar!!.getActualMaximum(Calendar.MONTH)) {
            calendar!!.set((calendar!!.get(Calendar.YEAR) + 1), calendar!!.getActualMinimum(Calendar.MONTH), 1)
        } else {
            calendar!!.set(Calendar.MONTH, calendar!!.get(Calendar.MONTH) + 1)
        }
        rebuildCalendar()
    }

    private fun rebuildCalendar() {
        if (monthTV != null) {
            monthTV!!.text = calendar!!.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + calendar!!.get(Calendar.YEAR)
            refreshCalendar()
        }
    }

    /**
     * Refreshes the calendar data
     */
    fun refreshCalendar() {
        calendarGV!!.clearChoices()  // clear the selection, if any
        if (dayListener != null) {
            dayListener!!.onDayClicked(null) // reset the selected day in MainActivity
        }
        mAdapter!!.refreshDays()
        mAdapter!!.notifyDataSetChanged()
    }

    fun resetDate() {
        val current = Calendar.getInstance()
        calendar!!.set(Calendar.MONTH, current.get(Calendar.MONTH))
        calendar!!.set(Calendar.YEAR, current.get(Calendar.YEAR))
        rebuildCalendar()
    }

    fun gotoDate(date: Long) {
        val c = Calendar.getInstance()
        c.timeInMillis = date
        calendar!!.set(Calendar.MONTH, c.get(Calendar.MONTH))
        calendar!!.set(Calendar.YEAR, c.get(Calendar.YEAR))

        rebuildCalendar()
    }

    fun setFirstDayOfWeek(day: Int) {
        calendar!!.firstDayOfWeek = day
        mAdapter!!.setFirstDayWeek(day)
        refreshCalendar()
    }

    fun getToday(): Day? {
        return mAdapter!!.getToday()
    }

    /**
     *
     * @param gestureType
     *
     * Allow swiping the calendarGV left/right or up/down to change the monthTV.
     *
     * Default value no gesture
     */
    fun setGesture(gestureType: Int) {
        this.gestureType = gestureType
    }
}
