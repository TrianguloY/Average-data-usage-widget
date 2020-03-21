package com.trianguloy.continuousDataUsage.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.Preferences;
import com.trianguloy.continuousDataUsage.common.Tweaks;
import com.trianguloy.continuousDataUsage.common.Utils;

/**
 * Implementation of the Rate widget functionality.
 * Displays a number with the rate between used_data / average_data
 */
public class AppWidgetRate extends AppWidgetBase {


    /**
     * Updates a widget adding its views and configuring them.
     *
     * @param context          base context
     * @param appWidgetManager widget manager, base class
     * @param appWidgetId      id of the widget to update
     */
    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_rate);
        updateViews(context, views);

        //update when clicking
        setOnClick(context, new int[]{appWidgetId}, views, new int[]{R.id.wdg_prgBar_data, R.id.wdg_txt_rate}, ACTION_INFO, AppWidgetRate.class);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Updates the views of a widget
     *
     * @param context base context
     * @param views   views to update
     */
    void updateViews(Context context, RemoteViews views) {
        ReturnedInfo commonInfo = getCommonInfo(context);
        Preferences pref = new Preferences(context);

        if (commonInfo.error != -1) {
            views.setTextViewText(R.id.wdg_txt_rate, context.getString(commonInfo.error));
            return;
        }

        //number
        double rate = commonInfo.percentData / commonInfo.percentDate;
        views.setTextViewText(R.id.wdg_txt_rate, Utils.formatData(pref, "{/}", rate));

        // tweaks
        if (pref.getTweak(Tweaks.Items.showConsumed)) {
            rate = commonInfo.megabytes;
            views.setTextViewText(R.id.wdg_txt_rate, Utils.formatData(pref, "{0}", rate));
        }
        if (pref.getTweak(Tweaks.Items.showAverage)) {
            rate = commonInfo.totalData;
            views.setTextViewText(R.id.wdg_txt_rate, Utils.formatData(pref, "{0}", rate));
        }
        if (pref.getTweak(Tweaks.Items.whiteWidgets)) {
            views.setInt(R.id.wdg_parent, "setBackgroundResource", R.drawable.background_rate_white);
            views.setTextColor(R.id.wdg_txt_rate, Color.BLACK);
        }

    }
}

