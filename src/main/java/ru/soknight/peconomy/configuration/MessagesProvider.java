package ru.soknight.peconomy.configuration;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomyPlugin;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Getter
public final class MessagesProvider {

    private static final List<String> LOCALES = Arrays.asList("en", "ru");
    
    private final PEconomyPlugin plugin;
    private Messages messages;
    
    public MessagesProvider(@NotNull PEconomyPlugin plugin, @NotNull Configuration config) {
        this.plugin = plugin;
        
        String locale = config.getString("messages.locale", "en").toLowerCase();
        if(!LOCALES.contains(locale)) {
            plugin.getLogger().severe("Unknown localization '" + locale + "', using English as default...");
            locale = "en";
        }
        
        String filename = "messages_" + locale + ".yml";
        InputStream source = plugin.getClass().getResourceAsStream("/locales/" + filename);
        
        if(source == null) {
            plugin.getLogger().severe("Couldn't find an internal localization resource.");
            return;
        }
        
        this.messages = new Messages(plugin, source, filename);
    }

    public void update(@NotNull Configuration config) {
        String locale = config.getString("messages.locale", "en").toLowerCase();
        
        if(!LOCALES.contains(locale)) {
            plugin.getLogger().severe("Unknown localization '" + locale + "', using English as default...");
            locale = "en";
        }
        
        String filename = "messages_" + locale + ".yml";
        InputStream source = plugin.getClass().getResourceAsStream("/locales/" + filename);
        
        if(source == null) {
            plugin.getLogger().severe("Couldn't find an internal localization resource.");
            return;
        }
        
        messages.setSource(source);
        messages.setFilename(filename);
        messages.refresh();
    }
    
}
