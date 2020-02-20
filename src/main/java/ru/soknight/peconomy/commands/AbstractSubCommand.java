package ru.soknight.peconomy.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public abstract class AbstractSubCommand implements SubCommand {

	private final CommandSender sender;
	
	private String[] args;
	private String permission;
    private int minArgsLength = 0;
    
    @Override
    public boolean isCorrectWallet(String wallet) {
    	if(wallet.equalsIgnoreCase("dollars") || wallet.equalsIgnoreCase("euro")) return true;
    	
    	sender.sendMessage(Messages.formatMessage("error-wallet-not-found", "%wallet%", wallet));
    	return false;
    }
    
    @Override
    public boolean isCorrectUsage() {
    	if(args.length >= minArgsLength) return true;
    		
    	sender.sendMessage(Messages.getMessage("error-wrong-syntax"));
    	return false;
    }

    @Override
    public boolean hasPermission() {
    	if(sender.hasPermission(permission)) return true;
    	
    	sender.sendMessage(Messages.getMessage("error-no-permissions"));
        return true;
    }
    
    @Override
    public boolean isPlayerRequired() {
    	if(sender instanceof Player) return true;
    	
    	sender.sendMessage(Messages.getMessage("error-only-for-players"));
    	return false;
    }
    
    @Override
    public boolean isPlayerInDatabase(String name) {
    	DatabaseManager dbm = PEconomy.getInstance().getDBManager();
    	if(dbm.isInDatabase(name)) return true;
    	
    	sender.sendMessage(Messages.formatMessage("error-not-in-a-database", "%player%", name));
    	return false;
    }
    
    @Override
    public boolean argIsInteger(String arg) {
    	try {
			Integer.parseInt(arg);
			return true;
		} catch (NumberFormatException ignored) {
			sender.sendMessage(Messages.formatMessage("error-arg-is-not-float", "%arg%", arg));
			return false;
		}
    }
	
}
