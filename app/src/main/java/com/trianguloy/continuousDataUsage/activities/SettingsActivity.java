package com.trianguloy.continuousDataUsage.activities;

import android.app.Activity;
import android.app.AppOpsManager;
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
import android.widget.EditText;
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
import java.util.Arrays;
import java.util.Locale;

/**
 * Main activity: settings
 */
public class SettingsActivity extends Activity {

    // used classes
    private Preferences pref = null;

    // variables
    private EditText view_accumulated;


    /**
     * When the activity is created, like a constructor
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
                    Log.d("settings","numberformatexception");
                    e.printStackTrace();
                }
            }
        });

        //firstDay
        Spinner view_firstDay = findViewById(R.id.stt_spn_firstDay);
        final Integer[] days = new Integer[28];
        for (int i = 0; i < 28; i++) {
            days[i] = i + 1;
        }
        view_firstDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, days));
        view_firstDay.setSelection(Arrays.binarySearch(days, pref.getFirstDay()));
        view_firstDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pref.setFirstDay(days[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
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
        final CheckBox view_accumulate = findViewById(R.id.stt_chkBx_accum);
        final View view_ll = findViewById(R.id.ll_accum);
        view_accumulate.setChecked(pref.getAccumulate());
        view_ll.setVisibility(pref.getAccumulate() ? View.VISIBLE : View.GONE);
        view_accumulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pref.setAccumulate(view_accumulate.isChecked());
                view_ll.setVisibility(view_accumulate.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        //accumulated
        view_accumulated = findViewById(R.id.stt_edTxt_accum);
        view_accumulated.setText(String.format(Locale.US, "%s", pref.getAccumulated().first));
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

                    pref.setAccumulated(accum, new PeriodCalendar(pref.getFirstDay()).getCurrentMonth());
                    view_accumulated.setHint(String.format(Locale.US, "%s", accum));
                } catch (ParseException | NullPointerException e) {
                    Log.d("settings","numberformatexception");
                    e.printStackTrace();
                }
            }
        });


        //clickable links
        for (int id : new int[]{R.id.stt_txt_perm_ps,R.id.stt_txt_perm_us} ) {
            ((TextView) findViewById(id)).setMovementMethod(LinkMovementMethod.getInstance());
        }

    }


    /**
     * Checks the permissions and changes the corresponding indicators accordingly (red-green)
     */
    private void checkPermissions() {

        //check readPhoneState
        setPermissionState(R.id.stt_txt_readPhone,checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED );

        //check getUsageStats
        int mode = AppOpsManager.MODE_DEFAULT;
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps != null) {
            //check permission
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        }
        setPermissionState(R.id.stt_txt_usageStats,mode == AppOpsManager.MODE_ALLOWED );

    }


    /**
     * Updates the specified id with the specified state.
     * state=true -> green and 'permission granted'
     * state=false -> red and 'permission needed'
     * @param textView_id id of the textview to update
     * @param state the state
     */
    private void setPermissionState(int textView_id, boolean state) {
        TextView txt = findViewById(textView_id);
        txt.setText(state ? getString(R.string.txt_permissionGranted) : getString(R.string.txt_permissionsNeeded));
        txt.setBackgroundColor(state ? Color.argb(128, 0, 255, 0) : Color.argb(128, 255, 0,0));
    }


    /**
     * Button clicked
     * @param view which button
     */
    public void onButtonClick(View view) {
        switch (view.getId()){
            case R.id.stt_btn_readPhone:
                //request 'read phone permission' button, request permission
                requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 0);//"android.permission.PACKAGE_USAGE_STATS" can't be asked this way
                break;
            case R.id.stt_btn_usageStats:
                //open usage settings to give permission
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                break;

            case R.id.stt_btn_accum:
                //auto-calculate accumulated
                try {
                    view_accumulated.setText(String.format(Locale.US, "%s", new DataUsage(this, pref).calculateAccumulated()));
                }catch(DataUsage.Error e){
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
     * Activity resumed, probably after opening usage settings, updates views accordingly.
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }


    /**
     * After requesting permissions, updates views accordingly.
     * @param i not used
     * @param s not used
     * @param j not used
     */
    @Override
    public void onRequestPermissionsResult(int i, String[] s, int[] j) {
       checkPermissions();
    }


}
