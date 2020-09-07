package com.trianguloy.continuousDataUsage.common;

import android.Manifest;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import com.trianguloy.continuousDataUsage.BuildConfig;
import com.trianguloy.continuousDataUsage.R;

/**
 * Data-related functions.
 */
public class DataUsage {

    /**
     * Returned when calling a function
     */
    public static class Error extends Exception {
        /**
         * The id of the string resource with the error
         */
        public int errorId;

        Error(int errorId) {
            this.errorId = errorId;
        }
    }

    /**
     * SubscriberId for the calculations
     */
    private final String subscriberId;

    /**
     * Class for the calculations
     */
    private final NetworkStatsManager nsm;

    /**
     * Preferences
     */
    private Preferences pref;


    //------------ Public --------------

    /**
     * Constructor to avoid duplicated Preferences
     *
     * @param context context
     * @param pref    preferences class
     * @throws Error if something bad happens
     */
    public DataUsage(Context context, Preferences pref) throws Error {
        this.pref = pref;

        //check permission
        if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //no permission given, can't continue
            Log.d("widget", "error on checkSelfPermission");
            throw new Error(R.string.txt_widget_noPermission);
        }

        //get subscriber id
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            //can't get telephony manager

            Log.d("widget", "error on TelephonyManager");
            throw new Error(R.string.txt_widget_errorService);
        }
        try {
            subscriberId = tm.getSubscriberId();
            //subscriberId = "";
        } catch (SecurityException e) {
            throw new Error(R.string.txt_widget_noPermission);
        }

        //get service
        nsm = context.getSystemService(NetworkStatsManager.class);
        if (nsm == null) {
            //can't get NetworkStatsManager
            Log.d("widget", "error on NetworkStatsManager");
            throw new Error(R.string.txt_widget_errorService);
        }

        // update
        updatePeriod();
    }

    /**
     * Checks if the currently saved period and accumulated data needs update, and does so
     */
    public void updatePeriod(){
        final PeriodCalendar perCal = new PeriodCalendar(pref);
        int current = perCal.getCurrentPeriod();

        if (current != 0) {
            // new period

            // update start of period
            pref.setPeriodStart(perCal.getStartOfPeriod(current));
            if (BuildConfig.DEBUG && perCal.getCurrentPeriod() != 0) {
                throw new AssertionError("Current period is "+perCal.getCurrentPeriod()+", not 0!");
            }

            // calculate new accumulated data
            try {
                pref.setAccumulated((float) calculateAccumulated(pref.getAccumulated(), -current, false, perCal));
            } catch (Error ignore) {
                // can't get, just ignore
            }

        }
    }

    /**
     * Returns the data usage on the given period
     *
     * @param from_to pair start&end of period
     * @return data usage in period
     * @throws Error if can't get the data
     */
    private double getDataFromPeriod(Pair<Long, Long> from_to) throws Error {
        return getDataFromPeriod(from_to.first, from_to.second);
    }

    /**
     * Returns the data usage on the given period
     *
     * @param from start of period
     * @param to   end of period
     * @return data usage in period
     * @throws Error if can't get the data
     */
    public double getDataFromPeriod(long from, long to) throws Error {

        //get data
        NetworkStats.Bucket bucket;
        try {
            bucket = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subscriberId, from, to);
        } catch (RemoteException e) {
            Log.d("widget", "error on querySummaryForDevice-RemoteException");
            throw new Error(R.string.txt_widget_errorQuering);
        } catch (SecurityException se) {
            Log.d("widget", "error on querySummaryForDevice-SecurityException");
            throw new Error(R.string.txt_widget_noPermission);
        }

        double bytesConversion = pref.getAltConversion() ?
                1f / 1000f / 1000f
                :
                1f / 1024f / 1024f;
        return (bucket.getRxBytes() + bucket.getTxBytes()) * bytesConversion;
    }


    /**
     * Returns the accumulated data from the previous period.
     *
     * @return the accumulated data in the previous period
     */
    public float getAccumulated() {
        return pref.getAccumulated();
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
     * @throws Error if can't calculate it
     */
    private double calculateAccumulated(double accum, int period, boolean skipEmpty, PeriodCalendar perCal) throws Error {
        if (period >= 0) {
            //end of recursion, final period
            return accum;
        }

        // onto next period
        double dataInPeriod = getDataFromPeriod(perCal.getLimitsOfPeriod(period));

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
     * Tries to calculate the accumulated data of the previous period from the latest 12 periods (ignoring empty)
     *
     * @return calculated accumulated data
     * @throws Error if can't get data
     */
    public double autoCalculateAccumulated() throws Error {
        return calculateAccumulated(0, -12, true, new PeriodCalendar(pref));
    }

}
