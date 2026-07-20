package com.beacat.calendar.ladycal

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TabHost
import android.widget.TextView

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.tyczj.extendedcalendarview.Med
import com.tyczj.extendedcalendarview.Period
import com.tyczj.extendedcalendarview.PeriodDatabase

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

/**
 * Activity for showing the statistics.
 */

class StatisticsActivity : AppCompatActivity() {

    private val ANIM_XY = 2000
    private var bar: ActionBar? = null

    /* Chart instances */
    private var barChart: BarChart? = null
    private var lineChart: LineChart? = null

    /* Database list */
    private lateinit var db: PeriodDatabase
    private var allPeriodsListAsc: List<Period>? = null
    private var allMedsList: List<Med>? = null
    private val entries: MutableList<Entry> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = intent
        if (i != null) {
            setTheme(i.getIntExtra(getString(R.string.KEY_THEME), R.style.AppTheme))
        }

        db = PeriodDatabase.getInstance(applicationContext)
        setContentView(R.layout.statistics)

        bar = supportActionBar

        // Change the title in the action bar
        bar!!.setTitle(R.string.statistics)
        bar!!.subtitle = ""

        // Add back navigation
        bar!!.setDisplayHomeAsUpEnabled(true)

        val infoTV = findViewById<TextView>(R.id.info)
        infoTV.setTextColor(Utilities.getChartInfoTextColor(this))
        infoTV.visibility = View.INVISIBLE

        val host = findViewById<TabHost>(R.id.tabHost)
        host.setup()

        //Tab 1
        var spec = host.newTabSpec("period")
        spec.setContent(R.id.tab)
        spec.setIndicator(resources.getText(R.string.period_length_tab_title))
        host.addTab(spec)

        //Tab 2
        spec = host.newTabSpec("cycle")
        spec.setIndicator(resources.getText(R.string.cycle_length_tab_title))
        spec.setContent(R.id.tab)
        host.addTab(spec)

        //Tab 3
        spec = host.newTabSpec("med")
        spec.setContent(R.id.tab)
        spec.setIndicator(resources.getText(R.string.pain_days_tab_title))
        host.addTab(spec)

        //Tab 4
        spec = host.newTabSpec("med2")
        spec.setIndicator(resources.getText(R.string.pain_periods_tab_title))
        spec.setContent(R.id.tab)
        host.addTab(spec)

        host.setOnTabChangedListener { tabId ->
            when (tabId) {
                "period" -> {
                    createPeriodChart()
                    infoTV.visibility = View.INVISIBLE
                }
                "med" -> {
                    createMedPerDayChart()
                    infoTV.visibility = View.INVISIBLE
                }
                "cycle" -> {
                    createCycleChart()
                    infoTV.visibility = View.INVISIBLE
                }
                "med2" -> {
                    createMedPerPeriodChart()
                    infoTV.visibility = View.VISIBLE
                }
            }
        }

        // trick to show the first tab with chart data correctly....
        host.currentTab = 1
        host.currentTab = 0
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.statistics_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.fit_to_screen -> {
                if (lineChart != null) {
                    lineChart!!.fitScreen()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createMedPerPeriodChart() {
        val chart = createAndStyleLineChart()

        // check if we already have all meds
        if (allMedsList == null) {
            allMedsList = db.getAllMeds()
        }
        if (!entries.isEmpty()) {
            entries.clear()
        }

        if (allMedsList != null && allMedsList!!.size != 0) {
            val formatter = XValueDateFormatter()
            formatter.formatMed(allMedsList!!)
            chart.xAxis.valueFormatter = formatter
            val descr = Description()
            descr.text = resources.getString(R.string.med_per_period_chart_descr)
            descr.textSize = 12f
            descr.textColor = resources.getColor(R.color.chart_text_color)
            chart.description = descr

            var i = 0f
            for (m in allMedsList!!) {
                entries.add(Entry(i, m.totalQuantity.toFloat()))
                i++
            }
            val dataSet = LineDataSet(entries, "")
            dataSet.setColors(Utilities.getLineChartDataColor(this))
            dataSet.valueFormatter = YValueFormatter(false)
            dataSet.setCircleColor(Utilities.getLineChartCircleColor(this))
            dataSet.highLightColor = Utilities.getThemeColor(this, R.attr.colorAccent)
            dataSet.circleRadius = 6f
            dataSet.lineWidth = 2f
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = resources.getColor(R.color.chart_text_color)

            val lineData = LineData(dataSet)

            // Set info dialog for value selection
            chart.isHighlightPerTapEnabled = true
            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    val alertDialog = AlertDialog.Builder(this@StatisticsActivity).create()
                    alertDialog.setTitle(formatter.getFormattedValue(e.x))
                    var mex = ""
                    val m = allMedsList!![e.x.toInt()]
                    for (i in 0 until m.meds.size) {
                        mex += resources.getQuantityString(R.plurals.med_per_period, (i + 1), (i + 1), m.getMedsinDay(i).toInt()) + "\n"
                    }
                    alertDialog.setMessage(mex)
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CLOSE") { dialog, _ ->
                        dialog.dismiss()
                        chart.highlightValue(null)
                    }
                    alertDialog.show()
                }

                override fun onNothingSelected() {
                    chart.highlightValue(null)
                }
            })

