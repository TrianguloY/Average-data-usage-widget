package com.trianguloy.continuousDataUsage.common;

public class Utils {
    /**
     * Rounds double to int
     *
     * @param d double value
     * @return round(d) as int
     */
    public static int dbl2int(double d) {
        return Math.round(Math.round(d));
    }
}
