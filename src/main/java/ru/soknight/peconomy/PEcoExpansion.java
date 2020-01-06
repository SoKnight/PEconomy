package ru.soknight.peconomy;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.utils.Utils;

public class PEcoExpansion extends PlaceholderExpansion {

	private PEconomy plugin;
	
	public PEcoExpansion(PEconomy plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public String getAuthor() {
		return "SoKnight";
	}

	@Override
	public String getIdentifier() {
		return "peco";
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}
	
	@Override
    public String onPlaceholderRequest(Player p, String id){
        if(p == null) return "";

        String[] parts = id.split("_");
        String name = p.getName();
        
        String subid = parts[0], wallet = "", count = "";
        if(parts.length > 1) wallet = parts[1];
        if(parts.length > 2) count = parts[2];
            
        switch (subid) {
        case "balance": {
        	return Utils.format(DatabaseManager.getAmount(name, wallet));
        }
        case "has": {
        	float amount = Float.parseFloat(count);
        	return String.valueOf(DatabaseManager.hasAmount(name, amount, wallet));
        }
        default:
        	break;
        }
        
        return null;
    }

}
