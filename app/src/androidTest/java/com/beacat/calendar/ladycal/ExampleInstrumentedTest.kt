package com.beacat.calendar.ladycal

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.Calendar

import org.junit.Assert.assertEquals

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var appContext: Context
    private lateinit var db: PeriodDatabase

    @Before
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = PeriodDatabase.getInstance(appContext)

        // set lengths because in the mainActivity is done during the onCreate
        // if you don't do that, the add will insert wrong values
        db.setCycleLength(28, true)
        db.setPeriodLength(6, true)

        assertEquals("com.beacat.calendar.ladycal", appContext.packageName)
    }

    /* Probably not the best use for tests, but I need to insert a bunch of data
    *  without do it by hand
    */
    @Test
    @Throws(Exception::class)
    fun createDb() {
        val cal = Calendar.getInstance()
        cal.clear()

        cal.set(2016, Calendar.SEPTEMBER, 2)
        var start = cal.timeInMillis
        cal.set(2016, Calendar.SEPTEMBER, 5)
        var end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        cal.set(2016, Calendar.OCTOBER, 10)
        start = cal.timeInMillis
        cal.set(2016, Calendar.OCTOBER, 14)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        cal.set(2016, Calendar.NOVEMBER, 12)
        start = cal.timeInMillis
        cal.set(2016, Calendar.NOVEMBER, 20)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        cal.set(2016, Calendar.DECEMBER, 25)
        start = cal.timeInMillis
        cal.set(2016, Calendar.DECEMBER, 30)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        cal.set(2017, Calendar.JANUARY, 9)
        start = cal.timeInMillis
        cal.set(2017, Calendar.JANUARY, 15)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        cal.set(2017, Calendar.JANUARY, 29)
        start = cal.timeInMillis
        cal.set(2017, Calendar.FEBRUARY, 4)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        cal.set(2017, Calendar.APRIL, 16)
        start = cal.timeInMillis
        cal.set(2017, Calendar.APRIL, 20)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))


        val list = db.getAllPeriods("DESC")
        assertEquals(list!!.size.toLong(), 7)
    }
}
