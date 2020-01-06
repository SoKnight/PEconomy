package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Requirements;
import ru.soknight.peconomy.utils.Utils;

public class CommandTake {

	public static void execute(CommandSender sender, String[] args) {
		if(Requirements.isInvalidUsage(sender, args, 4)) return;
		
		String name = args[1], amstr = args[2], wallet = args[3];
		if(Requirements.isInvalidWallet(sender, wallet)) return;
		if(!Requirements.argIsFloat(sender, amstr)) return;
		if(!Requirements.playerExist(sender, name)) return;
		
		float amount = Float.parseFloat(amstr);
		float current = DatabaseManager.getAmount(name, wallet);
		String as = Utils.format(amount), cs = Utils.format(current);
		if(!DatabaseManager.hasAmount(name, amount, wallet)) {
			if(wallet.equals("euro")) sender.sendMessage(Messages.getMessage("taking-failed-euro")
					.replace("%player%", name).replace("%current%", cs).replace("%amount%", as));
			else sender.sendMessage(Messages.getMessage("taking-failed-dollars").replace("%player%", name)
					.replace("%current%", cs).replace("%amount%", as));
			return;
		}
		current = DatabaseManager.takeAmount(name, amount, wallet);
		float newbal = current - amount;
		
		String ns = Utils.format(newbal); cs = Utils.format(current);
		String fsender, freceiver;
		if(wallet.equals("euro")) {
			fsender = Messages.getMessage("taked-euro").replace("%player%", name).replace("%taked%", as)
					.replace("%current%", cs).replace("%new%", ns);
			freceiver = Messages.getMessage("taked-euro-myself").replace("%taked%", as).replace("%current%", cs)
					.replace("%new%", ns);
		} else {
			fsender = Messages.getMessage("taked-dollars").replace("%player%", name).replace("%taked%", as)
					.replace("%current%", cs).replace("%new%", ns);
			freceiver = Messages.getMessage("taked-dollars-myself").replace("%taked%", as).replace("%current%", cs)
					.replace("%new%", ns);
		}
		
		sender.sendMessage(fsender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(freceiver);
		return;
	}
	
}
