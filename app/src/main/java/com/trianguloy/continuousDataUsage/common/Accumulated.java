package com.trianguloy.continuousDataUsage.common;

import android.util.Log;

import com.trianguloy.continuousDataUsage.BuildConfig;

/**
 * Functions related to the accumulated data.
 */
public class Accumulated {

    // objects
    private final Preferences pref;
    private final DataUsage dataUsage;
    private final PeriodCalendar periodCalendar;

    public Accumulated(Preferences pref, DataUsage dataUsage, PeriodCalendar periodCalendar) {
        this.pref = pref;
        this.dataUsage = dataUsage;
        this.periodCalendar = periodCalendar;
    }

    /**
     * Returns the used data from the current period (used - accumulated)
     */
    public double getUsedDataFromCurrentPeriod() throws DataUsage.Error {
        double usedData = dataUsage.getDataFromPeriod(periodCalendar.getLimitsOfPeriod(0).first, Long.MAX_VALUE);

        if (pref.getSavedPeriods() > 0) {
            // subtract accumulated from previous period
            usedData -= pref.getAccumulated();
        }

        return usedData;
    }

    /**
     * Sets the used data from the current period (by changing the accumulated value, which then returns)
     */
    public double setUsedDataFromCurrentPeriod(double usedData) throws DataUsage.Error {
        if (pref.getSavedPeriods() <= 0) return 0;

        double accumulated = dataUsage.getDataFromPeriod(periodCalendar.getLimitsOfPeriod(0).first, Long.MAX_VALUE) - usedData;
        pref.setAccumulated((float) accumulated);
        return accumulated;
    }

    /**
     * Checks if the currently saved period and accumulated data needs update, and does so.
     */
    public void updatePeriod() {
        int current = periodCalendar.getCurrentPeriod();

        if (current != 0) {
            // new period

            // update start of period
            pref.setPeriodStart(periodCalendar.getStartOfPeriod(current));
            if (BuildConfig.DEBUG && periodCalendar.getCurrentPeriod() != 0) {
                throw new AssertionError("Current period is " + periodCalendar.getCurrentPeriod() + ", not 0!");
            }

            // calculate new accumulated data
            try {
                pref.setAccumulated((float) calculateAccumulated(pref.getAccumulated(), -current, false, periodCalendar));
            } catch (DataUsage.Error ignore) {
                // can't get, just ignore
            }
            Log.d("UPDATE", "Updated");
        }
    }

    // -----------------------------------

    /**
     * Recursive function to get the accumulated data starting from any previous period (ignores empty)
     *
     * @param accum     accumulated data in latest period
     * @param period    which latest period
     * @param skipEmpty if true, unspent months will be skipped
     * @param perCal    PeriodCalendar object
     * @return accumulated data in previous current period
     * @throws DataUsage.Error if can't calculate it
     */
    public double calculateAccumulated(double accum, int period, boolean skipEmpty, PeriodCalendar perCal) throws DataUsage.Error {
        if (period >= 0) {
            //end of recursion, final period
            return accum;
        }

        // onto next period
        double dataInPeriod = dataUsage.getDataFromPeriod(perCal.getLimitsOfPeriod(period));

        // skip if required and nothing was spent (device not configured yet)
        if (!(skipEmpty && dataInPeriod == 0)) {

            // calculate accumulated
            accum += pref.getTotalData() - dataInPeriod;
            if (accum < 0) accum = 0; // if used more, accumulated=0 (used all, nothing saved)
            float periodsData = pref.getTotalData() * pref.getSavedPeriods();
            if (accum > periodsData) accum = periodsData; // if accumulated more than possible, cut
            skipEmpty = false;

        }

        return calculateAccumulated(accum, period + 1, skipEmpty, perCal);
    }

    /**
     * Tries to calculate the accumulated data of the previous period from the latest 100 periods (ignoring empty)
     *
     * @return calculated accumulated data
     * @throws DataUsage.Error if can't get data
     */
    public double autoCalculateAccumulated() throws DataUsage.Error {
        return calculateAccumulated(0, -100, true, periodCalendar);
    }
}
