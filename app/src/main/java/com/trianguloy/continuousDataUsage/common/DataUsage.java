package com.trianguloy.continuousDataUsage.common;

import android.Manifest;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.trianguloy.continuousDataUsage.R;

public class DataUsage {


    public class Error extends AndroidRuntimeException{
        public int error;
        Error(int error) {
            this.error = error;
        }
    }

    //--------------------------

    private Preferences pref;

    private final String subscriberId;
    private final NetworkStatsManager nsm;

    public DataUsage(Context context, Preferences pref) throws Error{
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
        subscriberId = tm.getSubscriberId();

        //get service
        nsm = context.getSystemService(NetworkStatsManager.class);
        if (nsm == null) {
            //can't get NetworkStatsManager
            Log.d("widget", "error on NetworkStatsManager");
            throw new Error(R.string.txt_widget_errorService);
        }
    }

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
}
