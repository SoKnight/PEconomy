package ru.soknight.peconomy.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Requirements;

public class SubCommands implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(Messages.getMessage("error-without-args"));
			return true; }
		
		switch(args[0]) {
		case "help":
			if(Requirements.hasPermission(sender, "peco.help")) CommandHelp.execute(sender);
			break;
		case "add":
			if(Requirements.hasPermission(sender, "peco.add")) CommandAdd.execute(sender, args);
			break;
		case "set":
			if(Requirements.hasPermission(sender, "peco.set")) CommandSet.execute(sender, args);
			break;
		case "reset":
			if(Requirements.hasPermission(sender, "peco.reset")) CommandReset.execute(sender, args);
			break;
		case "take":
			if(Requirements.hasPermission(sender, "peco.take")) CommandTake.execute(sender, args);
			break;
		case "reload":
			if(Requirements.hasPermission(sender, "peco.reload")) CommandReload.execute(sender);
			break;
		default:
			sender.sendMessage(Messages.getMessage("error-command-not-found"));
			break;
		}
		return true;
	}
	
	private static List<String> subcommands = Arrays.asList("help", "add", "set", "reset", "take", "reload"), 
			wallets = Arrays.asList("dollars", "euro");
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) return null;
		List<String> output = new ArrayList<>();
		if(args.length < 2) {
			for(String s : subcommands)
				if(s.startsWith(args[0].toLowerCase())) output.add(s);
			return output;
		}
		
		switch(args[0]) {
		case "add":
			if(!sender.hasPermission("peco.add")) break;
			if(args.length == 2) {
				for(Player p : Bukkit.getServer().getOnlinePlayers())
					if(p.getName().toLowerCase().startsWith(args[1].toLowerCase())) output.add(p.getName());
				for(OfflinePlayer op : Bukkit.getServer().getOfflinePlayers())
					if(op.getName().toLowerCase().startsWith(args[1].toLowerCase()) && !output.contains(op.getName())) 
						output.add(op.getName());
			}
			if(args.length == 4)
				for(String wallet : wallets)
					if(wallet.startsWith(args[3])) output.add(wallet);
			break;
		case "set":
			if(!sender.hasPermission("peco.set")) break;
			if(args.length == 2) {
				for(Player p : Bukkit.getServer().getOnlinePlayers())
					if(p.getName().toLowerCase().startsWith(args[1].toLowerCase())) output.add(p.getName());
				for(OfflinePlayer op : Bukkit.getServer().getOfflinePlayers())
					if(op.getName().toLowerCase().startsWith(args[1].toLowerCase()) && !output.contains(op.getName())) 
						output.add(op.getName());
			}
			if(args.length == 4)
				for(String wallet : wallets)
					if(wallet.startsWith(args[3])) output.add(wallet);
			break;
		case "reset":
			if(!sender.hasPermission("peco.reset")) break;
			if(args.length == 2) {
				for(Player p : Bukkit.getServer().getOnlinePlayers())
					if(p.getName().toLowerCase().startsWith(args[1].toLowerCase())) output.add(p.getName());
				for(OfflinePlayer op : Bukkit.getServer().getOfflinePlayers())
					if(op.getName().toLowerCase().startsWith(args[1].toLowerCase()) && !output.contains(op.getName())) 
						output.add(op.getName());
			}
			if(args.length == 3)
				for(String wallet : wallets)
					if(wallet.startsWith(args[2])) output.add(wallet);
			break;
		case "take":
			if(!sender.hasPermission("peco.take")) break;
			if(args.length == 2) {
				for(Player p : Bukkit.getServer().getOnlinePlayers())
					if(p.getName().toLowerCase().startsWith(args[1].toLowerCase())) output.add(p.getName());
				for(OfflinePlayer op : Bukkit.getServer().getOfflinePlayers())
					if(op.getName().toLowerCase().startsWith(args[1].toLowerCase()) && !output.contains(op.getName())) 
						output.add(op.getName());
			}
			if(args.length == 4)
				for(String wallet : wallets)
					if(wallet.startsWith(args[3])) output.add(wallet);
			break;
		default:
			break;
		}
		return output;
	}

}
