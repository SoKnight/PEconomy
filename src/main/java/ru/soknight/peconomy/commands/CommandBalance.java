package ru.soknight.peconomy.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ru.soknight.peconomy.database.Balance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Requirements;
import ru.soknight.peconomy.utils.Utils;

public class CommandBalance implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String name = sender.getName();
		if(args.length > 0) 
			if(Requirements.hasPermission(sender, "peco.balance.other")) name = args[0];
			else return true;
		else if(!Requirements.isPlayer(sender)) return true;
		
		if(!Requirements.isInDatabase(sender, name)) return true;
		Balance balance = DatabaseManager.getBalance(name);
		
		float dollars = balance.getDollars(), euro = balance.getEuro();
		String dstr = Utils.format(dollars), estr = Utils.format(euro), msg;
		
		if(args.length == 0) msg = Messages.getMessage("balance");
		else msg = Messages.getMessage("balance-other").replace("%player%", name);
		
		msg = msg.replace("%dollars%", dstr).replace("%euro%", estr);
		sender.sendMessage(msg);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("peco.balance.other")) return null;
		List<String> output = new ArrayList<>();
		if(args.length == 1) {
			for(Player p : Bukkit.getServer().getOnlinePlayers())
				if(p.getName().toLowerCase().startsWith(args[0].toLowerCase())) output.add(p.getName());
			for(OfflinePlayer op : Bukkit.getServer().getOfflinePlayers())
				if(op.getName().toLowerCase().startsWith(args[0].toLowerCase()) && !output.contains(op.getName())) 
					output.add(op.getName());
		}
		return output;
	}

	
	
}
