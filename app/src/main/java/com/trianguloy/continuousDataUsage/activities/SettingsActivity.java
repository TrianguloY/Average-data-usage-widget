package com.trianguloy.continuousDataUsage.activities;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.Accumulated;
import com.trianguloy.continuousDataUsage.common.DataUsage;
import com.trianguloy.continuousDataUsage.common.NumericEditText;
import com.trianguloy.continuousDataUsage.common.PeriodCalendar;
import com.trianguloy.continuousDataUsage.common.Preferences;
import com.trianguloy.continuousDataUsage.common.Tweaks;
import com.trianguloy.continuousDataUsage.common.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Main activity: settings
 */
public class SettingsActivity extends Activity {

    // used classes
    private Preferences pref = null;
    private Accumulated accumulated;

    // views
    private NumericEditText view_accumulated;
    private TextView view_txt_decimals;
    private EditText txt_periodStart;
    private TextView txt_usageStats;


    /**
     * When the activity is created, like a constructor
     *
     * @param savedInstanceState previous saved state, used on super only
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // get objects
        pref = new Preferences(this);
        accumulated = new Accumulated(pref, new DataUsage(this, pref), new PeriodCalendar(pref));

        // get views
        txt_usageStats = findViewById(R.id.stt_txt_usageStats);

        //initializes
        initialize();

        // updates
        accumulated.updatePeriod();
        updateViews();

    }


    /**
     * Initialize all the views
     */
    private void initialize() {

        // totaldata
        final NumericEditText view_totalData = findViewById(R.id.stt_edTxt_totalData);
        view_totalData.initFloat(false, pref.getTotalData(), new NumericEditText.OnNewFloatListener() {
            @Override
            public void newNumber(float number) {
                pref.setTotalData(number);
            }
        });

        // period length, amount
        final NumericEditText txt_periodLength = findViewById(R.id.stt_edTxt_periodLength);
        txt_periodLength.initInt(false, pref.getPeriodLength(), new NumericEditText.OnNewIntListener() {
            @Override
            public void newNumber(int number) {
                pref.setPeriodLength(number);
            }
        });

        // period length, type
        final Spinner spn_periodType = findViewById(R.id.stt_spn_periodType);
        final List<Integer> periodTypes = Arrays.asList(Calendar.DAY_OF_MONTH, Calendar.MONTH);
        spn_periodType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{getString(R.string.days), getString(R.string.months)}));
        spn_periodType.setSelection(periodTypes.indexOf(pref.getPeriodType()));
        spn_periodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pref.setPeriodtype(periodTypes.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                pref.setPeriodtype(periodTypes.get(0));
            }
        });

        // periodStart
        txt_periodStart = findViewById(R.id.stt_edTxt_periodStart);

        // decimals
        view_txt_decimals = findViewById(R.id.stt_txt_decimals);
        SeekBar view_sb_decimals = findViewById(R.id.stt_sb_decimals);
        view_sb_decimals.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                view_txt_decimals.setText(Integer.toString(i));
                pref.setDecimals(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        view_sb_decimals.setProgress(pref.getDecimals(), false); // setProgress may not update if the value is the same
        view_txt_decimals.setText(Integer.toString(pref.getDecimals()));

        //GB
        CheckBox view_gb = findViewById(R.id.stt_chk_gb);
        view_gb.setChecked(pref.getGB());
        view_gb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setGB(b);
            }
        });

        //alternate conversion
        final CheckBox view_alternateConversion = findViewById(R.id.stt_chkBx_alternateConversion);
        view_alternateConversion.setChecked(pref.getAltConversion());
        view_alternateConversion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pref.setAltConversion(view_alternateConversion.isChecked());
            }
        });

        // accumulated periods
        final NumericEditText view_sb_savedPeriods = findViewById(R.id.stt_edTxt_savedPeriods);
        final View view_ll = findViewById(R.id.ll_accum);
        view_ll.setVisibility(pref.getSavedPeriods() > 0 ? View.VISIBLE : View.GONE);
        view_sb_savedPeriods.initInt(true, pref.getSavedPeriods(), new NumericEditText.OnNewIntListener() {
            @Override
            public void newNumber(int number) {
                pref.setSavedPeriods(number);
                view_ll.setVisibility(number > 0 ? View.VISIBLE : View.GONE);
            }
        });

        // accumulated megas
        view_accumulated = findViewById(R.id.stt_edTxt_accum);
        view_accumulated.initFloat(true, pref.getAccumulated(), new NumericEditText.OnNewFloatListener() {
            @Override
            public void newNumber(float number) {
                pref.setAccumulated(number);
            }
        });

        //clickable links
        ((TextView) findViewById(R.id.stt_txt_perm_us)).setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void updateViews() {
        // period start
        final Calendar periodStart = pref.getPeriodStart();
        txt_periodStart.setText(SimpleDateFormat.getDateInstance().format(periodStart.getTime()));

        // accumulated megas
        view_accumulated.setValue(pref.getAccumulated());

        // getUsageStats permission
        int mode = AppOpsManager.MODE_DEFAULT;
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps != null) {
            //check permission
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        }
        setPermissionState(txt_usageStats, mode == AppOpsManager.MODE_ALLOWED);
    }


    /**
     * Updates the specified id with the specified state.
     * state=true -> green and 'permission granted'
     * state=false -> red and 'permission needed'
     *
     * @param txt   textview to update
     * @param state the state
     */
    private void setPermissionState(TextView txt, boolean state) {
        txt.setText(state ? getString(R.string.txt_permissionGranted) : getString(R.string.txt_permissionsNeeded));
        txt.setBackgroundColor(state ? Color.argb(128, 0, 255, 0) : Color.argb(128, 255, 0, 0));
    }


    /**
     * Button clicked
     *
     * @param view which button
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.stt_btn_usageStats:
                //open usage settings to give permission
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                break;

            case R.id.stt_edTxt_periodStart:
                // start date picker
                pickPeriodStart();
                break;

            case R.id.stt_btn_accum:
                //auto-calculate accumulated
                try {
                    view_accumulated.setValue((float) accumulated
                            .autoCalculateAccumulated());
                } catch (DataUsage.Error e) {
                    Toast.makeText(this, getString(e.errorId), Toast.LENGTH_LONG).show();
                }
                // update if nececesary
                txt_periodStart.setText(SimpleDateFormat.getDateInstance().format(pref.getPeriodStart().getTime()));
                break;
            case R.id.stt_btn_tweaks:
                // show tweaks
                new Tweaks(pref, this).showDialog();
                break;
        }
    }

    /**
     * Shows a date dialog to change the start date
     */
    private void pickPeriodStart() {
        final DatePickerDialog dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // new date
                final Calendar cal = PeriodCalendar.from(year, month, day);
                txt_periodStart.setText(SimpleDateFormat.getDateInstance().format(cal.getTime()));
                pref.setPeriodStart(cal);
            }
        });
        // set
        final Calendar cal = pref.getPeriodStart();
        dialog.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        // show
        dialog.show();
    }


    /**
     * Activity resumed, probably after opening usage settings, updates views accordingly.
     */
    @Override
    protected void onResume() {
        super.onResume();
        accumulated.updatePeriod();
        updateViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.updateAllWidgets(this);
    }

    /**
     * After requesting permissions, updates views accordingly.
     *
     * @param i not used
     * @param s not used
     * @param j not used
     */
    @Override
    public void onRequestPermissionsResult(int i, String[] s, int[] j) {
        updateViews();
    }


}
