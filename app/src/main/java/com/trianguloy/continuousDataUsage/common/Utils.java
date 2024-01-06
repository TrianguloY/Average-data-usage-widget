package com.trianguloy.continuousDataUsage.common;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.trianguloy.continuousDataUsage.widgets.AppWidgetProgress;
import com.trianguloy.continuousDataUsage.widgets.AppWidgetRate;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // ------------------- Format -------------------

    /**
     * for {@link #formatData(Preferences, String, Double...)}
     */
    private static final Pattern pattern = Pattern.compile("\\{(.)\\}");

    /**
     * Custom formatter based on the preferences.
     * <p>
     * Uses getDecimals to set the number of decimals for data numbers.
     * Uses getGB to set the suffix and convert the argument.
     * Uses getAltConversion for the GB conversion.
     * <p>
     * <li> '{0}' data without suffix -> '1.123' </li>
     * <li> '{M}' data with suffix -> '1.123 MB' </li>
     * <li> '{%}' percentage -> '1.23%' </li>
     * <li> '{/}' ratio -> '1.23' </li>
     *
     * @param pref   preferences
     * @param format format string, see above for formatting options
     * @param args   values
     * @return formatted string
     */
    public static String formatData(Preferences pref, String format, Double... args) {
        // init elements
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        StringBuffer formatted = new StringBuffer(format.length());
        final Matcher matcher = pattern.matcher(format);
        int i = 0;

        // replace each formatting element
        while (matcher.find()) {

            // set replacing properties
            String suffix = "";
            switch (matcher.group(1)) {
                case "M":
                    // data with suffix
                    suffix = (pref.getGB() ? " GB" : " MB");
                case "0":
                    // data without suffix
                    formatter.setMinimumFractionDigits(pref.getDecimals());
                    formatter.setMaximumFractionDigits(pref.getDecimals());
                    if (pref.getGB()) args[i] /= pref.getAltConversion() ? 1000 : 1024;
                    break;
                case "%":
                    // percentage
                    suffix = "%";
                case "/":
                    // ratio
                    formatter.setMinimumFractionDigits(2);
                    formatter.setMaximumFractionDigits(2);
                    break;
                default:
                    throw new IllegalArgumentException(matcher.group());
            }

            // replace
            matcher.appendReplacement(formatted,
                    Matcher.quoteReplacement(formatter.format(args[i]) + suffix)
            );
            i++;
        }
        matcher.appendTail(formatted);

        // return
        return formatted.toString();
    }

    /**
     * Update all widgets now
     */
    public static void updateAllWidgets(Context context) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        for (Class<?> cls : new Class[]{AppWidgetProgress.class, AppWidgetRate.class}) {
            Intent updateIntent = new Intent(context, cls);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, man.getAppWidgetIds(new ComponentName(context, cls)));
            context.sendBroadcast(updateIntent);
        }
    }
}
