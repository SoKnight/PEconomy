package ru.soknight.peconomy.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Getter;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.utils.HelpCommand;
import ru.soknight.peconomy.utils.Logger;

public class Messages {

	@Getter
	private static FileConfiguration config;
	private static Map<String, HelpCommand> help_list;
	
	public static void refresh() {
		String locale = Config.getConfig().getString("messages.locale", "en");
		
		PEconomy instance = PEconomy.getInstance();
		File datafolder = instance.getDataFolder();
		if(!datafolder.isDirectory()) datafolder.mkdirs();
		File file = new File(instance.getDataFolder(), "messages_" + locale + ".yml");
		if(!file.exists()) {
			try {
				InputStream input = instance.getResource("messages_" + locale + ".yml");
				if(input == null) {
					Logger.error("Unknown locale " + locale + ", localization for this locale not found.");
					input = instance.getResource("messages_en.yml");
					locale = "en";
				}
				
				Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				Logger.info("Generated new messages file for locale '" + locale + "'.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(file);
		initHelpList();
	}
	
	public static String getMessage(String section) {
		String output = getRawMessage(section);
		if(Config.isUsePrefix()) output = Config.getPrefix() + output;
		return output;
	}
	
	public static String getRawMessage(String section) {
		if(!config.isSet(section)) {
			Logger.error("Couldn't load message from messages.yml: " + section);
			return "Whoops! Message not found :(";
		}
		String output = config.getString(section).replace("&", "\u00A7");
		return output;
	}
	
	public static String formatMessage(String section, String... replacements) {
		if(!config.isSet(section)) {
			Logger.error("Couldn't load message from messages.yml: " + section);
			return "Whoops! Message not found :(";
		}
		String output = config.getString(section).replace("&", "\u00A7");
		
		for(int i = 0; i < replacements.length; i++) {
			String replacement = replacements[i];
			if(replacement.startsWith("%") && replacement.endsWith("%")) continue;
			
			String node = replacements[i - 1];
			output = output.replace(node, replacement);
		}
		
		if(Config.isUsePrefix()) output = Config.getPrefix() + output;
		return output;
	}
	
	public static HelpCommand getHelpString(String key) {
		return help_list.get(key);
	}
	
	public static List<String> getStringList(String key) {
		List<String> output = new ArrayList<>();
		for(String s : config.getStringList(key))
			output.add(s.replace("&", "\u00A7"));
		return output;
	}
	
	public static String format;
	
	private static void initHelpList() {
		format = getMessage("help-body");
		help_list = new HashMap<>();
		
		HelpCommand
			help = new HelpCommand("help", "help"),
			add = new HelpCommand("peco add", "add", "target", "amount", "wallet"),
			set = new HelpCommand("peco set", "set", "target", "amount", "wallet"),
			reset = new HelpCommand("peco reset", "reset", "target", "wallet"),
			take = new HelpCommand("peco take", "take", "target", "amount", "wallet"),
			balance = new HelpCommand("balance", "balance"),
			balance_other = new HelpCommand("balance", "balance-other", "target"),
			pay = new HelpCommand("pay", "pay", "target", "amount", "wallet"),
			reload = new HelpCommand("peco reload", "reload");
		
		help_list.put("help", help);
		help_list.put("add", add);
		help_list.put("set", set);
		help_list.put("reset", reset);
		help_list.put("take", take);
		help_list.put("balance", balance);
		help_list.put("balance-other", balance_other);
		help_list.put("pay", pay);
		help_list.put("reload", reload);
	}
	
}
