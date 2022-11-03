package com.gdxsoft.easyweb.utils;

import java.util.List;

public class UCheckerIn {
	public static boolean equals(String source, List<String> equalsList, boolean ignoreCase) {
		String[] arr = new String[equalsList.size()];
		equalsList.toArray(arr);

		return equals(source, arr, ignoreCase);
	}

	public static boolean equals(String source, String equalsList, boolean ignoreCase) {
		String[] arr = Utils.splitString(equalsList, ",");
		return equals(source, arr, ignoreCase);
	}

	public static boolean equals(String source, String[] equalsList, boolean ignoreCase) {
		String src = source.toLowerCase();
		for (int i = 0; i < equalsList.length; i++) {
			String find = equalsList[i];
			if (find == null) {
				continue;
			}
			find = find.trim();
			if (find.length() == 0) {
				continue;
			}
			if (ignoreCase) {
				if (src.equals(find.toLowerCase())) {
					return true;
				}
			} else {
				if (source.equals(find)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean startsWith(String source, List<String> startsWithList, boolean ignoreCase) {
		String[] arr = new String[startsWithList.size()];
		startsWithList.toArray(arr);

		return startsWith(source, arr, ignoreCase);
	}

	public static boolean startsWith(String source, String startsWithList, boolean ignoreCase) {
		String[] arr = Utils.splitString(startsWithList, ",");
		return startsWith(source, arr, ignoreCase);
	}

	public static boolean startsWith(String source, String[] startsWithList, boolean ignoreCase) {
		String src = source.toLowerCase();
		for (int i = 0; i < startsWithList.length; i++) {
			String find = startsWithList[i];
			if (find == null) {
				continue;
			}
			find = find.trim();
			if (find.length() == 0) {
				continue;
			}
			if (ignoreCase) {
				if (src.startsWith(find.toLowerCase())) {
					return true;
				}
			} else {
				if (source.startsWith(find)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean endsWith(String source, String endsWithList, boolean ignoreCase) {
		String[] arr = Utils.splitString(endsWithList, ",");
		return endsWith(source, arr, ignoreCase);
	}

	public static boolean endsWith(String source, List<String> endsWithList, boolean ignoreCase) {
		String[] arr = new String[endsWithList.size()];
		endsWithList.toArray(arr);

		return endsWith(source, arr, ignoreCase);
	}

	public static boolean endsWith(String source, String[] endsWithList, boolean ignoreCase) {
		String src = source.toLowerCase();
		for (int i = 0; i < endsWithList.length; i++) {
			String find = endsWithList[i];
			if (find == null) {
				continue;
			}
			find = find.trim();
			if (find.length() == 0) {
				continue;
			}
			if (ignoreCase) {
				if (src.endsWith(find.toLowerCase())) {
					return true;
				}
			} else {
				if (source.endsWith(find)) {
					return true;
				}
			}
		}

		return false;

	}
}
