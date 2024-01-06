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
     * Preferences
     */
    private Preferences pref;

    /**
     * Context
     */
    private Context cntx;


    //------------ Public --------------

    /**
     * Constructor to avoid duplicated Preferences
     *
     * @param cntx context
     * @param pref preferences class
     */
    public DataUsage(Context cntx, Preferences pref) {
        this.pref = pref;
        this.cntx = cntx;
    }


    /**
     * Initializes internal data
     */
    public void init() throws Error {
        //get service
        nsm = cntx.getSystemService(NetworkStatsManager.class);
        if (nsm == null) {
            //can't get NetworkStatsManager
            Log.d("widget", "error on NetworkStatsManager");
            throw new Error(R.string.txt_widget_errorService);
        }
    }

    /**
     * Returns the data usage on the given period
     *
     * @param from_to pair start&end of period
     * @return data usage in period
     * @throws Error if can't get the data
     */
    public double getDataFromPeriod(Pair<Long, Long> from_to) throws Error {
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
            bucket = getNsm().querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, from, to);
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

    // ------------------- internal data -------------------


    /**
     * Class for the calculations
     */
    private NetworkStatsManager nsm = null;

    private NetworkStatsManager getNsm() throws Error {
        if (nsm == null)
            init();
        return nsm;
    }

}
