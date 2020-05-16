package com.beacat.calendar.ladycal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.tyczj.extendedcalendarview.Med;
import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.beacat.calendar.ladycal.R.string.KEY_THEME;
import static com.beacat.calendar.ladycal.R.style.AppTheme;

/**
 * Activity for showing the statistics.
 */

public class StatisticsActivity extends AppCompatActivity {

    private final int ANIM_XY = 2000;
    private ActionBar bar;

    /* Chart instances */
    private BarChart barChart = null;
    private LineChart lineChart = null;

    /* Database list */
    private PeriodDatabase db;
    private List<Period> allPeriodsListAsc = null;
    private List<Med> allMedsList = null;
    private List<Entry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if(i != null){
            setTheme(i.getIntExtra(getString(KEY_THEME), AppTheme));
        }

        db = PeriodDatabase.getInstance(getApplicationContext());
        setContentView(R.layout.statistics);

        bar = getSupportActionBar();

        // Change the title in the action bar
        bar.setTitle(R.string.statistics);
        bar.setSubtitle("");

        // Add back navigation
        bar.setDisplayHomeAsUpEnabled(true);

        final TextView infoTV = (TextView)findViewById(R.id.info);
        infoTV.setVisibility(View.INVISIBLE);

        TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("period");
        spec.setContent(R.id.tab);
        spec.setIndicator(getResources().getText(R.string.period_length_tab_title));
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("cycle");
        spec.setIndicator(getResources().getText(R.string.cycle_length_tab_title));
        spec.setContent(R.id.tab);
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("med");
        spec.setContent(R.id.tab);
        spec.setIndicator(getResources().getText(R.string.pain_days_tab_title));
        host.addTab(spec);

