package com.trianguloy.continuousDataUsage;

import android.Manifest;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Implementation of App Widget functionality. Base class.
 */
abstract class AppWidgetBase extends AppWidgetProvider {
    
    //abstract
    abstract void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId);
    abstract void updateViews(Context context, RemoteViews views);
    
    private static final String EXTRA_ACTION = "cdu_action";
    static final int ACTION_REFRESH = 0;
    static final int ACTION_USAGE = 1;
    static final int ACTION_INFO = 2;
    
    
    /**
     * When a widget is created for the first time. Nothing done.
     *
     * @param context base context
     */
    @Override
    public void onEnabled(Context context) {
    }
    
    
    /**
     * When the last widget is removed. Nothing done
     *
     * @param context base context
     */
    @Override
    public void onDisabled(Context context) {
    }
    
    
    /**
     * Called when one or more widgets needs to be updated. Updates all of them
     *
     * @param context          the base context
     * @param appWidgetManager the widget manager
     * @param appWidgetIds     the ids of the widgets that needs to be updated
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    
    /**
     * Intent received, act accordingly.
     * @param context that magical class
     * @param intent the intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getIntExtra(EXTRA_ACTION, -1)) {
            case ACTION_USAGE:
                //open the android usage settings
                Intent settings = new Intent(Intent.ACTION_MAIN);
                settings.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    context.startActivity(settings);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.toast_activityNotFound, Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_INFO:
                //info requested, set flag
                new Preferences(context).infoRequested();
            default:
                super.onReceive(context, intent);
                break;
        }
    }
    
    /**
     * To return a bundle with multiple values in the #getCommonInfo function
     */
    static class ReturnedInfo{
        double percentDate = 0;
        double totalData = 0;
        
        int error = -1;
        
        double percentData = 0;
        double megabytes = 0;
    }
    
    /**
     * The core of the widgets, calculates the necessary data
     * @param context not to be confused with contest
     * @return calculated data in a packed class
     */
    ReturnedInfo getCommonInfo(Context context){
        
        ReturnedInfo returnedInfo = new ReturnedInfo();
    
        //get preferences
        Preferences pref = new Preferences(context);
    
        boolean infoRequested = pref.isInfoRequested();
    
    
        //date
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    
        //current
        long currentMillis = System.currentTimeMillis();
    
        // get start of the period
        int firstDay = pref.getFirstDay();
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        if (currentMillis < cal.getTimeInMillis()) {
            cal.add(Calendar.MONTH, -1);
        }
        long startOfPeriod = cal.getTimeInMillis();
    
        //get end of period
        cal.add(Calendar.MONTH, 1);
        long endOfPeriod = cal.getTimeInMillis();
    
        //upper bar
        double percentDate = (currentMillis - startOfPeriod) / (double) (endOfPeriod - startOfPeriod);
        returnedInfo.percentDate = percentDate;
        double totalData = pref.getTotalData();
        returnedInfo.totalData = totalData;
    
        //check permission
        if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //no permission given, can't continue
        
            returnedInfo.error = R.string.txt_widget_noPermission;
            Log.d("widget", "error on checkSelfPermission");
            return returnedInfo;
        }
    
        //get subscriber id
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            //can't get telephony manager
        
            returnedInfo.error = R.string.txt_widget_errorService;
            Log.d("widget", "error on TelephonyManager");
            return returnedInfo;
        }
        String subscriberId = tm.getSubscriberId();
    
        //get service
        NetworkStatsManager nsm = context.getSystemService(NetworkStatsManager.class);
        if (nsm == null) {
            //can't get NetworkStatsManager
        
            returnedInfo.error = R.string.txt_widget_errorService;
            Log.d("widget", "error on NetworkStatsManager");
            return returnedInfo;
        }
    
        //get data
        NetworkStats.Bucket bucket;
        try {
            bucket = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subscriberId, startOfPeriod, Long.MAX_VALUE);
        } catch (RemoteException e) {
            returnedInfo.error = R.string.txt_widget_errorQuering;
            Log.d("widget", "error on querySummaryForDevice-RemoteException");
            return returnedInfo;
        } catch (SecurityException se) {
            returnedInfo.error = R.string.txt_widget_noPermission;
            Log.d("widget", "error on querySummaryForDevice-SecurityException");
            return returnedInfo;
        }
    
        //bottom bar
        double bytesConversion = pref.getAltConversion() ?
                1f / 1000f / 1000f
                :
                1f / 1024f / 1024f;
        double megabytes = (bucket.getRxBytes() + bucket.getTxBytes()) * bytesConversion;
        returnedInfo.megabytes = megabytes;
        double percentData = megabytes / totalData;
        returnedInfo.percentData = percentData;
    
        //current usage as date
        if (infoRequested) {
            double millisEquivalent = (endOfPeriod - startOfPeriod) * percentData + startOfPeriod;
            cal.clear();
            cal.setTimeInMillis(Math.round(millisEquivalent));
            Toast.makeText(context, context.getString(R.string.toast_currentUsage,
                    SimpleDateFormat.getDateTimeInstance().format(cal.getTime()),
                    millisToInterval(cal.getTimeInMillis() - currentMillis)), Toast.LENGTH_LONG).show();
        }
    
        Log.d("Widget", "updated");
        
        return returnedInfo;
    }
    
    
    
    
    
    //--------------utils-------------------
    
    
    /**
     * Puts the updateWidget click event on the specific views
     * @param context base context
     * @param appWidgetIds ids to update
     * @param remoteViews where to find the views
     * @param views views to set
     * @param action intent action
     * @param classForIntent class for the intent
     */
    static void setOnClick(Context context, int[] appWidgetIds, RemoteViews remoteViews, int[] views, int action, Class<?> classForIntent) {
        Intent intent = new Intent(context, classForIntent);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.putExtra(EXTRA_ACTION, action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        for (int view : views) {
            remoteViews.setOnClickPendingIntent(view, pendingIntent);
        }
    }
    
    /**
     * Converts millis to a string display
     * @param millis mililiseconds of a specific time
     * @return the time as string
     */
    static String millisToInterval(long millis) {
        StringBuilder sb = new StringBuilder();
        String prefix = millis >= 0 ? "+ " : "- ";
        millis = millis > 0 ? millis : -millis;
        
        millis /= 1000;
        
        sb.insert(0, " seconds");
        sb.insert(0, millis % 60);
        millis /= 60;
        
        if (millis > 0) {
            sb.insert(0, " minutes, ");
            sb.insert(0, millis % 60);
            millis /= 60;
        }
        
        if (millis > 0) {
            sb.insert(0, " hours, ");
            sb.insert(0, millis % 24);
            millis /= 24;
        }
        
        if (millis > 0) {
            sb.insert(0, " days, ");
            sb.insert(0, millis);
        }
        
        sb.insert(0, prefix);
        
        return sb.toString();
    }
    
    
    /**
     * Rounds double to int
     *
     * @param d double value
     * @return round(d) as int
     */
    static int dbl2int(double d) {
        return Math.round(Math.round(d));
    }
    
}
