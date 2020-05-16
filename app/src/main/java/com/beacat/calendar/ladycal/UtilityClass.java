package com.beacat.calendar.ladycal;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.ColorInt;
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
}
