package com.trianguloy.continuousDataUsage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to manage the shared preferences
 */

class Preferences {


    /**
     * SharedPrefs
     */
    private static final String PREF_NAME = "pref";
    private SharedPreferences sharedPreferences;
    Preferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Preferences.PREF_NAME, Context.MODE_PRIVATE);
    }


    /**
     * Total data (float > 0)
     */
    private static final String KEY_TOTALDATA = "totalData";
    private static final float DEFAULT_TOTALDATA = 1024;
    float getTotalData(){
        return sharedPreferences.getFloat(KEY_TOTALDATA,DEFAULT_TOTALDATA);
    }
    void setTotalData(float totalData){
        sharedPreferences.edit().putFloat(KEY_TOTALDATA,totalData).apply();
    }


    /**
     * Alternative conversion (boolean)
     */
    private static final String KEY_ALTCONVERSION = "altConversion";
    private static final boolean DEFAULT_ALTCONVERSION = false;
    boolean getAltConversion(){
        return sharedPreferences.getBoolean(KEY_ALTCONVERSION,DEFAULT_ALTCONVERSION);
    }
    void setAltConversion(boolean altConversion){
        sharedPreferences.edit().putBoolean(KEY_TOTALDATA,altConversion).apply();
    }


    /**
     * First day of period (int [1,31])
     */
    private static final String KEY_FIRSTDAY = "firstDay";
    private static final int DEFAULT_FIRSTDAY = 1;
    int getFirstDay(){
        return sharedPreferences.getInt(KEY_FIRSTDAY,DEFAULT_FIRSTDAY);
    }
    void setFirstDay(int firstDay){
        sharedPreferences.edit().putInt(KEY_FIRSTDAY,firstDay).apply();
    }


}
