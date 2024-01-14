package com.trianguloy.continuousDataUsage.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private TextView txt_notif;
    private LinearLayout ll_notif;
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
        txt_notif = findViewById(R.id.stt_txt_notif);
        ll_notif = findViewById(R.id.ll_notif);
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
        view_totalData.initFloat(false, false, pref.getTotalData(), number -> pref.setTotalData(number));

        // period length, amount
        final NumericEditText txt_periodLength = findViewById(R.id.stt_edTxt_periodLength);
        txt_periodLength.initInt(false, false, pref.getPeriodLength(), number -> pref.setPeriodLength(number));

        // period length, type
        final Spinner spn_periodType = findViewById(R.id.stt_spn_periodType);
        final List<Integer> periodTypes = Arrays.asList(Calendar.DAY_OF_MONTH, Calendar.MONTH);
        spn_periodType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{getString(R.string.days), getString(R.string.months)}));
        spn_periodType.setSelection(periodTypes.indexOf(pref.getPeriodType()));
        spn_periodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pref.setPeriodType(periodTypes.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                pref.setPeriodType(periodTypes.get(0));
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
        view_gb.setOnCheckedChangeListener((compoundButton, b) -> pref.setGB(b));

        //alternate conversion
        final CheckBox view_alternateConversion = findViewById(R.id.stt_chkBx_alternateConversion);
        view_alternateConversion.setChecked(pref.getAltConversion());
        view_alternateConversion.setOnClickListener(view -> pref.setAltConversion(view_alternateConversion.isChecked()));

        // accumulated periods
        final NumericEditText view_sb_savedPeriods = findViewById(R.id.stt_edTxt_savedPeriods);
        view_sb_savedPeriods.initInt(true, false, pref.getSavedPeriods(), number -> pref.setSavedPeriods(number));

        // accumulated megas
        view_accumulated = findViewById(R.id.stt_edTxt_accum);
        view_accumulated.initFloat(true, true, pref.getAccumulated(), number -> pref.setAccumulated(number));

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
            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        }
        setPermissionState(txt_usageStats, mode == AppOpsManager.MODE_ALLOWED, true);

        // notifications permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setPermissionState(txt_notif, checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED, false);
            ll_notif.setVisibility(View.VISIBLE);
        } else {
            ll_notif.setVisibility(View.GONE);
        }
    }


    /**
     * Updates the specified id with the specified state.
     * state=true -> green and 'permission granted'
     * state=false && !required -> orange and 'permission not granted'
     * state=false && required -> red and 'permission needed'
     */
    private void setPermissionState(TextView txt, boolean granted, boolean required) {
        txt.setText(granted ? getString(R.string.txt_permissionGranted)
                : required ? getString(R.string.txt_permissionNeeded)
                : getString(R.string.txt_permissionNotGranted));
        txt.setBackgroundColor(granted ? Color.argb(128, 0, 255, 0) // green
                : required ? Color.argb(128, 255, 0, 0) // orange
                : Color.argb(128, 255, 127, 0)); // red
    }


    /**
     * Button clicked
     *
     * @param view which button
     */
    public void onClick(View view) {
        switch (view.getId()) {
            //open usage settings to give permission
            case R.id.stt_btn_usageStats -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            //request 'notifications permission' button, request permission
            case R.id.stt_btn_notif -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
            // start date picker
            case R.id.stt_edTxt_periodStart -> pickPeriodStart();
            case R.id.stt_btn_accum -> {
                //auto-calculate accumulated
                calculate();
            }
            // show tweaks
            case R.id.stt_btn_tweaks -> new Tweaks(pref, this).showDialog();
        }
    }

    /**
     * Allows the user to choose how to calculate the accumulated data
     */
    private void calculate() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_settings_accum)
                .setItems(R.array.itms_settings_accum, (dialog, which) -> {
                    try {
                        switch (which) {
                            case 0 -> {
                                view_accumulated.setValue((float) accumulated.autoCalculateAccumulated());

                                // update if necessary
                                txt_periodStart.setText(SimpleDateFormat.getDateInstance().format(pref.getPeriodStart().getTime()));
                            }
                            case 1 -> setVisibleData();
                        }
                    } catch (DataUsage.Error e) {
                        Toast.makeText(this, getString(e.errorId), Toast.LENGTH_LONG).show();
                    }
                })
                .show();


    }

    /**
     * Calculates the accumulated data based on the visible amount
     */
    private void setVisibleData() throws DataUsage.Error {

        double data = accumulated.getUsedDataFromCurrentPeriod();

        if (pref.getTweak(Tweaks.Tweak.showRemaining)) {
            data = pref.getTotalData() - data;
        }

        final float[] input = {(float) data};
        NumericEditText txt = new NumericEditText(this);
        txt.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        txt.initFloat(true, true, input[0], number -> input[0] = number);
        new AlertDialog.Builder(this)
                .setTitle(R.string.txt_visible_title)
                .setMessage(R.string.txt_visible_message)
                .setView(txt)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    if (pref.getTweak(Tweaks.Tweak.showRemaining)) {
                        input[0] = pref.getTotalData() - input[0];
                    }

                    try {
                        view_accumulated.setValue((float) accumulated.setUsedDataFromCurrentPeriod(input[0]));
                    } catch (DataUsage.Error e) {
                        Toast.makeText(this, getString(e.errorId), Toast.LENGTH_LONG).show();
                    }

                    // update if necessary
                    txt_periodStart.setText(SimpleDateFormat.getDateInstance().format(pref.getPeriodStart().getTime()));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Shows a date dialog to change the start date
     */
    private void pickPeriodStart() {
        final DatePickerDialog dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener((datePicker, year, month, day) -> {
            // new date
            final Calendar cal = PeriodCalendar.from(year, month, day);
            txt_periodStart.setText(SimpleDateFormat.getDateInstance().format(cal.getTime()));
            pref.setPeriodStart(cal);
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
     */
    @Override
    public void onRequestPermissionsResult(int i, String[] s, int[] j) {
        updateViews();
    }

}
