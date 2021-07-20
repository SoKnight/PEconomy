package ru.soknight.peconomy.format;

import org.bukkit.plugin.Plugin;

import java.util.IllegalFormatException;

public class AmountFormatter {

    private static final String DEFAULT_FORMAT = "%.2f";
    private static String FORMAT = DEFAULT_FORMAT;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void setFormat(Plugin plugin, String format) {
        if(format == null || format.isEmpty()) {
            FORMAT = DEFAULT_FORMAT;
            return;
        }

        try {
            String.format(format, 1.75F);
            FORMAT = format;
        } catch (IllegalFormatException ex) {
            plugin.getLogger().severe("You use invalid amount format '" + format + "'!");
            plugin.getLogger().severe("Details: " + ex.getMessage());
            FORMAT = DEFAULT_FORMAT;
        }
    }
    
    public static String format(float amount) {
        return String.format(FORMAT, amount);
    }
    
}
