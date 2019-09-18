package com.trianguloy.continuousDataUsage.common;

import android.util.Pair;

import java.util.Calendar;

/**
 * Calendar-related functions
 */
public class PeriodCalendar {

    /**
     * First day of the period
     */
    private int firstDay;

    /**
     * Constructor
     * @param firstDay first day of the period
     */
    public PeriodCalendar(int firstDay) {
        this.firstDay = firstDay;
    }

    /**
     * Returns the start and end millis of the specified period
     * @param period 0 for current, -1 for previous, -2 for two previous...
     * @return start&end millis pair
     */
    public Pair<Long, Long> getPeriod(int period){

        Calendar cal = getStartOfPeriod(period);

        // get start of the period
        long startOfPeriod = cal.getTimeInMillis();

        //get end of period
        cal.add(Calendar.MONTH, 1);
        long endOfPeriod = cal.getTimeInMillis();

        return new Pair<>(startOfPeriod, endOfPeriod);
    }

    /**
     * @return the month of the start of current period
     */
    public int getCurrentMonth(){
        return getStartOfPeriod(0).get(Calendar.MONTH);
    }


    // ----------------

    /**
     * @return a calendar at the start of the specified period
     * @param period 0 for current, -1 for previous...
     */
    public Calendar getStartOfPeriod(int period){
        //current
        long currentMillis = System.currentTimeMillis();

        //date
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // goto start of period 0
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        if (currentMillis < cal.getTimeInMillis()) {
            cal.add(Calendar.MONTH, -1);
        }

        // goto period wanted
        cal.add(Calendar.MONTH, period);

        return cal;
    }
}
