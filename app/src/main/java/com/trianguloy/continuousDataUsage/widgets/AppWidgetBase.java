package com.trianguloy.continuousDataUsage.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.activities.HistoryActivity;
import com.trianguloy.continuousDataUsage.common.Accumulated;
import com.trianguloy.continuousDataUsage.common.DataUsage;
import com.trianguloy.continuousDataUsage.common.PeriodCalendar;
import com.trianguloy.continuousDataUsage.common.Preferences;
import com.trianguloy.continuousDataUsage.common.Tweaks;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Implementation of App Widget functionality. Base class.
 */
abstract class AppWidgetBase extends AppWidgetProvider {

    //abstract
    abstract void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId);

    private static final String EXTRA_ACTION = "com.trianguloy.continuousDataUsage.action";
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
     *
     * @param context that magical class
     * @param intent  the intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getIntExtra(EXTRA_ACTION, -1)) {
            case ACTION_USAGE:
                //open the history activity
                Intent usage = new Intent(context, HistoryActivity.class);
                usage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(usage);
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
    static class ReturnedInfo {
        double percentDate = 0;
        double totalData = 0;

        int error = -1;

        double percentData = 0;
        double megabytes = 0;
    }

    /**
     * The core of the widgets, calculates the necessary data
     *
     * @param context not to be confused with contest
     * @return calculated data in a packed class
     */
    static ReturnedInfo getCommonInfo(Context context) {

        ReturnedInfo returnedInfo = new ReturnedInfo();

        // get objects
        Preferences pref = new Preferences(context);
        PeriodCalendar periodCalendar = new PeriodCalendar(pref);
        DataUsage dataUsage = new DataUsage(context, pref);

        // update
        new Accumulated(pref, dataUsage, periodCalendar).updatePeriod();

        boolean infoRequested = pref.isInfoRequested();

        //current time
        long currentMillis = System.currentTimeMillis();

        //current period
        Pair<Long, Long> val = periodCalendar.getLimitsOfPeriod(0);
        long startOfPeriod = val.first;
        long endOfPeriod = val.second;


        //upper bar
        double percentDate = (currentMillis - startOfPeriod) / (double) (endOfPeriod - startOfPeriod);
        returnedInfo.percentDate = percentDate;
        double totalData = pref.getTotalData();
        returnedInfo.totalData = totalData;

        //bottom bar
        double percentData;
        double megabytes;

        try {
            megabytes = dataUsage.getDataFromPeriod(startOfPeriod, Long.MAX_VALUE);

            if (pref.getSavedPeriods() > 0) {
                // subtract accumulated from previous period
                double prev = pref.getAccumulated();

                if (prev > 0)
                    megabytes -= prev;
                //if(megabytes<0)megabytes=0;
            }

        } catch (DataUsage.Error e) {
            returnedInfo.error = e.errorId;
            return returnedInfo;
        }

        returnedInfo.megabytes = megabytes;
        percentData = megabytes / totalData;
        returnedInfo.percentData = percentData;


        //current usage as date
        if (infoRequested) {
            double millisEquivalent = (endOfPeriod - startOfPeriod) * percentData + startOfPeriod;
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.setTimeInMillis(Math.round(millisEquivalent));
            Toast.makeText(context, context.getString(R.string.toast_currentUsage,
                    SimpleDateFormat.getDateTimeInstance().format(cal.getTime()),
                    millisToInterval(cal.getTimeInMillis() - currentMillis)), Toast.LENGTH_LONG).show();
        }

        // tweaks
        if (pref.getTweak(Tweaks.Tweak.showRemaining)) {
            returnedInfo.percentData = 1 - returnedInfo.percentData;
            returnedInfo.megabytes = returnedInfo.totalData - returnedInfo.megabytes;
            returnedInfo.percentDate = 1 - returnedInfo.percentDate;
        }

        Log.d("Widget", "updated");

        return returnedInfo;
    }


    //--------------utils-------------------


    /**
     * Puts the updateWidget click event on the specific views
     *
     * @param context        base context
     * @param appWidgetIds   ids to update
     * @param remoteViews    where to find the views
     * @param views          views to set
     * @param action         intent action
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
     *
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


}
