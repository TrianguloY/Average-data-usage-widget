package com.trianguloy.continuousDataUsage.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.DataUsage;
import com.trianguloy.continuousDataUsage.common.PeriodCalendar;
import com.trianguloy.continuousDataUsage.common.Preferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * Activity to show the usage each day from the period.
 */
public class HistoryActivity extends Activity {

    // objects
    private ListAdapter adapter;
    private PeriodCalendar periodCalendar;
    private DataUsage dataUsage;
    private Preferences pref;

    // variables
    private int period;
    private TextView view_title;
    private ProgressBar view_loading;
    private Button view_right;
    private final DateFormat dateFormat = SimpleDateFormat.getDateInstance();
    private Thread thread_setPeriod = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_history);

        // initialize objects
        pref = new Preferences(this);
        periodCalendar = new PeriodCalendar(pref);
        adapter = new ListAdapter(this);

        // get views
        ListView view_list = findViewById(R.id.h_lv_list);
        view_title = findViewById(R.id.h_tv_title);
        view_loading = findViewById(R.id.h_pb_loading);
        view_right = findViewById(R.id.h_btn_right);

        // initialize elements
        view_list.setAdapter(adapter);
        adapter.setDummyView(findViewById(R.id.h_item_dummy));

        try{
            dataUsage = new DataUsage(this, pref);
        }catch (DataUsage.Error e){
            // can't open, probably no permission
            Toast.makeText(this, e.errorId, Toast.LENGTH_LONG).show();
            finish();
        }

        period = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When resumed, reload period
        setPeriod();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.h_btn_left:
                // decrement period
                period--;
                setPeriod();
                break;
            case R.id.h_btn_right:
                // increment period (if possible)
                if(period < 0) {
                    period++;
                    setPeriod();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.h_action_info:
                //show info
                new AlertDialog.Builder(this).setMessage(R.string.h_toast_info).setCancelable(true).show();
                return true;
            case R.id.h_action_settings:
                //show settings
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.h_action_android:
                //open android usage screen
                Intent settings = new Intent(Intent.ACTION_MAIN);
                settings.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                try {
                    startActivity(settings);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.toast_activityNotFound, Toast.LENGTH_LONG).show();
                }
                return true;
            case android.R.id.home:
                //explicitly go back to main activity
                final Intent upIntent = getParentActivityIntent();
                if (shouldUpRecreateTask(upIntent) || isTaskRoot()) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    navigateUpTo(upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // reload period
    public void setPeriod() {

        //init variables
        final Calendar to = periodCalendar.getStartOfPeriod(period);
        SimpleDateFormat title_format = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        //get from
        String monthFrom = title_format.format(to.getTime());
        final long from = to.getTimeInMillis();


        //get to
        to.add(pref.getPeriodType(), pref.getPeriodLength());
        to.add(Calendar.DAY_OF_MONTH, -1);
        String monthTo = title_format.format(to.getTime());

        //set title
        final String month = monthTo.equals(monthFrom) ? monthFrom : monthFrom +" - " + monthTo;
        view_title.setText(month);
        to.add(Calendar.DAY_OF_MONTH, 1);


        //background update
        if(thread_setPeriod != null){
            thread_setPeriod.interrupt();
            try {
                thread_setPeriod.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //set as loading
        view_loading.setVisibility(View.VISIBLE);
        adapter.clearItems();
        adapter.notifyDataSetChanged();
        view_right.setEnabled(period < 0);


        thread_setPeriod = new Thread(new Runnable() {
            @Override
            public void run() {

                adapter.setDataPerDay(
                        pref.getTotalData() / Math.round((to.getTimeInMillis() - from) / 1000d / 60d / 60d / 24d)
                );

                try {
                    while (from < to.getTimeInMillis() && !Thread.currentThread().isInterrupted()) {
                        long end = to.getTimeInMillis();
                        to.add(Calendar.DAY_OF_MONTH, -1);
                        if(to.getTimeInMillis() <= System.currentTimeMillis()) {
                            adapter.addItem(dataUsage.getDataFromPeriod(to.getTimeInMillis(), end), dateFormat.format(to.getTime()));
                        }
                    }
                } catch (final DataUsage.Error e) {
                    adapter.clearItems();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HistoryActivity.this, e.errorId, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                if(Thread.currentThread().isInterrupted()) return;

                //notify
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        view_loading.setVisibility(View.GONE);
                    }
                });
            }
        });
        thread_setPeriod.start();

    }

}
