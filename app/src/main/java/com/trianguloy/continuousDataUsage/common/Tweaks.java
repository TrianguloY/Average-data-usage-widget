package com.trianguloy.continuousDataUsage.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.trianguloy.continuousDataUsage.R;

public class Tweaks implements DialogInterface.OnMultiChoiceClickListener {

    private static final String ID_PREFIX = "tweak_";

    private Preferences prefs;
    private Context cntx;

    /**
     * Tweak items.
     * Note: names are used as keys in preferences and as ids in strings (with prefix)
     */
    public enum Items {
        hideDate,
        hideData,
        whiteWidgets,
        showConsumed,
        showAverage,
        showRemaining,
        capNoWarp,
    }

    // ------------------- Tweaks -------------------


    public Tweaks(Preferences prefs, Context cntx) {
        this.prefs = prefs;
        this.cntx = cntx;
    }


    public void showDialog() {

        // get tweaks state
        Items[] tweaks = Items.values();

        CharSequence[] items = new CharSequence[tweaks.length];
        boolean[] checkItems = new boolean[tweaks.length];

        for (int i = 0; i < tweaks.length; i++) {
            Items tweak = tweaks[i];
            // initialize item and append
            items[i] = getItemDescr(tweak);
            checkItems[i] = prefs.getTweak(tweak);
        }

        // do cleanup
        prefs.cleanupTweaks();

        // show
        new AlertDialog.Builder(cntx)
                .setMultiChoiceItems(items, checkItems, this)
                .setTitle(R.string.btn_tweaks)
                .setPositiveButton(cntx.getString(R.string.btn_close),null)
                .show();

    }

    private String getItemDescr(Items item) {
        return cntx.getString(cntx.getResources().getIdentifier(ID_PREFIX + item.name(), "string", cntx.getPackageName()));
    }

    // ------------------- DialogInterface.OnMultiChoiceClickListener -------------------

    @Override
    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
        prefs.setTweak(Items.values()[i], b);
    }
}