        //Tab 4
        spec = host.newTabSpec("med2");
        spec.setIndicator(getResources().getText(R.string.pain_periods_tab_title));
        spec.setContent(R.id.tab);
        host.addTab(spec);

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                switch (tabId) {
                    case "period":
                        createPeriodChart();
                        infoTV.setVisibility(View.INVISIBLE);
                        break;
                    case "med":
                        createMedPerDayChart();
                        infoTV.setVisibility(View.INVISIBLE);
                        break;
                    case "cycle":
                        createCycleChart();
                        infoTV.setVisibility(View.INVISIBLE);
                        break;
                    case "med2":
                        createMedPerPeriodChart();
                        infoTV.setVisibility(View.VISIBLE);
                        break;
                }

            }
        });

        // trick to show the first tab with chart data correctly....
        host.setCurrentTab(1);
        host.setCurrentTab(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.statistics_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.fit_to_screen:
                if(lineChart != null){
                    lineChart.fitScreen();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createMedPerPeriodChart() {
        final LineChart chart = createAndStyleLineChart();

        // check if we already have all meds
        if(allMedsList == null) {
            allMedsList = db.getAllMeds();
        }
        if(!entries.isEmpty()){
            entries.clear();
        }
        
        if(allMedsList != null && allMedsList.size() != 0 ) {
            final XValueDateFormatter formatter = new XValueDateFormatter();
            formatter.formatMed(allMedsList);
            chart.getXAxis().setValueFormatter(formatter);
            Description descr = new Description();
            descr.setText(getResources().getString(R.string.med_per_period_chart_descr));
            descr.setTextSize(12f);
            chart.setDescription(descr);

            float i = 0;
            for (Med m : allMedsList) {
                entries.add(new Entry(i, m.getTotalQuantity()));
                i++;
            }
            LineDataSet dataSet = new LineDataSet(entries, "");
            dataSet.setColors(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimary));
            dataSet.setValueFormatter(new YValueFormatter(false));
            dataSet.setCircleColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimaryDark));
//            dataSet.setCircleColorHole(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimaryDark));
            dataSet.setHighLightColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));
            dataSet.setCircleRadius(6f);
            dataSet.setLineWidth(2f);
            dataSet.setValueTextSize(12f);

            LineData lineData = new LineData(dataSet);

            // Set info dialog for value selection
            chart.setHighlightPerTapEnabled(true);
            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    AlertDialog  alertDialog = new AlertDialog.Builder(StatisticsActivity.this).create();
                    alertDialog.setTitle(formatter.getFormattedValue(e.getX()));
                    String mex = "";
                    Med m = allMedsList.get((int)e.getX());
                    for(int i = 0; i < m.getMeds().length; i++){
                        mex = mex.concat(getResources().getQuantityString(R.plurals.med_per_period, (i + 1), (i + 1), (int)m.getMedsinDay(i)) + "\n");
                    }
                    alertDialog.setMessage(mex);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CLOSE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            chart.highlightValue(null);
                        }
                    });
                    alertDialog.show();
                }

                @Override
                public void onNothingSelected() {
                    chart.highlightValue(null);
                }
            });

            chart.setData(lineData);
            chart.animateXY(ANIM_XY, ANIM_XY);
        }
        // Delete subtitle
        bar.setSubtitle("");
    }

    private void createCycleChart() {
        LineChart chart = createAndStyleLineChart();

        // check if we already have all periods
        if(allPeriodsListAsc == null) {
            allPeriodsListAsc = db.getAllPeriods("ASC");
        }
        if(!entries.isEmpty()){
            entries.clear();
        }

        YAxis yAxis = chart.getAxisLeft();
        LimitLine max = new LimitLine(35, "SAFE MAX: 35");
        max.setLineColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));
        max.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        max.setLineWidth(2f);
        max.setTextSize(9f);
        LimitLine min = new LimitLine(21, "SAFE MIN: 21");
        min.setLineColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));
        min.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        min.setLineWidth(2f);
        min.setTextSize(9f);
        yAxis.addLimitLine(max);
        yAxis.addLimitLine(min);

        if(allPeriodsListAsc != null && allPeriodsListAsc.size() != 0 ) {
            XValueDateFormatter formatter = new XValueDateFormatter();
            formatter.formatPeriod(allPeriodsListAsc);
            chart.getXAxis().setValueFormatter(formatter);
            Description descr = new Description();
            descr.setText(getResources().getString(R.string.days_descr));
            descr.setTextSize(12f);
            chart.setDescription(descr);

            // skip the insertion of the last cycle because it's a guess, not real data
            for(int i = 0; i < allPeriodsListAsc.size() - 1; i++){
                entries.add(new Entry(i, allPeriodsListAsc.get(i).getCycleLength()));
            }

            if(entries.size() > 0) {
                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setColors(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimary));
                dataSet.setValueFormatter(new YValueFormatter(false));
                dataSet.setCircleColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimaryDark));
