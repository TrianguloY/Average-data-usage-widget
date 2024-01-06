package com.trianguloy.continuousDataUsage.common;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NumericEditText extends EditText {
    private boolean mAllowZero = false;
    private boolean mAllowNegative = false;

    public NumericEditText(Context context) {
        super(context);
    }

    public NumericEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // ------------------- Integer -------------------
    private OnNewIntListener mIntListener = null;

    public void initInt(boolean allowZero, boolean allowNegative, int value, OnNewIntListener listener) {
        mAllowZero = allowZero;
        mAllowNegative = allowNegative;
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // empty = nothing
                if (editable.toString().isEmpty()) return;

                try {
                    int value = NumberFormat.getInstance(Locale.US).parse(editable.toString()).intValue();
                    if ((value > 0) || (mAllowZero && value == 0)) {
                        // valid number
                        setHint(String.format(Locale.US, "%s", value));
                        if (mIntListener != null) mIntListener.newNumber(value);
                    } else {
                        // invalid number
                        setText("");
                    }
                } catch (ParseException | NullPointerException e) {
                    Log.d("settings", "numberformatexception");
                    e.printStackTrace();
                }
            }
        });
        setValue(value);
        mIntListener = listener;
    }

    public void setValue(int value) {
        setText(String.format(Locale.US, "%s", value));
    }

    public interface OnNewIntListener {
        void newNumber(int number);
    }

    // ------------------- Float -------------------

    private OnNewFloatListener mFloatListener = null;

    public void initFloat(boolean allowZero, boolean allowNegative, float initial, OnNewFloatListener listener) {
        mAllowZero = allowZero;
        mAllowNegative = allowNegative;
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // empty = nothing
                if (editable.toString().isEmpty()) return;

                try {
                    float value = NumberFormat.getInstance(Locale.US).parse(editable.toString()).floatValue();
                    if ((mAllowNegative && value < 0) || value > 0 || (mAllowZero && value == 0)) {
                        // valid number
                        setHint(String.format(Locale.US, "%s", value));
                        if (mFloatListener != null) mFloatListener.newNumber(value);
                    } else {
                        // invalid number
                        setText("");
                    }
                } catch (ParseException | NullPointerException e) {
                    Log.d("settings", "numberformatexception");
                    e.printStackTrace();
                }
            }
        });
        setValue(initial);
        mFloatListener = listener;
    }

    public void setValue(float value) {
        setText(String.format(Locale.US, "%s", value));
    }

    public interface OnNewFloatListener {
        void newNumber(float number);
    }
}
