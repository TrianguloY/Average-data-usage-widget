package com.trianguloy.continuousDataUsage.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    public float getTotalData() {
        return sharedPreferences.getFloat(KEY_TOTALDATA, DEFAULT_TOTALDATA);
    }

    public void setTotalData(float totalData) {
        sharedPreferences.edit().putFloat(KEY_TOTALDATA, totalData).apply();
    }


    /**
     * Alternative conversion (boolean)
     */
    private static final String KEY_ALTCONVERSION = "altConversion";
    private static final boolean DEFAULT_ALTCONVERSION = false;

    public boolean getAltConversion() {
        return sharedPreferences.getBoolean(KEY_ALTCONVERSION, DEFAULT_ALTCONVERSION);
    }

    public void setAltConversion(boolean altConversion) {
        sharedPreferences.edit().putBoolean(KEY_ALTCONVERSION, altConversion).apply();
    }


    /**
     * First day of period (int [1,28])
     */
    private static final String KEY_FIRSTDAY = "firstDay";
    private static final int DEFAULT_FIRSTDAY = 1;

    public int getFirstDay() {
        return sharedPreferences.getInt(KEY_FIRSTDAY, DEFAULT_FIRSTDAY);
    }

    public void setFirstDay(int firstDay) {
        sharedPreferences.edit().putInt(KEY_FIRSTDAY, firstDay).apply();
    }

    /**
     * Information requested flag. Cleared when read.
     */
    private static final String KEY_INFOREQUESTED = "infoRequested";

    public void infoRequested() {
        sharedPreferences.edit().putBoolean(KEY_INFOREQUESTED, true).apply();
    }

    public boolean isInfoRequested() {
        boolean b = sharedPreferences.getBoolean(KEY_INFOREQUESTED, false);
        sharedPreferences.edit().remove(KEY_INFOREQUESTED).apply();
        return b;
    }

    /**
     * Use accumulated data (boolean)
     */
    private static final String KEY_ACCUMULATE = "accumulate";
    private static final boolean DEFAULT_ACCUMULATE = false;

    public boolean getAccumulate() {
        return sharedPreferences.getBoolean(KEY_ACCUMULATE, DEFAULT_ACCUMULATE);
    }

    public void setAccumulate(boolean accumulate) {
        sharedPreferences.edit().putBoolean(KEY_ACCUMULATE, accumulate).apply();
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

    public Pair<Float, Integer> getAccumulated() {
        return new Pair<>(
                sharedPreferences.getFloat(KEY_ACCUMULATED, DEFAULT_ACCUMULATED),
                sharedPreferences.getInt(KEY_ACCUMULATEDM, DEFAULT_ACCUMULATEDM)
        );
    }

    public void setAccumulated(float accumulated, int month) {
        sharedPreferences.edit()
                .putFloat(KEY_ACCUMULATED, accumulated)
                .putInt(KEY_ACCUMULATEDM, month)
                .apply();
    }

    /**
     * Decimals (int)
     */
    private static final String KEY_DECIMALS = "decimals";
    private static final int DEFAULT_DECIMALS = 2;

    public int getDecimals() {
        return sharedPreferences.getInt(KEY_DECIMALS, DEFAULT_DECIMALS);
    }

    public String getDecimalsFormatter(){
        return "%."+getDecimals()+"f";
    }

    public void setDecimals(int decimals) {
        sharedPreferences.edit().putInt(KEY_DECIMALS, decimals).apply();
    }

    // ------------------- tweaks -------------------

    /**
     * Tweaks
     */
    private static final String KEY_TWEAKS = "tweaks";
    private final Set<String> DEFAULT_TWEAKS = Collections.emptySet();

    public boolean getTweak(Tweaks.Items tweak) {
        return sharedPreferences.getBoolean(tweak.name(), false);
    }

    public void setTweak(Tweaks.Items tweak, boolean enabled) {
        sharedPreferences.edit().putBoolean(tweak.name(), enabled).apply();
    }

    public void cleanupTweaks() {
        SharedPreferences.Editor edit = sharedPreferences.edit();

        // remove old tweaks:
        for (String name : sharedPreferences.getStringSet(KEY_TWEAKS, DEFAULT_TWEAKS)) {
            // check every saved tweak
            try {
                // check if still exists
                Tweaks.Items.valueOf(name);
            } catch (IllegalArgumentException e) {
                // if not, remove
                edit.remove(name);
            }
        }

        // save current tweaks list:
        Tweaks.Items[] items = Tweaks.Items.values();
        Set<String> names = new HashSet<>(items.length);
        for (Tweaks.Items tweak : items) {
            // add current tweak name to list
            names.add(tweak.name());
        }
        edit.putStringSet(KEY_TWEAKS, names);

        // save
        edit.apply();
    }
}
