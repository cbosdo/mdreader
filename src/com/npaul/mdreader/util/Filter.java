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
 * Copyright (c) 2014 Cedric Bosdonnat <cedric@bosdonnat.fr>
 */
package com.npaul.mdreader.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface to implement new filters to apply on an HTML string.
 *
 * @author Cedric Bosdonnat <cedric.bosdonnat@free.fr>
 *
 */
public abstract class Filter {

    /**
     * Do the actual filtering.
     *
     * @param in the provided HTML
     * @return the filtered HTML
     */
    public abstract CharSequence filter(CharSequence in);

    protected CharSequence appendToHead(CharSequence in, String content) {
        StringBuffer out = new StringBuffer();

        Matcher m = Pattern.compile("</head>", Pattern.CASE_INSENSITIVE).matcher(in);
        int pos = in.length();
        String toAppend = content;
        if (m.find()) {
            pos = m.start();
        } else {
            m = Pattern.compile("<html>", Pattern.CASE_INSENSITIVE).matcher(in);
            pos = 0;
            if (m.find()) {
                pos = m.end();
            }
            toAppend = "<head>" + content + "</head>";
        }
        out.append(in.subSequence(0, pos));
        out.append(toAppend);
        out.append(in.subSequence(pos, in.length()));

        return out;
    }

    protected CharSequence appendToBody(CharSequence in, String content) {
        StringBuffer out = new StringBuffer();

        Matcher m = Pattern.compile("</body>", Pattern.CASE_INSENSITIVE).matcher(in);
        int pos = in.length();
        if (m.find()) {
            pos = m.start();
        }
        out.append(in.subSequence(0, pos));
        out.append(content);
        out.append(in.subSequence(pos, in.length()));

        return out;
    }
}
