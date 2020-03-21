package com.trianguloy.continuousDataUsage.common;

import java.util.Locale;

public class Utils {
    /**
     * Rounds double to int
     *
     * @param d double value
     * @return round(d) as int
     */
    public static int dbl2int(double d) {
        return Math.round(Math.round(d));
    }

    /**
     * Formats data based on the preferences
     * @param pref preferences object
     * @param mb megabytes to use
     * @param units whether to show units
     * @return the formatted string
     */
    public static String formatData(Preferences pref, double mb, boolean units) {
        final String format = ("%." + pref.getDecimals() + "f") + (units ? (pref.getGB() ? " GB" : " MB") : "");
        final double arg = pref.getGB() ? mb / (pref.getAltConversion() ? 1000 : 1024) : mb;
        return String.format(Locale.US, format, arg);
    }
}
