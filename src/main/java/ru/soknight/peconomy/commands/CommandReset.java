package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Requirements;
import ru.soknight.peconomy.utils.Utils;

public class CommandReset {

	public static void execute(CommandSender sender, String[] args) {
		if(Requirements.isInvalidUsage(sender, args, 3)) return;
		
		String name = args[1], wallet = args[2];
		if(Requirements.isInvalidWallet(sender, wallet)) return;
		if(!Requirements.playerExist(sender, name)) return;
		
		float current = DatabaseManager.resetAmount(name, wallet);
		
		String cs = Utils.format(current);
		String fsender, freceiver;
		if(wallet.equals("euro")) {
			fsender = Messages.getMessage("reset-euro").replace("%player%", name).replace("%current%", cs);
			freceiver = Messages.getMessage("reset-euro-myself").replace("%current%", cs);
		} else {
			fsender = Messages.getMessage("reset-dollars").replace("%player%", name).replace("%current%", cs);
			freceiver = Messages.getMessage("reset-dollars-myself").replace("%current%", cs);
		}
		
		sender.sendMessage(fsender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(freceiver);
		return;
	}
	
}