//                dataSet.setCircleColorHole(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimaryDark));
                dataSet.setHighLightColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));
                dataSet.setCircleRadius(6f);
                dataSet.setLineWidth(2f);
                dataSet.setValueTextSize(12f);

                LineData lineData = new LineData(dataSet);

                chart.setData(lineData);
                chart.animateXY(ANIM_XY, ANIM_XY);

                // Set average as subtitle
                int avg = db.getCycleLengthAvg();
                bar.setSubtitle(getResources().getQuantityString(R.plurals.average, avg, avg));
            }
        }
        else {
            // Delete subtitle
            bar.setSubtitle("");
        }
    }

    private void createMedPerDayChart() {
        BarChart chart = createAndStyleBarChart();

        // check if we already have all meds
        if(allMedsList == null) {
            allMedsList = db.getAllMeds();
        }

        // we need a special type of entries here
        ArrayList<BarEntry> entries = new ArrayList<>();

        if(allMedsList != null && allMedsList.size() != 0 ) {
            XValueDayFormatter formatter = new XValueDayFormatter();
            chart.getXAxis().setValueFormatter(formatter);
            Description descr = new Description();
            descr.setText(getResources().getString(R.string.med_per_day_chart_descr));
            descr.setTextSize(12f);
            chart.setDescription(descr);

            //get the max length
            int maxLength = db.getMaxPeriodLength();

            // array of days
            float[] days = new float[maxLength];

            // for each day, add all the meds taken during that day
            for(int i = 0; i < maxLength; i++){
                for(Med m : allMedsList){
                    days[i] += m.getMedsinDay(i);
                }
                entries.add(new BarEntry((float)i, days[i]));
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setValueFormatter(new YValueFormatter(true));
            dataSet.setColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));

            BarData barData = new BarData(dataSet);

            chart.setData(barData);
            chart.animateXY(ANIM_XY, ANIM_XY);
        }
        // Delete subtitle
        bar.setSubtitle("");
    }

    private BarChart createAndStyleBarChart() {
        // get a layout defined in xml
        FrameLayout layout = (FrameLayout) findViewById(R.id.chart);
        // remove all chart
        layout.removeAllViews();

        // check if this chart has been created before
        if(barChart == null) {
            // programmatically create a BarChart
            BarChart chart = new BarChart(StatisticsActivity.this);
            chart.setNoDataText(getResources().getString(R.string.no_data));
            chart.setNoDataTextColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimary));

            XAxis xAxis = chart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setTextSize(12f);

            // data has AxisDependency.LEFT
            YAxis left = chart.getAxisLeft();
            left.setDrawLabels(false); // no axis labels
            left.setDrawAxisLine(false); // no axis line
            left.setDrawGridLines(false); // no grid lines
            left.setDrawZeroLine(true); // draw a zero line
            left.setGranularity(1f);
            chart.getAxisRight().setEnabled(false); // no right axis

            // Disable entry highlight
            chart.setHighlightPerDragEnabled(false);
            chart.setHighlightPerTapEnabled(false);

            Legend legend = chart.getLegend();
            legend.setEnabled(false);

            // add some space around the chart
            chart.setExtraOffsets(20f, 0f, 20f, 4f); // left, top, right, bottom

            // disable scaling on X and Y
            chart.setScaleEnabled(false);

            barChart = chart;
        }
        else{
            // clear the chart from all data
            barChart.clear();
        }

        // add the programmatically created chart
        layout.addView(barChart);

        return barChart;
    }

    private void createPeriodChart() {
        LineChart chart = createAndStyleLineChart();

        // check if we already have all periods
        if(allPeriodsListAsc == null) {
            allPeriodsListAsc = db.getAllPeriods("ASC");
        }
        if(!entries.isEmpty()){
            entries.clear();
        }

        YAxis yAxis = chart.getAxisLeft();
        LimitLine max = new LimitLine(7, "SAFE MAX: 7");
        max.setLineColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));
        max.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        max.setLineWidth(2f);
        max.setTextSize(9f);
        LimitLine min = new LimitLine(2, "SAFE MIN: 2");
        min.setLineColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));
        min.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        min.setLineWidth(2f);
        min.setTextSize(9f);
        yAxis.addLimitLine(max);
        yAxis.addLimitLine(min);

        if(allPeriodsListAsc != null && allPeriodsListAsc.size() != 0 ) {
            XValueDateFormatter formatter = new XValueDateFormatter();
            formatter.formatPeriod(allPeriodsListAsc);
            chart.getXAxis().setValueFormatter(formatter);
            Description descr = new Description();
            descr.setText(getResources().getString(R.string.days_descr));
            descr.setTextSize(12f);
            chart.setDescription(descr);

            // skip the insertion of the last period
            int i;
            for(i = 0; i < allPeriodsListAsc.size() - 1; i++){
                entries.add(new Entry(i, allPeriodsListAsc.get(i).getPeriodLength()));
            }
            // check if the last period is ended or not
            long last = allPeriodsListAsc.get(i).getEndDay();
            if(last <= Calendar.getInstance().getTimeInMillis()){
                entries.add(new Entry(i, allPeriodsListAsc.get(i).getPeriodLength()));
            }

            if(entries.size() > 0) {
                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setColors(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimary));
                dataSet.setValueFormatter(new YValueFormatter(false));
                dataSet.setCircleColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimaryDark));
