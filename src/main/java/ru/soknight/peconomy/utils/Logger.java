package ru.soknight.peconomy.utils;

import ru.soknight.peconomy.PEconomy;

public class Logger {
	
	public static void info(String info) {
		PEconomy.getInstance().getLogger().info(info);
	}
	
	public static void warning(String warning) {
		PEconomy.getInstance().getLogger().warning(warning);
	}
	
	public static void error(String error) {
		PEconomy.getInstance().getLogger().severe(error);
	}
	
}
