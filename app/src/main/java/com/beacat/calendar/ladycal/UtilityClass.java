package com.beacat.calendar.ladycal;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import android.util.TypedValue;

/**
 * Utility class used to group operation performed around the app
 */

public class UtilityClass {

    // Get color based on the theme
    @ColorInt
    public static int getThemeColor(final Context context, final int attributeColor)
    {
        final TypedValue value = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attributeColor, value, true);
        return value.data;
    }

    @ColorInt
    public static int getMonthColor(final Context context) {
        return isNightModeOn(context) ? getThemeColor(context, R.attr.colorAccent) : getThemeColor(context, R.attr.colorPrimaryDark);
    }

    @ColorInt
    public static int getNoDataTextColor(final Context context) {
        return isNightModeOn(context) ? context.getResources().getColor(R.color.white) : getThemeColor(context, R.attr.colorPrimary);
    }

    @ColorInt
    public static int getChartInfoTextColor(final Context context) {
        return isNightModeOn(context) ? context.getResources().getColor(R.color.white) : getThemeColor(context, R.attr.colorPrimaryDark);
    }

    public static boolean isNightModeOn(Context context) {
        int nightModeFlags =
                context.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}
