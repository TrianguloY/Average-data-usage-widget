package com.trianguloy.continuousDataUsage.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.Preferences;
import com.trianguloy.continuousDataUsage.common.Tweaks;
import com.trianguloy.continuousDataUsage.common.Utils;

import java.util.Locale;

/**
 * Implementation of the Progress Widget functionality.
 * Displays the values and two progress bar corresponding to the 'average' and 'current' usage.
 */
public class AppWidgetProgress extends AppWidgetBase {

    /**
     * When the size changes, update the widget
     *
     * @param context          base context
     * @param appWidgetManager widget manager, base class
     * @param appWidgetId      id of the widget to update
     * @param newOptions       unused
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        updateAppWidget(context, appWidgetManager, appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * Updates a widget adding its views and configuring them.
     *
     * @param context          base context
     * @param appWidgetManager widget manager, base class
     * @param appWidgetId      id of the widget to update
     */
    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        boolean small = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < 180;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), small ? R.layout.widget_progress_short : R.layout.widget_progress);
        updateViews(context, views);

        //update when clicking
        setOnClick(context, new int[]{appWidgetId}, views, new int[]{R.id.wdg_prgBar_data, R.id.wdg_prgBar_date}, ACTION_REFRESH, AppWidgetProgress.class);
        setOnClick(context, new int[]{appWidgetId}, views, new int[]{R.id.btn_showData}, ACTION_USAGE, AppWidgetProgress.class);
        setOnClick(context, new int[]{appWidgetId}, views, new int[]{R.id.wdg_txt_data, R.id.wdg_txt_date}, ACTION_INFO, AppWidgetProgress.class);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Updates the views of a widget
     *
     * @param context base context
     * @param views   views to update
     */
    public static void updateViews(Context context, RemoteViews views) {
        ReturnedInfo commonInfo = getCommonInfo(context);
        Preferences pref = new Preferences(context);

        //variables
        String formatter = " (%.2f%%)";
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point(context.getResources().getInteger(R.integer.DEFAULT_PROGRESS_PRECISION), 0);
        if (wm != null) {
            wm.getDefaultDisplay().getSize(p);
        }
        int progressPrecision = p.x;

        //top bar
        views.setProgressBar(R.id.wdg_prgBar_date, progressPrecision, Utils.dbl2int(commonInfo.percentDate * progressPrecision), false);
        views.setTextViewText(R.id.wdg_txt_date,
                Utils.formatData(pref, commonInfo.percentDate * commonInfo.totalData, true) +
                        String.format(Locale.US, formatter, commonInfo.percentDate * 100)
        );

        //error
        if (commonInfo.error != -1) {
            views.setTextViewText(R.id.wdg_txt_data, context.getString(commonInfo.error));
            return;
        }

        //bottom bar
        views.setProgressBar(R.id.wdg_prgBar_data, progressPrecision, Utils.dbl2int((commonInfo.percentData % 1) * progressPrecision), false);
        views.setInt(R.id.wdg_prgBar_data, "setSecondaryProgress",
                commonInfo.percentData > 1 ? progressPrecision
                        : commonInfo.percentData < 0 ? Utils.dbl2int((1 + commonInfo.percentData) * progressPrecision)
                        : 0);
        views.setTextViewText(R.id.wdg_txt_data,
                Utils.formatData(pref, commonInfo.megabytes, true) +
                        String.format(Locale.US, formatter, commonInfo.percentData * 100)
        );

        // tweaks
        if (pref.getTweak(Tweaks.Items.hideDate)) {
            views.setViewVisibility(R.id.wdg_txt_date, View.GONE);
            views.setViewVisibility(R.id.wdg_prgBar_date, View.GONE);
        }
        if (pref.getTweak(Tweaks.Items.hideData)) {
            views.setViewVisibility(R.id.wdg_txt_data, View.GONE);
            views.setViewVisibility(R.id.wdg_prgBar_data, View.GONE);
        }
        if (pref.getTweak(Tweaks.Items.capNoWarp)) {
            views.setProgressBar(R.id.wdg_prgBar_data, progressPrecision, Utils.dbl2int((commonInfo.percentData) * progressPrecision), false);
            views.setInt(R.id.wdg_prgBar_data, "setSecondaryProgress", 0);
        }
        if (pref.getTweak(Tweaks.Items.whiteWidgets)) {
            views.setInt(R.id.wdg_parent, "setBackgroundResource", R.drawable.background_progress_white);
            views.setTextColor(R.id.wdg_txt_date, Color.BLACK);
            views.setTextColor(R.id.wdg_txt_data, Color.BLACK);
            views.setInt(R.id.btn_showData, "setImageResource", R.drawable.ic_history_black);
        }

    }
}

