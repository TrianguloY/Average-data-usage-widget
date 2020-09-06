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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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
import com.trianguloy.continuousDataUsage.common.DataUsage;
import com.trianguloy.continuousDataUsage.common.PeriodCalendar;
import com.trianguloy.continuousDataUsage.common.Preferences;
import com.trianguloy.continuousDataUsage.common.Tweaks;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Main activity: settings
 */
public class SettingsActivity extends Activity {

    // used classes
    private Preferences pref = null;

    // variables
    private EditText view_accumulated;
    private TextView view_txt_decimals;
    private TextView view_txt_savedPeriods;
    private EditText txt_periodStart;


    /**
     * When the activity is created, like a constructor
     *
     * @param savedInstanceState previous saved state, used on super only
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get prefs
        pref = new Preferences(this);

        //initializes
        initialize();
        checkPermissions();

    }


    /**
     * Initialize all the views
     */
    private void initialize() {

        //totaldata
        final EditText view_totalData = findViewById(R.id.stt_edTxt_totalData);
        view_totalData.setText(String.format(Locale.US, "%s", pref.getTotalData()));
        view_totalData.setHint(view_totalData.getText());
        view_totalData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    float totalData = NumberFormat.getInstance(Locale.US).parse(editable.toString()).floatValue();
                    if (totalData > 0) {
                        //valid total data, save
                        pref.setTotalData(totalData);
                        view_totalData.setHint(String.format(Locale.US, "%s", totalData));
                    }
                } catch (ParseException | NullPointerException e) {
                    Log.d("settings", "numberformatexception");
                    e.printStackTrace();
                }
            }
        });

        // periodStart
        txt_periodStart = findViewById(R.id.stt_txt_periodStart);
        final Calendar periodStart = pref.getPeriodStart();
        txt_periodStart.setText(SimpleDateFormat.getDateInstance().format(periodStart.getTime()));

        // period amount
        final EditText txt_periodLength = findViewById(R.id.stt_txt_periodLength);
        txt_periodLength.setText(String.format(Locale.US, "%s", pref.getPeriodLength()));
        txt_periodLength.setHint(txt_periodLength.getText());
        txt_periodLength.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int length = NumberFormat.getInstance(Locale.US).parse(editable.toString()).intValue();
                    if (length > 0) {
                        //valid total data, save
                        pref.setPeriodLength(length);
                        txt_periodLength.setHint(String.format(Locale.US, "%s", length));
                    }
                } catch (ParseException | NullPointerException e) {
                    Log.d("settings", "numberformatexception");
                    e.printStackTrace();
                }
            }
        });

        // period type
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

        //alternate conversion
        final CheckBox view_alternateConversion = findViewById(R.id.stt_chkBx_alternateConversion);
        view_alternateConversion.setChecked(pref.getAltConversion());
        view_alternateConversion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pref.setAltConversion(view_alternateConversion.isChecked());
            }
        });

        //accumulate
        SeekBar view_sb_savedPeriods = findViewById(R.id.stt_sb_savedPeriods);
        view_txt_savedPeriods = findViewById(R.id.stt_txt_savedPeriods);
        final View view_ll = findViewById(R.id.ll_accum);
        view_sb_savedPeriods.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                view_txt_savedPeriods.setText(Integer.toString(i));
                view_ll.setVisibility(i > 0 ? View.VISIBLE : View.GONE);
                pref.setSavedPeriods(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        view_sb_savedPeriods.setProgress(pref.getSavedPeriods(), false); // setProgress may not update if the value is the same
        view_txt_savedPeriods.setText(Integer.toString(pref.getSavedPeriods()));
        view_ll.setVisibility(pref.getSavedPeriods() > 0 ? View.VISIBLE : View.GONE);

        //accumulated
        view_accumulated = findViewById(R.id.stt_edTxt_accum);
        view_accumulated.setText(String.format(Locale.US, "%s", pref.getAccumulated()));
        view_accumulated.setHint(view_accumulated.getText());
        view_accumulated.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    float accum = NumberFormat.getInstance(Locale.US).parse(editable.toString()).floatValue();

                    pref.setAccumulated(accum);
                    view_accumulated.setHint(String.format(Locale.US, "%s", accum));
                } catch (ParseException | NullPointerException e) {
                    Log.d("settings", "numberformatexception");
                    e.printStackTrace();
                }
            }
        });

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

        //clickable links
        for (int id : new int[]{R.id.stt_txt_perm_ps, R.id.stt_txt_perm_us}) {
            ((TextView) findViewById(id)).setMovementMethod(LinkMovementMethod.getInstance());
        }

    }


    /**
     * Checks the permissions and changes the corresponding indicators accordingly (red-green)
     */
    private void checkPermissions() {

        //check readPhoneState
        setPermissionState(R.id.stt_txt_readPhone, checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED);

        //check getUsageStats
        int mode = AppOpsManager.MODE_DEFAULT;
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps != null) {
            //check permission
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        }
        setPermissionState(R.id.stt_txt_usageStats, mode == AppOpsManager.MODE_ALLOWED);

    }


    /**
     * Updates the specified id with the specified state.
     * state=true -> green and 'permission granted'
     * state=false -> red and 'permission needed'
     *
     * @param textView_id id of the textview to update
     * @param state       the state
     */
    private void setPermissionState(int textView_id, boolean state) {
        TextView txt = findViewById(textView_id);
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
            case R.id.stt_btn_readPhone:
                //request 'read phone permission' button, request permission
                requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 0);//"android.permission.PACKAGE_USAGE_STATS" can't be asked this way
                break;
            case R.id.stt_btn_usageStats:
                //open usage settings to give permission
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                break;

            case R.id.stt_txt_periodStart:
                // start date picker
                pickPeriodStart();
                break;

            case R.id.stt_btn_accum:
                //auto-calculate accumulated
                try {
                    view_accumulated.setText(String.format(Locale.US, "%s", new DataUsage(this, pref).autoCalculateAccumulated()));
                } catch (DataUsage.Error e) {
                    Toast.makeText(this, getString(e.errorId), Toast.LENGTH_LONG).show();
                }
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
        checkPermissions();
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
        checkPermissions();
    }


}
