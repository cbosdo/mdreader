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
 * Copyright (c) 2014 Cedric Bosdonnat <cedric.bosdonnat@free.fr>
 */
package com.npaul.mdreader.util;

public class StyleFilter extends Filter {

    @Override
    public CharSequence filter(CharSequence in) {
        String css = "<style>"
                + "p {"
                + "    font-size: 2em;"
                + "}"
                + "h1 {"
                + "    font-size: 2.5em;"
                + "}"
                + "</style>";

        return appendToHead(in, css);
    }

}
