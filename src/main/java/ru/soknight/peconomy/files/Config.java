package ru.soknight.peconomy.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.utils.Logger;

public class Config {

	public static FileConfiguration config;
	public static String prefix;
	public static boolean use_prefix;
	public static float dollars, euro;
	
	public static void refresh() {
		PEconomy instance = PEconomy.getInstance();
		File datafolder = instance.getDataFolder();
		if(!datafolder.isDirectory()) datafolder.mkdirs();
		File file = new File(instance.getDataFolder(), "config.yml");
		if(!file.exists()) {
			try {
				Files.copy(instance.getResource("config.yml"), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				Logger.info("Generated new config file.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(file);
		use_prefix = config.getBoolean("messages.use-prefix");
		if(use_prefix) prefix = config.getString("messages.prefix").replace("&", "\u00A7");
		dollars = config.getInt("default.dollars"); euro = config.getInt("default.euro");
	}
	
	public static List<String> getStringList(String section) {
		List<String> output = new ArrayList<>();
		for(String s : config.getStringList(section))
			output.add(s.replace("&", "\u00A7"));
		return output;
	}
	
}
