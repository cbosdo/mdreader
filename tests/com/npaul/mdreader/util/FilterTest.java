package com.npaul.mdreader.util;

import org.junit.Assert;
import org.junit.Test;

public class FilterTest {

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
