package ru.soknight.peconomy.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.AmountFormatter;

import java.util.Arrays;
import java.util.List;

public final class PEconomyExpansion extends PlaceholderExpansion {

    private static final List<String> SUBIDS = Arrays.asList("balance", "has");
    
    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    
    public PEconomyExpansion(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        
        if(isRegistered())
            unregister();
        
        register();
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "SoKnight";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "peco";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String id) {
        if(player == null) return ChatColor.RED + "PLAYER IS NULL";

        String[] parts = id.split("_");
        String name = player.getName();
        
        String subid = parts[0].toLowerCase();
        
        // Cancelling if placeholder is not implemented
        if(!SUBIDS.contains(subid))
            return ChatColor.RED + "INVALID PLACEHOLDER";
        
        // Cancelling if currency is not specified
        if(parts.length == 1)
            return ChatColor.RED + "CURRENCY IS NOT SPECIFIED";
        
        String currencyid = parts[1];
        WalletModel walletModel = databaseManager.getWallet(name).join();
        
        switch (subid) {
        case "balance": {
            return walletModel == null ? "0.00" : AmountFormatter.format(walletModel.getAmount(currencyid));
        }
        case "has": {
            if(parts.length == 2)
                return ChatColor.RED + "AMOUNT IS NOT SPECIFIED";
            
            float amount = Float.parseFloat(parts[2]);
            return walletModel == null ? "false" : String.valueOf(walletModel.hasAmount(currencyid, amount));
        }
        default:
            break;
        }
        
        return "";
    }

}
