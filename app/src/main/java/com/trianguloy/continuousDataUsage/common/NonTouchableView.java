package com.trianguloy.continuousDataUsage.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * A view that removes all touches
 */
public class NonTouchableView extends FrameLayout {

    public NonTouchableView(Context context) {
        super(context);
    }

    public NonTouchableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonTouchableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NonTouchableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev){
        return true;
    }
}
