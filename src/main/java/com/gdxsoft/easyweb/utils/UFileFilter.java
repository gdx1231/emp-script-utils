package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Find file filter
 */
public class UFileFilter implements FilenameFilter {

	/**
	 * Get a images filter instance
	 * @return the images filter
	 */
	public static UFileFilter getImageInstance() {
		UFileFilter f = new UFileFilter();
		String[] ff = { ".png", ".gif", ".jpg", ".bmp", ".jiff", ".jpeg" };
		f.addFilter(ff);

		return f;
	}

	/**
	 * Get a zip filter instance
	 * @return the zip filter
	 */
	public static UFileFilter getZipInstance() {
		UFileFilter f = new UFileFilter();
		String[] ff = { ".zip" };
		f.addFilter(ff);

		return f;
	}

	/**
	 * Create a filter from the exts array 
	 * @param ff The filter array
	 * @return the filter
	 */
	public static UFileFilter getInstance(String[] ff) {
		UFileFilter f = new UFileFilter();
		f.addFilter(ff);

		return f;
	}

	private List<String> _Filters = new ArrayList<String>();

	public boolean accept(File arg0, String arg1) {
		if (this._Filters.size() == 0) {
			return true;
		}
		String name = arg1.toLowerCase();
		for (int i = 0; i < this._Filters.size(); i++) {
			if (name.endsWith(this._Filters.get(i))) {
				return true;
			}
		}
		return false;
	}

	public void addFilter(String ext) {
		if (ext == null) {
			return;
		}
		String ext1 = ext.toLowerCase().trim();
		if (ext1.length() == 0) {
			return;
		}

		String s = ext1.substring(0, 1);
		if (!s.equals(".")) {
			ext1 = "." + ext1;
		}

		this._Filters.add(ext1);
	}

	public void addFilter(String[] exts) {
		for (int i = 0; i < exts.length; i++) {
			this.addFilter(exts[i]);
		}
	}

}