            chart.data = lineData
            chart.animateXY(ANIM_XY, ANIM_XY)
        }
        // Delete subtitle
        bar!!.subtitle = ""
    }

    private fun createCycleChart() {
        val chart = createAndStyleLineChart()

        // check if we already have all periods
        if (allPeriodsListAsc == null) {
            allPeriodsListAsc = db.getAllPeriods("ASC")
        }
        if (!entries.isEmpty()) {
            entries.clear()
        }

        val yAxis = chart.axisLeft
        val max = LimitLine(35f, "SAFE MAX: 35")
        max.lineColor = Utilities.getLineChartLimitColor(this)
        max.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
        max.lineWidth = 2f
        max.textSize = 9f
        max.textColor = resources.getColor(R.color.chart_limit_text_color)

        val min = LimitLine(21f, "SAFE MIN: 21")
        min.lineColor = Utilities.getLineChartLimitColor(this)
        min.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        min.lineWidth = 2f
        min.textSize = 9f
        min.textColor = resources.getColor(R.color.chart_limit_text_color)

        yAxis.addLimitLine(max)
        yAxis.addLimitLine(min)

        if (allPeriodsListAsc != null && allPeriodsListAsc!!.size != 0) {
            val formatter = XValueDateFormatter()
            formatter.formatPeriod(allPeriodsListAsc!!)
            chart.xAxis.valueFormatter = formatter
            val descr = Description()
            descr.text = resources.getString(R.string.days_descr)
            descr.textSize = 12f
            descr.textColor = resources.getColor(R.color.chart_text_color)
            chart.description = descr

            // skip the insertion of the last cycle because it's a guess, not real data
            for (i in 0 until allPeriodsListAsc!!.size - 1) {
                entries.add(Entry(i.toFloat(), allPeriodsListAsc!![i].cycleLength.toFloat()))
            }

            if (entries.size > 0) {
                val dataSet = LineDataSet(entries, "")
                dataSet.setColors(Utilities.getLineChartDataColor(this))
                dataSet.valueFormatter = YValueFormatter(false)
                dataSet.setCircleColor(Utilities.getLineChartCircleColor(this))
                dataSet.highLightColor = Utilities.getThemeColor(this, R.attr.colorAccent)
                dataSet.circleRadius = 6f
                dataSet.lineWidth = 2f
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = resources.getColor(R.color.chart_text_color)

                val lineData = LineData(dataSet)

                chart.data = lineData
                chart.animateXY(ANIM_XY, ANIM_XY)

                // Set average as subtitle
                val avg = db.getCycleLengthAvg()
                bar!!.subtitle = resources.getQuantityString(R.plurals.average, avg, avg)
            }
        } else {
            // Delete subtitle
            bar!!.subtitle = ""
        }
    }

    private fun createMedPerDayChart() {
        val chart = createAndStyleBarChart()

        // check if we already have all meds
        if (allMedsList == null) {
            allMedsList = db.getAllMeds()
        }

        // we need a special type of entries here
        val entries = ArrayList<BarEntry>()

        if (allMedsList != null && allMedsList!!.size != 0) {
            val formatter = XValueDayFormatter()
            chart.xAxis.valueFormatter = formatter
            val descr = Description()
            descr.text = resources.getString(R.string.med_per_day_chart_descr)
            descr.textSize = 12f
            descr.textColor = resources.getColor(R.color.chart_text_color)
            chart.description = descr

            //get the max length
            val maxLength = db.getMaxPeriodLength()

            // array of days
            val days = FloatArray(maxLength)

            // for each day, add all the meds taken during that day
            for (i in 0 until maxLength) {
                for (m in allMedsList!!) {
                    days[i] += m.getMedsinDay(i)
                }
                entries.add(BarEntry(i.toFloat(), days[i]))
            }

            val dataSet = BarDataSet(entries, "")
            dataSet.valueFormatter = YValueFormatter(true)
            dataSet.color = Utilities.getThemeColor(this, R.attr.colorAccent)

            val barData = BarData(dataSet)

            chart.data = barData
            chart.animateXY(ANIM_XY, ANIM_XY)
        }
        // Delete subtitle
        bar!!.subtitle = ""
    }

    private fun createAndStyleBarChart(): BarChart {
        // get a layout defined in xml
        val layout = findViewById<FrameLayout>(R.id.chart)
        // remove all chart
        layout.removeAllViews()

        // check if this chart has been created before
        if (barChart == null) {
            // programmatically create a BarChart
            val chart = BarChart(this)
            chart.setNoDataText(resources.getString(R.string.no_data))
            chart.setNoDataTextColor(Utilities.getNoDataTextColor(this))

            val xAxis = chart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f // minimum axis-step (interval) is 1
            xAxis.textSize = 12f
            xAxis.textColor = resources.getColor(R.color.chart_text_color)

            // data has AxisDependency.LEFT
            val left = chart.axisLeft
            left.setDrawLabels(false) // no axis labels
            left.setDrawAxisLine(false) // no axis line
            left.setDrawGridLines(false) // no grid lines
            left.setDrawZeroLine(true) // draw a zero line
            left.granularity = 1f
            chart.axisRight.isEnabled = false // no right axis

            // Disable entry highlight
            chart.isHighlightPerDragEnabled = false
            chart.isHighlightPerTapEnabled = false

            val legend = chart.legend
            legend.isEnabled = false

            // add some space around the chart
            chart.setExtraOffsets(20f, 0f, 20f, 4f) // left, top, right, bottom

            // disable scaling on X and Y
            chart.setScaleEnabled(false)

            barChart = chart
        } else {
            // clear the chart from all data
            barChart!!.clear()
        }

        // add the programmatically created chart
        layout.addView(barChart)

        return barChart!!
    }

    private fun createPeriodChart() {
        val chart = createAndStyleLineChart()

        // check if we already have all periods
        if (allPeriodsListAsc == null) {
            allPeriodsListAsc = db.getAllPeriods("ASC")
        }
        if (!entries.isEmpty()) {
            entries.clear()
        }

        val yAxis = chart.axisLeft
        val max = LimitLine(7f, "SAFE MAX: 7")
        max.lineColor = Utilities.getLineChartLimitColor(this)
        max.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
        max.lineWidth = 2f
        max.textSize = 9f
        max.textColor = resources.getColor(R.color.chart_limit_text_color)

        val min = LimitLine(2f, "SAFE MIN: 2")
        min.lineColor = Utilities.getLineChartLimitColor(this)
        min.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        min.lineWidth = 2f
        min.textSize = 9f
        min.textColor = resources.getColor(R.color.chart_limit_text_color)

        yAxis.addLimitLine(max)
        yAxis.addLimitLine(min)

        if (allPeriodsListAsc != null && allPeriodsListAsc!!.size != 0) {
            val formatter = XValueDateFormatter()
            formatter.formatPeriod(allPeriodsListAsc!!)
            chart.xAxis.valueFormatter = formatter
            val descr = Description()
            descr.text = resources.getString(R.string.days_descr)
            descr.textSize = 12f
            descr.textColor = resources.getColor(R.color.chart_text_color)
            chart.description = descr

            // skip the insertion of the last period
            var i = 0
            while (i < allPeriodsListAsc!!.size - 1) {
                entries.add(Entry(i.toFloat(), allPeriodsListAsc!![i].periodLength.toFloat()))
                i++
            }
            // check if the last period is ended or not
            val last = allPeriodsListAsc!![i].endDay
            if (last <= Calendar.getInstance().timeInMillis) {
                entries.add(Entry(i.toFloat(), allPeriodsListAsc!![i].periodLength.toFloat()))
            }

            if (entries.size > 0) {
                val dataSet = LineDataSet(entries, "")
                dataSet.setColors(Utilities.getLineChartDataColor(this))
                dataSet.valueFormatter = YValueFormatter(false)
                dataSet.setCircleColor(Utilities.getLineChartCircleColor(this))
                dataSet.highLightColor = Utilities.getThemeColor(this, R.attr.colorAccent)
                dataSet.circleRadius = 6f
                dataSet.lineWidth = 2f
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = resources.getColor(R.color.chart_text_color)

                val lineData = LineData(dataSet)

                chart.data = lineData
                chart.animateXY(ANIM_XY, ANIM_XY)

                // Set average as subtitle
                val avg = db.getPeriodLengthAvg()
                bar!!.subtitle = resources.getQuantityString(R.plurals.average, avg, avg)
            }
        } else {
            // Delete subtitle
            bar!!.subtitle = ""
        }
    }

    private fun createAndStyleLineChart(): LineChart {
        // get a layout defined in xml
        val layout = findViewById<FrameLayout>(R.id.chart)
        // remove all chart
        layout.removeAllViews()

        // check if this chart has been created before
        if (lineChart == null) {
            // programmatically create a LineChart
            val chart = LineChart(this)
            chart.setNoDataText(resources.getString(R.string.no_data))
            chart.setNoDataTextColor(Utilities.getNoDataTextColor(this))

            val xAxis = chart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f // minimum axis-step (interval) is 1
            xAxis.textSize = 12f
            xAxis.textColor = resources.getColor(R.color.chart_text_color)

            // data has AxisDependency.LEFT
            val left = chart.axisLeft
            left.setDrawLabels(false) // no axis labels
            left.setDrawAxisLine(false) // no axis line
            left.setDrawGridLines(false) // no grid lines
            left.setDrawZeroLine(true) // draw a zero line
            left.granularity = 1f
            chart.axisRight.isEnabled = false // no right axis

            // Disable entry highlight
            chart.isHighlightPerDragEnabled = false
            chart.isHighlightPerTapEnabled = false

            val legend = chart.legend
            legend.isEnabled = false

            // add some space around the chart
            chart.setExtraOffsets(20f, 0f, 20f, 4f) // left, top, right, bottom

            // disable scaling on Y
            chart.setScaleYEnabled(false)

            lineChart = chart
        } else {
            // reset zoom
            lineChart!!.fitScreen()

            // clear the chart from all data
            lineChart!!.clear()

            // remove limits lines
            lineChart!!.axisLeft.removeAllLimitLines()

            // disable tap on entry
            lineChart!!.isHighlightPerTapEnabled = false
        }

        // add the programmatically created chart
        layout.addView(lineChart)

        return lineChart!!
    }

    /**
     * Inner class to format the axis values (from UTC to month/year)
     */
    private class XValueDateFormatter : ValueFormatter() {

        private lateinit var values: Array<String>

        fun formatPeriod(dates: List<Period>) {
            val cal = Calendar.getInstance()
            values = Array(dates.size) { i ->
                cal.timeInMillis = dates[i].startDay
                SimpleDateFormat("MMM yy", Locale.getDefault()).format(cal.time)
            }
        }

        fun formatMed(dates: List<Med>) {
            val cal = Calendar.getInstance()
            values = Array(dates.size) { i ->
                cal.timeInMillis = dates[i].date
                SimpleDateFormat("MMM yy", Locale.getDefault()).format(cal.time)
            }
        }

        override fun getFormattedValue(value: Float): String {
            return if (value >= 0 && value < values.size)
                values[value.toInt()]
            else
                ""
        }
    }

    private class YValueFormatter(private val hideValue: Boolean) : ValueFormatter() {

        private var mFormat: NumberFormat? = null

        init {
            if (!hideValue) {
                mFormat = NumberFormat.getInstance()
                mFormat!!.isParseIntegerOnly = true
            }
        }

        override fun getFormattedValue(value: Float): String {
            if (!hideValue) {
                return if (value < 0)
                    "N/A"
                else
                    mFormat!!.format(value.toDouble())
            }
            return ""
        }
    }

    private class XValueDayFormatter : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            var value = value
            value++
            return "Day " + value.toInt()
        }
    }
}
