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

    @Test
    public void testAppendToBodySimple() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<html><body></body></html>";
        String expected = "<html><body>APPENDED</body></html>";

        String actual = sut.appendToBody(in, "APPENDED").toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendToBodyNoclosing() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<p>text</p>";
        String expected = "<p>text</p>APPENDED";

        String actual = sut.appendToBody(in, "APPENDED").toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendToHeadSimple() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<html><head></head><body></body></html>";
        String expected = "<html><head>APPENDED</head><body></body></html>";

        String actual = sut.appendToHead(in, "APPENDED").toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendToHeadNohead() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<html><body></body></html>";
        String expected = "<html><head>APPENDED</head><body></body></html>";

        String actual = sut.appendToHead(in, "APPENDED").toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendToHeadNohtml() {
        FoldingFilter sut = new FoldingFilter();

        String in = "<body></body>";
        String expected = "<head>APPENDED</head><body></body>";

        String actual = sut.appendToHead(in, "APPENDED").toString();
        Assert.assertEquals(expected, actual);
    }
}
