package com.trianguloy.continuousDataUsage.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
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

    private ListAdapter adapter;
    private PeriodCalendar periodCalendar;
    private DataUsage dataUsage;
    private Preferences pref;

    private TextView view_title;

    private int period;
    private ProgressBar view_loading;
    private Button view_right;
    private final DateFormat dateFormat = SimpleDateFormat.getDateInstance();
    private Thread thread_setPeriod = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_history);

        pref = new Preferences(this);
        periodCalendar = new PeriodCalendar(pref.getFirstDay());
        adapter = new ListAdapter(this);

        ListView view_list = findViewById(R.id.h_lv_list);
        view_title = findViewById(R.id.h_tv_title);
        view_loading = findViewById(R.id.h_pb_loading);
        view_right = findViewById(R.id.h_btn_right);

        view_list.setAdapter(adapter);

        try{
            dataUsage = new DataUsage(this, pref);
        }catch (DataUsage.Error e){
            Toast.makeText(this, e.errorId, Toast.LENGTH_SHORT).show();
        }

        period = 0;

        view_list.post(new Runnable() {
            @Override
            public void run() {
                setPeriod();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    public void onButtonClick(View view){
        switch(view.getId()){
            case R.id.h_btn_left:
                period--;
                setPeriod();
                break;
            case R.id.h_btn_right:
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
                settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(settings);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.toast_activityNotFound, Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setPeriod() {

        //init variables
        final Calendar to = periodCalendar.getStartOfPeriod(period);
        SimpleDateFormat title_format = new SimpleDateFormat("MMM YYYY", Locale.getDefault());

        //get from
        String monthFrom = title_format.format(to.getTime());
        final long from = to.getTimeInMillis();


        //get to
        to.add(Calendar.MONTH, 1);
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
                            Toast.makeText(HistoryActivity.this, e.errorId, Toast.LENGTH_SHORT).show();
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

    // ------------------------------

    UpdatePeriod update = null;

    void updatePeriod(){
        if(update != null) update.cancel(true);
        update = new UpdatePeriod();
        update.execute();
    }

    class UpdatePeriod extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            view_loading.setVisibility(View.VISIBLE);
            adapter.clearItems();
            view_right.setVisibility(period >= 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            Calendar to = periodCalendar.getStartOfPeriod(period);
            String monthFrom = new SimpleDateFormat("MMM-YYYY", Locale.getDefault()).format(to.getTime());
            long from = to.getTimeInMillis();

            to.add(Calendar.MONTH, 1);

            adapter.setDataPerDay(
                    pref.getTotalData() / Math.round((to.getTimeInMillis() - from) / 1000d / 60d / 60d / 24d)
            );
            try {
                while(from < to.getTimeInMillis()){
                    long end = to.getTimeInMillis();
                    to.add(Calendar.DAY_OF_MONTH, -1);

                    if(monthFrom != null){
                        String monthTo = new SimpleDateFormat("MMM", Locale.getDefault()).format(to.getTime());
                        final String month = monthTo.equals(monthFrom) ? monthFrom : monthFrom +" - " + monthTo;
                        monthFrom = null;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view_title.setText(month + (period == 0 ? " (now)" : ""));
                            }
                        });
                    }

                    adapter.addItem(dataUsage.getDataFromPeriod(to.getTimeInMillis(), end), dateFormat.format(to.getTime()));
                }
            }catch(DataUsage.Error e){
                adapter.clearItems();
                Toast.makeText(HistoryActivity.this, e.errorId, Toast.LENGTH_SHORT).show();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    view_loading.setVisibility(View.GONE);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            view_loading.setVisibility(View.GONE);
        }
    }



}
