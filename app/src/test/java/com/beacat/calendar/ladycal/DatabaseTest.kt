package com.beacat.calendar.ladycal;

import android.content.Context;

import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testing the database functionality
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, manifest = "/src/main/AndroidManifest.xml")
public class DatabaseTest{

    private Context context;
    private PeriodDatabase db;

    @Before
    public void setup(){
        context = RuntimeEnvironment.application;
        db = PeriodDatabase.getInstance(context);
        db.setCycleLength(28, true);
        db.setPeriodLength(6, true);
    }

    @Test
    public void test_addPeriod() throws Exception{
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(2016, Calendar.NOVEMBER, 2);
        long start = cal.getTimeInMillis();
        cal.set(2016, Calendar.NOVEMBER, 5);
        long end = cal.getTimeInMillis();

        db.addPeriod(new Period(start, end));
        List<Period> list = db.getAllPeriods("DESC");
        assertEquals(list.size(), 1);

        // Re-insert the same
        db.addPeriod(new Period(start, end));
        list = db.getAllPeriods("DESC");

        assertEquals(1, list.size()); // expect to have only 1 entry

        Period p = list.get(0);
        assertEquals(start, p.getStartDay());
        assertEquals(end, p.getEndDay());
        assertEquals(4, p.getPeriodLength());
        assertEquals(28, p.getCycleLength());
    }

    @Test
    public void test_lengthValues() throws Exception{
        Calendar cal = Calendar.getInstance();
        cal.clear();

        // period #1
        cal.set(2017, Calendar.JANUARY, 2);
        long start = cal.getTimeInMillis();
        cal.set(2017, Calendar.JANUARY, 5);
        long end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        // period #2
        cal.set(2017, Calendar.MARCH, 10);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.MARCH, 20);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        // period #3 - history insertion. This should update itself and january
        cal.set(2017, Calendar.FEBRUARY, 4);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.FEBRUARY, 5);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));


        List<Period> list = db.getAllPeriods("DESC");

        Period p = list.get(0); //March
        assertEquals(10, p.getPeriodLength());
        assertEquals(28, p.getCycleLength());

        p = list.get(1); //February
        assertEquals(2, p.getPeriodLength());
        assertEquals(34, p.getCycleLength());

        p = list.get(2); //January
        assertEquals(4, p.getPeriodLength());
        assertEquals(33, p.getCycleLength());
    }

    @Test
    public void test_updatePeriod() throws Exception{
        Calendar cal = Calendar.getInstance();
        cal.clear();

        // period #1
        cal.set(2017, Calendar.JANUARY, 2);
        long start = cal.getTimeInMillis();
        cal.set(2017, Calendar.JANUARY, 5);
        long end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        // period #2
        cal.set(2017, Calendar.FEBRUARY, 4);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.FEBRUARY, 5);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        // period #3
        cal.set(2017, Calendar.MARCH, 10);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.MARCH, 20);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        List<Period> list = db.getAllPeriods("DESC");

        Period p = list.get(0); //march
        assertEquals(10, p.getPeriodLength());
        assertEquals(28, p.getCycleLength());

        p = list.get(1); //february
        assertEquals(2, p.getPeriodLength());
        assertEquals(34, p.getCycleLength());

        p = list.get(2); //january
        assertEquals(4, p.getPeriodLength());
        assertEquals(33, p.getCycleLength());
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
        cal.set(2017, Calendar.FEBRUARY, 10);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.FEBRUARY, 15);
        end = cal.getTimeInMillis();
        db.updatePeriod(list.get(1), new Period(start, end));

        list.clear();
        list = db.getAllPeriods("DESC");

        p = list.get(0); //march
        assertEquals(10, p.getPeriodLength());
        assertEquals(28, p.getCycleLength());

        p = list.get(1); //february
        assertEquals(6, p.getPeriodLength());
        assertEquals(28, p.getCycleLength());

        p = list.get(2); //january
        assertEquals(4, p.getPeriodLength());
        assertEquals(39, p.getCycleLength());

    }

    @Test
    public void test_deletePeriod() throws Exception{
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(2016, Calendar.NOVEMBER, 2);
        long start = cal.getTimeInMillis();
        cal.set(2016, Calendar.NOVEMBER, 5);
        long end = cal.getTimeInMillis();

        Period p = new Period(start, end);
        db.addPeriod(p);
        List<Period> list = db.getAllPeriods("DESC");
        assertEquals(1, list.size());

        db.deletePeriod(p);

        list = db.getAllPeriods("DESC");
        assertEquals(0, list.size());

    }

    @Test
    public void test_searchPeriod() throws Exception{
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(2016, Calendar.NOVEMBER, 2);
        long start = cal.getTimeInMillis();
        cal.set(2016, Calendar.NOVEMBER, 5);
        long end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        cal.set(2016, Calendar.JANUARY, 2);
        long start1 = cal.getTimeInMillis();
        cal.set(2016, Calendar.JANUARY, 5);
        long end1 = cal.getTimeInMillis();
        db.addPeriod(new Period(start1, end1));

        cal.set(2016, Calendar.JANUARY, 2);
        long start2 = cal.getTimeInMillis();
        cal.set(2016, Calendar.JANUARY, 5);
        long end2 = cal.getTimeInMillis();
        db.addPeriod(new Period(start2, end2));

        Period p = new Period(start, end);
        assertNotSame(-1, db.searchPeriodId(p));  // expected to exist

        Period p1 = new Period(50, 50);
        assertEquals(-1, db.searchPeriodId(p1));  // expected to not exist
    }

    @Test
    public void test_isPeriod() throws Exception{
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(2016, Calendar.NOVEMBER, 2);
        long start = cal.getTimeInMillis();
        cal.set(2016, Calendar.NOVEMBER, 10);
        long end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        Day d = new Day(context, 2016, Calendar.NOVEMBER, 4);
        assertNotSame(-1, db.isPeriod(d)); // Expected to be in the period

        Day d1 = new Day(context, 2016, Calendar.NOVEMBER, 2);
        assertNotSame(-1, db.isPeriod(d1)); // Expected to be in the period

        Day d2 = new Day(context, 2016, Calendar.NOVEMBER, 10);
        assertNotSame(-1, db.isPeriod(d2)); // Expected to be in the period

        Day d3 = new Day(context, 2016, Calendar.DECEMBER, 18);
        assertEquals(-1, db.isPeriod(d3)); // Expected NOT to be in the period
    }
}
