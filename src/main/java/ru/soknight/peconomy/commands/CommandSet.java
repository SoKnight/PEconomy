package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Requirements;
import ru.soknight.peconomy.utils.Utils;

public class CommandSet {

	public static void execute(CommandSender sender, String[] args) {
		if(Requirements.isInvalidUsage(sender, args, 4)) return;
		
		String name = args[1], amstr = args[2], wallet = args[3];
		if(Requirements.isInvalidWallet(sender, wallet)) return;
		if(!Requirements.argIsFloat(sender, amstr)) return;
		if(!Requirements.playerExist(sender, name)) return;
		
		float amount = Float.parseFloat(amstr);
		float current = DatabaseManager.setAmount(name, amount, wallet);
		float newbal = amount;
		
		String cs = Utils.format(current), ns = Utils.format(newbal);
		String fsender, freceiver;
		if(wallet.equals("euro")) {
			fsender = Messages.getMessage("set-euro").replace("%player%", name).replace("%current%", cs).replace("%new%", ns);
			freceiver = Messages.getMessage("set-euro-myself").replace("%current%", cs).replace("%new%", ns);
		} else {
			fsender = Messages.getMessage("set-dollars").replace("%player%", name).replace("%current%", cs).replace("%new%", ns);
			freceiver = Messages.getMessage("set-dollars-myself").replace("%current%", cs).replace("%new%", ns);
		}
		
		sender.sendMessage(fsender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(freceiver);
		return;
	}
	
}
