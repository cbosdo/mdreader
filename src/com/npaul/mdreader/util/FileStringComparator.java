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
 * Copyright (c) 2013 Nathan Paul
 */
package com.npaul.mdreader.util;

import java.io.File;
import java.util.Comparator;
import java.util.Locale;

/**
 * Used to compare 2 file objects and sort them into alphabetical order
 *
 * @author Nathan Paul
 * @version 1.0
 */
public class FileStringComparator implements Comparator<File> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(File lhs, File rhs) {
        String s1 = lhs.getName(),s2 = rhs.getName();
        //we need to use Locale.ENGLISH to ensure that international users get their files sorted in an English fashion
        return s1.toLowerCase(Locale.ENGLISH).compareTo(s2.toLowerCase(Locale.ENGLISH));
    }

}
