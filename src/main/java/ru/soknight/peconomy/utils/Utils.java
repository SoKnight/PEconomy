package ru.soknight.peconomy.utils;

public class Utils {

	private static final String format = "%.2f";
	
	public static String format(Object input) {
		return String.format(format, input);
	}
	
}
