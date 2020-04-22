package ru.soknight.peconomy.configuration;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomy;

public class MessagesProvider {

	private static final List<String> LOCALES = Arrays.asList("en", "ru");
	
	@Getter private Messages messages;
	
	public MessagesProvider(PEconomy plugin, Configuration config) {
		String locale = config.getString("messages.locale", "en").toLowerCase();
		
		if(!LOCALES.contains(locale)) {
			plugin.getLogger().severe("Unknown localization '" + locale + "', returns to English...");
			locale = "en";
		}
		
		String filename = "messages_" + locale + ".yml";
		InputStream source = plugin.getClass().getResourceAsStream("/locales/" + filename);
		
		if(source != null)
			this.messages = new Messages(plugin, source, filename);
		else plugin.getLogger().severe("Failed to get internal resource of messages file.");
	}

}
