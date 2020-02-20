package ru.soknight.peconomy.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Getter;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.utils.Logger;

public class Config {

	@Getter private static FileConfiguration config;
	@Getter private static String prefix;
	@Getter private static boolean usePrefix;
	
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
		usePrefix = config.getBoolean("messages.use-prefix");
		if(usePrefix) prefix = config.getString("messages.prefix").replace("&", "\u00A7");
	}
	
	public static List<String> getStringList(String section) {
		List<String> output = new ArrayList<>();
		for(String s : config.getStringList(section))
			output.add(s.replace("&", "\u00A7"));
		return output;
	}
	
}