//                dataSet.setCircleColorHole(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimaryDark));
                dataSet.setHighLightColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorAccent));
                dataSet.setCircleRadius(6f);
                dataSet.setLineWidth(2f);
                dataSet.setValueTextSize(12f);

                LineData lineData = new LineData(dataSet);

                chart.setData(lineData);
                chart.animateXY(ANIM_XY, ANIM_XY);

                // Set average as subtitle
                int avg = db.getPeriodLengthAvg();
                bar.setSubtitle(getResources().getQuantityString(R.plurals.average, avg, avg));
            }
        }
        else{
            // Delete subtitle
            bar.setSubtitle("");
        }
    }

    private LineChart createAndStyleLineChart(){
        // get a layout defined in xml
        FrameLayout layout = (FrameLayout) findViewById(R.id.chart);
        // remove all chart
        layout.removeAllViews();

        // check if this chart has been created before
        if(lineChart == null) {
            // programmatically create a LineChart
            LineChart chart = new LineChart(StatisticsActivity.this);
            chart.setNoDataText(getResources().getString(R.string.no_data));
            chart.setNoDataTextColor(UtilityClass.getThemeColor(StatisticsActivity.this, R.attr.colorPrimary));

            XAxis xAxis = chart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setTextSize(12f);

            // data has AxisDependency.LEFT
            YAxis left = chart.getAxisLeft();
            left.setDrawLabels(false); // no axis labels
            left.setDrawAxisLine(false); // no axis line
            left.setDrawGridLines(false); // no grid lines
            left.setDrawZeroLine(true); // draw a zero line
            left.setGranularity(1f);
            chart.getAxisRight().setEnabled(false); // no right axis

            // Disable entry highlight
            chart.setHighlightPerDragEnabled(false);
            chart.setHighlightPerTapEnabled(false);

            Legend legend = chart.getLegend();
            legend.setEnabled(false);

            // add some space around the chart
            chart.setExtraOffsets(20f, 0f, 20f, 4f); // left, top, right, bottom

            // disable scaling on Y
            chart.setScaleYEnabled(false);

            lineChart = chart;
        }
        else{
            // reset zoom
            lineChart.fitScreen();

            // clear the chart from all data
            lineChart.clear();

            // remove limits lines
            lineChart.getAxisLeft().removeAllLimitLines();

            // disable tap on entry
            lineChart.setHighlightPerTapEnabled(false);
        }

        // add the programmatically created chart
        layout.addView(lineChart);
        
        return lineChart;
    }

    /**
     * Inner class to format the axis values (from UTC to month/year)
     */
    private static class XValueDateFormatter extends ValueFormatter {

        private String[] values;

        XValueDateFormatter(){}

        void formatPeriod(List<Period> dates) {
            values = new String[dates.size()];
            Calendar cal = Calendar.getInstance();
            int i = 0;
            for(Period p : dates){
                cal.setTimeInMillis(p.getStartDay());
                values[i] = new SimpleDateFormat("MMM yy", Locale.getDefault()).format(cal.getTime());
                i++;
            }
        }

        void formatMed(List<Med> dates){
            values = new String[dates.size()];
            Calendar cal = Calendar.getInstance();
            int i = 0;
            for(Med m : dates){
                cal.setTimeInMillis(m.getDate());
                values[i] = new SimpleDateFormat("MMM yy", Locale.getDefault()).format(cal.getTime());
                i++;
            }
        }

        @Override
        public String getFormattedValue(float value) {
            if(value >= 0 && value < values.length)
                return values[(int)value];
            else
                return "";
        }

    }

    private static class YValueFormatter extends ValueFormatter {

        private NumberFormat mFormat;
        private boolean hideValue;

        YValueFormatter(boolean hideValue) {
            this.hideValue = hideValue;
            if(!hideValue) {
                mFormat = NumberFormat.getInstance();
                mFormat.setParseIntegerOnly(true);
            }
        }

        @Override
        public String getFormattedValue(float value) {
            if (!hideValue) {
                if (value < 0)
                    return "N/A";
                else
                    return mFormat.format(value);
            }
            return "";
        }
    }

    private static class XValueDayFormatter extends ValueFormatter {

        XValueDayFormatter(){}

        @Override
        public String getFormattedValue(float value) {
            value++;
            return "Day " + (int)value;
        }

    }

}
