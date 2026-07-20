package com.beacat.calendar.ladycal

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorInt

import android.util.TypedValue

/**
 * Utility class used to group operation performed around the app
 */

object Utilities {

    // Get color based on the theme
    @ColorInt
    @JvmStatic
    fun getThemeColor(context: Context, attributeColor: Int): Int {
        val value = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attributeColor, value, true)
        return value.data
    }

    @ColorInt
    @JvmStatic
    fun getMonthColor(context: Context): Int {
        return if (isNightModeOn(context)) getThemeColor(context, R.attr.colorAccent) else getThemeColor(context, R.attr.colorPrimaryDark)
    }

    @ColorInt
    @JvmStatic
    fun getNoDataTextColor(context: Context): Int {
        return if (isNightModeOn(context)) context.resources.getColor(R.color.white) else getThemeColor(context, R.attr.colorPrimary)
    }

    @ColorInt
    @JvmStatic
    fun getChartInfoTextColor(context: Context): Int {
        return if (isNightModeOn(context)) context.resources.getColor(R.color.white) else getThemeColor(context, R.attr.colorPrimaryDark)
    }

    @ColorInt
    @JvmStatic
    fun getLineChartCircleColor(context: Context): Int {
        return if (isNightModeOn(context)) getThemeColor(context, R.attr.colorAccent) else getThemeColor(context, R.attr.colorPrimaryDark)
    }

    @ColorInt
    @JvmStatic
    fun getLineChartDataColor(context: Context): Int {
        return if (isNightModeOn(context)) getThemeColor(context, R.attr.colorAccent) else getThemeColor(context, R.attr.colorPrimary)
    }

    @ColorInt
    @JvmStatic
    fun getLineChartLimitColor(context: Context): Int {
        return if (isNightModeOn(context)) context.resources.getColor(R.color.blu_grey) else getThemeColor(context, R.attr.colorAccent)
    }

    @ColorInt
    @JvmStatic
    fun getPeriodListPrimaryTextColor(context: Context): Int {
        return if (isNightModeOn(context)) getThemeColor(context, R.attr.colorAccent) else getThemeColor(context, R.attr.colorPrimaryDark)
    }

    @ColorInt
    @JvmStatic
    fun getPeriodListSecondaryTextColor(context: Context): Int {
        return if (isNightModeOn(context)) context.resources.getColor(R.color.light_grey) else getThemeColor(context, R.attr.colorAccent)
    }

    @JvmStatic
    fun isNightModeOn(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}
