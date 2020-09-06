package com.trianguloy.continuousDataUsage.common;

import android.util.Pair;

import java.util.Calendar;

/**
 * Calendar-related functions, manages a period
 */
public class PeriodCalendar {

    private final Preferences pref;

    /**
     * Constructor
     *
     * @param pref preferences to get info about saved period
     */
    public PeriodCalendar(Preferences pref) {
        this.pref = pref;
    }

    /**
     * Returns the start and end millis of the specified period
     *
     * @param period 0 for current, -1 for previous, -2 for two previous...
     * @return start&end millis pair
     */
    public Pair<Long, Long> getLimitsOfPeriod(int period) {

        Calendar cal = getStartOfPeriod(period);

        // get start of the period
        long startOfPeriod = cal.getTimeInMillis();

        // get end of period
        cal.add(pref.getPeriodType(), pref.getPeriodLength());
        long endOfPeriod = cal.getTimeInMillis();

        return new Pair<>(startOfPeriod, endOfPeriod);
    }

    /**
     * @return the current period based on the saved prefs.
     * Almost always 0
     * 1 (or more) when period changes
     * -1 or less when time travel
     */
    public int getCurrentPeriod() {
        int period = 0;

        long now = System.currentTimeMillis();

        while (true) {
            final Pair<Long, Long> limits = getLimitsOfPeriod(period);
            if (limits.first <= now && now < limits.second) return period;
            if (now >= limits.second) period++;
            if (now < limits.first) period--;
        }
    }


    // ----------------

    /**
     * @param period 0 for current, -1 for previous...
     * @return a calendar at the start of the specified period
     */
    public Calendar getStartOfPeriod(int period) {
        Calendar cal = pref.getPeriodStart();

        // goto period wanted
        while (period > 0) {
            // add length for future period
            cal.add(pref.getPeriodType(), pref.getPeriodLength());
            period--;
        }
        while (period < 0) {
            // substract length for past (or current) period
            cal.add(pref.getPeriodType(), -pref.getPeriodLength());
            period++;
        }

        return cal;
    }

    // ------------------- static -------------------

    /**
     * @return today as a calendar
     */
    public static Calendar today() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * @return A calendar from a @param year, @param month and @param day
     */
    public static Calendar from(int year, int month, int day) {
        Calendar cal = today();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }
}
