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

import java.util.Vector;

import com.github.rjeschke.txtmark.Processor;

/**
 * Formats the Markdown into HTML and adds some bits to make it more beautiful.
 *
 * @author Cedric Bosdonnat <cedric.bosdonnat@free.fr>
 *
 */
public class Formatter {

    private Vector<Filter> filters;

    public Formatter() {
        filters  = new Vector<Filter>();
    }

    public CharSequence format(String markdown) {
        long start = System.currentTimeMillis();
        CharSequence out = Processor.process(markdown);

        // Apply the filters on the output.
        // Note that the order of the filters is important
        for (Filter filter : filters) {
            out = filter.filter(out);
        }
        long end = System.currentTimeMillis();

        System.out.println("Rendering time (ms): " + (end - start));
        return out;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }
}
