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