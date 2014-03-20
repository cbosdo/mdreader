/*
 * This software is provided under a Creative Commons Attribution-NonCommercial-
 * ShareAlike 3.0 Unported license.
 *
 * You are free to Share and Remix this software, as long as you attribute the
 * original owner, and release it under the same license. You may not use this
 * work for commercial purposes.
 *
 * Full license available at: http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode
 *
 * Copyright (c) 2014 Cedric Bosdonnat
 */
package com.npaul.mdreader.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CheckBox;

/**
 * Checkbox that doesn't react to touch events.
 *
 * @author Cedric Bosdonnat <cedric.bosdonnat@free.fr>
 *
 */
public class InactiveCheckBox extends CheckBox {

    public InactiveCheckBox(Context context) {
        super(context);
    }

    public InactiveCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InactiveCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Just don't handle the touch event in this control
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
