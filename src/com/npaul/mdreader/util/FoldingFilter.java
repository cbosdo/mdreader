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
 * Use JQuery to add folding feature to titles.
 *
 * This aims at easing the readability of long articles on mobile devices.
 * All content will be folded be default and only H1 titles will be visible.
 * Clicking on a title will unfold its content.
 *
 * @author Cedric Bosdonnat <cedric.bosdonnat@free.fr>
 *
 */
public class FoldingFilter implements Filter {

    @Override
    public CharSequence filter(CharSequence in) {
        if (in == null)
            return in;

        CharSequence out = addContentDivs(in);
        out = includeCss(out);
        out = includeJQuery(out);

        String script = "<script>"
                      + "$(\"h1\").click(function() {"
                      + "    var content = $(this).get(0).nextElementSibling;"
                      + "    $(content).slideToggle(\"fast\");"
                      + "    $(this).get(0).classList.toggle(\"open_section\");"
                      + "});"
                      + "$(\".content\").hide()"
                      + "</script>";
        out = appendToBody(out, script);
        return out;
    }

    protected CharSequence addContentDivs(CharSequence in) {
        StringBuffer out = new StringBuffer();

        Matcher endMatch = Pattern.compile("</h1>", Pattern.CASE_INSENSITIVE).matcher(in);
        Matcher startMatch = Pattern.compile("<h1>", Pattern.CASE_INSENSITIVE).matcher(in);
        int pos = 0;
        while (pos < in.length()) {
            if (endMatch.find(pos)) {
                int start = endMatch.end();
                int end = in.length();
                out.append(in.subSequence(pos, start));
                out.append("<div class=\"content\">");
                if (startMatch.find(start)) {
                    end = startMatch.start();
                }
                out.append(in.subSequence(start, end));
                out.append("</div>");
                pos = end;
            } else {
                out.append(in.subSequence(pos, in.length()));
                pos = in.length();
            }
        }

        return out;
    }

    protected CharSequence includeCss(CharSequence in) {
        String css = "<style>"
                   + "h1 {"
                   + "    background: url(\"file:///android_asset/show.png\") no-repeat scroll left center rgba(0, 0, 0, 0);"
                   + "    border-bottom: 1px solid #E2E3E4;"
                   + "    padding-left: 26px;"
                   + "    position: relative;"
                   + "    line-height: 1.3;"
                   + "}"
                   + "h1.open_section {"
                   + "    background: url(\"file:///android_asset/hide.png\") no-repeat scroll left center rgba(0, 0, 0, 0);"
                   + "}"
                   + "</style>";

        return appendToHead(in, css);
    }

    protected CharSequence includeJQuery(CharSequence in) {
        String include = "<script src=\"file:///android_asset/jquery-2.1.0.min.js\"></script>";
        if (in.toString().indexOf(include) == -1)
            return appendToBody(in, include);
        return in;
    }

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
