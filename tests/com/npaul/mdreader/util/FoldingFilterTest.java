package com.npaul.mdreader.util;

import org.junit.Assert;
import org.junit.Test;

public class FoldingFilterTest {

    @Test
    public void testAddContentDivsSimple() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<h1>Title</h1>"
                  + "<p>some content</p><p>split in paragraphs</p>";
        String expected = "<h1>Title</h1>"
                        + "<div class=\"content\">"
                        + "<p>some content</p><p>split in paragraphs</p>"
                        + "</div>";

        String actual = sut.addContentDivs(in).toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAddContentDivsNocontent() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<h1>Title</h1>";
        String expected = "<h1>Title</h1>"
                        + "<div class=\"content\">"
                        + "</div>";

        String actual = sut.addContentDivs(in).toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAddContentDivsMultiple() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<h1>Title</h1>"
                  + "<p>some content</p><p>split in paragraphs</p>"
                  + "<h1>Other title</h1>"
                  + "<p>some other content</p>";
        String expected = "<h1>Title</h1>"
                        + "<div class=\"content\">"
                        + "<p>some content</p><p>split in paragraphs</p>"
                        + "</div>"
                        + "<h1>Other title</h1>"
                        + "<div class=\"content\">"
                        + "<p>some other content</p>"
                        + "</div>";

        String actual = sut.addContentDivs(in).toString();
        Assert.assertEquals(expected, actual);
    }

}
