package com.beacat.calendar.ladycal

import android.content.Context

import com.tyczj.extendedcalendarview.Day
import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

import java.util.Calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame

/**
 * Testing the database functionality
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = [23], manifest = "/src/main/AndroidManifest.xml")
class DatabaseTest {

    private lateinit var context: Context
    private lateinit var db: PeriodDatabase

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        db = PeriodDatabase.getInstance(context)
        db.setCycleLength(28, true)
        db.setPeriodLength(6, true)
    }

    @Test
    @Throws(Exception::class)
    fun test_addPeriod() {
        val cal = Calendar.getInstance()
        cal.clear()

        cal.set(2016, Calendar.NOVEMBER, 2)
        val start = cal.timeInMillis
        cal.set(2016, Calendar.NOVEMBER, 5)
        val end = cal.timeInMillis

        db.addPeriod(Period(start, end))
        var list = db.getAllPeriods("DESC")
        assertEquals(list!!.size.toLong(), 1)

        // Re-insert the same
        db.addPeriod(Period(start, end))
        list = db.getAllPeriods("DESC")

        assertEquals(1, list!!.size.toLong()) // expect to have only 1 entry

        val p = list[0]
        assertEquals(start, p.startDay)
        assertEquals(end, p.endDay)
        assertEquals(4, p.periodLength)
        assertEquals(28, p.cycleLength)
    }

    @Test
    @Throws(Exception::class)
    fun test_lengthValues() {
        val cal = Calendar.getInstance()
        cal.clear()

        // period #1
        cal.set(2017, Calendar.JANUARY, 2)
        var start = cal.timeInMillis
        cal.set(2017, Calendar.JANUARY, 5)
        var end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        // period #2
        cal.set(2017, Calendar.MARCH, 10)
        start = cal.timeInMillis
        cal.set(2017, Calendar.MARCH, 20)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        // period #3 - history insertion. This should update itself and january
        cal.set(2017, Calendar.FEBRUARY, 4)
        start = cal.timeInMillis
        cal.set(2017, Calendar.FEBRUARY, 5)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))


        val list = db.getAllPeriods("DESC")

        var p = list!![0] //March
        assertEquals(10, p.periodLength)
        assertEquals(28, p.cycleLength)

        p = list[1] //February
        assertEquals(2, p.periodLength)
        assertEquals(34, p.cycleLength)

        p = list[2] //January
        assertEquals(4, p.periodLength)
        assertEquals(33, p.cycleLength)
    }

    @Test
    @Throws(Exception::class)
    fun test_updatePeriod() {
        val cal = Calendar.getInstance()
        cal.clear()

        // period #1
        cal.set(2017, Calendar.JANUARY, 2)
        var start = cal.timeInMillis
        cal.set(2017, Calendar.JANUARY, 5)
        var end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        // period #2
        cal.set(2017, Calendar.FEBRUARY, 4)
        start = cal.timeInMillis
        cal.set(2017, Calendar.FEBRUARY, 5)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        // period #3
        cal.set(2017, Calendar.MARCH, 10)
        start = cal.timeInMillis
        cal.set(2017, Calendar.MARCH, 20)
        end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        var list = db.getAllPeriods("DESC")

        var p = list!![0] //march
        assertEquals(10, p.periodLength)
        assertEquals(28, p.cycleLength)

        p = list[1] //february
        assertEquals(2, p.periodLength)
        assertEquals(34, p.cycleLength)

        p = list[2] //january
        assertEquals(4, p.periodLength)
        assertEquals(33, p.cycleLength)
/*
        // update march
        cal.set(2017, Calendar.MARCH, 5);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.MARCH, 20);
        end = cal.getTimeInMillis();
        db.updatePeriod(list.get(0), new Period(start, end));

        list.clear();
        list = db.getAllPeriods();

         p = list.get(0); //march
        assertEquals(15, p.getPeriodLength());
        assertEquals(28, p.getCycleLength());

        p = list.get(1); //february
        assertEquals(2, p.getPeriodLength());
        assertEquals(29, p.getCycleLength());

        p = list.get(2); //january
        assertEquals(4, p.getPeriodLength());
        assertEquals(33, p.getCycleLength());
*/
/*
        // update january
        cal.set(2017, Calendar.JANUARY, 1);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.JANUARY, 5);
        end = cal.getTimeInMillis();
        db.updatePeriod(list.get(2), new Period(start, end));

        list.clear();
        list = db.getAllPeriods();

        p = list.get(0); //march
        assertEquals(10, p.getPeriodLength());
        assertEquals(28, p.getCycleLength());

        p = list.get(1); //february
        assertEquals(2, p.getPeriodLength());
        assertEquals(34, p.getCycleLength());

        p = list.get(2); //january
        assertEquals(5, p.getPeriodLength());
        assertEquals(34, p.getCycleLength());
*/
        // update february
        cal.set(2017, Calendar.FEBRUARY, 10)
        start = cal.timeInMillis
        cal.set(2017, Calendar.FEBRUARY, 15)
        end = cal.timeInMillis
        db.updatePeriod(list[1], Period(start, end))

        list!!.clear()
        list = db.getAllPeriods("DESC")

        p = list!![0] //march
        assertEquals(10, p.periodLength)
        assertEquals(28, p.cycleLength)

        p = list[1] //february
        assertEquals(6, p.periodLength)
        assertEquals(28, p.cycleLength)

        p = list[2] //january
        assertEquals(4, p.periodLength)
        assertEquals(39, p.cycleLength)

    }

    @Test
    @Throws(Exception::class)
    fun test_deletePeriod() {
        val cal = Calendar.getInstance()
        cal.clear()

        cal.set(2016, Calendar.NOVEMBER, 2)
        val start = cal.timeInMillis
        cal.set(2016, Calendar.NOVEMBER, 5)
        val end = cal.timeInMillis

        val p = Period(start, end)
        db.addPeriod(p)
        var list = db.getAllPeriods("DESC")
        assertEquals(1, list!!.size.toLong())

        db.deletePeriod(p)

        list = db.getAllPeriods("DESC")
        assertEquals(0, list!!.size.toLong())

    }

    @Test
    @Throws(Exception::class)
    fun test_searchPeriod() {
        val cal = Calendar.getInstance()
        cal.clear()

        cal.set(2016, Calendar.NOVEMBER, 2)
        val start = cal.timeInMillis
        cal.set(2016, Calendar.NOVEMBER, 5)
        val end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        cal.set(2016, Calendar.JANUARY, 2)
        val start1 = cal.timeInMillis
        cal.set(2016, Calendar.JANUARY, 5)
        val end1 = cal.timeInMillis
        db.addPeriod(Period(start1, end1))

        cal.set(2016, Calendar.JANUARY, 2)
        val start2 = cal.timeInMillis
        cal.set(2016, Calendar.JANUARY, 5)
        val end2 = cal.timeInMillis
        db.addPeriod(Period(start2, end2))

        val p = Period(start, end)
        assertNotSame(-1, db.searchPeriodId(p))  // expected to exist

        val p1 = Period(50, 50)
        assertEquals(-1, db.searchPeriodId(p1))  // expected to not exist
    }

    @Test
    @Throws(Exception::class)
    fun test_isPeriod() {
        val cal = Calendar.getInstance()
        cal.clear()

        cal.set(2016, Calendar.NOVEMBER, 2)
        val start = cal.timeInMillis
        cal.set(2016, Calendar.NOVEMBER, 10)
        val end = cal.timeInMillis
        db.addPeriod(Period(start, end))

        val d = Day(context, 2016, Calendar.NOVEMBER, 4)
        assertNotSame(-1, db.isPeriod(d)) // Expected to be in the period

        val d1 = Day(context, 2016, Calendar.NOVEMBER, 2)
        assertNotSame(-1, db.isPeriod(d1)) // Expected to be in the period

        val d2 = Day(context, 2016, Calendar.NOVEMBER, 10)
        assertNotSame(-1, db.isPeriod(d2)) // Expected to be in the period

        val d3 = Day(context, 2016, Calendar.DECEMBER, 18)
        assertEquals(-1, db.isPeriod(d3)) // Expected NOT to be in the period
    }
}
