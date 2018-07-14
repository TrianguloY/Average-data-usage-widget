package com.trianguloy.continuousDataUsage;

import android.Manifest;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    /**
     * Precision of the progress bars. Bigger number means more 'intermediate steps'
     */
    private static final int PROGRESS_PRECISION = 100;

    private static final String EXTRA_ACTION = "cdu_action";
    private static final int ACTION_REFRESH = 0;
    private static final int ACTION_USAGE = 1;
    private static final int ACTION_INFO = 2;


    /**
     * When a widget is created for the first time. Nothing done.
     * @param context base context
     */
    @Override
    public void onEnabled(Context context) {
    }


    /**
     * When the last widget is removed. Nothing done
     * @param context base context
     */
    @Override
    public void onDisabled(Context context) {
    }


    /**
     * Called when one or more widgets needs to be updated. Updates all of them
     * @param context the base context
     * @param appWidgetManager the widget manager
     * @param appWidgetIds the ids of the widgets that needs to be updated
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    /**
     * Updates a widget adding its views and configuring them.
     * @param context base context
     * @param appWidgetManager widget manager, base class
     * @param appWidgetId id of the widget to update
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        updateViews(context, views);

        //update when clicking
        setOnClick(context, new int[]{appWidgetId}, views, new int[]{R.id.wdg_prgBar_data,R.id.wdg_prgBar_date}, ACTION_REFRESH);
        setOnClick(context, new int[]{appWidgetId}, views, new int[]{R.id.btn_showData}, ACTION_USAGE);
        setOnClick(context, new int[]{appWidgetId}, views, new int[]{R.id.wdg_txt_data,R.id.wdg_txt_date}, ACTION_INFO);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setOnClick(Context context, int[] appWidgetIds, RemoteViews remoteViews, int[] views, int action) {
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.putExtra(EXTRA_ACTION, action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        for (int view : views) {
            remoteViews.setOnClickPendingIntent(view, pendingIntent);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getIntExtra(EXTRA_ACTION, -1)){
            case ACTION_USAGE:
                Intent settings = new Intent(Intent.ACTION_MAIN);
                settings.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(settings);
                break;
            case ACTION_INFO:
                new Preferences(context).infoRequested();
            default:
                super.onReceive(context, intent);
                break;
        }
    }


    /**
     * Updates the views of a widget
     * @param context base context
     * @param views views to update
     */
    private static void updateViews(Context context, RemoteViews views) {
        //variables
        String formatter = "%.2f MB (%.2f%%)";

        //get preferences
        Preferences pref = new Preferences(context);

        boolean infoRequested = pref.isInfoRequested();


        //date
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // get start of the period
        int firstDay = pref.getFirstDay();
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        long startOfPeriod = cal.getTimeInMillis();

        //get end of period
        cal.add(Calendar.MONTH, 1);
        long endOfPeriod = cal.getTimeInMillis();

        //upper bar
        double percentDate = (System.currentTimeMillis() - startOfPeriod) / (double) (endOfPeriod - startOfPeriod);
        views.setProgressBar(R.id.wdg_prgBar_date, PROGRESS_PRECISION, dbl2int(percentDate * PROGRESS_PRECISION), false);
        double totalData = pref.getTotalData();
        views.setTextViewText(R.id.wdg_txt_date, String.format(Locale.getDefault(), formatter, percentDate * totalData, percentDate * 100));

        //check permission
        if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //no permission given, can't continue

            views.setTextViewText(R.id.wdg_txt_data, context.getString(R.string.txt_widget_noPermission));
            Log.d("widget","error on checkSelfPermission");
            return;
        }

        //get subscriber id
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm == null){
            //can't get telephony manager

            views.setTextViewText(R.id.wdg_txt_data, context.getString(R.string.txt_widget_errorService));
            Log.d("widget","error on TelephonyManager");
            return;
        }
        String subscriberId = tm.getSubscriberId();

        //get service
        NetworkStatsManager nsm = context.getSystemService(NetworkStatsManager.class);
        if ( nsm == null){
            //can't get NetworkStatsManager

            views.setTextViewText(R.id.wdg_txt_data, context.getString(R.string.txt_widget_errorService));
            Log.d("widget","error on NetworkStatsManager");
            return;
        }

        //get data
        NetworkStats.Bucket bucket;
        try {
            bucket = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subscriberId, startOfPeriod, Long.MAX_VALUE);
        } catch (RemoteException e) {
            views.setTextViewText(R.id.wdg_txt_data, context.getString(R.string.txt_widget_errorQuering));
            Log.d("widget","error on querySummaryForDevice-RemoteException");
            return;
        } catch (SecurityException se){
            views.setTextViewText(R.id.wdg_txt_data, context.getString(R.string.txt_widget_noPermission));
            Log.d("widget","error on querySummaryForDevice-SecurityException");
            return;
        }

        //bottom bar
        double bytesConversion = pref.getAltConversion() ?
                                    1f / 1000f / 1000f
                                    :
                                    1f / 1024f / 1024f;
        double megabytes = (bucket.getRxBytes() + bucket.getTxBytes()) * bytesConversion;
        double percentData = megabytes / totalData;
        views.setProgressBar(R.id.wdg_prgBar_data, PROGRESS_PRECISION, dbl2int(percentData * PROGRESS_PRECISION), false);
        views.setTextViewText(R.id.wdg_txt_data, String.format(Locale.getDefault(), formatter, megabytes, percentData * 100));

        //current usage as date
        if(infoRequested){
            double millisEquivalent = (endOfPeriod - startOfPeriod) * percentData + startOfPeriod;
            cal.clear();
            cal.setTimeInMillis(Math.round(millisEquivalent));
            Toast.makeText(context, context.getString(R.string.toast_currentUsage,
                     SimpleDateFormat.getDateTimeInstance().format(cal.getTime()),
                    millisToInterval(cal.getTimeInMillis()-System.currentTimeMillis())), Toast.LENGTH_LONG).show();
        }

        Log.d("Widget", "updated");

    }

    private static String millisToInterval(long millis) {
        StringBuilder sb = new StringBuilder();
        String prefix = millis >= 0 ? "+ " : "- ";
        millis = millis > 0 ? millis : -millis;

        millis /= 1000;

        sb.insert(0," seconds");
        sb.insert(0,millis % 60);
        millis /= 60;

        if(millis > 0) {
            sb.insert(0, " minutes, ");
            sb.insert(0, millis % 60);
            millis /= 60;
        }

        if(millis>0) {
            sb.insert(0, " hours, ");
            sb.insert(0, millis % 24);
            millis /= 24;
        }

        if(millis>0){
            sb.insert(0," days, ");
            sb.insert(0, millis);
        }

        sb.insert(0,prefix);

        return sb.toString();
    }


    /**
     * Rounds double to int
     * @param d double value
     * @return round(d) as int
     */
    private static int dbl2int(double d) {
        return Math.round(Math.round(d));
    }
}

