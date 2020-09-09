package com.trianguloy.continuousDataUsage.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.Preferences;
import com.trianguloy.continuousDataUsage.common.Tweaks;
import com.trianguloy.continuousDataUsage.widgets.AppWidgetProgress;

/**
 * Main activity. Info
 */
public class Main extends Activity {

    private FrameLayout preview;

    /**
     * That function
     * @param savedInstanceState no idea what this is
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //clickable links
        for (int id : new int[]{R.id.stt_txt_info} ) {
            ((TextView) findViewById(id)).setMovementMethod(LinkMovementMethod.getInstance());
        }

        preview = findViewById(R.id.m_f_widget);

    }

    /**
     * When the activity is resumed, update the widget
     */
    @Override
    protected void onResume() {

        updatePreview();

        super.onResume();
    }

    /**
     * A button is clicked
     * @param view which one
     */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.m_btn_history:
                //open history
                startActivity(new Intent(this, HistoryActivity.class));
                break;
            case R.id.m_btn_settings:
                //open settings
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.m_f_widget:
                //show info
                Toast.makeText(this, R.string.toast_preview, Toast.LENGTH_LONG).show();
                break;
        }

    }

    /**
     * Updates the preview
     */
    private void updatePreview() {
        // Construct the RemoteViews object
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_progress);
        AppWidgetProgress.updateViews(this, remoteViews);

        try {

            View views = remoteViews.apply(this, preview);
            preview.removeAllViews();
            preview.addView(views);

        }catch (Throwable ignore){

            // if exception, disable
            Toast.makeText(this, "Exception detected, disabling tweaks", Toast.LENGTH_LONG).show();
            Preferences prefs = new Preferences(this);
            for (Tweaks.Tweak item : Tweaks.Tweak.values()) {
                prefs.setTweak(item, false);
            }
            updatePreview();

        }
    }

}
