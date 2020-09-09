package com.trianguloy.continuousDataUsage.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
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

        // updates
        final String KEY_ACCUMULATE = "accumulate";
        if (sharedPreferences.contains(KEY_ACCUMULATE)) {
            // accumulate=true/false -> savedPeriods=1/0
            setSavedPeriods(sharedPreferences.getBoolean(KEY_ACCUMULATE, false) ? 1 : 0);
            sharedPreferences.edit().remove(KEY_ACCUMULATE).apply();
        }

        final String KEY_FIRSTDAY = "firstDay";
        if (sharedPreferences.contains(KEY_FIRSTDAY)) {
            // firstDay=n -> periodStart=today(day=n)
            Calendar cal = PeriodCalendar.today();
            cal.set(Calendar.DAY_OF_MONTH, sharedPreferences.getInt(KEY_FIRSTDAY, 0));
            if (System.currentTimeMillis() < cal.getTimeInMillis()) {
                cal.add(Calendar.MONTH, -1);
            }
            setPeriodStart(cal);
            sharedPreferences.edit().remove(KEY_FIRSTDAY).apply();
        }

        final String KEY_ACCUMULATEDM = "accumulatedm";
        if (sharedPreferences.contains(KEY_ACCUMULATEDM)) {
            // accumulated month -> shift current month
            int diff = (getPeriodStart().get(Calendar.MONTH) - sharedPreferences.getInt(KEY_ACCUMULATEDM, 0) + 12) % 12;
            if (diff != 0) {
                Calendar cal = getPeriodStart();
                cal.add(Calendar.MONTH, -diff);
                setPeriodStart(cal);
            }
            sharedPreferences.edit().remove(KEY_ACCUMULATEDM).apply();
        }

//        sharedPreferences.edit().clear().apply();
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
     * Start date for current period (inclusive)
     */
    private static final String KEY_PERIODSTART = "periodStart";

    public Calendar getPeriodStart() {
        Calendar cal;
        final long millis = sharedPreferences.getLong(KEY_PERIODSTART, -1);
        if (millis != -1) {
            cal = Calendar.getInstance();
            cal.setTimeInMillis(millis);
        } else {
            // DEFAULT: first day of current month
            cal = PeriodCalendar.today();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            setPeriodStart(cal); // save, otherwise it will never be updated
        }
        return cal;
    }

    public void setPeriodStart(Calendar periodStart) {
        sharedPreferences.edit().putLong(KEY_PERIODSTART, periodStart.getTimeInMillis()).apply();
    }


    /**
     * Length of current period (int)
     * See Period
     */
    private static final String KEY_PERIODLENGTH = "periodLength";
    private static final int DEFAULT_PERIODLENGTH = 1;

    public int getPeriodLength() {
        return sharedPreferences.getInt(KEY_PERIODLENGTH, DEFAULT_PERIODLENGTH);
    }

    public void setPeriodLength(int periodLength) {
        sharedPreferences.edit().putInt(KEY_PERIODLENGTH, periodLength).apply();
    }

    /**
     * Type of current period (month or days)
     * See Length
     */
    private static final String KEY_PERIODTYPE = "periodType";
    private static final int DEFAULT_PERIODTYPE = Calendar.MONTH;

    public int getPeriodType() {
        return sharedPreferences.getInt(KEY_PERIODTYPE, DEFAULT_PERIODTYPE);
    }

    public void setPeriodtype(int periodType) {
        sharedPreferences.edit().putInt(KEY_PERIODTYPE, periodType).apply();
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
     * Saved Periods (int)
     */
    private static final String KEY_SAVEDPERIODS = "savedPeriods";
    private static final int DEFAULT_SAVEDPERIODS = 0;

    public int getSavedPeriods() {
        return sharedPreferences.getInt(KEY_SAVEDPERIODS, DEFAULT_SAVEDPERIODS);
    }

    public void setSavedPeriods(int savedPeriods) {
        sharedPreferences.edit().putInt(KEY_SAVEDPERIODS, savedPeriods).apply();
    }

    /**
     * Accumulated data of current period (float)
     */
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final float DEFAULT_ACCUMULATED = 0;

    public Float getAccumulated() {
        return sharedPreferences.getFloat(KEY_ACCUMULATED, DEFAULT_ACCUMULATED);
    }

    public void setAccumulated(float accumulated) {
        sharedPreferences.edit().putFloat(KEY_ACCUMULATED, accumulated).apply();
    }

    /**
     * Decimals (int)
     */
    private static final String KEY_DECIMALS = "decimals";
    private static final int DEFAULT_DECIMALS = 2;

    public int getDecimals() {
        return sharedPreferences.getInt(KEY_DECIMALS, DEFAULT_DECIMALS);
    }

    public void setDecimals(int decimals) {
        sharedPreferences.edit().putInt(KEY_DECIMALS, decimals).apply();
    }

    /**
     * Gigabytes (boolean)
     */
    private static final String KEY_GB = "gb";
    private static final boolean DEFAULT_GB = false;

    public boolean getGB() {
        return sharedPreferences.getBoolean(KEY_GB, DEFAULT_GB);
    }

    public void setGB(boolean gb) {
        sharedPreferences.edit().putBoolean(KEY_GB, gb).apply();
    }

    // ------------------- tweaks -------------------

    /**
     * Tweaks
     */
    private static final String KEY_TWEAKS = "tweaks";
    private final Set<String> DEFAULT_TWEAKS = Collections.emptySet();

    public boolean getTweak(Tweaks.Tweak tweak) {
        return sharedPreferences.getBoolean(tweak.name(), false);
    }

    public void setTweak(Tweaks.Tweak tweak, boolean enabled) {
        sharedPreferences.edit().putBoolean(tweak.name(), enabled).apply();
    }

    public void cleanupTweaks() {
        SharedPreferences.Editor edit = sharedPreferences.edit();

        // remove old tweaks:
        for (String name : sharedPreferences.getStringSet(KEY_TWEAKS, DEFAULT_TWEAKS)) {
            // check every saved tweak
            try {
                // check if still exists
                Tweaks.Tweak.valueOf(name);
            } catch (IllegalArgumentException e) {
                // if not, remove
                edit.remove(name);
            }
        }

        // save current tweaks list:
        Tweaks.Tweak[] items = Tweaks.Tweak.values();
        Set<String> names = new HashSet<>(items.length);
        for (Tweaks.Tweak tweak : items) {
            // add current tweak name to list
            names.add(tweak.name());
        }
        edit.putStringSet(KEY_TWEAKS, names);

        // save
        edit.apply();
    }
}
