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

import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements
        Checkable {

    boolean mChecked;
    private List<Checkable> mCheckableViews;

    public CheckableRelativeLayout(Context context) {
        super(context);
        mCheckableViews = new Vector<Checkable>();
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCheckableViews = new Vector<Checkable>();
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        mCheckableViews = new Vector<Checkable>();
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        for (Checkable c : mCheckableViews) {
            c.setChecked(checked);
        }
    }

    @Override
    public void toggle() {
        mChecked = !mChecked;
        for (Checkable c : mCheckableViews) {
            c.toggle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            findCheckableChildren(this.getChildAt(i));
        }
    }

    /**
     * Add to our checkable list all the children of the view that implement the
     * interface Checkable
     */
    private void findCheckableChildren(View v) {
        if (v instanceof Checkable) {
            mCheckableViews.add((Checkable) v);
        }
        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            final int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                findCheckableChildren(vg.getChildAt(i));
            }
        }
    }
}
