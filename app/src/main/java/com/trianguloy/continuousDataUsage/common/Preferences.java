package com.trianguloy.continuousDataUsage.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

/**
 * Utility class to manage the shared preferences
 */

public class Preferences {


    /**
     * SharedPrefs
     */
    private static final String PREF_NAME = "pref";
    private SharedPreferences sharedPreferences;
    public Preferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Preferences.PREF_NAME, Context.MODE_PRIVATE);
    }


    /**
     * Total data (float > 0)
     */
    private static final String KEY_TOTALDATA = "totalData";
    private static final float DEFAULT_TOTALDATA = 1024;
    public float getTotalData(){
        return sharedPreferences.getFloat(KEY_TOTALDATA,DEFAULT_TOTALDATA);
    }
    public void setTotalData(float totalData){
        sharedPreferences.edit().putFloat(KEY_TOTALDATA,totalData).apply();
    }


    /**
     * Alternative conversion (boolean)
     */
    private static final String KEY_ALTCONVERSION = "altConversion";
    private static final boolean DEFAULT_ALTCONVERSION = false;
    public boolean getAltConversion(){
        return sharedPreferences.getBoolean(KEY_ALTCONVERSION,DEFAULT_ALTCONVERSION);
    }
    public void setAltConversion(boolean altConversion){
        sharedPreferences.edit().putBoolean(KEY_ALTCONVERSION,altConversion).apply();
    }


    /**
     * First day of period (int [1,28])
     */
    private static final String KEY_FIRSTDAY = "firstDay";
    private static final int DEFAULT_FIRSTDAY = 1;
    public int getFirstDay(){
        return sharedPreferences.getInt(KEY_FIRSTDAY,DEFAULT_FIRSTDAY);
    }
    public void setFirstDay(int firstDay){
        sharedPreferences.edit().putInt(KEY_FIRSTDAY,firstDay).apply();
    }
    
    /**
     * Information requested flag. Cleared when read.
     */
    private static final String KEY_INFOREQUESTED = "infoRequested";
    public void infoRequested() {
        sharedPreferences.edit().putBoolean(KEY_INFOREQUESTED,true).apply();
    }
    public boolean isInfoRequested(){
        boolean b = sharedPreferences.getBoolean(KEY_INFOREQUESTED, false);
        sharedPreferences.edit().remove(KEY_INFOREQUESTED).apply();
        return b;
    }

    /**
     * Use accumulated data (boolean)
     */
    private static final String KEY_ACCUMULATE = "accumulate";
    private static final boolean DEFAULT_ACCUMULATE = false;
    public boolean getAccumulate(){
        return sharedPreferences.getBoolean(KEY_ACCUMULATE,DEFAULT_ACCUMULATE);
    }
    public void setAccumulate(boolean accumulate){
        sharedPreferences.edit().putBoolean(KEY_ACCUMULATE,accumulate).apply();
    }

    /**
     * Accumulated data (float)
     * and
     * Month (integer)
     */
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final String KEY_ACCUMULATEDM = "accumulatedm";
    private static final float DEFAULT_ACCUMULATED = 0;
    private static final int DEFAULT_ACCUMULATEDM = 0;
    public Pair<Float, Integer> getAccumulated(){
        return new Pair<>(
                sharedPreferences.getFloat(KEY_ACCUMULATED, DEFAULT_ACCUMULATED),
                sharedPreferences.getInt(KEY_ACCUMULATEDM, DEFAULT_ACCUMULATEDM)
        );
    }
    public void setAccumulated(float accumulated, int month){
        sharedPreferences.edit()
                .putFloat(KEY_ACCUMULATED, accumulated)
                .putInt(KEY_ACCUMULATEDM, month)
                .apply();
    }
}
