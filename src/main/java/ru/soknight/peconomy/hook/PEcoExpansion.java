package ru.soknight.peconomy.hook;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.command.tool.AmountFormatter;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Wallet;

public class PEcoExpansion extends PlaceholderExpansion {

	private static final List<String> SUBIDS = Arrays.asList("balance", "has");
	
	private final DatabaseManager databaseManager;
	
	@Getter private final String author;
	@Getter private final String identifier;
	@Getter private final String version;
	
	public PEcoExpansion(PEconomy plugin, DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
		
		this.author = "SoKnight";
		this.identifier = "peco";
		this.version = plugin.getDescription().getVersion();
	}
	
	@Override
    public String onPlaceholderRequest(Player p, String id) {
        if(p == null) return ChatColor.RED + "PLAYER IS NULL";

        String[] parts = id.split("_");
        String name = p.getName();
        
        String subid = parts[0].toLowerCase();
        
        // Cancelling if placeholder is not implemented
        if(!SUBIDS.contains(subid)) return ChatColor.RED + "INVALID PLACEHOLDER";
        
        // Cancelling if currency is not specified
        if(parts.length == 1) return ChatColor.RED + "CURRENCY IS NOT SPECIFIED";
        String currencyid = parts[1];
        
        // Getting player's wallet
        Wallet wallet = databaseManager.getWallet(name);
        
        // Handling implemented placeholders
        switch (subid) {
        case "balance": {
        	return wallet == null ? "0.00" : AmountFormatter.format(wallet.getAmount(currencyid));
        }
        case "has": {
        	if(parts.length == 2) return ChatColor.RED + "AMOUNT IS NOT SPECIFIED";
        	float amount = Float.parseFloat(parts[2]);
        	return wallet == null ? "false" : String.valueOf(wallet.hasAmount(currencyid, amount));
        }
        default:
        	break;
        }
        
        return "";
    }

}
