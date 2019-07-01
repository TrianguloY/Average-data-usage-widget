package com.trianguloy.continuousDataUsage.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.DataUsage;
import com.trianguloy.continuousDataUsage.common.Preferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        ListAdapter adapter = new ListAdapter(this);
        ((ListView) findViewById(R.id.lv_list)).setAdapter(adapter);

        Preferences pref = new Preferences(this);
        DataUsage dataUsage;
        try{
            dataUsage = new DataUsage(this, pref);
        }catch (DataUsage.Error e){
            return;
        }

        //date
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        DateFormat dateFormat = SimpleDateFormat.getDateInstance();

        for(int i=0;i<31;++i){
            long endOfPeriod = cal.getTimeInMillis();

            cal.add(Calendar.DAY_OF_MONTH, -1);

            double megabytes;
            try {
                megabytes = dataUsage.getDataFromPeriod(cal.getTimeInMillis(), endOfPeriod);
            }catch(Error e){
                megabytes = -1;
            }

            adapter.addItem(megabytes, dateFormat.format(cal.getTime()));
            Log.d("debug",megabytes+"");
        }
        adapter.notifyDataSetChanged();
    }
}
