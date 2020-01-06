package ru.soknight.peconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;

public class Requirements {
	
	public static boolean hasPermission(CommandSender sender, String permission) {
		if(!sender.hasPermission(permission)) {
			sender.sendMessage(Messages.getMessage("error-no-permission"));
			return false;
		} else return true;
	}
	
	public static boolean isPlayer(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(Messages.getMessage("error-only-for-players"));
			return false;
		} else return true;
	}
	
	public static boolean playerExist(CommandSender sender, String nickname) {
		for(Player p : Bukkit.getOnlinePlayers())
			if(p.getName().equals(nickname)) return true;
		for(OfflinePlayer op : Bukkit.getOfflinePlayers())
			if(op.getName().equals(nickname)) return true;
		sender.sendMessage(Messages.getMessage("error-player-not-found").replace("%nickname%", nickname));
		return false;
	}
	
	public static boolean isInvalidUsage(CommandSender sender, String[] args, int neededargscount) {
		if(args.length < neededargscount) {
			sender.sendMessage(Messages.getMessage("error-invalid-syntax"));
			return true;
		} else return false;
	}
	
	public static boolean isInvalidWallet(CommandSender sender, String arg) {
		if(arg.equals("dollars") || arg.equals("euro")) return false;
		else sender.sendMessage(Messages.getMessage("error-wallet-not-found"));
		return true;
	}
	
	public static boolean isInDatabase(CommandSender sender, String name) {
		if(!DatabaseManager.isInDatabase(name)) {
			sender.sendMessage(Messages.getMessage("error-not-in-database").replace("%player%", name));
			return false;
		} else return true;
	}
	
	public static boolean argIsFloat(CommandSender sender, String arg) {
		try {
			Float.parseFloat(arg);
			return true;
		} catch (NumberFormatException e) {
			sender.sendMessage(Messages.getMessage("error-arg-is-not-float").replace("%arg%", arg));
			return false;
		}
	}
	
}
