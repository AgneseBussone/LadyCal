package com.beacat.calendar.ladycal;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private Context appContext;
    private PeriodDatabase db;

    @Before
    public void useAppContext() throws Exception {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getTargetContext();
        db = PeriodDatabase.getInstance(appContext);

        // set lengths because in the mainActivity is done during the onCreate
        // if you don't do that, the add will insert wrong values
        db.setCycleLength(28, true);
        db.setPeriodLength(6, true);

        assertEquals("com.beacat.calendar.ladycal", appContext.getPackageName());
    }

    /* Probably not the best use for tests, but I need to insert a bunch of data
    *  without do it by hand
    */
    @Test
    public void createDb() throws Exception{
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(2016, Calendar.SEPTEMBER, 2);
        long start = cal.getTimeInMillis();
        cal.set(2016, Calendar.SEPTEMBER, 5);
        long end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        cal.set(2016, Calendar.OCTOBER, 10);
        start = cal.getTimeInMillis();
        cal.set(2016, Calendar.OCTOBER, 14);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        cal.set(2016, Calendar.NOVEMBER, 12);
        start = cal.getTimeInMillis();
        cal.set(2016, Calendar.NOVEMBER, 20);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        cal.set(2016, Calendar.DECEMBER, 25);
        start = cal.getTimeInMillis();
        cal.set(2016, Calendar.DECEMBER, 30);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        cal.set(2017, Calendar.JANUARY, 9);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.JANUARY, 15);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        cal.set(2017, Calendar.JANUARY, 29);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.FEBRUARY, 4);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));

        cal.set(2017, Calendar.APRIL, 16);
        start = cal.getTimeInMillis();
        cal.set(2017, Calendar.APRIL, 20);
        end = cal.getTimeInMillis();
        db.addPeriod(new Period(start, end));


        List<Period> list = db.getAllPeriods("DESC");
        assertEquals(list.size(), 7);

    }
}
