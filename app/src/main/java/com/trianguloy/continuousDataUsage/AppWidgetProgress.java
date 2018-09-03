package com.trianguloy.continuousDataUsage;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;

import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidgetProgress extends AppWidgetBase {
    
    
    /**
     * Updates a widget adding its views and configuring them.
     *
     * @param context          base context
     * @param appWidgetManager widget manager, base class
     * @param appWidgetId      id of the widget to update
     */
    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_progress);
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
    void updateViews(Context context, RemoteViews views) {
        ReturnedInfo commonInfo = getCommonInfo(context);
        
        //variables
        String formatter = "%.2f MB (%.2f%%)";
        
        //top bar
        views.setProgressBar(R.id.wdg_prgBar_date, PROGRESS_PRECISION, dbl2int(commonInfo.percentDate * PROGRESS_PRECISION), false);
        views.setTextViewText(R.id.wdg_txt_date, String.format(Locale.US, formatter, commonInfo.percentDate * commonInfo.totalData, commonInfo.percentDate * 100));
        
        //error
        if(commonInfo.error != -1){
            views.setTextViewText(R.id.wdg_txt_data, context.getString(commonInfo.error));
            return;
        }
        
        //bottom bar
        views.setProgressBar(R.id.wdg_prgBar_data, PROGRESS_PRECISION, dbl2int(commonInfo.percentData * PROGRESS_PRECISION), false);
        views.setTextViewText(R.id.wdg_txt_data, String.format(Locale.US, formatter, commonInfo.megabytes, commonInfo.percentData * 100));
        
    }
}

